# Game Screen Concept — Audience Questions Review

Reviewing the Player / Judge / Spectator mode-switching concept
(https://claude.ai/artifact/KBixJbyPdeV3NXctQzN7w2) by role-playing each
audience and asking the questions they'd realistically have about board
state. Each question is marked by whether the current concept answers it:

- **Yes** — answered directly, at a glance or one click
- **Partial** — answerable but requires inference, scanning, or isn't targeted
- **No** — not represented in the design at all

Use this list to sanity-check future revisions of the concept, and add to it
as new questions come up.

## Player (seated)

| # | Question | Answered? | Notes |
|---|---|---|---|
| 1 | How much pool/VP do I have right now? | Yes | Own dock, large numerals |
| 2 | What's in my hand? | Yes | Hand strip, named cards |
| 3 | Is my minion tapped or in torpor? | Partial | Tile dimming only — no text state label, easy to miss at a glance or for colour-blind users |
| 4 | Am I being attacked right now, by whom, how much? | Yes | Pending-action banner is prominent and specific |
| 5 | What are my options to respond? | Partial | Block/React/Pass buttons exist but aren't tied to which hand cards actually enable them |
| 6 | How many cards does each opponent hold? | Yes | Shown as a stat per seat |
| 7 | What's contested, and by what/whom? | Partial | Red dot shows *that* it's contested, not the reason or contesting party |
| 8 | Is it my turn, or am I waiting on someone else? | No | No explicit "your turn" / "waiting on X" indicator — must infer from chat text |
| 9 | What's actually in my ash heap? | Partial | Count only, no contents — ash heap is public info in VTES rules, so this under-serves the "at a glance" goal |
| 10 | How close is anyone to winning? | No | VP shown per seat, but no target/threshold context |

## Judge (unseated, full visibility)

| # | Question | Answered? | Notes |
|---|---|---|---|
| 1 | Is there an open ruling request waiting on me? | Yes | Banner is prominent and un-missable |
| 2 | What's everyone's actual hand, to check for irregularities? | Yes | Full names listed per seat |
| 3 | What specific command failed, and why? | Yes | One rejection example shown with reason + invocation id |
| 4 | Can I intervene directly on the board? | Partial | Buttons exist (Adjust Pool, Void Action, etc.) but aren't targeted — no way to say *which* card/seat/action they apply to |
| 5 | Full history behind this dispute, not just this turn? | No | Audit log only shows current-turn chat; History tab exists but isn't wired to anything |
| 6 | Does the board state actually let me answer the rules question asked? | No | E.g. "can I use Freak Drive while embraced" needs to know *when* an effect/status started — no timing/effect-duration state exists in the model at all |
| 7 | Is this a tournament game (affects my ruling authority)? | No | Backend (`JudgeResource.canRule`) gates on this; nothing in the UI surfaces it |
| 8 | Is a deck/crypt actually legal? | No | No deck or crypt-list view represented |
| 9 | Why is this card contested? | Partial | Same gap as player — visible that it's contested, not why |

## Spectator (read-only)

| # | Question | Answered? | Notes |
|---|---|---|---|
| 1 | Who's winning right now? | Partial | Standings sorts by VP only; with most players at 0 VP, pool trend matters more and isn't combined into one ranking |
| 2 | Whose turn is it / what's happening now? | No | Same gap as player mode — no active-seat highlight on the ring |
| 3 | What's in play for each seat? | Yes | This was the point of the revision — tiles now show real board state |
| 4 | Is anyone close to being ousted? | Yes | Low pool renders in red (Sable Voss at 8) |
| 5 | Why is a seat greyed out? | Yes | "Ousted" badge is explicit |
| 6 | What does this card actually do? | No | Clicking shows stats (capacity/disciplines/blood) but no rules text — likely the first thing an unfamiliar viewer wants |
| 7 | How many players are still in? | No | No explicit "4 of 5 remaining" summary, only inferable by counting ousted badges |
| 8 | Is this game popular / how many are watching? | Yes | "142 watching" shown |
| 9 | How did we get to this point (match history)? | No | No full-match timeline, only current-turn chat |

## Cross-cutting gaps (all audiences)

- **No "whose turn / acting now" indicator** anywhere — biggest recurring gap, affects Player and Spectator equally.
- **Contested cards don't explain themselves** — visible as a state, not as a reason.
- **Clicking a card shows its printed stats, not its current effects/timing** — matters most for Judge rulings, but useful for all three.
- **No card rules-text lookup** — likely wanted by Spectator most, but arguably useful everywhere.
