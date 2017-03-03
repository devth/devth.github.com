
If the first post didn't convince you that type classes are useful in the real
world, maybe this post will. We'll look at some code seen in the wild, examine
its faults, then fix it up with a type class.

The code addresses the problem of deserializing some type-lossy configuration
format (e.g. JSON, properties files).

```scala
val config = 


getString(key: String): String

getBoolean(key: String): Boolean

getBooleanMap(key: String): Map[String, Boolean]

getInteger(key: String): Int

getLong(key: String): Long

getDouble(key: String): Double

getFloat(key: String): Float
```

First, can we agree that this is just plain disgusting? Ok, good. The type is
included in the name of the method in addition to the signature, and 


