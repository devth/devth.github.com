---
layout: article
title: Glossary of terms
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

— Bartosz Milewski from [Understanding Yoneda](https://www.fpcomplete.com/user/bartosz/understanding-yoneda)

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



## Domain

Types represent domains: they restrict the values in a particular domain. For an
interesting expansion of this idea, see slides from the [Age is not an
int](http://www.slideshare.net/oxbow_lakes/age-is-not-an-int) talk. And from
that talk, here's some very brief reasoning on why you want your types to
semantically restrict their domain.

![java.lang](http://image.slidesharecdn.com/ageisnotanint-150113065504-conversion-gate01/95/age-is-not-an-int-8-638.jpg)
