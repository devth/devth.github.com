---
layout: article
title: Theorems for free in Haskell
image:
  feature: overture.jpg
  caption: Mount Rainier from Mailbox Peak, WA
---

I found the syntax Philip Wadler's 1989 paper [Theorems for
free!](http://ttic.uchicago.edu/~dreyer/course/papers/wadler.pdf)
hard to internalize so I decided to translate pieces of it it to Haskell and
rewrite sections in my own words as a learning excercise.

> From the type of a polymorphic function we can derive a theorem that it
> satisfies. Every function of the same type satisfies the same theorem. This
> provides a free source of useful theorems, courtesy of Reynolds' abstraction
> theorem for the polymorphic lambda calculus.

Given a function `r`{:.language-haskell} of the type:

```haskell
r :: [x] -> [x]
```

Wadler says from this it's possible to conclude that `r`{:.language-haskell}
satisfies the following theorem:

```haskell
f :: a -> b
map :: (a -> b) -> [a] -> [b]

f1 = map f . r
f2 = r . map f
-- f1 and f2 are identical
```

Since `r` must work on lists of any type, it can't perform any operations on the
elements; it can only perform operations on the list, e.g.
`reverse`{:.language-haskell}.

Let's quickly check that with a concrete example:

```haskell
r :: [x] -> [x]
r = reverse

f :: String -> Int
f = length

map :: (String -> Int) -> [String] -> [Int]
map = fmap

-- map f . r = r . map f
-- e.g.
xs = ["foo", "bar"]
lhs = map f . tail
rhs = tail . map f

lhs xs == rhs xs
-- True
```

A few examples from the paper:

```haskell
import Data.Char (ord)

fmap ord (reverse ['a', 'b', 'c'])
-- [99,98,97]
reverse (fmap ord ['a', 'b', 'c'])
-- [99,98,97]

fmap succ (tail [1, 2, 3])
-- [3,4]
tail (fmap succ [1, 2, 3])
-- [3,4]
```

It's important to note that if you let `r`{:.language-haskell} be a function
that accepts a list of integers and filters out all odd elements then the
theorem is *not* satisfied:

```haskell
r :: [Int] -> [Int]
r = filter even

fmap succ (r [1, 2, 3])
-- [3]
r (fmap succ [1, 2, 3])
-- [2, 4]
```

However, this is not a counterexample because `r`{:.language-haskell} is too
specific: the theorem only applies to the type `[x] -> [x]`{:.language-haskell}.

> The result that allows theorems to be derived from types will be referred to
> as the parametricity result, because it depends in an essential way on
> parametric polymorphism.

> The key idea is that types may be read as relations.



References:

0. [The Relation with Types (slides)](https://github.com/LambdaCon/2015/blob/master/The%20relation%20with%20types/slides/relation%20with%20types.pdf)
0. [Reasoning about type inhabitants in Haskell](https://gist.github.com/pchiusano/444de1f222f1ceb09596)
0. [Theorems for free!](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CB4QFjAA&url=http%3A%2F%2Fttic.uchicago.edu%2F~dreyer%2Fcourse%2Fpapers%2Fwadler.pdf&ei=mLTOVI3FLtCJNvyHhNgB&usg=AFQjCNHSssGM3vM9RLjlQeG0E29lNJisZQ&sig2=MrmxPit07TEutkLtQP8ydA&bvm=bv.85076809,d.eXY)
0. [A brief bibliography on parametricity](http://wadler.blogspot.it/2015/06/a-brief-bibliography-on-parametricity.html)
