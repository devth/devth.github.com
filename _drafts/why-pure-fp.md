
Like you really need another one of these posts. At least I won't use the M
word. Just kidding I will. Seriously though, you know those people who argue and
rage about the right way, the moral way, even, but never really explain why
aside from mutterings about referential transparency and equational reasoning?
Well I'm not on of those, but I've wanted to believe them for years, and I think
I finally do. I'm here to help you do the same sans typical BS or elitism.

It's pretty simple really. The heart of the whole thing is composition. You've
probably read about the benefits of declarative over imperative programming and
I hope you agree: telling the computer how to do things over and over again is
getting really old.  It's time we started telling it what to do instead of
telling it how to do its job. This means composition: wiring together pieces of
code to achieve some greater transformation.

Now, you might be thinking "um, yeah dude, I compose stuff all the time without
worrying about academic nonsense like RT or pure FP. Have you even heard of
`_.compose`?" The answer is: LOL, yes I have.

But here's the problem: in a language that doesn't encode side effects in the
type system, there's no guarantee what the composition of two functions really
does. You pass something in and you get something out, but what happens in
between? It's a black box as far as the compiler is concerned. Of course you can
lookup the source, but that only tells *you* what the function does. The
compiler is still left in the dark. Plus, do you really want to rely on
executing every line of every function in your head to verify correctness? Is
that even possible? No. Why not let the computer act like a computer, and you
act like a human.

Of course this argument applies to individual functions too, not just
compositions. But with composition things get really interesting. Now, pure
composition itself, f . g is not that flexible or interesting, but we can get at
the context of composition by wrapping it up in our friendly friend the Monad.
This lets us answer questions like: "what does it mean to compose functions who
lock resources or write to file?" or "how many log statements should be produced
by a single function which composed two functions who each log a statement?"

In side effectful code you don't get to specify the answers to those questions;
you only get to observe or find out when pager duty calls you at 2:29 in the
morning.

Lastly, you might be saying "that's ridiculous I'm always very careful about the
execution of side effects in my code" and to that I say: growth of the code
base, additional contributors, distributed and multicore computing.

Let's assume you're buying all this and want to know more. Congratulations on
reaching this point. In order to write pure FP programs, we must separate our
programs into two distinct parts:

1. Application code: this is what we typically think of as the program, but the
   difference is the way we express it. Instead of performing side effects we
   build data structures that __represent__ side effects. That way our programs
   can remain referentially transparent.
2. Runtime code: this walks your data structures and actually runs them,
   performing the side effects on your behalf. Note that this code is not pure!
   But on the plus side, we can leave it up to really smart people to write this
   code for us, and it only needs to be written once and can be continually
   improved over time. In the case of Scala, we have the scalaz-stream library. In the
   case of Haskell, we have the language runtime itself.

