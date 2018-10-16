# A Roadmap to Learning Functional Programming

Functional Programming can be an intimidating subject to dive into. After many
years of learning and dabbling in functional programming I wanted to provide a
roadmap that outlines one possible path for others to follow.

## Introduction to Functional Programming

Functional programming (FP) is a somewhat rigid style of programming that adopts
a few constraints. In doing so it narrows the  amount of possible things a
program can or might do. While other styles of program have idioms, FP is more
rigorous with a mathematical basis that defines structure and laws.

Functional programming is practiced on a spectrum. At one end their is pure
FP, which is usually the end goal. At the other end are programs that take
advantage of some of the tenants of FP but do not fully buy into the paradigm.
Because of this you might see phrases like "pure inner core" and "impure outer
shell" which describes exactly what it sounds like: purity within the core
functions of a program with effects pushed to the outer edges.

## Roadmap

1. What is FP
  1. A style of programming
  1. Composition: the ultimate goal
  1. Purity
  1. Referential transparency
  1. Comparison to OOP
1. Benefits of FP
  1. Equational reasoning
  1. Declarative programming
  1. Concurrency
  1. Testability
  1. Mathematical rigor
1. How to actually achieve FP
  1. Language support for FP
    1. Haskell - enforces purity at the language level
    1. Scala - able to achieve purity naturally using libs like Cats or Scalaz
       but the compiler is not enforcing anything
    1. Clojure - closer in the spectrum to ES, but includes a wealth of data
       transformation functions, immutable data structures by default, and
       very well designed concurrency primitives
    1. TypeScript
    1. ReasonML
    1. PureScript
    1. Elm
  1. Pure FP or just FP?
  1. Algebraic data structures
    1. Type classes
    1. Monoid
    1. Semigroup
    1. Monad
  1. Category Theory and FP: what is it and how does it apply?

## Continue your journey with external resources

1. Category Theory for Programmers series
1. Learn you a Haskell
1. Haskell Book
