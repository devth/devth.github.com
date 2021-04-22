# Viz

Play with Elm while making physiology-related visualizations.

## Setup

```bash
brew search elm
brew info elm
brew install elm
npm install -g elm elm-test elm-oracle elm-format

elm install elm-lang/html
```

## Usage

```bash
elm repl
```

Put some Elm stuff in the REPL:

```elm
point = { x = 3, y = 4 }
point.x
point.y

List.map .x [point, point]

xUnder5 {x} = x < 5

List.map xUnder5 [point]
```
