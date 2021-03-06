---
layout: article
title: Glossary of terms
image:
  teaser: granite_300_200.jpg
  feature: granite_1200_300.jpg
  caption: On the summit of Granite Peak, Montana's highest point at 12,808 ft
toc: true
---

These are terms and concepts that I found unfamiliar the first time I
encountered them. In most cases I'll provide a brief definition or example with
a link to a more complete definition. This is a work in progress.


## Natural Transformation

> A natural transformation is a mapping between functors that preserves the
> structure of the underlying categories.
> — <cite>Bartosz Milewski from [Understanding Yoneda](https://www.fpcomplete.com/user/bartosz/understanding-yoneda)</cite>

This is a case where definitions are confusing, but the actual thing is quite
simple in practice. Let's try that out in Scala using the Functors `List`{:.language-scala} and
`Option`{:.language-scala}.

``` scala
val someString: Option[String] = Some("foo")
val noneString: Option[String] = None

// We can intuitively think of Option as being a List of 0 or 1 elements, so the
// natural transformation is trivial (note: I'm intentionally avoiding the use
// of Scala's own .toList):
def optionToList[A](a: Option[A]): List[A] =
  a.map(x => List(x)).getOrElse(List.empty[A])

optionToList(someString)
//=> res: List[String] = List(foo)

optionToList(noneString)
//=> res: List[String] = List()

optionToList(None)
//=> res: List[Nothing] = List()
```

Thus, `optionToList`{:.language-scala} is a natural transformation from `Option`{:.language-scala} to `List`{:.language-scala}. Note
that a natural transformation does not exist from `List`{:.language-scala} to `Option`{:.language-scala}.


## Isomorphism

> In mathematics, an isomorphism (from the Greek: isos "equal", and morphe
> "shape") is a homomorphism (or more generally a morphism) **that admits an
> inverse**. Two mathematical objects are isomorphic if an isomorphism exists
> between them.
> — <cite>[Wikipedia](http://en.wikipedia.org/wiki/Isomorphism)

Now the Scala in which we'll describe the world's most trivial isomophism:

```scala
// These two objects are isomorphic because a morphism (i.e. function) exists
// that maps each to the other.
val nameAge = ("foo", 42)
val ageName = (42, "foo")

def tuple2Iso[A, B](p: (A, B)): (B, A) = (p._2, p._1)

// A => B
tuple2Iso(nameAge) == ageName
//=> true

// B => A
tuple2Iso(ageName) == nameAge
//=> true

tuple2Iso(tuple2Iso(nameAge)) == nameAge
//=> true
```

Another resource on Isomorphisms in Scalaz:
[learning Scalaz — Isomorphisms](http://eed3si9n.com/learning-scalaz/Isomorphisms.html)

## Domain and Codomain

Domains come from set theory, and represent the set of input values for a
function, while codomains represent the set of output values. Therefore, a
function is the mapping between its domain and codomain.

In programming, types and domains are related but not quite the same. Domains
are strictly related to functions, while a type specifies a set of values.

In the context of functions, we can say that types are used to represent a given
function's domain and codomain.

For an interesting expansion of this concept, see slides from
the [Age is not an int](http://www.slideshare.net/oxbow_lakes/age-is-not-an-int)
talk. From that talk, here's some very brief reasoning on why you want your
types to semantically restrict domains:

> In java.lang: not a good start<br />
> public interface Comparable<T> {<br />
>   public int compareTo(T o);<br />
> }<br />
> Representing a 3-valued type with a 2³² valued one


## Existential types

Read [Existential type](https://www.haskell.org/haskellwiki/Existential_type) on
the HaskellWiki.

Existential types provide a way of baking the generics into a type instead of
explicitly declaring them. Consider the ultra-contrived example where we have a
type `Things`{:.language-scala} which contains a list of stuff whose type we don't care about
because the only operation we want to perform on it is to count how many there
are.

```scala
// Without existential types
case class Things[A](list: List[A])
val intThings: Things[Int] = Things(List(1, 2, 3))
def count[A](ts: Things[A]) = ts.list.size
count(intThings)
//=> 3
```

Notice how we had to specify a type `A`{:.language-scala} on
`count`{:.language-scala} even though we didn't actually care about it? Also,
the type of `intThings`{:.language-scala} was `Things[Int]`{:.language-scala},
though we could have used `Things[_]`{:.language-scala} to indicate we don't
care, or even better just let its type be inferred. But that's not the point.

Now let's use existential types to bake `A`{:.language-scala} into
`Things`{:.language-scala} since we don't care.

```scala
case class Things(list: List[A] forSome { type A })
def count(ts: Things) = ts.list.size
val someThings = Things(List("apathetic", "types"))
count(someThings)
//=> 2
```

Existential types let us drop the type annotation on `count`{:.language-scala}! Note, there's a
shorthand way of expressing this:

``` scala
case class Things(list: List[_])
```

Existential types can also rely on upper or lower bounds.

``` scala
// Let's make Things only take Seqs while using Existential types shorthand in
// our upper bound
case class SeqThings(list: List[A] forSome { type A <: Seq[_] })

// Strings are Seqs
val stringThings = SeqThings(List("foo", "bar"))

// As are Lists of course
val listThings = SeqThings(List(List(1, 2), List(3)))

// Nope!
val intThings = SeqThings(List(1, 2, 3))
// <console>:9: error: type mismatch;
//  found   : Int(3)
//  required: Seq[_]
//        val intThings = SeqThings(List(1, 2, 3))
//                                             ^
```

Note: Scala does not have existential types apart from `Any`{:.language-scala} (according to
[Rúnar Bjarnason](https://www.youtube.com/watch?v=hzf3hTUKk8U) who mentions this
in his [FP is terrible](https://www.youtube.com/watch?v=hzf3hTUKk8U) talk).

## Rank-1 Polymorphic Function

An example based on
[Higher-Rank Polymorphism in Scala](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala/):

```scala
def r1[A](f: A => A, a: A): A = f(a)
r1({ i: Int => i * i }, 10)
// res4: Int = 100
```

## Rank-2 Polymorphic Function

Again from
[Higher-Rank Polymorphism in Scala](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala/):

Scala doesn't have rank-n types, so we need to rely on a workaround:

```scala
trait ~>[F[_],G[_]] {
  def apply[A](a: F[A]): G[A]
}
```

Read the post to see how the `~>`{:.language-scala} trait can be used to achieve rank-n types.

Another resource describing Haskell's support is
[Higher rank types](http://en.wikibooks.org/wiki/Haskell/Polymorphism#Higher_rank_types).

## Parametricity

>> captures the intuition that all instances of a polymorphic function act the
>> same way
> — <cite>[Wikipedia](https://en.wikipedia.org/wiki/Parametricity)

## Partial Function

A function which is not defined for some inputs.

## Total Function

A function which is defined for all inputs, as opposed to a partial function.

## Extensionality and Intensionality

> In logic, extensionality, or extensional equality, refers to principles that
> judge objects to be equal if they have the same external properties.  It
> stands in contrast to the concept of intensionality, which is concerned with
> whether the internal definitions of objects are the same.  —
> <cite>[Wikipedia](http://en.wikipedia.org/wiki/Extensionality)</cite>

```haskell
f :: Int -> Int
f x = x + x + x

g :: Int -> Int
g x = x * 3
```

`f`{:.language-haskell} and `g`{:.language-haskell} are extensionally equal, but
not intensionally equal.

```haskell
h :: Int -> Int
h x = x + x + x
```

`f`{:.language-haskell} and `h`{:.language-haskell} are intensionally equal.
