---
layout: article
title: Write you a Functor
categories: scala
comments: true
image:
  feature: cartesian_1024_256.jpg
  teaser: cartesian_410_228.jpg
  caption: Beartooth Highway. June 2014
---

Implementing a Scalaz Functor is pretty straightforward in itself, but due to
how awkward/tricky type classes are in Scala, it's a bit hard to follow,
especially if you're coming from Scala's OO-style implementation of Functors.

The Functor we're going to create allows chaining many function calls together
and skipping a function if its result is null. This could come in useful while
mapping over functions you don't own, such as in a Java library that uses null.

Example:

```java
public class JavaLikesToNPE {

  public static Integer intFromString(String s) {
    try { return new Integer(text); }
    catch (NumberFormatException e) { return null; }
  }

  public static Integer double(Integer i) { return i * 2; }

}
```


