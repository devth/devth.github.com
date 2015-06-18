---
layout: article
title: Compiled queries in Scala with ASM
categories: scala
comments: true
image:
  feature: cartesian_1024_256.jpg
  teaser: cartesian_410_228.jpg
  caption: Beartooth Highway. June 2014
---

Why would you ever want to generate bytecode from Scala, you ask?

<img src="/images/speed-cage.jpg" />

I can see you don't believe me. Or maybe Nic Cage just isn't that
convincing. In any case, let me help you by building an interpreter that runs
projection and filtering on a fake database, then build a compiled version and
look at some performance numbers.

Goal: compile a data structure representing a query into native code to speed up
a query loop.

This is gonna be a long one, so I've broken it up into 4 parts:

- Part 1: A query system
- Part 2: Intro to ASM, including transforming a Tree into nested while loops
- Part 3: Using ASM to dynamically generate a query
- Part 4: Using Scala Quasiquotes to generate the query


## Part 1: A query system

First let's define our fake "database" consisting of a single dataset,
represented as a simple in-memory data structure using Tuples as rows.

```scala
// Store a mapping of column name to ordinal for fast projection
val schema = Seq("name", "birthYear", "dissertation").zipWithIndex.toMap

val db = Seq(
  ("John McCarthy", 1927, "Projection Operators and Partial Differential Equations."),
  ("Haskell Curry", 1900, "Grundlagen der kombinatorischen Logik"),
  ("Philip Wadler", 1956, "Listlessness is Better than Laziness"),
  ("Alonzo Church", 1903, "Alternatives to Zermelo's Assumption"),
  ("Alan Turing", 1912, "Systems of Logic based on Ordinals")
)
```


Next, we need structures that hold:

0. fields to be projected
0. filter expression tree

An efficient represtation of fields is simply storing the ordinals. For example,
this is how we'd project the name and dissertation columns:

```scala
val projections = Seq(0, 2)
```

The filter expression is a little more complicated, since it's treeish in
nature. Even though we're not supporting SQL, let's use it to help understand:

```sql
WHERE name != null AND birthYear < 1910
```

A JSON tree representation of this query would look like:

```json
{
   "AND": [
      {
         "IS_NOT_NULL": [
            {
               "item": "name"
            }
         ]
      },
      {
         "LESS_THAN": [
            {
               "item": "birthYear"
            },
            {
               "literal": 1910
            }
         ]
      }
   ]
}
```

We could represent this in Scala using nested Tuple2s and Seqs of Products.

```scala
type FilterExpr = Tuple2[String, Seq[Product]]

val filterExpr: FilterExpr = ("AND",
  Seq(
    ("IS_NOT_NULL", Seq(("item", "name"))),
    ("LESS_THAN", Seq(("item", "birthYear"), ("literal", 1910)))
  )
)
```

An operation is the first item in a Tuple; the second is the operands.

Using this we can build a filter interpreter that evaluates whether a row should
be included. Some caveats about limitations of this simple example:

- We'll run filtering before projection. In reality, which to run first is a
  decision that should be made by a query optimizer.
- I'm only going to implement a few of the most common operators.
- The filter interpreter is doing its own projection, which may overlap with the
  projected fields requested by the user and create duplicate work.
- I'm only supporting `Int`{:.language-scala} for comparison. IRL, we'd keep
  better track of the types with a full blown schema.

These are all issues I won't address in this simple example.

```scala
class FilterInterpreter(expr: FilterExpr, schema: Map[String, Int]) {
  /** return true if row is filtered out by expr */
  def isFiltered(row: Product): Boolean = evalFilterOn(row)

  private def evalFilterOn(row: Product): Boolean = {
    def eval(expr: Product): Boolean = {
      val (operator, operands: Seq[Product]) = expr
      operator match {
        case "AND" => operands.forall(eval)
        case "OR" => operands.exists(eval)
        case "IS_NOT_NULL" => valueFor(operands(0)) != null
        case "LESS_THAN" => {
          val (x :: y :: _) = operands.map(o => valueFor(o).asInstanceOf[Int])
          x < y
        }
      }
    }
    def valueFor(node: Product) = node match {
      case ("item", column: String) => row.productElement(schema(column))
      case ("literal", lit) => lit
    }
    eval(expr)
  }
}
```

And here's a simple query loop:

```scala
def query(projections: Seq[Int], filter: FilterExpr): Seq[Seq[Any]] = {
  val filterer = new FilterInterpreter(filter, schema)
  db.flatMap { row =>
    // Filter
    if (filterer.isFiltered(row)) None
    // Project
    else Some(projections.map(row.productElement))
  }
}
```

*NB: we could use [ScalaBlitz](https://scala-blitz.github.io/) for that flatMap
if this were for real and we were ultra-concerned about performance.*

Note how we lose type information during projection. Ideally, we would return a
fully typed Tuple, but there's not an easy way to do that when running
projection via interpretation. We'll fix that when we dynamically compile the
query.

With all this in place, let's run it:

```scala
// i.e. SELECT name, dissertation
val projections = Seq(0, 2)

// i.e. WHERE name != null AND birthYear < 1910
val filterExpr: FilterExpr = ("AND",
  Seq(
    ("IS_NOT_NULL", Seq(("item", "name"))),
    ("LESS_THAN", Seq(("item", "birthYear"), ("literal", 1910)))))

val result = query(projections, filterExpr)
result.map(println)

//=> List(John McCarthy, Projection Operators and Partial Differential Equations.)
//=> List(Philip Wadler, Listlessness is Better than Laziness)
//=> List(Alan Turing, Systems of Logic based on Ordinals)
```

So far so good. Next up, let's dive into some bytecodes.

Read the [full runnable source]() for this system.

## Bytecode primer

Let's start by looking at the bytecode for a minimum viable Scala class:

```scala
class Foo
```

Compile it with `scalac` then view the bytecode with `java -c`:

```bash
scalac Foo.scala
javap -c Foo.class
```


```java
public class Foo {
  public Foo();
    Code:
       0: aload_0
       1: invokespecial #12                 // Method java/lang/Object."<init>":()V
       4: return
}
```

From the [Java bytecode instructions listings
reference](http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings) we
can find out the meaning of these bytecode instructions:

- `aload_0` — load a reference onto the stack from local variable 0 (local var 0
  is always `this`)
- `invokespecial` — invoke instance method on object objectref (this would be
  the object that we just loaded onto the stack) and puts the result on the
  stack (might be void)
- `return` — return void from method

This is the generated constructor for `Foo`{:.language-scala}. Since nothing
else is going on, it simply returns void after calling the constructor.  Now
what if we actually do something, like instantiate a `Foo`{:.language-scala}?

```scala
class Foo

object RunFoo {
  val f = new Foo
}
```

Compiling this yields a single `Foo.class` identical to the one above along with
two class files: `RunFoo.class` and `RunFoo$.class`.

```java
// RunFoo.class
public final class RunFoo {
  public static Foo f();
    Code:
       0: getstatic     #16                 // Field RunFoo$.MODULE$:LRunFoo$;
       3: invokevirtual #18                 // Method RunFoo$.f:()LFoo;
       6: areturn
}
```

```java
// RunFoo$.class
public final class RunFoo$ {
  public static final RunFoo$ MODULE$;

  public static {};
    Code:
       0: new           #2                  // class RunFoo$
       3: invokespecial #12                 // Method "<init>":()V
       6: return

  public Foo f();
    Code:
       0: aload_0
       1: getfield      #17                 // Field f:LFoo;
       4: areturn
}
```

The JVM runs these opcodes in a stack machine: values are pushed on to the stack
then used as operands to later operations. For example, here's how you could add
two constants:

```java
bipush 28
bipush 14
iadd
```

0. Push 28 onto the stack with `bipush`
0. Push 14 onto the stack with `bipush`
0. Execute `iadd` which adds two ints: it pops two values off the stack to use
   as its operands: first 14, then 28. It adds those two operands and pushes the
   result, 28, onto the stack.

At this point we could work with the new 42 value on the stack. Here's how we
would check that the value is indeed 42:

```java
20: bipush 42
22: if_icmpne 28
24: ldc               #3
26: goto 32
28: ldc               #4
32: ...
```

0. Push the value 42 onto the stack
0. Use `if_icmpne` to compare two values from the stack. If they are not equal,
   jump to position 28, which pushes constant `#4` onto the stack using `ldc`.
   If they are equal, the next code is executed, which instead pushes constant
   `#3` onto the stack, then jumps to position 32.

Tedious, but simple.

This primer is only intended to wet your feet. If you want to learn more about
bytecode, see the [Further reading](#further-reading) section at the end of this
post.

## ASM

ASM is a bytecode manipulation framework. It's one of several options for
manipulating bytecode, but I chose it because it's one of the most mature,
requires the least amount of memory, and it's very fast. The downside is it's
also quite low level.  If you aren't super-concerned with performance, you
should check out other options, like
[Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/), which is much
easier to work with.

Now, to use ASM, the more familiar you are with bytecode the better off you'll
be, but for newbs like me, there is ASMifier, which takes a compiled class and
generates the ASM code for it. I'm going to avoid Scala for this excercise,
since Java maps more closely to bytecode, and we can use the resulting bytecode
from Scala either way. I want to use Tuples to represent fully typed rows, so
let's see what the ASM code looks like for this Java class:

```java
import scala.Tuple2;

public class TupleFromJava {

  Tuple2<String, Integer> tup = new Tuple2<String, Integer>("foo", 2);

  public TupleFromJava() {
  }

  public Tuple2<String, Integer> getTup() {
    return tup;
  }

}
```

Compile it, making sure the scala-lib jar is on your classpath:

```bash
javac -cp $SCALA_LIB TupleFromJava.java
```

Then use the ASMifier:

```bash
java -cp asm-5.0.3/lib/all/asm-all-5.0.3.jar \
  org.objectweb.asm.util.ASMifier TupleFromJava.class
```

Here is the generated ASM code, heavily annotated:

```java
import java.util.*;
import org.objectweb.asm.*;
public class TupleFromJavaDump implements Opcodes {

  public static byte[] dump () throws Exception {

    ClassWriter cw = new ClassWriter(0);
    FieldVisitor fv;
    MethodVisitor mv;
    AnnotationVisitor av0;

    // Generate a public class inheriting from java.lang.Object
    cw.visit(52, ACC_PUBLIC + ACC_SUPER, "TupleFromJava", null,
      "java/lang/Object", null);

    // Initialize the `tup` field with a null value. When fields are declared in
    // Java classes, they aren't fully initialized until the constructor runs.
    {
      fv = cw.visitField(0, "tup", "Lscala/Tuple2;",
        "Lscala/Tuple2<Ljava/lang/String;Ljava/lang/Integer;>;", null);
      fv.visitEnd();
    }

    {
      // Public constructor
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();

      // Put `this` on the stack
      mv.visitVarInsn(ALOAD, 0);

      // Invoke constructor on `this`. Note that it takes no params and returns
      // void, and it consumes the `this` that we put on the stack above.
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

      // Put `this` on the stack again
      mv.visitVarInsn(ALOAD, 0);

      //
      // Start `tup` initialization {
      //

      // Put a new scala.Tuple2 on the stack then duplicate the object reference
      // on top of the stack. Note that since it is an object reference and not
      // the object itself, any mutation we do to the object will be reflected in
      // any references to that object on the stack.
      mv.visitTypeInsn(NEW, "scala/Tuple2");
      mv.visitInsn(DUP);

      // Push constant "foo" from the constant pool onto the stack
      mv.visitLdcInsn("foo");

      // Load the int constant 2 onto the stack
      mv.visitInsn(ICONST_2);

      // Autobox the int in a java.lang.Integer. This pops the int off the stack
      // and replaces it with the Integer. 
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
        "(I)Ljava/lang/Integer;", false);

      // At this point our stack looks like:
      // Integer.valueOf(2)
      // "foo"
      // Tuple2 (reference)
      // Tuple2 (reference)

      // Initialize the Tuple2. Looking at the signature, we can see it takes
      // two java.lang.Objects, which it will consume from the stack in addition
      // to one of the Tuple2 references.
      mv.visitMethodInsn(INVOKESPECIAL, "scala/Tuple2", "<init>",
        "(Ljava/lang/Object;Ljava/lang/Object;)V", false);

      // Finally, we store our Tuple2 value in the `tup` field. This consumes
      // the second copy of the Tuple2 reference we had on the stack.
      mv.visitFieldInsn(PUTFIELD, "TupleFromJava", "tup", "Lscala/Tuple2;");

      //
      // End `tup` initialization }
      //

      // Return void from constructor
      mv.visitInsn(RETURN);
      mv.visitMaxs(5, 1);
      mv.visitEnd();
    }

    {
      // Create a public method `getTup` which takes no args and returns a
      // scala.Tuple2
      mv = cw.visitMethod(ACC_PUBLIC, "getTup", "()Lscala/Tuple2;",
        "()Lscala/Tuple2<Ljava/lang/String;Ljava/lang/Integer;>;", null);
      mv.visitCode();
      // Load `this` onto the stack
      mv.visitVarInsn(ALOAD, 0);
      // Load a reference to the `tup` field on `this` onto the stack, consuming
      // `this` in the process
      mv.visitFieldInsn(GETFIELD, "TupleFromJava", "tup", "Lscala/Tuple2;");
      // Return the reference to the `tup` field
      mv.visitInsn(ARETURN);
      // We only needed a max stack size of 1 and maximum of 1 local vars
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
}
```

Not too bad. ASMifier helpfully wraps each logical chunk in blocks. The first
block creates the `tup`{:.language-scala} field. The second block is our public
constructor. It calls super, which invokes the constructor on the
`java.lang.Object`{:.language-scala} superclass, then initializes the
`tup`{:.language-scala} field, and finally returns void. The third block
is the `getTup`{:.language-scala} getter.

*NB: If you're having trouble following this, I recommend generating some ASM
code with ASMifier, then annotating it yourself. It really helps to internalize
the JVM bytecodes and how to work on a stack machine.*

Inside a method, the parameters can be referenced by corresponding local
variable indices. For example:

```scala
def add(x: Int, y: Int)
```

In this case, `x`{:.language-scala} is available at `1`{:.language-scala} and
`y`{:.language-scala} is available at `2`{:.language-scala}. To load and use
these, we would use `visitVarInsn`{:.language-scala} which visits a local
variable instruction.  Using ASM, this is how we'd add `x`{:.language-scala} and
`y`{:.language-scala} and store the result in local variable `3`{:.language-scala}:

```java
mv.visitVarInsn(ILOAD, 1);  // Load x onto the stack
mv.visitVarInsn(ILOAD, 2);  // Load y onto the stack
mv.visitInsn(IADD);         // Pop two values off the stack, add them,
                            // then put the result back on the stack
mv.visitVarInsn(ISTORE, 3); // Pop a value off the stack and store
                            // it in local variable 3
```

When you generate ASM using ASMifier it can generate all the labels and local
var mappings, which is necessary information for debuggers to show you the
correct names of the local variables in a given stack, since in the JVM indices
are used instead of names. When writing by hand, you could opt to not write
these instructions, or add them later if you need them.

Let's familiarize ourselves with another "pattern" in ASM: instantiating a class
and passing arguments to its constructor:



Let's use this knowledge to compile the query we originally interpreted in part
1.




## Further reading

- [Java bytecode instruction listings (Wikipedia - useful reference)](http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)
- [Secrets of the Bytecode Ninjas (InfoQ)](http://www.infoq.com/articles/Secrets-of-the-Bytecode-Ninjas)
- [Hacking Java Bytecode for Programmers (part 1 of a 4 part series)](http://www.acloudtree.com/hacking-java-bytecode-for-programmers-part1-the-birds-and-the-bees-of-hex-editing/)
- [java-bytecode-asm tag on StackOverflow](http://stackoverflow.com/questions/tagged/java-bytecode-asm)
- [JVM Internals (blog post)](http://blog.jamesdbloom.com/JVMInternals.html)

