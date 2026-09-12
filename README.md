# CricketNeo

Want to play cricket? But don't have the space or equipment to play it? Well,
this app solves your problem — a simple game that lets you play cricket on
your computer!

A desktop "hand cricket" game written in Java Swing. Two players — you and the
computer — each pick a number from 1 to 10 per "ball"; matching (or, in CRAZY
mode, near-matching) numbers gets you out. Two runnable variants ship in this
repo:

| Class                  | What it is                                                             |
|------------------------|-------------------------------------------------------------------------|
| `CricketNeo_Standard`  | The base game: toss, NORMAL/CRAZY modes, batting/bowling, two innings. |
| `CricketNeo_Grand`     | Everything in Standard, plus a REAL mode (boundaries only: 1/2/4/6) and a HyperCrazy mode with randomly-triggered power-ups. |

Both are self-contained `JFrame` apps with no external dependencies.

## Features

- **Toss**: call odd/even, then both sides pick a number; the parity of the
  sum decides the toss winner.
- **NORMAL mode**: out on an exact number match; otherwise you score the
  batter's number.
- **CRAZY mode**: out only when the two numbers differ by exactly 1; an exact
  match is instead a bonus that multiplies the two numbers together.
- **REAL mode** (`CricketNeo_Grand` only): restricts input to 1, 2, 4, or 6,
  mimicking real cricket scoring shots.
- **HyperCrazy mode** (`CricketNeo_Grand` only): every 5-10 balls a random
  power-up activates for 5-7 balls: Double Runs, Reverse Scoring, Sticky
  Wicket, Lucky Multiplier, Fusion Runs, Chaos Ball, Shield Mode. See
  [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md) for exact rules.
- Two innings with a target once the first innings ends, a light/dark theme
  toggle, an in-game help dialog, and an optional Super Over to break a tie.

Full game-flow and rules are documented in
[`docs/SPECIFICATION.md`](docs/SPECIFICATION.md). Known issues, and the
reasoning behind the recent refactor, are in
[`docs/IMPROVEMENTS.md`](docs/IMPROVEMENTS.md).

## Project layout

```
src/
  CricketNeo_Standard.java   Standard game UI + flow
  CricketNeo_Grand.java      Grand game UI + flow (REAL / HyperCrazy)
  CricketLogic.java          Shared, UI-free rules (scoring, dismissals, toss, power-ups)
  META-INF/MANIFEST.MF       Manifest used by the IntelliJ jar artifact (Main-Class: CricketNeo_Standard)
  resources/CricketNeo.png   Window icon
test/
  CricketLogicTest.java      Dependency-free unit tests for CricketLogic
docs/
  SPECIFICATION.md           Game rules and screen/state flow
  IMPROVEMENTS.md            Code review notes and suggested next steps
```

There is no Maven/Gradle build — this is a single IntelliJ module
(`CricketNeo.iml`) with `src` as its only source root, matching how the
project was originally set up.

## Running the game

**From IntelliJ IDEA:** open the folder, let it index, then run
`CricketNeo_Standard` or `CricketNeo_Grand` (each has a `main` method).

**From the command line** (requires a JDK; developed against JDK 17+):

```bash
javac -d out/production/CricketNeo src/CricketNeo_Standard.java src/CricketNeo_Grand.java src/CricketLogic.java
cp -r src/resources src/META-INF out/production/CricketNeo/
java -cp out/production/CricketNeo CricketNeo_Standard
# or: java -cp out/production/CricketNeo CricketNeo_Grand
```

(`out/` is git-ignored; IntelliJ manages its own copy of this same layout,
plus a jar + launch4j `.exe` artifact under `out/artifacts/`.)

## Building the Windows .exe

`CricketNeo_Standard` can be wrapped into a standalone `CricketNeo_Standard.exe`
(via [Launch4j](https://launch4j.sourceforge.net/)) that bundles its own Java
runtime, so it runs on a machine with no JDK installed. The tracked config is
[`packaging/launch4j/CricketNeo_Standard.xml`](packaging/launch4j/CricketNeo_Standard.xml)
and its icon is [`src/resources/CricketNeo.ico`](src/resources/CricketNeo.ico).

1. **Build the jar** (Main-Class comes from `src/META-INF/MANIFEST.MF`):
   ```bash
   javac -d out/artifacts/CricketNeo_jar/build src/CricketNeo_Standard.java src/CricketLogic.java
   cp -r src/resources out/artifacts/CricketNeo_jar/build/
   jar --create --file out/artifacts/CricketNeo_jar/CricketNeo_Standard.jar --main-class CricketNeo_Standard -C out/artifacts/CricketNeo_jar/build .
   ```
2. **Bundle a runtime**, if `out/artifacts/CricketNeo_jar/custom-jre` doesn't already
   exist (a `jlink` image, so it only needs to be built once per JDK version):
   ```bash
   jlink --module-path "$JAVA_HOME/jmods" --add-modules java.base,java.datatransfer,java.xml,java.prefs,java.desktop --output out/artifacts/CricketNeo_jar/custom-jre --no-header-files --no-man-pages
   ```
3. **Wrap it into an .exe** with Launch4j's console builder (`launch4jc.exe`,
   installed separately — [download](https://launch4j.sourceforge.net/)):
   ```bash
   launch4jc packaging/launch4j/CricketNeo_Standard.xml
   ```

The result is self-contained: `CricketNeo_Standard.exe` needs
`CricketNeo_Standard.jar` and `custom-jre/` alongside it (all three live under
`out/artifacts/CricketNeo_jar/`), but nothing else.

## Running the tests

The project has no build tool, so tests are plain Java with no external
dependencies — compile and run them directly:

```bash
javac -d out-test src/CricketLogic.java test/CricketLogicTest.java
java -cp out-test CricketLogicTest
```

This prints a PASS/FAIL line per case and exits non-zero if anything fails.
