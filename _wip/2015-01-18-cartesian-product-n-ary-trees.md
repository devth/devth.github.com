---
layout: article
title: Cartesian product of n-ary trees
categories: scala
comments: true
image:
  feature: cartesian_1024_256.jpg
  teaser: cartesian_410_228.jpg
  caption: Beartooth Highway. June 2014
---

Given an arbitrarily nested n-ary tree, how can we flatten it via Cartesian
product? Let's jump right in, first by solving it in a highly questionable
type-unsound manner using nested `List[(String, Any)]`s. FP zealots might even
call it immoral!

Since the Scala representation is a little hard to read I'm going to show the
structure first in JSON for reference.


```json
{
  "title": "Functional Programming in Scala",
  "year": 2014,
  "authors": [
    {
      "name": "Paul Chiusano",
      "urls": [
        {"url": "twitter.com/pchiusano"},
        {"url": "pchiusano.github.io"}
      ]
    },
    {
      "name": "Rúnar Bjarnason",
      "urls": [
        {"url": "blog.higher-order.com"},
        {"url": "twitter.com/runarorama"}
      ]
    }
  ],
  "formats": [
    {"formatName": "PDF"},
    {"formatName": "ePub"},
    {"formatName": "Kindle"}
  ]
}
```

In the Scala version we're only using values (no keys). Imagine the key
reference info is elsewhere and we're using simpler value-only structures for
efficiency in a query loop.

```scala
import scala.util.Try

// Hierarchical representation of a single book with a bit of extra
// embellishment for the purpose of demonstrating deeply nested records
val book =
  List(("title" -> "Functional Programming in Scala"),
       ("year" -> 2014),
       ("authors" -> List(
         List(("name" -> "Paul Chiusano"),
              ("urls" -> List(
                ("url" -> "twitter.com/pchiusano"),
                ("url" -> "pchiusano.github.io")))),
         List(("name" -> "Rúnar Bjarnason"),
              ("urls" -> List(
                ("url" -> "blog.higher-order.com"),
                ("url" -> "twitter.com/runarorama")))))),
       ("formats" -> List(("formatName" -> "PDF"),
                          ("formatName" -> "ePub"),
                          ("formatName" -> "Kindle"))))
```

Now that we got that unpleasantness out of the way let's see how it performs.
Benchmarks:

```
```


Let's now look at a much more elegant solution using the mind-bendingly
cool [Shapeless library](https://github.com/milessabin/shapeless) by
[@milessabin](https://twitter.com/milessabin), retaining types at every level.

```
```

Given all the type-level magic, let's see how that translates performance wise.
Benchmarks:

```
```

Of course my grotesque untyped algorithm isn't the only way to shave the yeti,
but its performance vs the Shapeless-powered equivalent is quite convincing that
we should be using types here!
