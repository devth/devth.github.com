---
layout: article
title: Working with Scalaz Trees
categories: scala
comments: true
image:
  feature: cartesian_1024_256.jpg
  teaser: cartesian_410_228.jpg
  caption: Beartooth Highway. June 2014
---

Scalaz folks will just tell you to follow the types, which is some special kind
of troll when you're just getting started. Since examples of working with
`Tree`s are so lacking, here are some common usages.

Map over every node in a tree

```scala
    loc.cojoin.toTree.flatten.filter(_.isLeaf).map { leaf =>
      println(leaf.path.force)
    }.force
```


TreeLoc

Constructing trees

Updating nodes in a tree


Flesh out examples from:
http://docs.typelevel.org/api/scalaz/stable/6.0/doc.sxr/scalaz/example/ExampleTree.scala.html

