# Code Review & Improvements

Findings from a review of the original two-file Swing app, split into what
was fixed as part of this pass and what's left as a suggestion.

## Fixed in this pass

1. **Crash on theme toggle in `CricketNeo_Grand`.** `Real` and `HyperCrazy`
   were declared as `JButton` fields but the actual buttons were only ever
   assigned to local variables (`realBtn`, `hyperBtn`). `toggleTheme()`
   dereferenced the never-assigned fields, so clicking the 🌙/☀️ button threw
   a `NullPointerException` and effectively crashed the window's event
   handling. Fixed by assigning `Real = realBtn;` / `HyperCrazy = hyperBtn;`
   after construction.

2. **NORMAL, CRAZY, and REAL modes did nothing in `CricketNeo_Grand`.**
   `playGame` only updated score/state inside `if (isHyperCrazyMode) { ... }`.
   Picking NORMAL, CRAZY, or REAL at the mode screen led to a play screen
   where every button press just redrew "You: X | Comp: Y" — no score change,
   no dismissals, no innings transition, no way to finish a match. `isOut`,
   `calculateRuns`, and `handleOutSequence` existed but were dead code, never
   called from `playGame`. Rewrote `playGame` so the base NORMAL/CRAZY rules
   always apply (matching `CricketNeo_Standard`), with HyperCrazy layered on
   top only when it's active and a power-up has triggered. This also added
   the previously-missing "innings 2 target reached" win check that
   `CricketNeo_Standard` had and `CricketNeo_Grand` never did.

3. **`maxWickets = 0` was a confusing way to say "single wicket."** The field
   read like "zero wickets are allowed," which is misleading — the actual
   effect (first dismissal ends the innings) is identical to
   `CricketNeo_Standard`'s unconditional `handleOutSequence` call. Removed
   the redundant threshold field/checks and now route every dismissal through
   `handleOutSequence`, same as Standard.

4. **~120 lines of duplicated rules logic** (`isOut`, `calculateRuns`, toss
   parity/winner logic) existed almost verbatim in both classes. Extracted
   into a new dependency-free `CricketLogic` class; both UI classes now
   delegate to it. This is also what made the code unit-testable at all,
   since instantiating either `JFrame` subclass directly requires a display
   (see "Tests," below).

5. **Dead code removed**: `endInnings()` (never called) and an unused
   `activePowerUps` list field (the singular `activePowerUp` was the one
   actually used) in `CricketNeo_Grand`.

6. **No tests existed.** Added `test/CricketLogicTest.java` — 46 assertions
   over dismissal rules, scoring, toss parity, and all seven HyperCrazy
   power-ups (including the two that can end a ball as a dismissal or a
   score-wipe). See the README for how to run it.

## Suggested, not done (would change scope/behavior further)

- **No build tool.** The project is a single IntelliJ module with `src` as
  a raw source root — no Maven/Gradle, no dependency manager, no CI. That's
  why the new tests are dependency-free plain Java rather than JUnit: there
  was no way to pull in a test framework without first deciding on (and
  verifying) a build tool in this environment. If you adopt Maven or Gradle,
  switching `CricketLogicTest` to JUnit 5 and wiring a GitHub Actions job to
  run `javac`/tests on push would be natural follow-ups.
- **No package declarations.** Every class lives in the default package,
  which most tooling (and Maven, in particular) discourages.
- **`CricketNeo_Standard` and `CricketNeo_Grand` are still ~85% identical
  UI code** (layout, theming, toss flow, help dialog, exit confirmation).
  Grand is a strict superset of Standard's behavior. A further refactor
  could make `CricketNeo_Grand` extend `CricketNeo_Standard` (or extract a
  shared `CricketNeoFrame` base) and add only the REAL/HyperCrazy-specific
  UI, rather than maintaining two near-duplicate ~400-600 line files.
- **Layout is entirely absolute positioning** (`setLayout(null)` +
  `setBounds(...)` everywhere), and the window is fixed-size and
  non-resizable. This is brittle (any font/DPI difference shifts things) and
  makes the UI code harder to read. A `GridBagLayout`/`MigLayout`-based
  rebuild would be more maintainable, at the cost of a full UI rewrite.
- **Random is unseeded and not injectable**, so full game-flow behavior
  (toss outcomes, computer's per-ball number, which power-up triggers) can't
  be deterministically tested end-to-end — only the pure rules in
  `CricketLogic` are unit tested. Threading a `Random` (or a seed) through
  the constructors would let a future test drive a whole match
  deterministically.
- **Emoji-heavy UI text** (🌙 ☀️ 🏆 😒 🤝) depends on the platform having a
  font with those glyphs; worth a fallback or at least awareness when
  packaging the launch4j `.exe` for Windows users who may lack emoji fonts.
- **Grand's `showFinalPopup`/Super Over** uses `frame` (an always-`null`
  field distinct from `this`) as the dialog parent, and Super Over scoring
  (`simulateBall`) ignores the mode entirely (always 0-6 uniform runs,
  independent of NORMAL/CRAZY/REAL/HyperCrazy). Both are pre-existing
  behaviors, left as-is since changing them changes gameplay rather than
  fixing a defect.
