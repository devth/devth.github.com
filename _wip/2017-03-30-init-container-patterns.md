# Init Container Patterns

Now that Kubernetes 1.6 is out, init containers are first class and no longer
need to exist as nasty JSON blobs in an annotation. I started using them when
they were alpha because it's such a useful concept, but now I intend to really
lean hard on the pattern.

Let's explore some ideas on how to use them.

The general pattern looks like:

1. Compute something
1. Put it in an emptyDir volume to share with the app
1. Consume that volume from the app

## Render configuration templates

ConfigMap + Secret = config
Store it in an emptyDir Volume to share it with the app.

TODO: make a simple open source thingy to do this, or use existing stuff.

## Fetch SSL certificates

Pods have dynamic IP addresses and no stable DNS (unless they belong to a
StatefulSet). This means we need to fetch a cert for a pod when it starts up,
using the IP address it happened to be assigned.
