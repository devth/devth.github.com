---
layout: article
title: Compiled queries in Scala
categories: scala
comments: true
toc: true
image:
  feature: compiled-queries-feature.jpg
  teaser: compiled-queries-teaser.jpg
  caption: Northwest San Francisco and the Golden Gate strait from the Hamon Observation Tower at de Young Museum. February 2017
---

Why resort to the complexities of dynamic code generation and compilation at
runtime?

**Speed**.

To demonstrate, lets build an interpreter that runs projection and filtering on
a simulated database, then build a compiled version and look at some performance
numbers.

To be clear, this is our goal: **compile a data structure representing a query
into native code to speed up a query loop**.

We'll look at two specific code-generation tools to achieve our goal: ASM and
Scala Quasiquotes.

ASM has been used by Java developers for years for all sorts of codegen
purposes. It's is very fast and has low-memory requirements, but it's also very
low-level, making it time-consuming and tedious to use and very difficult to
debug.

Quasiquotes are a new Scala tool for code generation. They are similar to
macros, except they can be used at runtime whereas macros are compile-time only.
They're much higher-level than ASM, so we'll compare benchmarks against the two
to see what cost these high-levelel semantics incur, if any.

## A query system

First let's define our "database" consisting of a single dataset, represented as
a simple in-memory data structure using tuples as rows.

```scala
// Store a mapping of column name to ordinal for fast projection
val schema = Seq("name", "birthYear", "dissertation").zipWithIndex.toMap
// schema: scala.collection.immutable.Map[String,Int] =
//   Map(name -> 0, birthYear -> 1, dissertation -> 2)

val db = Seq(
  ("John McCarthy", 1927, "Projection Operators and Partial Differential Equations."),
  ("Haskell Curry", 1900, "Grundlagen der kombinatorischen Logik"),
  ("Philip Wadler", 1956, "Listlessness is Better than Laziness"),
  ("Alonzo Church", 1903, "Alternatives to Zermelo's Assumption"),
  ("Alan Turing", 1912, "Systems of Logic based on Ordinals")
)
```

Next, we need structures that hold:

1. fields to be projected
1. filter expression tree

An efficient representation of projection fields is simply storing the ordinals.
For example, this is how we'd represent a projection of the name and
dissertation columns:

```scala
val projections = Seq(0, 2)
```

The filter expression is a little more complicated, since it's treeish in
nature. Even though we're not supporting SQL, let's use it to help understand:

```sql
WHERE name != null AND birthYear < 1910
```

A tree is a natural way to represent this syntax in abstract form:

<img src="/images/querytree.svg" />

In Scala, we can represent this with a Scalaz `Tree[String]`{:.language-scala}.

```scala
type FilterExpr = Tree[String]

val filterExpr: FilterExpr = And.node(
  IsNotNull.node(
    Item.node("name".leaf)),
  LessThan.node(
    Item.node("birthYear".leaf),
    Literal.node("1910".leaf)))
```

The root node of each tree or subtree is always an operator. The sub-forests are
the operands, which may be made up of operator trees.

Using this we can build a filter interpreter that evaluates whether a row should
be included. Some caveats about limitations of this simple example:

- We'll run filtering before projection. In reality, which to run first is a
  decision that should be made by a query optimizer.
- I'm only going to implement a few of the most common operators.
- The filter interpreter is doing its own projection, which may overlap with the
  projected fields requested by the user and create duplicate work.
- I'm only supporting `Int`{:.language-scala} for comparison, and I'm doing some
  nasty type casting. IRL, we'd keep better track of the types with a full blown
  schema.

These are all issues I won't address.

Here's our limited set of operators:

```scala
object Operators {
  val And = "AND"
  val Or = "OR"
  val IsNotNull = "IS_NOT_NULL"
  val LessThan = "LESS_THAN"
  val Item = "ITEM"
  val Literal = "LITERAL"
}
import Operators._
```

And the interpreter itself:

```scala
class FilterInterpreter(expr: FilterExpr, schema: Map[String, Int]) {
  import Operators._
  /** return true if row is filtered out by expr */
  def isFiltered(row: Product): Boolean = evalFilterOn(row)

  private def evalFilterOn(row: Product): Boolean = {
    def eval(expr: FilterExpr): Boolean = {
      val (operator, operands: Stream[FilterExpr]) = (expr.rootLabel, expr.subForest)
      operator match {
        case And => operands.forall(eval)
        case Or => operands.exists(eval)
        case IsNotNull => valueFor(operands(0)) != null
        case LessThan => {
          val (x :: y :: _) = operands.map(o => valueFor(o).toString.toInt).toList
          x < y
        }
      }
    }
    def valueFor(node: FilterExpr) = node.rootLabel match {
      // Item expects a single operand
      case Item => row.productElement(schema(node.subForest.head.rootLabel))
      // Literal expects a single operand
      case Literal => node.subForest.head.rootLabel
    }
    eval(expr)
  }
}
```

And finally, a simple query loop:

```scala
def query(projections: Seq[Int], filterExpr: FilterExpr): Seq[Seq[Any]] = {
  val filterer = new FilterInterpreter(filterExpr, schema)
  db.flatMap { row =>
    // Filter
    if (filterer.isFiltered(row)) None
    // Project
    else Some(projections.map(row.productElement))
  }
}
```

*NB: we could use [ScalaBlitz](https://scala-blitz.github.io/) to optimize that
`flatMap`{:.language-scala} if this was real and we were ultra-concerned about
performance.*

Notice how we return `Seq[Seq[Any]]`. At compile-time we don't know how to
typefully represent a row so we have to resort to the lowest-common type,
`Any`{:.language-scala}. This is another issue that we'll fixup later in the
compiled version.

With all this in place, let's run it:

```scala
// SELECT name, dissertation
val projections = Seq(0, 2)

// WHERE name != null AND birthYear < 1910
val filterExpr: FilterExpr = And.node(
  IsNotNull.node(
    Item.node("name".leaf)),
  LessThan.node(
    Item.node("birthYear".leaf),
    Literal.node("1910".leaf)))

val result = query(projections, filterExpr)
result.map(println)

//=> List(John McCarthy, Projection Operators and Partial Differential Equations.)
//=> List(Philip Wadler, Listlessness is Better than Laziness)
//=> List(Alan Turing, Systems of Logic based on Ordinals)
```

So far so good. Next up, let's dive into some bytecodes to gain a basic
understanding of what's going on when we generate code on the JVM.

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
else is going on, it simply returns void after calling the constructor. Now
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
then used as operands to later operations. For example, this is how you could
add two constants:

```java
bipush 28
bipush 14
iadd
```

1. Push 28 onto the stack with `bipush`
1. Push 14 onto the stack with `bipush`
1. Execute `iadd` which adds two ints: it pops two values off the stack to use
   as its operands: first 14, then 28. It adds those two operands and pushes the
   result, 28, onto the stack.

At this point we could work with the new 42 value on the stack. This is how we
would check that the value is indeed 42:

```java
20: bipush 42
22: if_icmpne 28
24: ldc               #3
26: goto 32
28: ldc               #4
32: ...
```

1. Push the value 42 onto the stack
1. Use `if_icmpne` to compare two values from the stack. If they are not equal,
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
also quite low level. If you aren't super-concerned with performance, you
should check out other options, like
[Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/), which is much
easier to work with.

Now, to use ASM, the more familiar you are with bytecode the better off you'll
be, but for newbs like us, there is ASMifier, which takes a compiled class and
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

Then use the ASMifier on the classfile:

```bash
java -cp asm-5.0.3/lib/all/asm-all-5.0.3.jar \
  org.objectweb.asm.util.ASMifier TupleFromJava.class
```

Here is the generated ASM code, heavily annotated with comments. It's a little
tedious to work through, but if you really want to understand bytecode and ASM,
I encourage you to read the comments and work through every line until it's
internalized and you feel comfortable with it.

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
    // Note the format of the string representation of `Tuple2`'s constructor.
    {
      fv = cw.visitField(0, "tup", "Lscala/Tuple2;",
        "Lscala/Tuple2<Ljava/lang/String;Ljava/lang/Integer;>;", null);
      fv.visitEnd();
    }

    // Generate the public constructor named <init> by convention
    {
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
      // two java.lang.Objects (instead of a String and Integer, due to type
      // erasure), which it will consume from the stack in addition
      // to one of the Tuple2 references.
      mv.visitMethodInsn(INVOKESPECIAL, "scala/Tuple2", "<init>",
        "(Ljava/lang/Object;Ljava/lang/Object;)V", false);

      // Finally, we store our Tuple2 value in the `tup` field. This consumes
      // the second copy of the Tuple2 reference we had on the stack.
      mv.visitFieldInsn(PUTFIELD, "TupleFromJava", "tup", "Lscala/Tuple2;");

      //
      // End `tup` initialization }
      //

      // Constructors always return void because constructors should only
      // initialize the object and do nothing else.
      mv.visitInsn(RETURN);

      // Sets the max stack size and max number of local vars. This can be
      // calculated automatically for you if you use COMPUTE_FRAMES or COMPUTE_MAXS
      // in the ClassWriter constructor.
      mv.visitMaxs(5, 1);
      mv.visitEnd();
    }

    // Create a public method `getTup` which takes no args and returns a
    // scala.Tuple2
    {
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

    // Finish writing the class
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
`y`{:.language-scala} and store the result in local variable
`3`{:.language-scala}:

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

## Compiled queries with ASM

Let's use this knowledge to compile the query we originally interpreted. To
start, let's write a fast but static version of the query so we can quickly
figure out which parts need to be made dynamic. When using ASM it's a good idea
to distil the essense of which part needs to be made dynamic so the generator
code ends up being as small as possible. Since ASM is hard to read, write, and
debug, this is very important.

Let's perform the same projection and filtering we used before, but this time
without the interpretation.

```scala
object StaticQuery {

  def query(db: Seq[(String, Int, String)]) = {
    db.flatMap { case (name, birthYear, dissertation) =>
      if (birthYear < 1910 && name != null) Some((name, dissertation))
      else None
    }
  }

}
```

This should be *much* faster than our interpreted query because it's running a
simple conditional instead of walking and evaluating a
`FilterExpr`{:.language-scala} on every row.

Let's construct a quick benchmark using
[ScalaMeter](http://scalameter.github.io/) to verify our assumptions.

```scala
object QueryBenchmark extends PerformanceTest.Quickbenchmark {

  val birthYears = Gen.range("birthYears")(9990, 10000, 1)

  val records = for {
    birthYear <- birthYears
  } yield (0 until birthYear).map(("name", _, "diss"))

  performance of "Querying" in {
    measure method "StaticQuery" in {
      using (records) in { StaticQuery.query(_) }
    }

    measure method "InterpretedQuery" in {
      using (records) in {
        InterpretedQuery.query(_, Main.projections, Main.filterExpr)
      }
    }
  }

}
```

Results from the last result of each measurement:

- `StaticQuery`{:.language-scala}: 1.329709ms
- `InterpretedQuery`{:.language-scala}: 6.949921ms

The static query is between 5 and 6 times faster than interpreting.

Let's rewrite `StaticQuery`{:.language-scala} in Java and use it as a template
for compiling queries. There are at least two reasons why I significantly
dislike running ASMifier against Scala classes:

1. It generates a gnarly blob of unreadable bytes for the ScalaSignature (which
   is apparently used to store Scala-specific bits in class files and is
   required for reflection and for compiling against).
1. Scala objects and methods get split into separate class files when they're
   compiled, making it hard to stitch together the results with multiple
   ASMifier runs.

```java
import scala.Tuple3;
import scala.collection.Seq;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.Iterator;

public class StaticJavaQuery {

  public static Seq<Tuple3<String, Integer, String>> query(
      Seq<Tuple3<String, Integer, String>> db) {
    Iterator<Tuple3<String, Integer, String>> iter = db.iterator();
    ArrayBuffer<Tuple2<String, String>> acc =
      new ArrayBuffer<Tuple2<String, String>>();
    while (iter.hasNext()) {
      Tuple3<String, Integer, String> row = iter.next();
      Integer birthYear = row._2();
      if (birthYear.intValue() < 1910 && row._1() != null) {
        acc.$plus$eq(new Tuple2<String, String>(
          row._1(),
          row._3()
        ));
      }
    }
    return acc;
  }

}
```

It's not pretty, but it works, and it happens to be even faster than the static
Scala query. We'll start by feeding it to ASMifier, convert the output to Scala,
then work on making the result dynamic. Converted output, heavily annotated:

```scala
import org.objectweb.asm._, Opcodes._
import Database.FilterExpr

object CompiledQueryGen extends Opcodes {

  def generate: Array[Byte] = {

    val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
    var mv: MethodVisitor = null

    cw.visit(52, ACC_PUBLIC + ACC_SUPER, "CompiledQuery", null,
      "java/lang/Object", null)

    // Constructor
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
      mv.visitCode()
      mv.visitVarInsn(ALOAD, 0)
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
      mv.visitInsn(RETURN)
      mv.visitMaxs(1, 1)
      mv.visitEnd()
    }

    // Static query method
    {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "query", "(Lscala/collection/Seq)Lscala/collection/Seq", "(Lscala/collection/Seq<Lscala/Tuple3<Ljava/lang/StringLjava/lang/IntegerLjava/lang/String>>)Lscala/collection/Seq<Lscala/Tuple3<Ljava/lang/StringLjava/lang/IntegerLjava/lang/String>>", null)
      mv.visitCode()

      // Load the `db` argument onto the stack
      mv.visitVarInsn(ALOAD, 0)
      // Invoke the `iterator` method on db, putting the iterator on the stack
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Seq", "iterator", "()Lscala/collection/Iterator", true)
      // Store the iterator object at index 1
      mv.visitVarInsn(ASTORE, 1)

      // Stack size = 0

      // Instantiate a new ArrayBuffer `acc` {
      mv.visitTypeInsn(NEW, "scala/collection/mutable/ArrayBuffer")
      // Duplicate the reference to it on the stack
      mv.visitInsn(DUP)
      // Initialize the `acc` ArrayBuffer
      mv.visitMethodInsn(INVOKESPECIAL, "scala/collection/mutable/ArrayBuffer", "<init>", "()V", false)
      // Store the ArrayBuffer at index 2
      mv.visitVarInsn(ASTORE, 2)

      // Stack size = 0

      // A label is a point we can jump to with GOTO-style instructions. l0
      // marks the start of the while loop. The point at which the label is
      // visited represents its position and l0 is visited immediately.
      // while (...) {
      val l0 = new Label
      mv.visitLabel(l0)

      // Check the while condition
      // Load the iterator onto the stack from index 1
      mv.visitVarInsn(ALOAD, 1)
      // Call `hasNext` on the iterator, storing the boolean result on the
      // stack. The JVM stores boolean as int: 0 is false, 1 is true.
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator",
        "hasNext", "()Z", true)

      // Stack size = 1, hasNext boolean

      // Create another jump location for the end of the loop. l1 isn't visited
      // until later at the end of the loop body but we need to create the label
      // here in order to reference it in `IFEQ`.
      val l1 = new Label
      // A jump instruction with IFEQ ("if equals") checks the current value on
      // the stack. If it's 0 (false) it jumps to the label, thus ending our
      // while loop.
      mv.visitJumpInsn(IFEQ, l1)

      // Stack size = 0

      // Load iterator onto the stack again
      mv.visitVarInsn(ALOAD, 1)
      // Obtain the `row` value from the iterator
      mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Iterator", "next", "()Ljava/lang/Object", true)
      // Ensure the value is of expected type, Tuple3. This instruction pops a
      // value off the stack, checks it, then puts it back on the stack.
      mv.visitTypeInsn(CHECKCAST, "scala/Tuple3")
      // Store the row Tuple3 at local variable index 3
      mv.visitVarInsn(ASTORE, 3)
      // Load it again
      mv.visitVarInsn(ALOAD, 3)

      // Stack size = 1, row Tuple3

      // Invoke the `_2` method on the row to get the birthYear
      mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_2", "()Ljava/lang/Object", false)
      // Ensure the expected type, Integer
      mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
      // Store birthYear at local var 4
      mv.visitVarInsn(ASTORE, 4)
      // Load birthYear from local var 4
      mv.visitVarInsn(ALOAD, 4)
      // Invoke the `intValue` method on birthYear
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
      // Push a short constant on to the stack
      mv.visitIntInsn(SIPUSH, 1910)

      // Stack size = 2, birthYear int, 1910 short

      // Any time we need to branch in some way, we need labels and jump
      // instructions. l2 marks the end of the filtering if statement, allowing
      //
      // us to jump over the body.
      val l2 = new Label()

      // Jump instructions are always the inverse predicate because if it
      // evaluates to true then it jumps, skipping the body of the if block.
      // IF_ICMPGE is short for "if int compare greater than or equal", so:
      // If value1 >= value2 then jump to l2, where
      // value1 = birthYear
      // value2 = 1910
      mv.visitJumpInsn(IF_ICMPGE, l2)
      // That's the first half of the if predicate. Now we check the other half.

      // Load the row Tuple3
      mv.visitVarInsn(ALOAD, 3)
      // Invoke the `_1` method on the row to get the name String
      mv.visitMethodInsn(INVOKEVIRTUAL, "scala/Tuple3", "_1", "()Ljava/lang/Object", false)
      // This condition is much simpler and the JVM even has an instruction to
      // check for null. If the name String is null, jump to l2.
      mv.visitJumpInsn(IFNULL, l2)

      // Body of the if block {

        // Load the `acc` ArrayBuffer
        mv.visitVarInsn(ALOAD, 2)
        // Load the `row` Tuple3
        mv.visitVarInsn(ALOAD, 3)
        // Invoke the `$plus$eq` method on `acc` which mutates it, appending the
        // `row` Tuple3, and stores the result (which is simply itself) on the
        // stack.
        mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/ArrayBuffer", "$plus$eq", "(Ljava/lang/Object)Lscala/collection/mutable/ArrayBuffer", false)
        // Discard the last item on the stack since we no longer need it.
        mv.visitInsn(POP)

      // }

      // Mark the end of the if block
      mv.visitLabel(l2)

      // Jump back to the start of the while loop
      mv.visitJumpInsn(GOTO, l0)
      // Mark the end of the while loop
      mv.visitLabel(l1)
      // } // end while

      // Load the acc Tuple3
      mv.visitVarInsn(ALOAD, 2)
      // Return the object on the stack
      mv.visitInsn(ARETURN)
      // Compute the max stack size and number of local vars (computed
      // automatically for us via COMPUTE_FRAMES)
      mv.visitMaxs(0, 0)
      // End the method
      mv.visitEnd()
    }

    // End the class
    cw.visitEnd()

    // Return the bytes representing a generated classfile
    cw.toByteArray
  }
}
```

Since we're using `ClassWriter.COMPUTE_FRAMES`{:.language-scala} I was able to
remove all the `visitFrame`{:.language-scala} calls that ASMifier generated. I
also deleted the generated `FieldVisitor`{:.language-scala} and
`AnnotationVisitor`{:.language-scala} as they were both unused. I used `0` for
all arguments to `visitMaxs`{:.language-scala} as
`COMPUTE_FRAMES`{:.language-scala} implies `COMPUTE_MAXS`, which still requires
calls to `visitMaxs`{:language-scala} but ignores the arguments.

## Scala Quasiquotes

This part of the post is not yet written. The intent was to explore Scala
Quasiquotes facilities for codegen.

- [Quasiquotes](http://docs.scala-lang.org/overviews/quasiquotes/intro.html).
- [Generative Programming Basics](https://scala-lms.github.io/tutorials/02_basics.html#__toc_id:54231)
- [Introduction to code generation with scalameta](http://www.michaelpollmeier.com/2016/12/01/scalameta-code-generation-tutorial)

## Next steps

An interesting direction to take this, now that we have a foundation for
dynamically compiling queries, would be to add a SQL interface. [Apache
Calcite](https://calcite.apache.org/) is well-suited to do just that.

## Further reading

- [Java bytecode instruction listings (Wikipedia - useful reference)](http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)
- [Secrets of the Bytecode Ninjas (InfoQ)](http://www.infoq.com/articles/Secrets-of-the-Bytecode-Ninjas)
- [Hacking Java Bytecode for Programmers (part 1 of a 4 part series)](http://www.acloudtree.com/hacking-java-bytecode-for-programmers-part1-the-birds-and-the-bees-of-hex-editing/)
- [java-bytecode-asm tag on StackOverflow](http://stackoverflow.com/questions/tagged/java-bytecode-asm)
- [JVM Internals (blog post)](http://blog.jamesdbloom.com/JVMInternals.html)
- [3 approaches to Scala code generation](http://yefremov.net/blog/scala-code-generation/)
