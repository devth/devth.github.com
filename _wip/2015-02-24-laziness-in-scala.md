---
layout: article
title: Laziness in Scala
categories: scala
comments: true
excerpt: "The freedom to do anything you wish is rarely a good thing, whether you're an experienced developer or a 2-year-old child"
image:
  feature: constraint_1200_300.jpg
  teaser: constraint_410_228.jpg
  caption: Oak Alley Plantation, Vacherie, LA. September 2014
---

Let's look at the various ways Scala allows us to express laziness.


## ||

The familiar `||` operator might be the simplest case of laziness. Most
languages provide laziness on this operator, and Scala is no exception.

```scala
def a: Boolean = {
  println("compute a")
  true
}

def b: Boolean = {
  println("compute b")
  false
}

scala> a && b
compute a
compute b
res1: Boolean = false

// b is not computed, because a's true result short-circuits the expression
scala> a || b
compute a
res1: Boolean = true
```

Ok, that was pretty boring. Let's move on.


## lazy val


Scala lets us lazily compute the values marked with `lazy`{:.language-scala} on
an as-needed basis, and memoizes the result.

```scala
class C {
  lazy val e = expensive
  val a = notExpensive
  def expensive: Int = {
    println("I have acquired very particular set of skills.")
    Thread.sleep(1000)
    42
  }
  def notExpensive: Int = {
    println("Hello, this is notExpensive")
    24
  }
}

scala> new C
Hello, this is notExpensive
res7: C = C@770c2e6b

scala> res7.a
res9: Int = 24

scala> res7.e
I have acquired very particular set of skills.
res8: Int = 42

scala> res7.e
res8: Int = 42
```

The lazy `e`{:.language-scala} value on `C`{:.language-scala} didn't get
evaluated until we explicitly referenced it. Once it was evaluated `res7.e` got
memoized so the second time we evaluated it we didn't have to wait
1000 ms nor observe `expensive`'s [weirdo side effects](http://knowyourmeme.com/memes/i-will-find-you-and-i-will-kill-you).
Neat.



## Thunks

A thunk is a function that takes zero arguments and returns a value. The point
of wrapping it in a function is to delay or potentially avoid execution.

```scala
val thunk = () => {
  println("i am thunk")
  42
}

thunk
//=> res2: () => Int = <function0>

thunk()
//=> i am thunk
//=> res3: Int = 42
```

N.B. thunks are not memoized, so every time you evaluate it, the function gets
called. Something to be aware of if the function has any significant complexity.

Scala has some nice call-by-name syntax on function args for delaying the
execution of a value-producing computation.

```scala
def sq(x: => Int) = x * x

// You can call it with ordinary values
sq(5)

// Or with a thunk
def producesInt = {
  println("producing an int...")
  42
}
sq(producesInt)
//=> producing an int...
//=> producing an int...
//=> res2: Int = 1764
```

Note how `producesInt`{.:language-scala} was called twice. To avoid this you
need to manually store the value of the computation inside a function if you
plan to use it more than once.


## Streams

## Working with data structures: filter map fold
Build a lazy cons cell?
Views



## References

https://plus.google.com/+DanPiponi/posts/iu8rVgk2bcW

