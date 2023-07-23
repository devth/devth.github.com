# Will it survive entropy?

Modern web apps are built in a complex style, requiring many tools
and systems to support both their development (e.g. CI/CD, secrets, config,
feature flags, etc.) and production runtime (e.g. databases, caches, CDNs, etc.).

For each of these components engineers must decide where on the spectrum of
fully-managed to custom-built the solution should lie. It can be useful
to think through the lens of _entropy_ and _extropy_.

## Forces of extropy

Discounting pivots and shutdowns, the core product itself is undergoing constant
extropy. Engineers come and go but the product is always on a growth trajectory,
being reconciled from current state into some better state with better utility
and better design and newer underlying technology. It's the focus of everyone
from CEO to legal to customer support to engineers to designers. High degree of
attention from a diverse set of disciplines prevents the product from
defaulting into a state of entropy.

Underneath the product are many layers of supporting infrastructure and
technology. The further away from the core product a component lives the less
often forces of extropy are applied, and the further that component's
implementation should be toward the fully-managed side of the spectrum.

## Fast forward

Ask ChatGPT to write feedback 3 years in the future for what you're building,
written by engineers who will join the company 18 months from now. Which of
these will it most closely resemble?

> Wow whoever wrote this did a great job thinking about the future needs of
> the company and set us up for success

> I understand why they did it this way at the time but now it no longer makes
> sense

> I wish they had integrated this in an orthogonal manner so it'd more of a
> two-way decision

## Focus

It might be fun to custom build a CI/CD system, but unless your core product is
in the CI/CD space, this creates unnecessary, compounding risk and complexity.
By focusing on what unique value your product delivers you can build a better
product, faster.

Beware entropy over time.

## Further reading

- [Extropy by Kevin Kelly](https://kk.org/thetechnium/extropy/)
