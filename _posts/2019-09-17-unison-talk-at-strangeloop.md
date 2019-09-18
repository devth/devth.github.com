---
layout: article
title: |
  Notes from the Strangeloop talk 'Unison: a new distributed programming language'
categories: [plt, unison, talks, notes]
comments: true
excerpt: |
  Notes on the Strangeloop talk 'Unison: a new distributed programming language'
  by Paul Chiusano. 🤯 Warning: mind expanding future tech 🤯
image:
  feature: unison_feature.png
  teaser: unison_teaser.png

---

These are my notes from the mind-expanding talk ["Unison: a new distributed
programming language" by Paul
Chiusano](https://www.youtube.com/watch?v=gCWtkvDQ2ZI&feature=youtu.be).

> Unison is an open source functional programming language with special support
> for building distributed, elastic systems. It began as an experiment: rethink
> all aspects of the programming experience, including the core language,
> runtime, tooling, as well as code versioning and publishing, and then do
> whatever is necessary to eliminate needless complexity and make building
> software once again delightful, or at the very least, reasonable.
>
> We're used to thinking of a program as a thing that describes what a single OS
> process will do, and then using a separate layer of technologies outside of
> our programming languages to "configure" many separate programs into a single
> distributed, elastic "system". This gets complicated. The core language of
> Unison starts with the premise that no matter how many nodes a computation
> occupies, it should be expressible via a single program, not many separate
> programs. Unison programs can describe their own deployment, elastically scale
> and orchestrate themselves, and deploy themselves in parallel onto any number
> of nodes for execution.
>
> This talk introduces the Unison language and its tooling and shows what it can
> be like to program systems of any size with this model of computing.
>
> Paul Chiusano<br />
> Unison Computing<br />
> @pchiusano
>
> Paul Chiusano started the research that led to the Unison language and is a
> cofounder of Unison Computing, a public benefit corp. He has over a decade of
> experience with purely functional programming in Haskell and Scala and
> coauthored the book Functional Programming in Scala. He lives and works in
> Somerville, MA.

Unison is language inspired by Haskell, Erlang and Frank, designed in a way that
lends itself to some amazing characteristics. The issues that Unison addresses
cause tons of headache and lost hours in other languages and build systems.

Unison's goal is to drastically improve the developer experience around writing,
testing and deploying code "by rethinking anything and everything about
programming".

The language is based on a core technical principle: **content-addressed code**.
Definitions are identified by the hash of their content. Names are simply
metadata for the purpose of human consumption but have no affect on code that
depends on that function since they simply reference it by hash.

So what happens when you embrace this principle? You gain a ton of benefits,
including:

- **Function renames never break the rest of the code**, including downstream
  dependencies because all code is content-addressable via the hash of its
  definition. Names are just metadata attached to the hash.
- **No builds**. Once someone adds a function to a codebase, its AST and
  metadata (like name) is built and stored in the code base, so when you
  checkout a code base everything is already built.
- **Cached test results**. Test results can be cached because unit tests are
  pure (aka referentially transparent) and only run again if a function being
  called by the test changes.
- **Codebase is append only**. Definitions are hashed, so if a definition
  changes a new hash is created and appended to the codebase. The cache never
  needs to be invalidated.
- **Code is indexed in interesting ways**, such as by types like `Nat -> [a] ->
  [a]` which yields `List.drop` and `List.take`.
- **Solves the diamond dependency problem** (e.g. `D` depends on `B` and `C`,
  but `B` and `C` both depend on `A` - which version of `A` does `D` get?). Libs
  in traditional langs operate on shared/global namespaces but since Unison
  relies on hashes instead of names there are no conflicts. Both "versions" of A
  can co-exist.
- **Typed durable storage**. Typically when we persist data we serialize
  it into some other form like bytes, JSON, or SQL then write a ton of code to
  transfer in and out of that format and validate it back into some type, e.g.
  `Employee`. When you stop referring to things by name, multiple "versions" of
  a type can exist, and you can simply persist and un-persist structures.
- 🤯 **Programs that deploy themselves** + describe whole elastic distributed
  systems 🤯
  - Because code is content-addressable it's easy to distribute definitions
    across a cluster via nodes asking other nodes for the definition on demand
    when that node is asked to evaluate a function
  - Nodes require no setup to run your code other than provisioning Unison
  - Code can provision new nodes to run code at runtime
  - Think of it as "distributed programming as a library"
  - Abilities like `Remote` (which allows code to run on a remote machine) can
    be defined in the type system (this looks like a Type Class).
  - Can define custom ability types and a corresponding interpreter that encodes
    the meaning of that type i.e. what it does at runtime
    - e.g. `Cloud.usEast.run '(dsort hugeDataset)`
  - What about stateful elastic services?
    - Unison is researching this currently. For example: a system with
      high-concurrency that still involves some sort of consensus, like a KV
      store with updates happening concurrently on many nodes

Highly recommend watching the talk.

Learn more at:

- [unisonweb.org](https://unisonweb.org)
- [Paul's Unison blog posts](https://pchiusano.github.io/unison/)
