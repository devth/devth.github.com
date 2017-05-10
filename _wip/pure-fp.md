A few words on what it means to program in a *pure, functional* style.

When I first encountered the term, I (and probably most people) immediately
though, "yeah that's nice, but no side effects? <abbr>GLHF</abbr>." Then I
continued trying to write pure functions as much as possible but not giving it a
second thought when I had to resort to side-effects. It wasn't until much later
that I understood what they meant. I wish they had just told me up front instead
of making it sound like some kind of mystical magic of FP wizards.

So, this is me telling you, up front, that you can have your cake and eat it
too: pure functional programs that—when executed—may perform side effects. This
may sound like a paradox, and there is some amount of controversy around it in
the community, but the distinction lies in modeling side-effects vs. performing
side effects. Another way this is sometimes presented is as a functionally pure
core with a thin impure outer layer. Observe:

```scala
def randInt: Int = scala.util.Random.nextInt
```

`randInt` is impure because `nextInt` is impure. It pseudorandomly generates an
`Int`{:.language-scala} from a *seed*. In Scala/Java, this seed is obtained
impurely in that it depends on the global state of `System.nanoTime`.

Let's look at a pure equivalent:

```scala
def randInt = Rnd[Int] = Rng.int
```

`Rnd[Int]` is a data structure that represents an operation to get a random
integer. Instead of actually fetching the integer (and relying on
`System.nanoTime`), it's simply constructing an instruction.

When modeling side effects instead of performing them, composition becomes very
important. You don't have to think about it much when you program imperatively
because you get to deal directly with more primitive values. When you want a
random `Int`{:.language-scala} you get an `Int`{:.language-scala}. In our pure
example, you get a `Rng[Int]`. How do we compose this with other functions that
are only wanting a regular `Int`?
