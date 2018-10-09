# A case against mocking

Mocking adds incidental complexity to tests and makes them fragile. It
always ultimately points to unwanted side effects.

Impure code is hard to test because side effects take a dependency on something
in the real world, and the real world is not reproducible. In test land we want
everything to be logical and deterministic, affecting nothing.

We can learn something about this from pure FP tech such as Haskell or
[Scala ZIO](https://github.com/scalaz/scalaz-zio): side effects should be
represented as pure data.

## What are you even talking about

This means instead of:

```javascript
const getUsers = (count) => fetch(`/users?count=${count}`);
```

We write something more like:

```javascript
const getUsers = (count) => ({
  op: 'fetch',
  path: '/users',
  queryString: {count}
});
```

Separate the intent from the effect. This is basically how Haskell works by
default. Your code is pure and effects are only performed outside of your code
by the Haskell runtime. (With Scala this practice needs to be more intentional
since Scala is not a pure FP language).

Now we can test `getUsers` without having to mock out `fetch`.

```javascript
const operation = getUsers(5);

expect(operation).to.deep.equal({
  op: 'fetch',
  path: '/users',
  queryString: {count: 5}
});
```

## But we need side effects

As for actually performing the side effect, we build up a separate interpreter
that takes data structures and performs corresponding effects on our behalf.
This allows us to completely isolate the impure part of our code base from the
pure as well as make the impure surface area much smaller. The impure part could
even be extracted into a separate runtime executor library, similar to how ZIO
works. Then you get the benefit of many potential consumers of the runtime, all
functionally pure, with all the benefits of pure code and equational reasoning.
