---
layout: article
title: Beautiful constraint
categories: philosophy
comments: true
excerpt: "The freedom to do anything you wish is rarely a good thing, whether you're an experienced engineer or a 2-year-old child"
image:
  feature: constraint_1200_300.jpg
  teaser: constraint_410_228.jpg
  caption: Oak Alley Plantation, Vacherie, LA. September 2014
---

The freedom to do anything you wish is rarely a good thing, whether you're an
experienced engineer or a 2-year-old child. Our interests and desires are too
diverse and of varying merit to actually produce anything good without
constraint. That's not to say freedom is intrinsically bad: sometimes we are
able to choose the right constraints ourselves; other times they are chosen for
us. Either way, constraint is what guides and sometimes dictates, removes
decisions and forces creative thinking. Constraint is a framework.

These are some constraints I've embraced over my years as a developer.

## Terminal

The terminal is a beautiful constraint. Because of its spartan nature, tools
built in terminal turn out more consistent in behavior and UX, faster, and less
bug-ridden than their graphical counterparts. You know right away when an app
was built on Java/Swing vs native APIs. Look at all differences between UI junk
in Chrome vs Safari on OS X. Then compare that to the simulated UI junk in Flash
or Silverlight. Then look at Eclipse. It's grotesque and highly inconsistent.

Now compare operating systems. The way in which humans interact with a computer
should be more of a UX science and less of an artifact of personalization, and
especially not the whim of corporations whose interests (profit) don't
necessarily align with advancing human-computer interaction.

You may say terminal is rudimentary. Well I agree, but that's part of what makes
it so great. In my early days, it annoyed me greatly when I had to resort to
using the terminal, but I eventually embraced it, moving all my tools and
workflow into it because it's consistent, fast, reliable, and most importantly
*automation is intrinsic*.

## Keyboard

As many have observed, using a mouse is almost always less efficient than using
a keyboard shortcut or command (there are exceptions, but not many apply to the
type of work done by developers). I've increasing eschewed mouse use and it
so happens that Terminal and vim are a very natural way to do so. The reliance
on keyboard shortcuts is a productivity framework.

## $EDITOR

Consistent with my use of terminal and keyboard shortcuts, I use vim, though
Emacs is also an excellent choice for the precise and thoughtful
automation-and-productivity-minded developer. Many of the points regarding
Terminal's simplicity and consistency also apply to these great editors,
originally built by yesterday's technologists and finely tuned over the decades.
`$EDITOR`{:.language-bash} only gets better with age. Vim's minimalism and
simplicity give way to an uncluttered mind, allowing you to focus on the problem
at hand rather than the tools—a framework for clear thinking.

<img src="/images/vim_emacs.png" alt="Vim and Emacs vs Atom" />


## Functional Programming

In a similar way that tools constrain us, the functional style of programming
introduces tremendous restrictions on how we write programs. FP is a huge topic
that I won't even attempt to cover here, but we can briefly take a look at some
of its tenets and the benefits that come with.

### Composition

One way to constrain your code is to implement functions purely in terms of core
or library functions, as in this JavaScript example.

```javascript
var mergeValues = _.compose(_.partialRight(_.reduce, _.merge), _.values)
mergeValues({a: {foo: 1, bar: 2}, b: {qux: 3}})
//=> {foo: 1, bar: 2, qux: 3}
```

I'm specifically avoiding the use of `function` in the definition of
`mergeValues`. That is a strong indicator to the reader that *nothing* fancy is
going on here. It's simply a composition of library functions (lodash.js) which
prevents me from writing any custom code. Instead, I'm forced to think of
existing functions, and compose them in a way that produces intended behavior.

Composition is the foundation of reusability — a worthy pursuit.

### Immutability

The benefits of immutability are quite well-known by now, even among OO
programmers. It prevents all sorts of nonsense like defensive copying and
combinatorial explosion of possible states and aligns well with reality and
intuition. After all, a value is a value. When you say `2 + 3`{:.language-scala}
you are not mutating `2`{:.language-scala} into `5`{:.language-scala}; you're
producing a brand new, immutable, unchangeable value, `5`{:.language-scala}. Why
should `2 :: List(3)`{:.language-scala} behave any differently?

Immutability enforces a restriction on how we write algorithms. No longer can we
loop over some collection and store computed results on another collection via
mutation. Instead we look to elegant functional constructs like `map` and
`fold`.

See these excellent presentations by Rich Hickey if you'd like to go deeper on
the benefits of immutability. It's a wonderful constraint.

- [The Value of Values](http://www.infoq.com/presentations/Value-Values)
- [Are We There Yet?](http://www.infoq.com/presentations/Are-We-There-Yet-Rich-Hickey)


### Referential transparency

Referential transparency (RT) is an attribute of pure functions, which do not
perform side effects (e.g. IO, mutation), mutate their arguments, nor depend on
outside variables. RT says you can substitute the value for any expression.

```scala
val double = {x: Int => x * 2}
double(4)
// => 8
// double(4) simply represents the expression 4 * 2 so let's substitute it:
4 * 2
// => 8
// 4 * 2 represents 8 so let's substitute it:
8
//=> 8
```

It's obvious how the substitution works in this context. Now imagine the use of
`double`{:.language-scala} in a slightly wider context:

```scala
double(50) / double(5)
//=> 100 / 10
//=> 10
```

Running this code in your head is very easy because you can safely substitute
`100`{:.language-scala} for `double(50)`{:.language-scala} and
`10`{:.language-scala} for `double(5)`{:.language-scala}. This concept of
substitution scales up as your context gets wider and wider, as it naturally
does in a program of any meaningful size. This is called equational reasoning.
RT gives you the ability to reason locally in a small context instead of having
to worry about who is mutating what or where unpredictable values are coming
from. It also makes functions more testable, composable, and parallelizable.


## Constraint as freedom

Being intentional about which constraints you adopt and embrace is freeing. No
longer are you lost at sea, being tossed about. Your constraints are fixed, and
you have the freedom to become highly skilled at working with and around them,
using them as a rudder to guide you during the creative problem-solving process.

Maker, choose your constraints wisely.

## Further exploration

- [https://www.youtube.com/watch?v=GqmsQeSzMdw](https://www.youtube.com/watch?v=GqmsQeSzMdw):
   an excellent talk by the always-brilliant [Rúnar Bjarnason](http://blog.higher-order.com/),
   co-author of [Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala)
