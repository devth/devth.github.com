---
layout: article
title: Monad laws in Scala
categories: scala
comments: true
excerpt: "The three Monad laws may seem pretty abstract at first, but they're quite practical"
image:
  feature: monad_laws_1200_300.jpg
  teaser: monad_laws_410_228.jpg
  caption: Mount St. Helens from Mount Adams, WA. July 2011
---

The three Monad laws may seem pretty abstract at first, but they're quite
practical.  Let's try to internalize the laws by running through two of Scala's
most popular monads and making sure they adhere. We could use something like
[ScalaCheck](http://scalacheck.org/) to more rigorously check these laws, but
the purpose of this post is to help internalize them, so we'll only be manually
verifying them using our intuition.

Here are the laws, from [Monad laws](https://wiki.haskell.org/Monad_laws) on HaskellWiki.

1. Left identity: `return a >>= f ≡ f a`{:.language-haskell}
1. Right identity: `m >>= return ≡ m`{:.language-haskell}
1. Associativity: `(m >>= f) >>= g ≡  m >>= (\x -> f x >>= g)`{:.language-haskell}

In Haskell, `return`{:.language-haskell} is used to "inject a value into the
monadic type". In Scala, we do this via constructors (unless you're using
Scalaz, in which case you probably already know everything this post has to
offer).

The `>>=`{:.language-haskell} operator in Haskell corresponds to Scala's
`flatMap`{:.language-scala} method.

## List

The List Monad deals with the context of non-determinism—that is, it represents
multiple values. When we run multiple lists through a sequence comprehension we
end up with the all combinations of values from each list.

```scala
for (x <- List(1, 2, 3); y <- List('a', 'b', 'c')) yield (x, y)
=> List((1,a), (1,b), (1,c), (2,a), (2,b), (2,c), (3,a), (3,b), (3,c))
```

Now the laws. Let's setup two simple functions `f` and `g`, both of type `Int =>
List[Int]`{:.language-scala}.

```scala
// Let f be a function that takes an Int and produces a List of its
// neighboring Ints along with itself:
val f: (Int => List[Int]) = x => List(x - 1, x, x + 1)

// Let g be a function that takes an Int x
// and produces a List containing +x and -x
val g: (Int => List[Int]) = x => List(x, -x)
```

### Left identity

```scala
val a = 2
val lhs = List(a).flatMap(f)
=> List(1, 2, 3)

val rhs = f(a)
=> List(1, 2, 3)

lhs == rhs
=> true
```

### Right identity

```scala
val m = List(2)

val lhs = m.flatMap(List(_))
=> List(2)

val rhs = m
=> List(2)

lhs == rhs
=> true
```

### Associativity

```scala
val m = List(1, 2)

val lhs = m.flatMap(f).flatMap(g)
=> List(0, 0, 1, -1, 2, -2, 1, -1, 2, -2, 3, -3)
// Sidenote: now do you see what is meant by non-determinism?

val rhs = m.flatMap(x => f(x).flatMap(g))
=> List(0, 0, 1, -1, 2, -2, 1, -1, 2, -2, 3, -3)

lhs == rhs
=> true
```

Looks good to me.

## Option

Let's create new test functions `f` and `g` of type `Int =>
Option[Int]`{:.language-scala}. Given the type signature, it's natural to think
of `f` and `g` as *partial functions* that are only defined on certain inputs.

```scala
// If x is not less than 10, return 2x
val f: (Int => Option[Int]) = x => if (x < 10) None else Some(x * 2)

// If x is reater than 50, return x + 1
val g: (Int => Option[Int]) = x => if (x > 50) Some(x + 1) else None
```

For the sake of testing our laws, the implementations of these functions really
don't matter as long as the types line up.

### Left identity

```scala
val a = 30
val lhs = Option(a).flatMap(f)
=> Some(60)

val rhs = f(a)
=> Some(60)

lhs == rhs
=> true
```

### Right identity

```scala
val m = Option(30)

val lhs = m.flatMap(Option(_))
=> Some(30)

val rhs = m
=> Some(30)

lhs == rhs
=> true
```

### Associativity

```scala
val m = Option(30)

val lhs = m.flatMap(f).flatMap(g)
=> Some(61)

val rhs = m.flatMap(x => f(x).flatMap(g))
=> Some(61)

lhs == rhs
=> true
```

## The end

I hope this post helped you internalize the Monad laws. If you need more
practice, continue this exercise in your REPL for the `Try`{:.language-scala}
and `Either`{:.language-scala} monads, or better yet: create your own Monad and
verify that it obeys the laws!

### Further reading

- [Monad laws — learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad+laws.html)
- [Monad laws — HaskellWiki](https://wiki.haskell.org/Monad_laws)
- [A Fistful of Monads — Learn You a Haskell](http://learnyouahaskell.com/a-fistful-of-monads#monad-laws)
