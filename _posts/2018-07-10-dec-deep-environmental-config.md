---
layout: article
title: "dec: Deep Environmental Config"
categories: config
comments: true
published: true
toc: true
image:
  feature: dec_wide.png
  teaser: dec_410_228.png
---

There are many [too-strong
opinions](https://hn.algolia.com/?query=environment%20variables&sort=byDate&prefix&page=0&dateRange=all&type=story)
on the "right" way to do configuration but they primarily come down to these
options:

1. flat files in some known format like JSON, YAML, properties files, EDN, etc.
1. environment variables
1. CLI arguments at start up
1. config from a data store (which ironically requires separate initial config
   in one of the above methods in order to access the data store)

While #4 is an interesting option in light of modern cluster-based infrastructure
and tools like `consul` and `etcd`, we're going to focus on the first three
mechanisms for this post: flat files,  env vars and CLI arguments.

**Systems should not require configuration via only one mechanism, but instead
allow any or all available mechanisms in combination.** This affords the
operator of the system to configure it in the way that's most suitable according
to their infrastructure, preferences, policies, and politics.

When using multiple mechanisms there needs to be a very clear priority in which one
mechanism overrides the other. Typically env vars override flat file config, as
they are more ephemeral and easy to set.

A few modern examples of systems that work this way today include:

- [ElasticSearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/settings.html)
- HashiCorp tools like [Consul](https://www.consul.io/docs/agent/options.html)
- TICK stack e.g. [Chronograf](https://docs.influxdata.com/chronograf/v1.6/administration/config-options#chronograf-service-options)

## Symmetry

A problem that immediately comes to light once we say that any individual value
can be configured by any available mechanism is the representational difference
between mechanisms:

- Env vars and CLI args are stringly typed key value pairs
- Config files support arbitrarily-deep tree structures with varying support for
  primitive and collection types like strings, numbers, booleans, arrays and
  maps

Because of these intrinsic significant structural differences we need a
translation layer between KV pair and tree. If we adopt a single constraint the
translation layer becomes simpler to express: **All leafs in the tree are
strings.**

The implication is that our system which consumes the config must parse the
string to obtain its expected format.

## Introducing dec: Deep Environmental Config

[dec](https://github.com/devth/dec) is a tiny library that embraces this
constraint and provides an `explode` function to transform KV pairs into an
expected shape equivalent to a potentially deep EDN structure.

The following configurations are equivalent from the perspective of dec:

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

## Future

It might be useful to create a library that actually slurps the config and munge
multiple mechanisms, using `dec`. Prefix handling and merging is slightly
tricky. This is all currently part of
[yetibot.core](https://github.com/yetibot/yetibot.core/blob/4b607726bae926de31a48bb8a05e7345a8668484/src/yetibot/core/config.clj#L19-L47).
This library should also utilize `clojure.spec` to validate the expected shape
of a config, provide validation, friendly error messages, and config generation.

## Conclusion

There are known pros and cons to both approaches. A system should not perscribe
which sets of constraints to adopt, but instead allow consumers to weigh their
own tradeoffs and allow them to run things however they want.

By adopting a simple constraint and providing a very small library we can
support this.
