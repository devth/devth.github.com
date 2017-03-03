Tree walking is such a common thing to do in software development. Aside from
the temporary effects of cramming for technical interviews, are you comfortable
writing code that walks trees, transforms them, or interprets them? You should
be, because it's both fun and highly useful in many common tasks.

Let's get the boring stuff out of the way. There are 3 ways to traverse a tree:

## Pre order

## Post order

## In order



## Language level tools

Now to explore what Scala, Clojure, and Haskell have to offer us for tree walker
tools.

### Scala

Scala doesn't give us anything itself, so we'll use the awesome scalaz library.

#### Add a node to a tree in a Scalaz tree:

```scala
val t = "foo".node("bar".leaf, "baz".node("qux".leaf))
val loc = t.loc
val addNode = loc.modifyTree { t => (t.rootLabel.node(t.subForest :+ "NEW NODE".leaf : _*)) }
```

loc.insertRight

loc.insertDownFirst


### Clojure

Clojure has amazingly-neat `clojure.walk` namespace containing the usual suspects.

### Haskell

In Haskell, all trees are Monads (NB: read this like a "In Soviet Russia..."
joke). Which is awesome. Because all we have to do is `>>=`{:.language-haskell}
(aka "bind") our way through a tree, letting us work with it at a highly generic
fashion.

![/images/haskell.jpg](This is Haskell)

That's what it feels like to work in Haskell.




