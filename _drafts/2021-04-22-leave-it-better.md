

Starting a new job is hard. Really hard. It might take anywhere from 6 months to
2 years to really hit one's stride.

Depending on the organization, pockets of legacy tech, turnover, culture of
technical excellence (or not) and a hundred other reasons, the state of tech
could be anywhere on a spectrum of terrible to amazing. And most likely it
leans a little harder toward terrible.

Lately I've been reading the [Staff Eng Book](https://staffeng.com/book) and
thinking about the qualities of Staff+. I'm not going to enumerate any (check
the amazing content at [staff Eng](https://staffeng.com/) for that!), but
instead consider one particular quality.

There are many things competing for an IC's time:

- Interviewing
- Reviewing PRs
- Meetings
  - Eng allhands
  - Team standups
  - Org standups
  - Company days
  - Demo days
  - and on and on and on

Internal projects tend to have a perpetually-alpha quality. I've seen it at
every company I've worked for over the last 22 years. It's rare to escape the
sticky gravity well of perm-alpha. (One trick for doing it is to open source
your thing).

When a new user clones the repo and opens README.md, do they very quickly find:

1. links to the environments it's running in
1. how to build (this should be 1 command that works on everyone's machine)
1. docs
1. specs for expected shapes of data
1. how to run the tests
1. a link to the project's CI/CD

As an engineer, I want to interact with well-defined black boxes. Until I have a
reason to understand the internals and turn the black box into a white box by
consuming available materials, including the code itself, which is the ultimate
source of truth, as well as the other hooks the authors have given me.

A system is a composition of black boxes, each with its own unique
characteristics.

A great engineer should be taking the apps and services they work on beyond
alpha.

A great SRE or platform architect should be applying the same holistic
excellence to the collection of apps they care for. How are you exposing a
consistent interface into the system? Is it a GraphQL BFF? A consistent
adherence to gRPC with well defined Protobufs?


