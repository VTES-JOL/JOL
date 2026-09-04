# In-game log / command-output review

Review of every state-changing command's chat-log output: is it clear **who**
acted and **what** changed, does it ever leak the identity of a hidden or
face-down card, and how is it rendered. Written cold-start-friendly, same shape
as `game-page-review-brief.md` (ranked findings: what's wrong / why it matters /
fix sketch).

## 1. Scope & method

- Backend text generation: `net.deckserver.game.model.JolGame` (all
  `ChatService.sendCommand` / `sendSystemMessage` call sites),
  `net.deckserver.game.model.DoCommand` (command dispatch),
  `net.deckserver.services.ChatService`, `net.deckserver.game.enums.RegionType`.
- Delivery: `net.deckserver.rest.GameActionResource#getHistory` +
  `withoutInvocation`, `net.deckserver.storage.json.game.ChatData`.
- Rendering: `src/main/webui/src/pages/game/GameChatLog.tsx`,
  `GameChatPanel.tsx`, `components/MessageContent.tsx`,
  `utils/parseMessageTokens.ts`, `src/pages/GamePage.css`.
- Sampled real output from the local dev Postgres snapshot
  (`jdbc:postgresql://localhost:5432/jol`, `jol`/`jol`) — 206 games, ~159k
  `game_chat_message` rows — for every `command` prefix (`draw` 18.7k,
  `transfer` 14.7k, `counter` 14k, `play` 13.3k, `lock` 11.5k, … down to
  `clan` 4, `timeout` 1). Note: stored rows were rewritten by the migration, so
  wording differs slightly from today's code; findings below are against the
  current source.

## 2. How it works today

`DoCommand.doCommand` parses a raw command and calls a `JolGame` mutator. Each
mutator changes `GameData` in memory and calls
`ChatService.sendCommand(gameId, source, message, command...)` — `source` is the
acting player's name, `message` is a pre-built prose fragment, `command...` is a
structured `verb arg arg` string. `ChatService` wraps it in `ChatData` and (via
`beginInvocation`/`endInvocation` on the resource) stamps the raw typed command
+ issuer + sequence for the judge view.

`GameChatLog.tsx` renders each row as:
`<span.timestamp>` + `<b>{source}</b>` (unless `source === 'SYSTEM'`) +
`<MessageContent>` (token substitution: `[card:id:name]` → hover link,
`[disc:x]` → icon, `[d]`, `[style:x]`). Judges additionally see a `»` line with
the raw command above each run, and interleaved `⚠` lines for failed attempts.

So the intended contract is: **the bold `source` column is "who"; the message
body is "what", written as a verb phrase with the actor implied.** Most of the
newer code follows it; a lot of the older code doesn't.

## 3. Hidden / face-down card handling

### Working correctly

- `playCard` face-down branch (`JolGame.java:149`) — "plays a card face down
  from their hand …", no `[card:…]` token, no catalog id in the message.
- `moveToRegion` / `moveToCard` (`:895`, `:725`) —
  `hideIdentity = faceDown || card.isFaceDown()` → renders "a card"; a hidden
  *target* card renders "Card #\<coords\>".
- `getTargetCardName` (`:762`) — face-down or `OTHER_HIDDEN_REGIONS` target →
  "Card #\<coords\>", never the name.
- `getCardName` (`:784`) — both endpoints hidden and same owner → "card
  #\<coords\> in their \<region\>".
- `discard` / `burn` / `rfg` set `faceDown = false` **before** building the link
  (`:112`, `:933`, `:952`) — identity deliberately becomes public as the card
  leaves play. Rules-consistent for these three.

### Findings

**H1 — the structured `command` string leaks the hidden card's catalog id to
every client.** `GameActionResource.withoutInvocation` (`:150`) strips
`invocation` / `invocationBy` / `invocationSeq` for non-judges but **copies
`command` through** (`copy.setCommand(c.getCommand())`). For a face-down play
the call is
`sendCommand(id, player, msg, "play", cardId, destPlayer, region)`
(`JolGame.java:153`) and for a face-down move
`sendCommand(id, player, msg, "move", card.getId(), …)` (`:908`) — so the
`/game/{id}/history` JSON every seated player receives contains
`command: "play 201703 Bob ready"`, i.e. the exact card. `GameChatLog.tsx`
never reads `line.command`, so nothing shows on screen — but it's one open
dev-tools tab away. *Fix:* drop `command` in `withoutInvocation` too (it's
unused client-side); or, if a use appears later, redact the id for
`play`/`move` whose card is currently face-down. Verify no other consumer of
`ChatData.command` exists first (`types.ts:611` declares it; no reader found).

**H2 — `burn` / `rfg` of a card that is face-down *in play* reveals it.** e.g. a
Powerbase flipped down, or a card put into play face-down by an effect, then
burned: the line names it. A card leaving play via burn *is* revealed under the
rules, so this is most likely correct — flag it only to get an explicit "yes,
intended" and a one-line comment at `burn` / `rfg`. `rfg` can also run from
`uncontrolled` / `ashheap` (`DoCommand.java:329`); same reasoning.

**H3 — `discard` unconditionally clears face-down (`:112`).** Currently moot —
`discard` only targets `RegionType.HAND` (`DoCommand.java:440`) and the model
has no face-down-in-hand state — so no fix, just noted so a future
"discard from anywhere" doesn't inherit a leak.

**H4 — position coordinates are not a leak but are shown for own hidden
cards.** `getDifferentiators` (`:808`) appends a board index to every non-unique
card name; it is only reached from `getCardName`'s *visible* branch, and the
hidden branches return first — checked, clean. The actor's own uncontrolled /
hand cards still show as "card #N in their inactive region", which is expected
(opponents can't see the region anyway).

**H5 — `show` is clean.** The actual card list goes only into each recipient's
`PlayerData.notes` (`:707`); the public line leaks count + region only. Minor:
no guard that `recipients` are live players, and the note is appended with a
bare `\n` and no turn context.

## 4. Cross-cutting text findings

**CC1 — the actor's name is duplicated into the message body.** The UI already
renders `source` in bold, so these read "**JonD** JonD has gained 1 victory
points.":
- `updateVP` (`:69`) — "\<name\> has gained/lost N victory points."
- `changePool` (`:432`) — "\<name\>'s pool was X, now is Y."
- `setEdge` (`:401`) / `burnEdge` (`:408`) — "\<name\> gains the edge …" /
  "\<name\> burns the edge."
- `setOrder` (`:685`) — "Player order \<names\>" (no verb, no subject).
- `contestCard` (`:508`) — "\<owner\>'s \<card\> is now contested." (dup when
  owner == actor).
*Fix:* the body never starts with the actor's own name. "gains 1 victory
point.", "pool 14 → 15.", "takes the edge from Alice.", "sets the seating
order: Alice, Bob, Carol." Keep a *target* player's name when it differs from
the actor (judge adjusting someone else's pool/VP).

**CC2 — tense / voice is inconsistent.** Almost everything is present-tense
third person ("draws", "plays", "burns", "locks"). Exceptions:
- `transfer` (`:322`) — past tense "transferred 1 blood off …".
- `changeCapacity` (`:598`) — no verb at all: "Capacity of \<card\> now 10".
*Fix:* present tense throughout — "moves 1 blood onto …", "raises capacity of
… to 10.".

**CC3 — terminal punctuation / trailing whitespace vary.** `discard` (`:116`)
ends with no period; `changeCounters` (`:334`) ends `"… now 1. "` (trailing
space); `changeCapacity` no period; `transfer` / `influence` have one. *Fix:*
one sentence, one trailing period, no trailing space — ideally via a single
message-builder helper rather than per-call `String.format`.

**CC4 — region labels are not prose.** `RegionType.xmlLabel()` yields
`ashheap`, `rfg`, `inactive region`, `ready region`; `description()` yields
`Ash heap`, `Library`, `Uncontrolled region` (capitalised). Both leak into
sentences today:
- `burn` (`:942`) "from their ready region." — ok-ish
- `rfg` (`:962`) "in their ashheap from the game." — "ashheap"
- `show` (`:700`) "cards of their Library." — capital L mid-sentence
- `banish` (`:924`) hard-codes "uncontrolled region" while `move` says
  "inactive region" for the same region.
*Fix:* add `RegionType.logLabel()` → `ready region`, `uncontrolled region`,
`ash heap`, `hand`, `library`, `crypt`, `torpor`, `the removed-from-game pile`;
use it everywhere in log text; delete the hard-coded string in `banish`.

**CC5 — the region is named even when the command has only one.** Per the
brief's "banish only works on ready cards" note:
- `banish` — source is always ready, destination always uncontrolled →
  "banishes \<card\>." (optionally "… (now uncontrolled)").
- `burn` — 11.5k of 13k burns are from ready (the default). Omit the region
  when it's ready; keep it only for ash heap / uncontrolled / torpor.
- `discard`, `lock`/`unlock`, `influence` already omit — good, keep.
- Keep the region for `move`, `show`, `shuffle`, `rfg` (defaults there are not
  the common case).

**CC6 — "their" shortcut is only half-applied.** `playCard`, `banish`, `burn`,
`rfg`, `moveToRegion`, `getTargetCardName`, `getCardName` already collapse
same-owner to "their". Not done in:
- `contestCard` (`:508`) — "\<owner\>'s \<card\>" even when owner == actor.
- `changePool` (`:432`) — "\<name\>'s pool" even for self.
*Fix:* same-owner → "their"; else "\<Name\>'s". Apply uniformly.

**CC7 — see H1** (`command` field shipped to all clients).

## 5. Per-command notes

| command | method | issue |
|---|---|---|
| `vp` | `updateVP` :67 | CC1 dup name; "1 victory point**s**" (no pluralisation) |
| `vp withdraw` | `withdraw` :61 | ok |
| `pool` | `changePool` :419 | CC1, CC6; "was X, now is Y" → "X → Y"; **S1** no ousted/remap line when pool ≤ 0 |
| `blood` | `changeCounters` :326 | CC3 trailing space |
| `transfer` | `transfer` :312 | CC2 past tense; "Currently: 2, Pool: 12" capitalised labels; "off"/"onto" vs `blood`'s "to" |
| `capacity` | `changeCapacity` :590 | CC2 no verb, CC3 no period, capital "Capacity" mid-line |
| `draw` | `_drawCard` :750 | clean; but `draw 3` emits 3 identical lines — coalesce to "draws 3 cards from their library." |
| `discard` | `discard` :109 | CC3 no period |
| `play` | `playCard` :120 | face-up path good (src omitted for hand, dest omitted for ash heap, "their"/"X's" correct); face-down path good; multi-mode `modeMessage` has no separator between icons |
| `influence` | `influenceCard` :196 | good; contest re-announce duplicates `checkContested` logic instead of calling it |
| `move` | `moveToRegion` :895 / `moveToCard` :725 | two verbs for one command — "moves … to …" vs "puts … on …"; CC4 region label |
| `burn` | `burn` :930 | CC4, CC5; H2 |
| `banish` | `banish` :918 | CC4 (hard-coded "uncontrolled region"), CC5 (both regions fixed) |
| `rfg` | `rfg` :949 | CC4 "ashheap"; "in their X from the game" clumsy |
| `lock` / `unlock` | `setLocked` :515 | good, terse — correct for an 11k/turn command |
| `unlock` (all) | `unlockAll` :534 | good; "unlocks." + SYSTEM exceptions list |
| `contest` | `contestCard` :504 | CC6; actor may legitimately be a third party (judge) |
| `disc` | `setDisciplines` :602 / :614 | combined add+remove sentence is ungrammatical: "added [X] removed [Y] **to** \<card\>."; `reset` path uses different phrasing ("reset … back to …") |
| `path` | `setPath` :223 | empty old value → "changes path of \<card\> from&nbsp;&nbsp;to Caine" (double space); → "sets \<card\>'s path to Caine." / "clears …" |
| `sect` / `clan` | `setSect` :214 / `setClan` :232 | verbose but valid ("changes sect of \<card\> from Independent to Laibon") |
| `votes` | `setVotes` :477 | "now has 1 vote**s**" (no pluralisation) |
| `label` | `setLabel` :451 | "removes label from \<card\> " trailing space (CC3) |
| `show` | `show` :695 | CC4 capitalisation; "1 cards"; positional `%3$s` format is fragile |
| `shuffle` | `shuffle` :241 | good |
| `order` | `setOrder` :680 | CC1; "Player order Alice Bob Carol" → "sets the seating order: …" |
| `edge` | `setEdge` :400 | CC1; "from no one" when nobody held it → "takes the edge." |
| `open` | `setOpenHand` :522 | grammar — "plays now with an open hand" → "now plays with an open hand."; "NO longer plays now with…" → "no longer plays with an open hand." |
| `random` / `flip` | :469 / :473 | " : " spacing — cosmetic |
| `choose` / `tally` | `setChoice` :664 / `getChoices` :669 | hidden correctly ("has made their choice."); reveal interpolates `PlayerData` — confirm `toString()` is the name |
| `hide` / `reveal` | `setCardFaceDown` :183 | correct — "turns \<card\> face down." / "reveals \<card\>." |

## 6. State changes with no log line

**S1 — ousting and predator re-mapping are silent.** `changePool` / `updateVP`
set `ousted = true` and call `updatePredatorMapping()` (`:425`) but emit only
the pool/VP line. "\<X\> is ousted." and "\<Y\> is now \<Z\>'s prey." should be
`SYSTEM` lines — this is one of the biggest state changes in the game and it's
invisible in the log.

**S2 — no turn marker in the log stream.** `newTurn` (`:554`) writes the turn
row and sets the phase but emits no "Turn 12 — Alice's turn." line; the only
per-turn phase line is misattributed (U1).

**S3 — hiding a unique card doesn't clear other copies' contests** (documented
intentional at `:180`) and gives no reminder. A "(other copies still contested —
clear manually)" `SYSTEM` hint would help.

## 7. UI / rendering findings

**U1 — phase markers are attributed to the active player, not SYSTEM.**
`setPhase` (`:586`) → `sendMsg(getActivePlayer(), "START OF … PHASE.")` →
`ChatService.sendMessage` with `source = <active player>`. `GameChatLog.tsx`
only quiets rows where `source === 'SYSTEM'` (`:91`), so "START OF INFLUENCE
PHASE." renders as ordinary chat with the player's bold name — looks like they
typed it. `GamePage.css:96` even claims phase markers use the quiet channel;
they don't. *Fix:* send phase / turn / oust / contest / timeout markers via
`sendSystemMessage` (or add an explicit `kind` to `ChatData`), and give them a
divider style, not just grey italic body text.

**U2 — no turn dividers.** `GameChatLog` renders one turn's lines flat; the
boundary lives only in the separate turn picker. A "── Turn 12 · Alice ──" rule
between turns (in the "all turns" view) aids scanning. Product call — may be
deliberate given the picker.

**U3 — no per-actor colour.** Every line is the same colour; the only "who" cue
is the bold name, which several messages bury or duplicate (CC1). The board
already assigns a colour per seat — tint the `source` name with it, and
optionally group consecutive same-actor lines.

**U4 — action type is available but unused.** `command[0]`
(`draw`/`burn`/`play`/`lock`/…) rides on every structured row. A small glyph or
colour band in a left gutter (⚔ play, 🔥 burn, ↻ unlock, ⬇ draw, ⛓ lock) would
make the log skimmable. At minimum, align the actor-name column.

**U5 — timestamp is noisy.** Every row shows "d-MMM HH:mm"; the date repeats on
every line within a turn. Show `HH:mm` only, date once per change, full
`postedAt` on hover (it's already sent).

**U6 — `chat-attempt` rows** render `row.data.timestamp` (minute text) with no
`occurredAt`-based formatting — cosmetically out of step with chat rows that
sort on the ISO stamp.

**U7 — wrapped machine lines have no hanging indent.** A wrapped `transfer` line
continues under the timestamp, reading like a new entry. Add
`text-indent` / `padding-left` hanging indent to `p.chat`.

**U8 — malformed `[disc:x]` renders an empty invisible span**
(`MessageContent.tsx` `case 'disc'`). Low priority; add a text fallback for an
unknown code.

## 8. Proposed house style

1. One line = one sentence, **present tense, third person**, actor implied by
   the `source` column — the body never contains the actor's own name.
2. Ends with a single period; no trailing whitespace. Built through one helper,
   not ad-hoc `String.format` per call site.
3. Card references: visible & face-up → `[card:id:name]` token (+ position
   suffix only when non-unique *and* ambiguous). Face-down, or in a region the
   audience can't see → "a card" in prose / "card #\<coords\>" when a position
   is needed. Never emit the catalog id in the message **or** the structured
   `command` for a hidden card.
4. Regions via `RegionType.logLabel()` (lower-case prose). Omit the region when
   the command has exactly one legal region (`banish`, `lock`/`unlock`,
   `discard`, `influence`, and the ready-region case of `burn`/`move`); name it
   otherwise.
5. Possessive: actor's own card/region → "their"; another player's →
   "\<Name\>'s". Every command, no exceptions.
6. Coalesce multi-count commands (`draw N`, repeated `transfer`) into one line
   where feasible.
7. Machine lines (phase, turn, ousts, contests, timeouts) use `SYSTEM` (or an
   explicit `kind`) and render as quiet dividers — never attributed to a player.

## 9. Ranked priorities

**P1 — correctness / leak**
1. **H1 / CC7** — strip `command` (hidden card's catalog id) from the non-judge
   history payload.
2. **U1** — phase markers attributed to the active player instead of SYSTEM.
3. **S1** — ousting / predator re-map emit no log line.

**P2 — clarity**
4. **CC1** — stop duplicating the actor name (`pool`, `vp`, `edge`, `contest`,
   `order`).
5. **CC4** — `RegionType.logLabel()`; remove "ashheap" / "rfg" /
   "inactive region" / "Library" from prose.
6. **CC2 / CC3** — normalise tense + punctuation (`transfer`, `capacity`,
   `blood`, `discard`, `counter`).
7. **CC6** — "their" for `contest` and `pool`.

**P3 — polish / UI**
8. **CC5** — omit unambiguous regions (`banish`, `burn` from ready).
9. Grammar: "1 victory point**s**", "1 cards", "from&nbsp;&nbsp;to", the
   "added … removed … to" sentence, "plays now with an open hand".
10. **U3 / U4** — per-seat colour on the source name + action-type gutter.
11. **U2 / U7** — turn dividers + hanging indent for wrapped lines.
12. **U5** — collapse the repeated date; full stamp on hover.
13. Coalesce multi-`draw`; unify `move`/`put` verb; fix `disc` phrasing.

---

## 10. Resolutions — agreed direction (2026-09-04)

Owner decisions on §4–§7, with concrete approaches. **U2 and U4 are dropped**
(deliberate / not wanted). This section is the implementation brief.

> **Status: implemented 2026-09-04.** Backend: `GameLog` helper +
> `RegionType.logLabel()`, every `JolGame` log call site rewritten to the
> §10.3 table, `draw N` coalesced, S1 oust/restore + S2 turn + U1 phase lines
> as `SYSTEM`, and `GameActionResource` now strips `command` (not just
> `invocation*`) for non-judges. Frontend: `chatLogStyle.ts` (log palette +
> `shortTime`/`dayLabel`), `GameChatLog` per-actor accent bar + dimmed repeat
> name + date separators + shared `LogTimestamp`, hanging indent in
> `GamePage.css`, unknown-discipline fallback in `MessageContent`. Tests:
> `GameLogTest` added; full backend suite (156) + frontend (195) green.
>
> **Live-checked** against dev DB game "Strict coating march" as ShanDow:
> `flips a coin: Tails.`, `rolls 1–6: 5.`, `adds 1 blood to X (now 7).`,
> `marks their X contested.` / `clears the contest on their X.`, HH:mm
> timestamps, `FRI 4 SEPT` separator, accent bar + dimmed repeat names all
> render as intended. One bug found & fixed in the pass: a date separator
> wasn't breaking the visual run (line under it stayed dimmed).
>
> **Known pre-existing (not a regression), left alone:** card-name tokens
> render in the `matrix` webfont with wide side-bearings, so `verb X.` reads
> as `verb  X .` — visible on historical lines too. Tighten separately if it
> bothers.

### 10.1 Shared backend helpers (build these first)

Every finding below leans on three helpers. Put them next to the log call
sites (a `GameLog` static util, or private methods on `JolGame`).

```java
// CC3 — one entry point; trims, collapses internal runs of whitespace,
// guarantees exactly one terminal . ! or ?  Nothing calls
// ChatService.sendCommand from JolGame directly any more.
void log(String actor, String body, String... command) {
    ChatService.sendCommand(id, actor, GameLog.sentence(body), command);
}

// CC6 — "their" when the owner is the actor, else "<Owner>'s".
static String possessive(String ownerName, String actorName) {
    return ownerName.equals(actorName) ? "their" : ownerName + "'s";
}

// CC4 — lower-case prose region name (see 10.4).
region.logLabel();
```

Add one unit test that drives every `DoCommand` verb once and asserts each
emitted `message` matches `^\S.*[.!?]$` and contains no `\s{2,}` and no
`RegionType.xmlLabel()` value that differs from its `logLabel()` (catches
"ashheap" / "rfg" leaking into prose).

### 10.2 CC1 — actor vs. affected player

Rule (no schema change): **the message body never begins with a player name,
and never contains the actor's own name.** Two shapes:

- **Acting on yourself** → bare verb phrase. Bold `source` is the whole "who".
  `gains 1 pool (12 → 15).` · `takes the edge.` · `sets the seating order: …`
- **Acting on another player P** → the body names P in object position, with a
  verb that makes the direction explicit.
  `reduces Bob's pool by 2 (14 → 12).` · `gives the edge to Carol.` ·
  `marks Bob's [card] contested.` · `awards Bob 1 victory point.`

Commands that can target another player: `pool`, `vp`, `edge`, `contest`,
`order` (affects everyone — treat as "self/neutral"). Judge-issued versions
already arrive with `source = "Judge - X"`, so the same two shapes still read
correctly ("**Judge - Alice** reduces Bob's pool by 2 …").

Rejected alternatives: a new `ChatData.target` field (structural, but nothing
in the render needs it once the phrasing rule is in place); routing
cross-player effects through `SYSTEM` (loses the "who").

### 10.3 CC2 — canonical wording

Fragments below are the message body; the UI prepends **`source`**. `{c}` = card
token or hidden-card name, `{r}` = `logLabel()`, `{poss}` = `possessive(...)`,
`{P}` = another player.

| command | condition | new text |
|---|---|---|
| `vp` | self, +1 | `gains 1 victory point.` |
| `vp` | self, −0.5 | `loses 0.5 victory points.` |
| `vp` | other | `awards {P} 1 victory point.` / `removes 1 victory point from {P}.` |
| `vp withdraw` | | `withdraws and gains 0.5 victory points.` |
| `pool` | self | `gains 3 pool (12 → 15).` / `loses 1 pool (15 → 14).` |
| `pool` | other | `increases {P}'s pool by 1 (14 → 15).` / `reduces {P}'s pool by 2 (14 → 12).` |
| `blood` | add / remove | `adds 1 blood to {c} (now 4).` / `removes 1 blood from {c} (now 2).` |
| `transfer` | onto / off | `moves 1 blood onto {c} (now 5) (pool 18).` / `moves 1 blood off {c} (now 2) (pool 21).` |
| `capacity` | up / down | `raises capacity of {c} to 10.` / `lowers capacity of {c} to 1.` |
| `draw` | 1 | `draws a card from their library.` / `… their crypt.` |
| `draw` | N (coalesced) | `draws 3 cards from their library.` |
| `discard` | | `discards {c}.` / `discards {c} (picked at random).` |
| `play` | face up | `plays {c}{ from their {r}}{ at {modes}}{ on {target}}{ to {poss} {r}}.` |
| `play` | face down | `plays a card face down{ from their {r}}{ on {target}}{ to {poss} {r}}.` |
| `influence` | | `influences out {c}.` / `influences out {c} (votes: 3).` |
| `move` | to region | `moves {c} to {poss} {r}.` / `… to the top of their library.` / `… face down.` |
| `move` | onto card | `moves {c} onto {target}.` / `… onto {target} face down.` |
| `move` | hidden source | `moves a card to {poss} {r}.` |
| `burn` | from ready (default) | `burns {c}.` / `burns {c} at random.` |
| `burn` | elsewhere | `burns {c} from {poss} {r}.` |
| `banish` | | `banishes {c}.` |
| `rfg` | from ash heap (default) | `removes {c} from the game.` / `… at random.` |
| `rfg` | elsewhere | `removes {c} from the game (from {poss} {r}).` |
| `lock` / `unlock` | one card | `locks {c}.` / `unlocks {c}.` |
| `unlock` | all | `unlocks.` + SYSTEM `These cards do not unlock as normal: {list}.` |
| `contest` | set / clear | `marks {poss} {c} contested.` / `clears the contest on {poss} {c}.` |
| `disc` | add only | `adds {icons} to {c}.` |
| `disc` | remove only | `removes {icons} from {c}.` |
| `disc` | both | `updates {c}: +{icons} −{icons}.` |
| `disc` | reset | `resets {c} to {icons}.` |
| `path` / `sect` / `clan` | set | `sets {c}'s path to Caine.` |
| `path` / `sect` / `clan` | clear (NONE) | `clears {c}'s path.` |
| `votes` | | `{c} now has 2 votes.` / `{c} now has 1 vote.` / `{c} now has no votes.` / `{c} is now priscus.` |
| `label` | set / remove | `labels {c}: "big str".` / `removes the label from {c}.` |
| `show` | self | `looks at 5 cards of their library.` (1 → `1 card`) |
| `show` | all | `shows everyone 5 cards of their library.` |
| `show` | some | `shows Bob, Carol 5 cards of their library.` |
| `shuffle` | | `shuffles their library.` / `shuffles the top 3 cards of their library.` |
| `order` | | `sets the seating order: Alice, Bob, Carol.` |
| `edge` | take (unheld) | `takes the edge.` |
| `edge` | take from holder | `takes the edge from Alice.` |
| `edge` | give to other | `gives the edge to Carol.` |
| `edge burn` | | `burns the edge.` |
| `open` | on / off | `now plays with an open hand.` / `no longer plays with an open hand.` |
| `random` | | `rolls 1–8: 5.` |
| `flip` | | `flips a coin: Heads.` |
| `choose` | | `makes a choice.` |
| `tally` | | SYSTEM `Choices revealed:` then SYSTEM `Alice chose 3.` |
| `hide` / `reveal` | | `turns {c} face down.` / `reveals {c}.` |
| `timeout` | request / confirm | `requests a game timeout.` / `confirms the game timeout.` |

### 10.4 CC4 — `RegionType.logLabel()`

Add a fourth constructor arg, prose lower-case, initial letter lower:

| enum | `logLabel()` |
|---|---|
| `READY` | `ready region` |
| `UNCONTROLLED` | `uncontrolled region` |
| `ASH_HEAP` | `ash heap` |
| `HAND` | `hand` |
| `LIBRARY` | `library` |
| `CRYPT` | `crypt` |
| `TORPOR` | `torpor` |
| `REMOVED_FROM_GAME` | `the removed-from-game pile` |
| `RESEARCH` | `research area` |

`xmlLabel()` stays the wire/command token; `description()` stays the UI heading
(title-case). All prose in `JolGame` switches to `logLabel()`; delete the
hard-coded `"uncontrolled region"` string in `banish` (`:924`). Rejected:
folding this into `description()` — it's used title-cased in headings.

### 10.5 CC5 — drop the redundant region

- `banish` — always omit (source ready, dest uncontrolled are both fixed).
- `burn` — omit `from … {r}` when `srcRegion == READY`; keep otherwise.
- `rfg` — omit `(from … {r})` when `srcRegion == ASH_HEAP`; keep otherwise.
- `move` — keep (destination genuinely varies).
- `draw` — keep "their library" / "their crypt" (names a real choice).
- `discard`, `influence`, `lock`/`unlock` — already omit; no change.

### 10.6 CC6 — one possessive helper

Replace every ad-hoc `sameOwner ? "their" : owner + "'s"` /
`playerTitle` / `showRegionOwner ? …` with `possessive(ownerName, actorName)`
(10.1). Call sites: `playCard`, `banish`, `burn`, `rfg`, `moveToRegion`,
`moveToCard`, `getTargetCardName`, `getCardName`, **`contestCard` (currently
missing)**, **`changePool` (currently missing)**. `shuffle` / `show` are
always self — leave as literal "their".

### 10.7 S1 — predator / prey summary on oust

In `changePool` (`:425`), capture the ousted player's neighbours **before**
`updatePredatorMapping()`, then after the remap emit one `SYSTEM` line:

- oust: `{X} is ousted — {predatorName} now preys on {preyName}.`
  (drop the clause if either neighbour is null / already ousted)
- restore (pool goes positive again): `{X} is back in — between {predatorName} and {preyName}.`

Same treatment where `withdraw` / `timeout` remove a player. Keep it to the one
line; no per-edge spam.

### 10.8 S2 — turn marker as SYSTEM

`newTurn` (`:554`) emits, after `ChatService.addTurn`:
`ChatService.sendSystemMessage(id, "Turn " + turnId + " — " + nextPlayer.getName() + ".")`
so it picks up the `chat-system` divider style. (No between-turn separator in
the flat per-turn view — that's U2, deliberately not done.)

### 10.9 S3 — no change

A contested card is out of play; turning it face down then has no gameplay
effect, so no reminder line and no contest-clear bookkeeping is needed. Closed.

### 10.10 U1 — every phase marker via SYSTEM

`setPhase` (`:586`): replace
`sendMsg(getActivePlayer(), "START OF " + phase + " PHASE.", false)` with
`ChatService.sendSystemMessage(id, "Start of " + phase.description().toLowerCase() + " phase.")`.
Audit for any other `sendMsg(getActivePlayer(), …)` machine strings and move
them too. After this, `GameChatLog`'s `isSystem` branch (`source === 'SYSTEM'`)
covers phase + turn + oust + timeout + contest + player-replace uniformly.

### 10.11 U3 — group & colour consecutive same-actor lines

Data: `GameSnapshot.seating: string[]` is already in the snapshot. Build a seat
→ colour map from a fixed N-colour palette (N = max table size; reuse the
board's seat palette so the log matches the table). Pass
`seatColors: Record<string,string>` into `GameChatPanel` → `GameChatLog`.

Render, per run of adjacent rows with the same non-SYSTEM `source` (a judge `»`
header or a SYSTEM line breaks the run):

1. **Left accent bar** — `border-left: 3px` in the actor's seat colour on every
   row of the run (SYSTEM keeps its existing `--jt-line` bar). Strongest group
   cue, survives wrapping, pairs with the U7 hanging indent.
2. **Coloured name** — tint the `<b>{source}</b>` with the seat colour.
3. **Dim the repeat** — on rows 2..n of a run, render the name at
   `opacity: 0.45` (keep it in the DOM for copy-paste / screen readers) or
   swap it for a thin spacer of the same width so the messages stay aligned.

Non-seated sources (`"Judge - X"`, `SYSTEM`) get fixed colours, not a seat
slot. Purely presentational — no `ChatData` change.

### 10.12 U5 — timestamps

- Show `HH:mm` only on each row (derive from `postedAt` when present, else
  slice the legacy `"d-MMM HH:mm"` string).
- `title={postedAt}` (full ISO) on the timestamp `<span>` for hover.
- When a row's date differs from the previous row's, emit a slim date-separator
  row ("Tue 3 Feb") before it.

### 10.13 U6 — attempt-row timestamp

Extract `<LogTimestamp iso={} legacy={} />` and use it in **both** the chat
branch and the `chat-attempt` (`⚠`) branch, so a mistyped attempt formats and
hovers exactly like a chat line (prefer `occurredAt`, fall back to
`data.timestamp`).

### 10.14 U7 — hanging indent

`p.chat` in `GamePage.css`: `padding-left` for the accent bar +
`text-indent: -<indent>` (or a flex row: fixed timestamp/name column + a
`min-width: 0` message column) so wrapped `transfer` / `disc` lines align under
the message, not the timestamp. Verify a long line in light + dark.

### 10.15 U8 — unknown discipline fallback

`MessageContent` `case 'disc'`: keep a client `DISCIPLINE_CODES` set (mirror of
`ParserService.isDiscipline`). If `seg.code` is not in it, render `[{code}]` as
plain text instead of `<span class="icon {code}">` (which is invisible when the
class is unknown). Add `title={code}` to the icon span in the known case too.

### 10.16 Owner decisions (settled)

- **CC1** — phrasing convention only, **no `ChatData.target` field`.** The 10.3
  table stands as written.
- **U3** — the log gets its **own** palette (a log-specific muted set), not the
  board's seat colours. Define it in the chat-log module; no shared-module
  extraction needed.
- **`transfer`** — trailing-bracket style, matching `blood`:
  `moves 1 blood onto {c} (now 5) (pool 18).`
