---
layout: article
title: Parametric polymorphism syntax in Scala and Haskell
toc: true
image:
  feature: overture.jpg
  caption: Mount Rainier from Mailbox Peak, WA
---

This is a quick comparison in parametric polymorphism syntax between Scala and
Haskell. Note that in reality, Scala's
[functions are monomorphic](http://www.chuusai.com/2012/04/27/shapeless-polymorphic-function-values-1/)
while its methods are polymophic, but we'll pay no mind to that distinction here.


## Simple

```scala
  def id[A](x: A): A = x
```

```haskell
  id :: a -> a
  id x = x
```

## Algebraic data type

```scala
sealed trait Bool
case object True extends Bool
case object False extends Bool

def and(x: Bool, y: Bool): Bool = (x, y) match {
  case (True, True) => True
  case _ => False
}

and(True, False)
// False

def or(x: Bool, y: Bool): Bool = (x, y) match {
  case (False, False) => False
  case _ => True
}

or(False, True)
// True
```

```haskell
data Bool = False | True deriving (Show)

and :: Bool -> Bool -> Bool
and True True = True
and _ _ = False

and True False
-- False

or :: Bool -> Bool -> Bool
or False False = False
or _ _ = True

or False True
-- True
```

In terms of clarity Haskell is the clear winner.

## Type bounds




## Existential types

## Rank 2

## Dependent
