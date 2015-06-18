I'm writing this to serve as a reference for my future self as I don't use
Apache Curator often enough and end up forgetting the subtleties.

We'll look at:

- Leader election
- Sharing a value between nodes
- Using Curator's Shared Reentrant Read Write Lock to synchronize reads and
  writes of the a shared value


doing leader election in
ZooKeeper. I ocassionally need to do it, then months go by and I forget most of
the ZK subtleties until the next time I need to use it and am forced to re-learn
everything nearly from scratch. Here are the primary tasks involved in using ZK
to do leader election:

0. Start a ZK client
  - Listen to ZK connection events
  - Listen for unhandled errors from ZK
0. Start a LeaderLatch
  - Listen for events on the LeaderLatch to detect changes in leadership
0. Profit

Scroll to the end for the full code listing.


## References

- [ZooKeeper: ](http://shop.oreilly.com/product/0636920028901.do) — chapter 8
  covers Curator usage
