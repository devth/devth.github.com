---
layout: article
title: "dec: Deep Environmental Config"
categories: config
comments: true
published: true
toc: true
image:
  feature: compiled-queries-feature.jpg
  teaser: compiled-queries-teaser.jpg
  caption: Northwest San Francisco and the Golden Gate strait from the Hamon Observation Tower at de Young Museum. February 2017
---

There are many too-strong opinions (TODO link to recent reddit posts about how
to config) on the "right" way to do configuration but they primarily come down
to three options:

1. flat files in some known format like JSON, YAML, properties files, EDN, etc.
1. environment variables
1. config from a data store (which ironically requires separate initial config
   in one of the above two methods in order to access the data store)

While #3 is an interesting option in light of modern cluster-based infrastructure
and tools like `consul` and `etcd`, we're going to focus on the first two
methods for this post: flat files and env vars.

**Systems should not require configuration via only one of the two above
mechanisms, but instead allow either or both.** This affords the operator of the
system to configure it in the way that's most suitable according to their
infrastructure, preferences, policies, and politics.

When using both mechanisms there needs to be a very clear priority in which one
mechanism overrides the other. Typically env vars override flat file config, as
they are more ephemeral and easy to set.

A few modern examples of systems that work this way today include:

- [ElasticSearch](todo link to config docs)
- HashiCorp tools?
- TICK stack?
- K8S stuff or Docker?

## Symmetry

A problem that immediately comes to light once we say that any individual value
can be configured by either or both of two mechanisms is the difference between
flat files and env vars:

- Env vars are stringly typed key value pairs
- Config files support arbitrarily-deep tree structures with varying support for
  primitive and collection types like strings, numbers, booleans, arrays and
  maps

Because of these intrinsic significant structural differences we need a
translation layer between KV pair and tree. If we adopt a single constraint the
translation layer becomes simpler to express: **All leafs in the tree are
strings.**

The implication is that our system which consumes the config must parse the
string to obtain its expected format.

## dec: Deep Environmental Config

[dec](https://github.com/devth/dec) is a tiny library that embraces this
constraint and provides an `explode` function to transform env var KV pairs into
an expected shape equivalent to a potentially deep EDN structure.

The following configurations are equivalent from the perspective of [new lib
that consumes dec]:

```bash
MY_DB_PORT=4567
MY_DB_HOST=database
MY_URL=https://my-system/
```

```clojure
{:my
 {:db {:port "4567" :host "database}
  :url "https://my-system"}}
```

Notice how trees are serialized into KV pairs with a simple `_` delimiter. (The
delimiter is customizable in `dec` but `_` is the default.)

Similarly, arrays can be represented:

```bash
MY_SERVER_0_HOST=serverA
MY_SERVER_1_HOST=serverB
```

```clojure
{:my
 {:server [{:host "serverA"} {:host "serverB}]}}
```

TODO: should a library actually slurp the config and munge the two mechanisms?
Maybe it also bakes in environ?
Prefix handling is slightly tricky.
Bake in clojure.spec validation logic and helpers

Currently part of yetibot.core
[here](https://github.com/yetibot/yetibot.core/blob/4b607726bae926de31a48bb8a05e7345a8668484/src/yetibot/core/config.clj#L19-L47):

## Schema

Due to our constraint that all values be strings, it becomes important that
the system consuming the config strings have a valid way to parse and validate
the strings into actual expected type.

Building a configuration schema into config consumers has some other nice
properties, particularly when using clojure.spec:

- Validate expected shape of config at runtime with precise error messaging
  describing the exact location of invalid config and expected values
- Generate config example structures from schema in any format (e.g. the full
  list of supported env vars or EDN tree)

## In the wild

To demonstrate we can look at [Yetibot](https://yetibot.com), an open source
chat bot written in Clojure that embraces the above concepts.

Yetibot can be configured via env, edn, or a combination of both. For example, a
minimal Yetibot config might be:

```bash
YB_ADAPTERS_MYSLACK_TYPE=Slack
YB_ADAPTERS_MYSLACK_TOKEN=xoxb-my-token
YB_DB_URL=postgresql://yetibot:yetibot@postgres:5432/yetibot
```

Yetibot expects all env vars to be either prefixed by `YB` or `YETIBOT`, thus
the following is equivalent:

```bash
YETIBOT_ADAPTERS_MYSLACK_TYPE=Slack
YETIBOT_ADAPTERS_MYSLACK_TOKEN=xoxb-my-token
YETIBOT_DB_URL=postgresql://yetibot:yetibot@postgres:5432/yetibot
```

And since it uses `dec`, an edn file is also equivalent:

```clojure
{:yetibot
 {:adapters {:myslack {:type "Slack" :token "xoxb-my-token"}}
  :db {:url "postgresql://yetibot:yetibot@postgres:5432/yetibot"}}}
```

Note that `edn` isn't a requirement; anything that can be parsed into Clojure
collections would be equivalent.

## Conclusion

There are known pros and cons to both approaches. A system should not perscribe
which sets of constraints to adopt, but instead allow consumers to weigh their
own tradeoffs and allow them to run things however they want.

By adopting a simple constraint and providing a very small library we can
support this.