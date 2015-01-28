---
layout: article
title: Beautiful constraints
categories: philosophy
comments: true
excerpt: "Being able to do *anything* is rarely a good thing, whether you're an experienced developer or a 2-year-old child"
image:
  feature: on_learning_1024_256.jpg
  teaser: on_learning_410_228.jpg
  caption: Highline Singletrack, Rimrocks, Billings, MT. October 2014
---

Being able to do *anything* is rarely a good thing, whether you're an
experienced developer or a 2-year-old child. Our interests and desires are too
diverse and of varying merit to actually produce anything good without
constraint. That's not to say freedom is intrinsically bad: sometimes we are
able to choose the right constraints ourselves; other times they are chosen for
us. Either way, constraint is what guides and sometimes dictates, removes
decisions and forces creative thinking. Constraint is a framework.

Let's look at some constraints I've embraced over my years as a developer.

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
turns out Terminal and vim are a very natural way to do so.

## $EDITOR

Consistent with my use of terminal and keyboard shortcuts, I use vim, though
Emacs is also an excellent choice for the precise and thoughtful
automation-and-productivity-minded developer. Many of the points regarding
Terminal's simplicity and consistency also apply to these great editors,
originally built by yesterday's technologists and finely tuned over the decades.
$EDITOR only gets better with age.

<img src="/images/vim_emacs.png" alt="Vim and Emacs vs Atom" />


## Functional Programming

FP is a huge topic that I won't even attempt to cover here, but we can briefly
take a look at some of its tenants and the benefits that come with.

### Composition

Implementing a custom function purely in terms of core or library functions as a
constraint:

```javascript
var mergeValues = _.compose(_.partialRight(_.reduce, _.merge), _.values)
```

In this JavaScript example I'm specifically avoiding the use of `function` in
the definition of `mergeValues`. That is a strong indicator to the reader that
*nothing* fancy is going on here. It's simply a composition of library functions
(lodash.js). It also means we don't have to write any custom code!

Composition is the core of reusability.

### Immutability

The benefits of immutability are quite well-known by now, even among OO
programmers. It prevents all sorts of nonsense like defensive copying and
combinatorial explosion of possible states and aligns well with reality and
intuition. After all, a value is a value. When you say `2 + 3`{:.language-scala}
you are not mutating `2`{:.language-scala} into `5`{:.language-scala}; you're
producing a brand new, immutable, unchangeable value, `5`{:.language-scala}. Why
should `2 :: List(3)`{:.language-scala} behave any differently?

See these excellent presentations by Rich Hickey if you'd like to go deeper on
the benefits of immutability. It's a wonderful constraint.

- [The Value of Values](http://www.infoq.com/presentations/Value-Values)
- [Are We There Yet?](http://www.infoq.com/presentations/Are-We-There-Yet-Rich-Hickey)


### Referential transparency

Referential transparency (RT) is an attribute of pure functions, which do not
perform side effects (e.g. IO, mutation) and whose arguments are also immutable.
RT says you can substitute the value for any expression.

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
//=> 100
```

Running this code in your head is very easy because you can safely substitute
`double(50)`{:.language-scala} for 100 and `double(5)`{:.language-scala} for 10.
This concept of substitution scales up as your context gets wider and wider, as
it naturally does in a program of any meaningful size. This is called local
reasoning. RT gives you the ability to reason locally in a small context
instead of having to worry about who is mutating what or where unpredictable
values are coming from. It also makes functions more testable, composable, and
parallelizable.


## Constraint as freedom

Being intentional about which constraints you adopt and embracing them is
freeing. No longer are you lost at sea, being tossed about. Your constraints are
fixed, and you have the freedom to become highly skilled at working with or
around them.

Maker, choose your constraints wisely.

