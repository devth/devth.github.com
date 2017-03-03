# Calcite: add SQL to anything

Calcite takes a SQL query, parses it and produces a relational algebra.

The most naïve use of Calcite is simply feeding it an
`Enumerator[AnyRef]` and letting Calcite perform any necessary projection,
aggregation and sorting in-memory.

However, Calcite provides hooks for you to transform nodes of the relational
algebra into your own implementations, extract data from them and push it down
to the guts of your query engine or interface.

A node in the relational algebra tree is represented as a [RelNode](todo..).

Calcite also performs query planning. There is often no single way to transform
a SQL query into a relational tree. Calcite gives you cost methods to override
to inform its planner.

## Built in `_MAP` support

## Scala interop considerations



## Presentation

0. Intro [slide: Apache Calcite & Portico]

I'm going to talk about Apache Calcite and how we're using it to support
SQL-like queries in Portico.

0.  Rational

[slide: code vs sql]


1. Apache Calcite Overview [slide]

Calcite is a dynamic data management framework. It's used by other open source
projects like:

[slide: logo cloud] Apache Hive, Apache Drill, Apache Phoenix, Apache Kylin

A few features Calcite provides:

- query planning/rewriting with cost-based query optimization [diagram: query planning]
- join disparate data sources [diagram: different data sources]
- jdbc server implementation [diagram: sql query?]

2. Relational Algebra [slide]

- Relational Algebra is the common language underneath SQL. Calcite provides
  many of the standard Relational Algebra operaters, including projection,
  aggregation and restriction.

- [slide: select listingId from listings where sellerId = 42]
  Let's take a look at a simple query to see how it gets translated into an
  expression tree of nodes representing Relational Algebra operations.

  - [slide: sql query with corresponding nodes
    table: listings
    project: listingId
    filter: sellerId=42]

    Calcite takes a SQL statement and builds the expression tree from it. Each
    operation in the relational algebra is represented as a node in an
    expression tree. These nodes can be re-ordered for efficiency.

- Calcite provides a default implementation of each expression, but also
  provides hooks to augment or replace that implementation

- Rules allow you to specify the cost of an expression, which lets Calcite
  determine whether to run the rule or not using Cost Based Optimization

3. Use of Calcite in Portico

- Pushing down projection into Portico

  - [slide screenshot of ListingsSchema] Portico supports schema with
    potentially very deep hierarchies and many fields.

  - [diagram of initial expression tree]
    Simply collecting all the data in a Map-like structure and relying on
    Calcite to pair it down to requested fields is unrealistic, so the first
    step in building our Calcite adapter was to pushdown projection.
    [replacing Calcite's default projection with Portico-specific projection node]

  - [slide with sql statement demonstrating nested query:
      select
        listingId,
        listingTitleImage.title,
        aspects.aspectName,
        aspects.aspectValues.aspectValue
      from listings
      where siteId = 1]
    Every sub-schema in Portico has its own cardinality N, which may be of 0 or
    more. At each query level we must advance column iterators N times,
    collecting the results along the way. We end up with a nested Map-like data
    structure, which must be recursively flattened via Cartesian Product, just
    like a normal join.

  - [slide: projection] this significantly reduces the amount of data Portico
    needs to retrieve, hold in memory, and collect from distributed nodes

- [slide: jdbc server/client] Calcite provides a jdbc server for free


4. Future work

- Push down more operators

  - [diagram: filtering] filtering massively reduces the amount of data Portico
    retrieves

  - [diagram: aggregation] pushing down aggregation is important because of
    Portico's distributed nature. Even though we still have to scan the same
    number of records, performing aggregation at each node reduces the amount
    of data on the wire during collection

  - [diagram: UDF - scala screenshot?] user defined functions let us encapsulate
    complex business logic and apply them in queries. Calcite has built-in
    support for UDFs.

- Cross-schema joins

- Contribute to Calcite

  Calcite is a relatively young project that was only recently accepted into the
  Apache incubation process.

  - features
  - bugfixes
  - documentation
  - blog posts

5. Outro

Apache Calcite is a powerful and flexible tool for making any data system feel
like a traditional database.
