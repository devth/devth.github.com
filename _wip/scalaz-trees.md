---
layout: article
title: Working with Scalaz Trees
categories: scala
comments: true
toc: true
image:
  feature: cartesian_1024_256.jpg
  teaser: cartesian_410_228.jpg
  caption: Beartooth Highway. June 2014
---

Scalaz folks will just tell you to follow the types, which is some special kind
of troll when you're just getting started. Since examples of working Scalaz
`Tree` are so lacking, let's look at some various usages.

All examples will be over this tree unless otherwise noted:

Our tree example comes from a small section of tpolecat's Scalaz class hierarchy
diagram:
http://tpolecat.github.io/assets/scalaz.svg

```scala

val tree =
  "Monad".node(
    "MonadPlus".leaf,
    "MonadReader".leaf,
    "MonadTell".node(
      "MonadListen".leaf),
    "MonadState".leaf,
    "Nondeterminism".leaf)

```

## Print a Tree

```scala
drawTree
res1: String =
""Monad"
|
+- "MonadPlus"
|
+- "MonadReader"
|
+- "MonadTell"
|  |
|  `- "MonadListen"
|
+- "MonadState"
|
`- "Nondeterminism"
"
```

N.B. This works because we're using a `Tree[String]`{:.language-scala} and
Scalaz includes a instance of `Show`{:.language-scala} for
`String`{:.language-scala}, along with many other types in the standard Scala
library.

## Print a Tree of a custom type

```scala
case class Num(i: Int)
val treeNum =
  Num(0).node(
    Num(1).node(
      Num(2).leaf))
```

If we try to `drawTree` on a `Tree[Num]` we'll get an error:

```scala
treeNum.drawTree
<console>:17: error: could not find implicit value for parameter sh: scalaz.Show[Num]
              treeNum.drawTree
```

One of the easiest ways of providing an implicit `Show` for a custom type is to
rely on the type's built-in `toString` like so:

```scala
implicit val showNum: Show[Num] = Show.showFromToString[Num]

treeNum.drawTree
"Num(0)
|
`- Num(1)
   |
   `- Num(2)
"
```


## Map over every node in a tree

With the full path back to the root at each node.

```scala
tree.loc.cojoin.toTree.flatten.filter(_.isLeaf).map { leaf =>
  println(leaf.path.force)
}.force
Stream(MonadPlus, Monad)
Stream(MonadReader, Monad)
Stream(MonadListen, MonadTell, Monad)
Stream(MonadState, Monad)
Stream(Nondeterminism, Monad)
```

## Collect immediate children of a tree

```scala
tree.subForest.map(_.rootLabel).force
```

TreeLoc

Constructing trees

Updating nodes in a tree


Flesh out examples from:
http://docs.typelevel.org/api/scalaz/stable/6.0/doc.sxr/scalaz/example/ExampleTree.scala.html

