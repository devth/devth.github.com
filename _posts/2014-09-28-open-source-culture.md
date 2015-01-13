---
layout: article
title: Corporate process should mirror open source
categories: process
comments: true
image:
  feature: open_source.jpg
  teaser: open_source_teaser.jpg
---

Within any big tech corporation you will find many disparate teams. Most of
these teams know very little about the work, makeup or very existence of other
teams, yet they depend on each other's output, directly or indirectly.

When one team needs to use another's product / service / library, meetings are
setup, use cases are discussed, and all kinds of bureaucratic nonsense takes
place. Efficiency Failure.

Open source is need-driven. And while project authors and maintainers like to
know who their users are, it isn't a requirement. Code is built in a way to
solve a general problem. There is no need to "discuss your use case". A README
provides all the information a dev needs to:

- Get the code
- Run it
- Generally understand what it does and how it works

Beyond the README, well-built open source projects will provide docs, mailing lists,
and an IRC channel. Everything you need to quickly ramp up or get help.

Contrast this with typical corporate projects:

 - missing or inadequatte README
 - no docs or docs that are several versions (or years) out-of-date
 - docs probably live on some separate wiki on the internal network and may or
   may not be referenced from project README
 - requests for help are met with meeting requests

It's no wonder corporate tech moves so slowly.

A derivatory problem that corps suffer from is re-inventing the wheel over and
over again instead of using existing solutions. That's because it's often easier
to build something from scratch yourself than to try to deal with the awful
inter-team politics and meetings-driven-communication. Over time this leads to a
plethora of abandonware as teams move on and no longer need their
hacked-together solution. Unnecessary LoC growth.

## Solution

Start developing your internal libraries and services as if they were open
source, even when you have 0 users. Write an excellent README and keep it
up-to-date. Write in-repo docs to make it easier to keep them up-to-date, and be
disciplined about maintaining them. Write tests as necessary. Give examples.
Make it so good that when people start using your code, you don't even know it
because they didn't have any questions. But when they do have questions, **don't
schedule a meeting**.
