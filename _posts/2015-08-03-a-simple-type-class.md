---
layout: article
title: A Simple type class
published: false
image:
  feature: typeclass_wide.jpg
  teaser: typeclass_teaser.jpg
  caption: Schilling Cider House, Seattle, WA
---

Type classes provide a way of achieving [ad hoc
polymorphism](https://en.wikipedia.org/wiki/Ad_hoc_polymorphism). We'll look at
what they are, why they're useful, how they're typically encoded in Scala.
We'll make our own `Simple`{:.language-scala} type class that has a single
operation called `simplify`{:.language-scala} returning a
`String`{:.language-scala}, and provide instances of it for Scala's
`List`{:.language-scala} and `Int`{:.language-scala} classes, along with our own
class `C`{:.language-scala}. Finally, we'll add convenient syntax in the Scalaz
style to make it extremely easy to use the `simplify`{:.language-scala}
operation. This might be the most hands-on, easy to understand explanation of
type classes you've ever encountered!

## Rationale

A common way to express polymorphism in Scala is inheritance. This is brittle
because the polymorphic relationship between supertype and subtype must be
defined where the subtype itself is defined. For example, if you have an
`Animal`{:.language-scala} trait, your `Dog`{:.language-scala} class must
declare its relationship as a subtype of `Animal`{:.language-scala} as part of
its definition. This is problematic when you do not own potential subtypes (i.e.
they're provided by a library). It also tightly couples the subtype to all of
its implementations of its potential supertypes. A given subtype may already
have all the methods needed to implement the behavior of a given supertype but
doesn't know or care about the supertype at definition time. Though less common,
another problem is a subtype may be able to implement a supertype's operations
in multiple, distinct ways (e.g. multiplication and addition implementations of
a Monoid over `Int`{:.language-scala}). With inheritance this is impossible.

## Scalaz

Let's start with one of the simplest type classes in Scalaz:
`Equal`{:.language-scala}. `Equal`{:.language-scala} describes "a type safe
alternative to universal equality" (if you're wondering why this is useful or
necessary, ask me in the comments). Here's an example of providing an instance
of `Equal`{:.language-scala} over our own class `C`{:.language-scala} that uses
value equality to compare two instances.

```scala
class C(val name: String)

implicit val equalC: Equal[C] = new Equal[C] {
  override def equal(c1: C, c2: C) = c1.name == c2.name
}
```

Now let's say we want a `notEqual`{:language-scala} method that works on any
`A`{:.language-scala} provided there is evidence of
`Equal[A]`{:.language-scala}:


```scala
def notEqual[A: Equal](a1: A, a2: A): Boolean =
  !implicitly[Equal[A]].equal(a1, a2)

val c1 = new C("foo")
val c2 = new C("bar")

notEqual(c1, c2)
//=> true
```

Sidenote for those unfamiliar with *context bounds*: the context bound `[A:
Equal]`{:.language-scala} is what ensures we have an implicit
`Equal[C]`{:.language-scala} instance available when running `notEqual(c1,
c2)`{:.language-scala}. An equivalent and perhaps more clear implementation
without context bounds would look like this:

```scala
def notEqual[A](a1: A, a2: A)(implicit e: Equal[A]) = !e.equal(a1, a2)
notEqual(c1, c2)
//=> true
```

However, there's an even more concise way of writing this using context bounds
and Scalaz' `/==`{:.language-scala} operator for instances of
`Equal`{:.language-scala}, or even its unicode `≠`{:.language-scala} alias:

```scala
def notEqual[A: Equal](a1: A, a2: A): Boolean = a1 /== a2
// or
def notEqual[A: Equal](a1: A, a2: A): Boolean = a1 ≠ a2
```

Since Scalaz already provides these operators, `notEqual`{:.language-scala} is purely
didactic.

Another common typelcass in Scalaz is `Show`{:.language-scala}. This corresponds
to Haskell's `Show`{:.language-haskell} type class, and is used to indicate a
type that can be represented in some way as a string, e.g. for logging or
printing in a REPL. Here's an instance for our `C`{:.language-scala} class
example from before.

```scala
implicit val showC: Show[C] = new Show[C] {
  override def show(c: C) = s"C[name=${c.name}]"
}
```

Scalaz provides syntax helpers that allow us to simply call
`.show`{:.language-scala} on any type that provides evidence of
`Show`{:.language-scala}. We'll look at how that works in detail toward the end.

```scala
new C("qux").show
//=> res22: scalaz.Cord = C[name=qux]
```

## Simple

Now that we've gotten a taste for using a few of Scalaz' type classes, let's
build our own, along with some syntax helpers in the Scalaz style.

```scala
trait Simple[F] {
  def simplify(f: F): String
}
```

This is our type class definition. It defines a single operation
`simplify`{:.language-scala} for a given `F`{:language-scala} and returns a
`String`{:.language-scala}. Before we provide instances, let's define a method
that expects a `Seq`{:.language-scala} of `Simple`{:.language-scala} instances
and outputs them separated by newlines.

```scala
def manySimple[A](simples: Seq[A])(implicit s: Simple[A]): String =
  "Many simples:\n\t" + simples.map(s.simplify).mkString("\n\t")
```

We can use this to easily try out instances. Let's start with an instance of
`C`{:.language-scala}:

```scala
implicit def simpleC: Simple[C] = new Simple[C] {
  override def simplify(c: C) = s"Simplified: ${c.show}"
}
```

We can manually call this by implicitly obtaining a
`Simple[C]`{:.language-scala} then calling `simplify`{:.language-scala}:

```scala
implicitly[Simple[C]].simplify(new C("hello"))
```

Or we can try out the `manySimple`{:.language-scala} method:

```scala
manySimple(Stream(new C("foo"), new C("bar"), new C("qux")))
//=> Many simples:
//=>        Simplified: C[name=foo]
//=>        Simplified: C[name=bar]
//=>        Simplified: C[name=qux]
```

Let's try another one: `Int`{:.language-scala}.

```scala
implicit val simpleInt: Simple[Int] = new Simple[Int] {
  override def simplify(i: Int) = s"Simplified Int with value of $i"
}
```

This is getting easy, right?

```scala
implicitly[Simple[Int]].simplify(123)
//=> Simplified Int with value of 123
```


We can even provide an instance for `List[A]`{:.language-scala} but only if
`A`{:.language-scala} itself has a `Simple`{:.language-scala} instance:

```scala
// Evidence for Simple[List[A]] given evidence of Simple[A]
implicit def simpleList[A: Simple]: Simple[List[A]] = new Simple[List[A]] {
 override def simplify(l: List[A]) = {
   val simplifyA = implicitly[Simple[A]].simplify _
   s"Simplified list:\n${l.map(simplifyA).mkString("\n")}"
 }
}
```

Try it out:

```scala
implicitly[Simple[List[Int]]].simplify((1 to 5).toList)
//=> Simplified list:
//=> Simplified Int with value of 1
//=> Simplified Int with value of 2
//=> Simplified Int with value of 3
//=> Simplified Int with value of 4
//=> Simplified Int with value of 5
```

Hopefully you have a feel for how this works (ignoring how unuseful our
`Simple`{:.language-scala} is IRL). Next, let's follow Scalaz lead and
provide some convenient syntax for working with our new type class; typing
`implicitly[Simple[_]]`{:.language-scala} over and over again is starting to get
old.


## Syntax

Wouldn't it be nice if we could just call `.simplify` on objects which provide
evidence of a `Simple`{:.language-scala}? Well, we can via some neat implicit
tricks. Check it out:

```scala
final class SimpleOps[F](val self: F)(implicit val F: Simple[F]) {
  final def /^ = F.simplify(self)
  final def ⬈ = F.simplify(self)
}

trait ToSimpleOps {
  implicit def ToSimpleOps[F](v: F)(implicit F0: Simple[F]) =
    new SimpleOps[F](v)
}

object simple extends ToSimpleOps

trait SimpleSyntax[F] {
  def F: Simple[F]
  implicit def ToSimpleOps(v: F): SimpleOps[F] =
    new SimpleOps[F](v)(SimpleSyntax.this.F)
}

// New definition of our Simple typeclass that provides an instance of
// SimpleSyntax
trait Simple[F] { self =>
  def simplify(f: F): String
  val simpleSyntax = new SimpleSyntax[F] { def F = Simple.this }
}
```

Here we've provided two syntax operators as aliases to
`simplify`{:.language-scala} (because no type class is legit without unicode
operator aliases).

```scala
import simple._

1 ⬈
//=> Simplified Int with value of 1

new C("I am C") ⬈
//=> Simplified: C[name=I am C]

List(1,2,3) ⬈
//=> Simplified Int with value of 1
//=> Simplified Int with value of 2
//=> Simplified Int with value of 3

// boring
new C("bar") /^
//=> Simplified: C[name=bar]
```

## Conclusion

Type classes are a powerful method of adding support for a set of operations on
an existing type in an ad hoc manner. Because Scala doesn't encode type classes
at the language level, there is a bit of boilerplate and implicit trickery to
create your own type classes (compare this with Haskell, where classes are
elegantly supported at the language level) and operator syntax.

[View the full code listing for this post](https://gist.github.com/devth/735ddd34e8f29fc6b872).

### Further reading

- [Introduction to Typeclasses in Scala (2013)](http://tpolecat.github.io/2013/10/12/type class.html)
- [Types and Typeclasses — Learn you a Haskell](http://learnyouahaskell.com/types-and-typeclasses)
- [Simalacrum](https://github.com/mpilquist/simulacrum) — a modern, concise type
  class encoding for Scala using annotations
