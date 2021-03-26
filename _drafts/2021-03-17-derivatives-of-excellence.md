# Derivatives of excellence

Or: Higher Order Correctness

When changes are made in production, what checks are made to ensure correctness?

How easy is it to inject a bug into production? If you intentionally wrote a
buggy component, opened a PR, ran tests, got a review, would these guardrails
prevent your bug from getting shipped? What if it was a tricky bug? What if it
was in a component that had no test coverage? What if you were the only person
who was familiar with your code base? What if it was in an area of the
application that was rarely used? What if the bug relied on an extremely rare
combination of things happening in order to manifest? How would the answer to
these questions be answered by your most senior person leaving the company? How
about half of your engineers working?

What I'm getting at is understanding the automation or guardrails or checks in
place to guarantee bugs are not being shipped. If it relies on humans, you've
already failed. Humans make mistakes. 

Yet humans are the ones that create the checks. The automation. They are the
ones who 


What we need is extra-human intelligence to carry out our intentions.

Think about this through the lens of derivatives. A derivative models rate of
change. The idea lends itself to recursion: e.g. considering how fast the rate
of change is changing. In other words:

In physics they usually introduce this through acceleration.
Second derivative is how quickly your acceleration is increasing.

change change x
change change change x
change change change change x

Let's extend that to correctness.

Anything beyond the first derivative is sometimes called a "higher order
derivative". Higher Order Correctness

A first line of defense might be unit testing.

Another way to consider this concept is through layers, which is pretty obvious
since we already mentioned recursion. What layers of automation are in place to
ensure a given thing is correct?


1. Unit testing tests very small chunks of a component. Its nowhere near
   comprehensive, and is prone to weakness of humans testing the conditions or
   not testing at all.

1. Property based testing improves upon unit testing: it covers edge cases that
   humans may have overlooked by generating large sets of inputs to functions
   under test.

1. Integration tests zoom out and begin to test the boundaries of a system, and
   in the process exercise entire sub-systems.


Many teams operate in a move-fast-break-things mentality (explicit or implicit)
because their products are not regulated, and when things break, customer impact
is low or insignificant, so answer to questions posed don't have serious
consequences, but other industries are heavily regulated, and failures in
production, whether data loss or exposure, or incorrect algorithms can have
serious consequences.



