---
layout: article
title: ThrushCond is not a Monad
categories: scala
comments: true
excerpt: "Clojure has a useful macro called cond-> — let's explore a Scala equivalent"
image:
  feature: thrush_cond_feature.jpg
  teaser: thrush_cond_teaser.jpg
  caption: Singletrack sunrise, Billings, MT. October 2014
---

Clojure has a useful macro called
[cond->](https://clojuredocs.org/clojure.core/cond-%3E) that conditionally
threads an initial value through a series of predicate/function pairs only
applying each function if its predicate returns true. In this post we're going
to look at a Scala representation, and whether it fits the shape and laws of any
[common algebraic structures](https://en.wikipedia.org/wiki/Outline_of_algebraic_structures#Types_of_algebraic_structures).
We'll look at Functor, Monad, Semigroup, and Monoid.

Let's start with an example in Clojure. We want to build up a request based on
some arbitrary conditions:

```clojure
;; sample values from user input

(def user-id 1)
(def user-name "devth")
(def user-address nil)
(def accept :json)

;; validation and helper functions

(def accept-map {:html "text/html" :json "application/json"})
(defn valid-accept? [a] (accept-map a))
(defn set-header [req k v] (update-in req [:headers] assoc k v))
(defn set-param [req k v] (update-in req [:params] assoc k v))

;; build up a request map using cond-> to decide which items to add to params
;; and headers maps

(def request
  (cond-> {:target "/users" :params {:user-id user-id} :headers {}}
    user-name (set-param :user-name user-name)
    user-address (set-param :user-address user-address)
    (valid-accept? accept) (set-header :accept accept)))

;; request value:

{:target "/users"
 :query-params {:user-id 1 :user-name "devth"}
 :headers {:accept "application/json"}}
```

Since Clojure's `->`{:.language-clojure} operator is sometimes referred to as
the "thrush" operator, I'm going to call `cond->`{:.language-clojure}
in Scala `ThrushCond`{:.language-scala}.

First let's model the `Request`{:.language-scala} and helpers equivalent to
those we used in the Clojure example:

```scala
case class Request(
  target: String,
  params: Map[String, String] = Map.empty,
  headers: Map[String, String] = Map.empty) {

  // validation and helper functions
  val acceptMap = Map("html" -> "text/html", "json" -> "application/json")
  val isValidAccept: (String => Boolean) = acceptMap.isDefinedAt _

  def addParam(k: String, v: String) = this.copy(params=params.updated(k, v))

  def addHeader(k: String, v: String) = this.copy(headers=headers.updated(k, v))
}

// sample values from user input

val userId: Int = 1
val userName: Option[String] = Some("devth")
val address: Option[String] = None
val accept = "json"
```

Now we'll create the `ThrushCond`{:.language-scala} class that takes an initial
value, the steps as a `Seq`{:.language-scala} of a predicate/function pairs,
and defines a `fold`{:.language-scala} method which runs the computation:

```scala
type Step[A] = (A => Boolean, A => A)

class ThrushCond[A](init: A, steps: Seq[Step[A]]) {
  def fold = steps.foldLeft(init) { case (acc: A, step: Step[A]) =>
    if (step._1(acc)) step._2(acc) else acc
  }
}
```

Try it out:

```scala
val steps: Seq[Step[Request]] = Seq(
  ({_ => userName.isDefined}, {_.addParam("userName", userName.get)}),
  ({_ => address.isDefined}, {_.addParam("address", address.get)}),
  ({_.isValidAccept(accept)}, {req => req.addHeader("accept",
    req.acceptMap(accept))}))

val thrushCond = new ThrushCond(Request("/users"), steps)
val request = thrushCond.fold

//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

As you can see, it correctly skipped the 2nd step based on the
`address.isDefined`{:.language-scala} condition.


We can simplify this a bit by distilling the essense of
`ThrushCond`{:.language-scala} into a higher-order `guard` function, then use
function composition to do the sequencing:

```scala
object ThrushCond {
  def guard[A](pred: (A => Boolean), fn: (A => A)): (A => A) =
    (a: A) => if (pred(a)) fn(a) else a
}

import ThrushCond.guard

val requestPipeline: (Request => Request) =
  Function.chain(Seq(
    guard({_ => userName.isDefined}, {_.addParam("userName", userName.get)}),
    guard({_ => address.isDefined}, {_.addParam("address", address.get)}),
    guard({_.isValidAccept(accept)}, {req => req.addHeader("accept",
      req.acceptMap(accept))})))

val request = requestPipeline(Request("/users"))

//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

Will this work as one of the algebraic structures mentioned at the start?

## Functor

Consider Functor's `fmap`{:.language-scala}:

```scala
def fmap[A, B](f: A => B): F[A] => F[B]
```

In our case, both `A`{:.language-scala} and `B`{:.language-scala} are the
same type, `Request`{:.language-scala}. `guard`{:.language-scala} produces a
function that fits, but we could easily use that with an existing Functor,
e.g.:

```scala
val step: Request => Request =
  guard({_ => userName.isDefined}, {setParam(_, "userName", userName.get)})
Some(Request("/users")).map(step)
```

The essense of `ThrushCond`{:.language-scala} is in `guard`{:.language-scala}
itself so it makes no sense to design a new Functor around it.

## Monad

Likewise, Monad's `flatMap`{:.language-scala}:

```scala
def flatMap[A, B](f: A => F[B]): F[A] => F[B]
```

We could make `guard`{:.language-scala} fit `flatMap`{:.language-scala}'s
signature, but there's no point in doing so for the same reason it didn't make
sense for Functor: the essense is not how a transformation is applied, it's
*whether* the transformation is applied, and because of the signature, the
decision whether to perform a transformation must be embedded in the
transformation itself, hence `guard`{:.language-scala}.

## Semigroup

Let's see if it meets Semigroup's associativity laws:

```scala
case class F(x: Int)
val f = F(10)
val always = Function.const(true) _

val mult2: F => F = guard(always, {f => f.copy(x = f.x * 2)})
val sub4: F => F = guard(always, {f => f.copy(x = f.x - 4)})
val sub6: F => F = guard(always, {f => f.copy(x = f.x - 6)})

val g: (F => F) = (mult2 andThen sub6) andThen sub4
val h: (F => F) = mult2 andThen (sub6 andThen sub4)

g(f)
//=> F(10)
h(f)
//=> F(10)
```

`guard`{:.language-scala} is associative when composed with itself because
[function composition is associative](https://en.wikipedia.org/wiki/Function_composition#Properties).
Because of this associative binary operation we can provide evidence that
ThrushCond is a Semigroup using `scalaz`{:.language-scala}'s
`Semigroup`{:.language-scala} representation:

```scala
import scalaz._, Scalaz._

type Step[A] = (A => Boolean, A => A)

case class ThrushCond[A](steps: Seq[Step[A]] = Seq.empty) {
  /** Perform a pipeline step only if the value meets a predicate */
  def guard[A](pred: (A => Boolean), fn: (A => A)): (A => A) =
    (a: A) => if (pred(a)) fn(a) else a
  /** Compose the steps into a single function */
  def comp = Function.chain(steps.map { step => guard(step._1, step._2) })
  /** Run a value through the pipeline */
  def run(a: A) = comp(a)
}

case object ThrushCond {
  /** Evidence of a Semigroup */
  implicit def thrushCondSemigroup[A]: Semigroup[ThrushCond[A]] =
    new Semigroup[ThrushCond[A]] {
      def append(t1: ThrushCond[A], t2: => ThrushCond[A]): ThrushCond[A] =
        ThrushCond[A](Seq((Function.const(true), t2.comp compose t1.comp)))
    }
}
```

We've defined a Semigroup over the set of all ThrushConds. What does this give
us? We can now combine any number of ThrushConds using Semigroup's
`|+|`{:.language-scala} operator. A simple example using
`ThrushCond[Int]`{:.language-scala}:

```scala
import ThrushCond.thrushCondSemigroup

val addPipeline = ThrushCond[Int](Seq(
  ((_ > 10), (_ + 2)),
  ((_ < 20), (_ + 20))))

val multPipeline = ThrushCond[Int](Seq(
  ((_ == 70), (_ * 10)),
  ((_ > 0), (_ * 7))))

val pipeline = addPipeline |+| multPipeline

// Examples
multPipeline run 70 //=> 70 * 10 * 7 == 4900
pipeline run 2 //=> (2 + 20) * 7 == 154
pipeline run 12 //=> (12 + 2 + 20) * 7 == 238
```


## Monoid (with PlusEmpty)

Monoids are Semigroups with an identity element. ThrushCond's
identity is simply a ThrushCond with an empty `Seq`{:.language-scala} of
steps. However, as [@lmm mentioned in the comments](http://devth.com/2015/thrush-cond-is-not-a-monad/#comment-2082941866):

> it's not ThrushCond itself that forms a Monoid but rather ThrushCond[A] for
> any given A

This is where PlusEmpty comes in. PlusEmpty is a ["universally quantified
Monoid"](https://github.com/scalaz/scalaz/blob/series/7.2.x/core/src/main/scala/scalaz/PlusEmpty.scala#L3-7)
which means it's like a Monoid but for first-order `* -> *`{:.language-scala}
types instead of proper `*`{:.language-scala} types. PlusEmpty itself is a
higher-order `(* -> *) -> *` type. A helpful quote from #scalaz:

> tpolecat: so `String`{:.language-scala} is a monoid, but
> `List`{:.language-scala} is a PlusEmpty (which means that
> `List[A]`{:.language-scala} is a monoid for all `A`{:.language-scala})

To provide evidence of a PlusEmpty, we must be able to implement these two
methods (where `F`{:.language-scala} is `ThrushCond`{:.language-scala}):

```scala
def plus[A](a: F[A], b: => F[A]): F[A] // from Plus
def empty[A]: F[A] // from PlusEmpty which extends Plus
```

We already implemented `plus` for Semigroup's `append`, and `empty` is simply
a `ThrushCond` with an empty `Seq` of steps.

```scala
case object ThrushCond {
  /** Evidence of a PlusEmpty */
  implicit def thrushCondPlusEmpty: PlusEmpty[ThrushCond] =
    new PlusEmpty[ThrushCond] {
      def plus[A](a: ThrushCond[A], b: => ThrushCond[A]): ThrushCond[A] =
        ThrushCond[A](Seq((Function.const(true), b.comp compose a.comp)))

      def empty[A]: ThrushCond[A] = ThrushCond[A]()
    }
  /** Use PlusEmpty to provide evidence of a Monoid[Request] */
  implicit def requestMonoid: Monoid[ThrushCond[Request]] =
    thrushCondPlusEmpty.monoid[Request]
}
```

Let's go back to our `Request`{:.language-scala} example in Clojure and use
PlusEmpty's `<+>`{:.language-scala} to combine separate transformation
pipelines:

```scala
import ThrushCond._ // evidence

val userPipeline = ThrushCond[Request](Seq(
  ({_ => userName.isDefined}, {_.addParam("userName", userName.get)}),
  ({_ => address.isDefined}, {_.addParam("address", address.get)})))

val headerPipeline = ThrushCond[Request](Seq(
  ({_.isValidAccept(accept)}, {req =>
    req.addHeader("accept", req.acceptMap(accept))})))

// <+> is an alias for plus
val requestPipeline = userPipeline <+> headerPipeline
// A PlusEmpty[ThrushCond] is implicitly obtained and used to plus the two
// ThrushCond[Request]s

requestPipeline run Request("/users")
//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

Because PlusEmpty can derive a Monoid for a given type, we can combine any
number of ThrushConds from a List. Let's construct one more ThrushCond
pipeline that conditionally adds a cache-control header and try out our Monoid
using `Foldable`{:.language-scala}'s `suml`{:.language-scala}:

```scala
import scala.language.postfixOps

val shouldCache = false

val cachePipeline = ThrushCond[Request](Seq(
  ({_ => !shouldCache}, {_.addHeader("cache-control", "no-cache")})))

val requestPipeline = List(userPipeline, headerPipeline, cachePipeline) suml
//=>
Request(/users,
  Map(userName -> devth),
  Map(accept -> application/json, cache-control -> no-cache))
```

ThrushCond is not a Monad, nor a Functor, **but it is a PlusEmpty from which can
be derived a Monoid**.

*Updated July 1, 2015: incorporated lmm's PlusEmpty suggestion.*

