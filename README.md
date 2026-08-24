# HavocCasino

Casino games for **Purpur / Paper 1.21.x** — an animated slot machine, a mines
board, and a progressive jackpot, all played with server **money** via Vault.

## Features

- `/slots <bet>` — animated 3-reel slot GUI with weighted symbols and payouts.
- `/mines <bet> [mines]` — reveal tiles on a 5x5 board; the multiplier climbs with each safe tile, cash out any time before hitting a mine.
- `/jackpot [bet]` — progressive pool; each entry feeds the pot and rolls for the whole thing.
- `/gcrate [name]` — CS:GO-style crate opening: pay the crate cost, watch the reel spin, and win a weighted money multiple (or bust). Crates are defined in `crates.yml`.
- `/gamblingroom` (`/groom`) — gives each player their own gambling room: a saved schematic is pasted onto a plot grid and the player is teleported in. `/gamblingroom tp` returns them, `/gamblingroom delete` releases it. Optional creation cost via `rooms.cost`.
- `/casinomenu` (`/casino`) — opens a casino hub GUI (Slots, Mines, Jackpot bet-pickers, Crates, and your room). Designed to attach to an NPC.
- `/havoccasino` (`/hc`) — admin: reload config and manage the jackpot pool.
- MiniMessage-styled output, configurable bets, weights, odds and prefix.
- **Customizable messages** in `messages.yml` with placeholders (internal `{tokens}` + PlaceholderAPI).
- **Per-player message toggle** — each player turns HavocCasino messages on/off for themselves via a green (ON) / red (OFF) button (`/hc messages`).

## Requirements

- Java 21
- Purpur or Paper 1.21.x
- **Vault** + an economy plugin (e.g. EssentialsX) — required; the plugin disables itself without an economy
- (Optional) PlaceholderAPI for `%...%` placeholders in messages

## Building

The build is wired to compile **and obfuscate** in one step (ProGuard runs in the
`package` phase and rewrites the jar in place).

```bash
mvn clean package
```

The finished plugin lands at:

```
target/HavocCasino-1.0.0.jar
```

Drop that jar into your server's `plugins/` folder.

### CI build

`.github/workflows/build.yml` builds the obfuscated jar on every push and uploads
it as a workflow artifact (`HavocCasino-plugin`). Push this repo to GitHub and grab
the jar from the **Actions** tab — no local Maven needed.

## Obfuscation notes

`proguard.conf` renames and repackages classes, flattens the package hierarchy,
strips source-file names and runs several optimization passes, so decompiled output
is messy and hard to follow. What's deliberately kept readable (because the server
needs it): the main class named in `plugin.yml`, `@EventHandler` methods, enum
`values()/valueOf()`, and the GUI holder type.

Honest caveat: no tool makes JVM bytecode *impossible* to decompile. ProGuard raises
the effort a lot, but a determined person can still recover logic. If you need to run
after obfuscation and something misbehaves, loosen the aggressive options
(`-overloadaggressively` is intentionally omitted for that reason) and re-test.

## Messages & placeholders

All player-facing game text lives in `messages.yml` and is fully editable. Templates
support MiniMessage formatting and two kinds of placeholders:

- **Internal tokens** filled by the plugin: `{amount}`, `{multiplier}`, `{player}`,
  `{pool}`, `{chance}`.
- **PlaceholderAPI** `%...%` placeholders (when PlaceholderAPI is installed), including
  this plugin's own expansion:
  - `%havoccasino_jackpot%` — formatted jackpot pool (`%havoccasino_jackpot_raw%` for the number)
  - `%havoccasino_messages%` — `ON` / `OFF` for that player

### Turning messages on/off (client-side, per player)

Each player controls whether they receive HavocCasino messages:

- `/hc messages` — opens a settings screen with a **green ON** / **red OFF** toggle button; click to flip.
- `/hc messages on` / `/hc messages off` — quick toggle without the GUI.

The preference is saved per player in `settings.yml` and defaults to ON. Jackpot
broadcasts and game results respect each player's choice individually.

## Bets, shorthand & odds

- Bets accept shorthand: `5m` = 5,000,000, `10k` = 10,000, `2.5b`, `1t` (also `1,000`). Works for `/slots`, `/mines`, `/jackpot`, and menu entries.
- Big amounts display compactly (e.g. `$5m`); toggle with `currency.compact-display` in config.
- Odds/chances are shown in-GUI: Mines shows the next-pick safe % and next multiplier on the Cash Out button, the Mines setup screen shows per-mine-count odds, and the crate picker lists each reward's multiplier and %.

## Mines

- `/mines` with no bet opens a setup screen: pick a bet and a mine count (each shows the odds), then Play. `/mines <bet> [mines]` still plays directly.
- When a round ends the board shows **Play Again** (same bet & mines), **Change Bet / Mines**, and **Close** — no need to retype anything.

## Gambling rooms (wand + schematics)

A built-in, self-contained schematic system (no WorldEdit required) lets you turn any
build into a room players can spawn for themselves.

Set-up (admin, needs `havoccasino.admin`):

1. Build the room somewhere.
2. `/hc wand` — get the selection wand (a golden axe). **Left-click** one corner, **right-click** the opposite corner.
3. `/hc schem save <name>` — saves the selection to `schematics/<name>.yml`.
4. Stand where the room's minimum corner should paste and run `/hc room origin`.
5. `/hc room schematic <name>` — set that schematic as the room template. (`/hc room info` shows current settings.)

Players then run `/gamblingroom` to build-and-enter their own copy; each new room is
placed on the next free plot of a grid (`rooms.spacing` / `rooms.columns`). Pastes are
batched over ticks (`rooms.blocks-per-tick`) so big rooms don't freeze the server, and
selections are capped by `rooms.max-volume`.

## Settings-dialog integration (Wager Alerts)

This plugin does not register a `/wager` command. It still exposes the alert status
placeholder for dialogs, and toggling is done through `/hc messages`:

- Placeholder `%wagers_messages%` — the coloured ON/OFF status of the player's alerts
  (text/colour configurable via `alerts-placeholder` in config); `%wagers_messages_raw%`
  gives plain `ON`/`OFF`. (`%havoccasino_messages%` also works.)
- Toggle with `/hc messages` (opens the settings screen) or `/hc messages on|off`.

Example dialog row:

```
- LABEL: "  Wager Alerts: %wagers_messages%"
  COMMAND: "hc messages"
```

If the `wagers` placeholder itself clashes with another plugin, tell me and I'll remove it too.

## NPC integration (Citizens)

`/casinomenu` opens the hub GUI, so any NPC plugin that can run a command on click works.
With Citizens:

```
/npc select
/npc command add casinomenu
```

Citizens runs the command as the clicking player, so the menu opens for them. From the
menu they can play Slots/Mines/Jackpot (choosing from the `casino-menu.bet-presets`),
open crates, or create/enter their gambling room — no typing required. If `/casino`
clashes with another plugin, use `/casinomenu` (or the namespaced `havoccasino:casinomenu`).

## Config quick reference

- `betting.min-bet` / `max-bet` — slot bet bounds.
- `slots.two-match-multiplier` — payout when two reels match.
- `mines.default-mines` / `min-mines` / `max-mines` — mine count bounds on the 5x5 board.
- `mines.house-edge` — fraction shaved off the fair cash-out multiplier.
- `jackpot.seed` — pool value after a win.
- `jackpot.contribution-percent` — fraction of each entry added to the pool.
- `jackpot.win-chance` — 0.0–1.0 chance an entry wins the pool.
- `jackpot.min-entry` — minimum entry amount.

Crates live in their own file, `crates.yml`: each crate has a `cost` and a weighted
`rewards` table where `payout = cost * multiplier` (multiplier `0` is a bust). Tune
weights vs. multipliers to set the house edge; the defaults sit near 5%.

## Permissions

| Permission           | Default | Grants                    |
|----------------------|---------|---------------------------|
| `havoccasino.slots`  | true    | `/slots`                  |
| `havoccasino.mines`  | true    | `/mines`                  |
| `havoccasino.jackpot`| true    | `/jackpot`                |
| `havoccasino.crates` | true    | `/gcrate`                 |
| `havoccasino.room`   | true    | `/gamblingroom`           |
| `havoccasino.menu`   | true    | `/casinomenu`             |
| `havoccasino.messages`| true   | `/hc messages` (self)     |
| `havoccasino.admin`  | op      | `/havoccasino` admin cmds |
