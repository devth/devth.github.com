---
layout: article
title: ThrushCond is not a Monad
categories: scala
comments: true
excerpt: "Clojure has a useful macro called cond-> that conditionally threads an initial value through a series of predicate/function pairs — let's explore a Scala equivalent"
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
We'll look at Functor (Endofunctor, to be specific), Monad, Monoid, and
Semigroup.

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
val userAddress: Option[String] = None
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
  ({_ => userAddress.isDefined}, {_.addParam("userAddress", userAddress.get)}),
  ({_.isValidAccept(accept)}, {req => req.addHeader("accept",
    req.acceptMap(accept))}))

val thrushCond = new ThrushCond(Request("/users"), steps)
val request = thrushCond.fold

//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

As you can see, it correctly skipped the 2nd step based on the
`userAddress.isDefined`{:.language-scala} condition.


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
    guard({_ => userAddress.isDefined}, {_.addParam("userAddress", userAddress.get)}),
    guard({_.isValidAccept(accept)}, {req => req.addHeader("accept",
      req.acceptMap(accept))})))

val request = requestPipeline(Request("/users"))

//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

Will this work as one of the algebraic structures mentioned at the start?

- **Functor** — Consider Functor's `fmap`{:.language-scala}:

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

- **Monad** — Likewise, Monad's `flatMap`{:.language-scala}:

  ```scala
  def flatMap[A, B](f: A => F[B]): F[A] => F[B]
  ```

  We could make `guard`{:.language-scala} fit `flatMap`{:.language-scala}'s
  signature, but there's no point in doing so for the same reason it didn't make
  sense for Functor: the essense is not how a transformation is applied, it's
  *whether* the transformation is applied, and because of the signature, the
  decision whether to perform a transformation must be embedded in the
  transformation itself, hence `guard`{:.language-scala}.

- **Monoid** — Let's see if it meets Monoid's associativity laws:

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
  [function composition is associative](https://en.wikipedia.org/wiki/Function_composition#Properties),
  but it doesn't have a Monoidial `zero`{:.language-scala}, so let's move on to
  Semigroup, which is like a Monoid in that it has an associative binary
  operation, but is more general in that it doesn't have a
  `zero`{:.language-scala} (AKA `identity`{:.language-scala}).

- **Semigroup** — ThrushCond is a Semigroup because of its associative binary
  operation, `guard`{:.language-scala}. Let's provide evidence using
  `scalaz`{:.language-scala}'s `Semigroup`{:.language-scala}:

  ```scala
  import scalaz._, Scalaz._

  type Step[A] = (A => Boolean, A => A)

  case class ThrushCond[A](steps: Seq[Step[A]]) {
    /** Perform a pipeline step only if the value meets a predicate */
    def guard[A](pred: (A => Boolean), fn: (A => A)): (A => A) =
      (a: A) => if (pred(a)) fn(a) else a
    /** Compose the steps into a single function */
    def comp = Function.chain(steps.map { step => guard(step._1, step._2) })
    /** Run a value through the pipeline */
    def run(a: A) = comp(a)
  }

  /** Evidence of a Semigroup */
  case object ThrushCond {
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

  And finally, back to our `Request`{:.language-scala} example in Clojure,
  splitting it into two pipelines for the purpose of demonstration (and possible
  separation of concerns):

  ```scala
  val userPipeline = ThrushCond[Request](Seq(
    ({_ => userName.isDefined}, {_.addParam("userName", userName.get)}),
    ({_ => userAddress.isDefined}, {_.addParam("userAddress", userAddress.get)})))

  val headerPipeline = ThrushCond[Request](Seq(
    ({_.isValidAccept(accept)}, {req => req.addHeader("accept",
      req.acceptMap(accept))})))

  val requestPipeline = userPipeline |+| headerPipeline

  requestPipeline run Request("/users")
  //=>
  Request(/users,Map(userName -> devth),Map(accept -> application/json))
  ```

ThrushCond is not a Monad, nor an Endofunctor, nor a Monoid, **but it is a
Semigroup**.


