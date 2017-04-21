# Incremental reconciliation

I like to talk about the development process as reconciliation: reducing the
delta between current state and desired state.

We often see a need for improvement in a codebase but add it to the backlog or
throw a TODO in the comments, or maybe complain about how software is broken on
Twitter.

Instead, where possible, we should instead embrace incremental improvements.
This sets convention and precedent so the next person (or your future self) who
comes along sees the intended way. When they add features or make changes, they
can decrease the delta between desired and current state.

In other words, you're documenting a loose spec that can be built upon.

## Desired state

- Acknowledge implicit context by defining the spec of where a project should
  be, and make that spec available for feedback from others
- Communicates direction of tech
- Informs others specs on their own projects and potential integration points

You can't (or it's very hard to) overcommunicate. Too often teams
undercommunicate with individuals going off on their current project for days or
weeks in heads down mode. This is fine and necessary, but individuals need to
expose their state along the way, and do it in the most precise, asynchronous
method possible.

Avoid questions like: "What did you do last week?". There are so many details in
the answer to that question and there's no way an individual can recall them
all at a given time. Instead, rely on precise medium of capturing state over
time and consume that medium.

## Risks

Of course there are risks and potential downsides with this approach. If you're
always setting new convention incrementally and never getting above some
significant portion of reconciliation (let's say 80% but this depends), then
your codebase will be a mishmash of various possibly-conflicting conventions
that are never fully realized. It still takes discipline.

It only works if you are continually weeding the garden and prioritizing
specific reconciliations. You want your codebase to be in a good state at least
part of the time.

## Examples where it works

- Beginning to add clojure.specs to a codebase
- Increasing test coverage
- Experimenting with generative testing

## Examples where it doesn't work

- Large re-factors
