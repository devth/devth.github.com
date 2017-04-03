# Spec coupling

How do you create a standard set of documents and practices that go with every
project, but also enforce it?

It's not very useful to say "Every project should have a README, tests, CI, pass
lint, adopt X naming conventions, etc" if you can't automatically verify whether
a project actually does that.

We want a tight coupling between practices and artifacts.

As an example, given an arbitrary set of requirements like:

> We want every project repository to include a README that documents:
>
> How to run in development
> How to run in production
> Sample configuration
> FAQ that exposes current thinking and why certain design choices were made

Could you "lint" this and determine whether it met your specs?

What if the FAQ was out of date with the implementation?

## Spec

We need a more strict and explicit spec. The spec must reside apart from the
implementation. In this case, the spec is our requirements list, and e.g. the
"FAQ" is an implementation of one aspect of that spec.

In code we have tools like type checkers, unit tests, and schemas that help to
tightly couple, but at a higher project/team/organization-wide level, it's much
harder to express that coupling in an automatic way.

If you don't have this, it's up to the human to continually improve a repo over
time. This is fine, but it means only mature repos that have been sufficiently
thrashed upon reach this state, and only if the contributors and users think
about these sorts of things.

## Tools and problems

- Cucumber? This has English specs.
- When thinking changes how do you know the FAQ is out-of-date?

Maybe CI exposes a list of assertions that the repo is making.

Using Kubernetes infra as an example:

- We use 2 replicas in order to provide HA in prod.
- We use 2 replicas in staging too in order to simulate prod and how we would
  deal with potential downtime.
- We used a stateful set because we need stable DNS for every pod
- This service X must be up and running before other service Y

How much of a burden is it to encode these assertions?
