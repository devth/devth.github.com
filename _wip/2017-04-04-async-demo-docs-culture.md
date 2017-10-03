The benefits of demoing your work among your team are well know. Typically this
is done during some periodic meeting, e.g. "Demo Fridays". But we often have
work to demo at odd times. Maybe it's 2am or maybe you wrapped up some work
early in the week, days away from some meeting.

The best time to demo work is when it's fresh. You're often most excited and
knowledgeable about your work during or right after you "complete" (for some
fuzzy definition of complete) it. So demo it. Right away. You might even
discover some delta between ideal and current state that you can reconcile.

One lightweight high fidelity way to demo work is using the amazing
[asciinema](https://asciinema.org/).

After you record your demo, put it in the docs. This is an excellent way to
capture tons of implicit context that is often very hard or impossible to
capture otherwise. And it serves as a loose spec of how things are supposed to
work, or were supposed to work at some point in time.

## Tmux

If you use tmux, these aliases make it super easy to setup an asciinema
recording:

```bash
# asciinema {{{
  alias demo_setup="tmux new -s demo || tmux attach -t demo"
  alias demo_rec="asciinema rec -c 'tmux attach -t demo'"
# }}}
```

Note: these are extracted from my [dotfile zen garden](TODO post zshrc) but can
easily be adapted to your own environment.
