# CricketNeo — Game Specification

This describes observable behavior of both `CricketNeo_Standard` and
`CricketNeo_Grand`, as implemented. Differences between the two are called
out explicitly; anything not called out applies to both.

## 1. Screen flow

Both classes drive a `CardLayout` with the same named cards:

```
TOSS ──(pick ODD/EVEN)──> PLAY (toss numbers)
  │                            │
  │                    user picks a number 1-10
  │                            │
  │                 ┌──────────┴───────────┐
  │            user wins toss         comp wins toss
  │                 │                       │
  │                 v                       v
  │              MODE                 (popup) → PLAY (match)
  │                 │
  │        user picks a mode
  │                 │
  │                 v
  │             CHOICE (bat or bowl)
  │                 │
  │                 v
  │            PLAY (match) <──────────────┘
  │                 │
  │      innings 1 ends on dismissal (see §3)
  │                 │
  │            PLAY (innings 2)
  │                 │
  │   ends on dismissal, or (batting side) reaching target
  │                 │
  │                 v
  │               END  →  RESULTS! / RESTART / EXIT
  v
TOSS (after RESTART)
```

- **Toss**: the user calls ODD or EVEN, then both the user and the computer
  pick a number from 1-10. If `(userNumber + compNumber)` is even the "EVEN"
  call wins, otherwise "ODD" wins.
  - If the **user** wins the toss, they proceed to the **MODE** screen to
    choose NORMAL/CRAZY (Standard), or NORMAL/CRAZY/REAL/HYPERCRAZY (Grand),
    then the **CHOICE** screen to bat or bowl first.
  - If the **computer** wins the toss, the mode (NORMAL or CRAZY — the
    computer never picks REAL or HyperCrazy) and the bat/bowl choice are both
    randomized, shown in a dialog, and play starts immediately.
- **Play (match)**: 10 number buttons (1-10). Each click is one "ball".
- **End**: shown once an innings-2 dismissal or target is reached. Offers
  RESULTS! (re-shows the outcome popup), RESTART (back to TOSS, scores
  zeroed), EXIT (confirmation dialog, then quits the process).

## 2. Modes

| Mode        | Available in         | Dismissal rule                    | Scoring rule |
|-------------|-----------------------|------------------------------------|--------------|
| NORMAL      | Standard, Grand        | `userChoice == compChoice`         | Batter's own picked number |
| CRAZY       | Standard, Grand        | `abs(userChoice - compChoice) == 1`| On exact match: `userChoice * compChoice` (bonus, **not** an out); otherwise the batter's picked number |
| REAL        | Grand only              | Same as NORMAL                     | Same as NORMAL, but only 1, 2, 4, or 6 are accepted as input — anything else is rejected with an inline error and does not consume a ball |
| HYPERCRAZY  | Grand only              | Depends on the active power-up (see §4); NORMAL rules apply when no power-up is active | Depends on the active power-up |

"Batter's picked number" means: whichever side is currently batting, the runs
scored are that side's chosen number for the ball (the user's number while
the user bats, the computer's random number while the computer bats).

When the **computer** wins the toss, the mode for that match is randomized
between NORMAL and CRAZY (50/50); REAL and HYPERCRAZY are only ever chosen by
the user.

## 3. Innings, target, and results

- Each match is (at most) two innings, single-wicket: **one dismissal ends
  the current side's innings** immediately.
- **Innings 1** ends on a dismissal. The side's final score becomes
  `target = score + 1` for innings 2, and batting/bowling roles swap.
- **Innings 2** ends when the batting side is dismissed, or as soon as their
  score reaches `target` (checked after every non-dismissal ball).
- **Result**: higher total wins. A tie prompts the user to either play a
  **Super Over** or accept the draw.
  - In `CricketNeo_Standard`, a tie is simply reported as a draw (no Super
    Over prompt).
  - In `CricketNeo_Grand`, accepting the Super Over runs
    `startSuperOver()`: 6 random balls (0-6 runs each, uniformly) for "You"
    and 6 for "Computer", independently of the mode just played, and reports
    a winner or another tie via dialogs.

## 4. HyperCrazy power-ups (`CricketNeo_Grand` only)

Selecting HYPERCRAZY as the mode arms a random-interval trigger: every
5-10 balls, a random power-up activates for 5-7 balls (both ranges rerolled
each time). While a power-up is active it fully replaces normal scoring for
that at-bat side; when it expires, scoring reverts to NORMAL rules for that
side until the next trigger. Bowling by the computer during HyperCrazy still
uses whichever power-up is active, applied from the perspective of whoever is
currently batting.

| Power-up          | Effect |
|--------------------|--------|
| Double Runs        | Base runs (NORMAL-mode runs for the ball) are doubled. |
| Reverse Scoring     | Runs = `11 - userChoice`, regardless of the computer's number. |
| Sticky Wicket       | Dismissal if the two numbers match **or** differ by exactly 1; otherwise scores base runs. |
| Lucky Multiplier    | Base runs are multiplied by a random value from `{-3, -2, -1, 0, 1, 2, 3}` (can reduce the score). |
| Fusion Runs         | Runs = `userChoice + compChoice`. |
| Chaos Ball          | On an exact number match, the batting side's **entire score is wiped to 0** (not a dismissal — the innings continues at 0). Otherwise, base runs are tripled. |
| Shield Mode         | Base runs are halved (integer division); "OUTs ignored" per the in-game message — an exact match under Shield Mode is not evaluated as a dismissal. |

"Base runs" above means the plain NORMAL-mode runs for that ball
(`calculateRuns` with `crazyMode = false`), independent of whichever mode
(NORMAL/CRAZY/REAL) was originally selected before HyperCrazy activated.

The active power-up and its remaining ball count are shown in a banner above
the play buttons; a context line under the score explains the outcome of the
most recent ball.

## 5. Theming, help, exit

- A moon/sun button in the top-right toggles a light/dark theme, restyling
  the header, labels, and every game button (including, in Grand, the REAL
  and HYPERCRAZY mode buttons).
- A `?` button shows a static instructions dialog (toss → mode → bat/bowl,
  NORMAL vs CRAZY rules). It does not mention REAL or HyperCrazy.
- Closing the window or pressing EXIT asks "Did you like it?" (Yes/No/Cancel)
  before calling `System.exit(0)`; Cancel returns to the game.
