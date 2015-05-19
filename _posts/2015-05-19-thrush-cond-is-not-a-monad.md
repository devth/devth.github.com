---
layout: article
title: ThrushCond is not a Monad
categories: scala
comments: true
excerpt: "Clojure has a useful macro called cond-> that conditionally threads an initial value through a series of predicate/function pairs"
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
common algebraic structures: Monoid, Functor, and Monad.

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
`ThrushCond`{:.language-scala}.

First let's model the `Request`{:.language-scala} and helpers equivalent to
those we used in the Clojure example:

```scala
case class Request(
  target: String,
  params: Map[String, String] = Map.empty,
  headers: Map[String, String] = Map.empty)

// sample values from user input

val userId: Int = 1
val userName: Option[String] = Some("devth")
val userAddress: Option[String] = None
val accept = "json"

// validation and helper functions

val acceptMap = Map("html" -> "text/html", "json" -> "application/json")
val isValidAccept: (String => Boolean) = acceptMap.isDefinedAt _

// too bad Scala doesn't have built-in Lenses!
def setParam(req: Request, k: String, v: String) =
  req.copy(params=req.params.updated(k, v))

def setHeader(req: Request, k: String, v: String) =
  req.copy(headers=req.headers.updated(k, v))
```

Now let's create the `ThrushCond`{:.language-scala} class that takes an initial
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

Let's try it out.

```scala
val steps: Seq[Step[Request]] = Seq(
  ({_ => userName.isDefined}, {setParam(_, "userName", userName.get)}),
  ({_ => userAddress.isDefined}, {setParam(_, "userAddress", userAddress.get)}),
  ({_ => isValidAccept(accept)}, {setHeader(_, "accept", acceptMap(accept))})
)

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
    guard({_ => userName.isDefined}, {setParam(_, "userName", userName.get)}),
    guard({_ => userAddress.isDefined}, {setParam(_, "userAddress", userAddress.get)}),
    guard({_ => isValidAccept(accept)}, {setHeader(_, "accept", acceptMap(accept))})))

val request = requestPipeline(Request("/users"))

//=>
Request(/users,Map(userName -> devth),Map(accept -> application/json))
```

Will this work as a Monoid, Functor, or Monad?

- **Monoid** — It fails to meet Monoid's associativity laws: the application order of the
  "steps" *does* matter.
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

ThrushCond is not a Monad (nor a Functor, nor a Monoid).
