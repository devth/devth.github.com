---
layout: article
title: Glossary of terms
toc: true
---

These are terms and concepts that I found unfamiliar the first time I
encountered them. In most cases I'll provide a brief definition or example with
a link to a more complete definition.


## Rank-1 Polymorphic Function

A rank-1 polymorhipc function is simply a function that takes a single type
parameter and whose arguments operate over the given type. An example based on
[Higher-Rank Polymorphism in Scala](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala/):

{% highlight scala %}
def r1[A](f: A => A, a: A): A = f(a)
r1({ i: Int => i * i }, 10)
// res4: Int = 100
{% endhighlight %}

## Rank-2 Polymorphic Function

Again from
[Higher-Rank Polymorphism in Scala](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala/):

Scala doesn't have rank-n types, so we need to rely on a workaround:

{% highlight scala %}
trait ~>[F[_],G[_]] {
  def apply[A](a: F[A]): G[A]
}
{% endhighlight %}

Read the post to see how the `~>` trait can be used to achieve rank-n types.

Another resource describing Haskell's support is
[Higher rank types](http://en.wikibooks.org/wiki/Haskell/Polymorphism#Higher_rank_types).


## Natural Transformation

> A natural transformation is a mapping between functors that preserves the
> structure of the underlying categories.
> — <cite>Bartosz Milewski from [Understanding Yoneda](https://www.fpcomplete.com/user/bartosz/understanding-yoneda)</cite>

This is a case where definitions are confusing, but the actual thing is quite
simple in practice. Let's try that out in Scala using the Functors `List` and
`Option`.

{% highlight scala %}
val someString: Option[String] = Some("foo")
val noneString: Option[String] = None

// We can intuitively think of Option as being a List of 1 element, so the
// natural transformation is trivial (avoiding the use of Scala's own .toList):
def optionToList[A](a: Option[A]): List[A] =
  a.map(x => List(x)).getOrElse(List.empty[A])

optionToList(someString)
//=> res: List[String] = List(foo)

optionToList(noneString)
//=> res: List[String] = List()

optionToList(None)
//=> res: List[Nothing] = List()
{% endhighlight %}

Thus, `optionToList` is a natural transformation from `Option` to `List`. Note
that a natural transformation does not exist from `List` to `Option`.


## Isomorphism

> In mathematics, an isomorphism (from the Greek: isos "equal", and morphe
> "shape") is a homomorphism (or more generally a morphism) **that admits an
> inverse**. Two mathematical objects are isomorphic if an isomorphism exists
> between them.
> — <cite>[Wikipedia](http://en.wikipedia.org/wiki/Isomorphism)

Now the Scala:

{% highlight scala %}
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
{% endhighlight %}

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

> In java.lang: not a good start
> public interface Comparable<T> {
>   public int compareTo(T o);
> }
> Representing a 3-valued type with a 2³² valued one
