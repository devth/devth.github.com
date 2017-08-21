# Working with Datomic

Datomic is a mind-bendingly cool piece of technology, especially if you're
coming from other populate data stores. But it can be hard to ramp up on because
Datomic-related blogs are sparse, it's not open source, and docs are on the
light side (but getting better!).

This post is an attempt to augment the docs with use cases and code samples that
will hopefully make it quicker to gain understanding through practice.

## Make it easy to work with your data

Datomic's query capabilities are very powerful and expressive, but a little
verbose and hard to type at the REPL. If you always reach for `d/q` to peak at
your data, you're going to get mentally exhausted. Write functions specific to
your domain that help you more easily query and transact changes to data from
your REPL.

I keep a `project.db.scratch` namespace for this purpose. It ships to my
environments, and is easily accessible via the REPL.

## Translating between dbs, txs, and ts

If you have a `db`, a `tx-id` or a `t`, you can get any of the others via the
Datomic API.

```clojure
;; assume you just performed a transaction
(def tx-result (datomic/transact conn [...]))

;; db to t
(def t (datomic/basis-t (:db-after tx-result)))

```

## Excision

You can completely erase the existence of an entity from history, but note that
this process happens asynchronously while Datomic rebuilds its index after a
transaction containing excision.

You can ask for Datomic to tell you when Excision is complete though:

```
```

## Look at the audit trail for an entity

### Look at values only modified by transactions with certain metadata

## Query examples

## Pull examples

## Other reosurces

There are a few very knowledgeable users who hang out in the #datomic Slack
channel on Clojurians Slack. It's a great place to drop in and ask questions.

