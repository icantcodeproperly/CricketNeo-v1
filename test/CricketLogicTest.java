/**
 * Self-contained unit tests for {@link CricketLogic}.
 *
 * The project has no build tool (no Maven/Gradle), so this suite intentionally
 * has zero external dependencies: compile and run with plain javac/java.
 *
 *   javac -d out-test src/CricketLogic.java test/CricketLogicTest.java
 *   java -cp out-test CricketLogicTest
 *
 * Exits with status 0 if every assertion passes, 1 otherwise.
 */
public class CricketLogicTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testIsOutNormalMode();
        testIsOutCrazyMode();
        testCalculateRunsNormalMode();
        testCalculateRunsCrazyModeBonus();
        testIsValidRealModeChoice();
        testTossSumParity();
        testUserWonToss();
        testPowerUpDoubleRuns();
        testPowerUpReverseScoring();
        testPowerUpStickyWicketSurvives();
        testPowerUpStickyWicketOutOnMatch();
        testPowerUpStickyWicketOutOnAdjacent();
        testPowerUpLuckyMultiplier();
        testPowerUpFusionRuns();
        testPowerUpChaosBallOut();
        testPowerUpChaosBallTriple();
        testPowerUpShieldMode();
        testPowerUpUnknownFallsBackToBaseRuns();
        testPowerUpNullFallsBackToBaseRuns();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testIsOutNormalMode() {
        check("normal mode: equal choices is out", CricketLogic.isOut(false, 4, 4));
        check("normal mode: unequal choices is not out", !CricketLogic.isOut(false, 4, 5));
        check("normal mode: adjacent-but-unequal is not out", !CricketLogic.isOut(false, 4, 3));
    }

    private static void testIsOutCrazyMode() {
        check("crazy mode: difference of 1 is out", CricketLogic.isOut(true, 4, 5));
        check("crazy mode: difference of 1 (reversed) is out", CricketLogic.isOut(true, 5, 4));
        check("crazy mode: exact match is NOT out (it's a bonus)", !CricketLogic.isOut(true, 4, 4));
        check("crazy mode: difference of 2+ is not out", !CricketLogic.isOut(true, 4, 6));
    }

    private static void testCalculateRunsNormalMode() {
        check("normal mode, batting: runs = user's choice",
                CricketLogic.calculateRuns(false, true, 6, 3) == 6);
        check("normal mode, bowling: runs = computer's choice",
                CricketLogic.calculateRuns(false, false, 6, 3) == 3);
    }

    private static void testCalculateRunsCrazyModeBonus() {
        check("crazy mode: exact match multiplies the two numbers",
                CricketLogic.calculateRuns(true, true, 4, 4) == 16);
        check("crazy mode: non-match while batting behaves like normal mode",
                CricketLogic.calculateRuns(true, true, 6, 2) == 6);
        check("crazy mode: non-match while bowling behaves like normal mode",
                CricketLogic.calculateRuns(true, false, 6, 2) == 2);
    }

    private static void testIsValidRealModeChoice() {
        for (int valid : new int[]{1, 2, 4, 6}) {
            check("REAL mode allows " + valid, CricketLogic.isValidRealModeChoice(valid));
        }
        for (int invalid : new int[]{3, 5, 7, 8, 9, 10}) {
            check("REAL mode rejects " + invalid, !CricketLogic.isValidRealModeChoice(invalid));
        }
    }

    private static void testTossSumParity() {
        check("3 + 4 = 7 is odd", !CricketLogic.isTossSumEven(3, 4));
        check("3 + 5 = 8 is even", CricketLogic.isTossSumEven(3, 5));
    }

    private static void testUserWonToss() {
        check("caller 'e' wins when sum is even", CricketLogic.userWonToss("e", 3, 5));
        check("caller 'o' loses when sum is even", !CricketLogic.userWonToss("o", 3, 5));
        check("caller 'o' wins when sum is odd", CricketLogic.userWonToss("o", 3, 4));
        check("caller 'e' loses when sum is odd", !CricketLogic.userWonToss("e", 3, 4));
    }

    private static void testPowerUpDoubleRuns() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Double Runs", 4, 7, 4, 0);
        check("Double Runs doubles the base runs", r.scoreDelta == 8);
        check("Double Runs is never a dismissal", !r.isOut);
        check("Double Runs never wipes the score", !r.resetScore);
    }

    private static void testPowerUpReverseScoring() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Reverse Scoring", 4, 7, 4, 0);
        check("Reverse Scoring maps choice to 11 - choice", r.scoreDelta == 7);
    }

    private static void testPowerUpStickyWicketSurvives() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Sticky Wicket", 2, 7, 2, 0);
        check("Sticky Wicket: far-apart choices survive", !r.isOut);
        check("Sticky Wicket: surviving ball scores base runs", r.scoreDelta == 2);
    }

    private static void testPowerUpStickyWicketOutOnMatch() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Sticky Wicket", 5, 5, 0, 0);
        check("Sticky Wicket: exact match is a dismissal", r.isOut);
    }

    private static void testPowerUpStickyWicketOutOnAdjacent() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Sticky Wicket", 5, 6, 5, 0);
        check("Sticky Wicket: adjacent choices are a dismissal", r.isOut);
    }

    private static void testPowerUpLuckyMultiplier() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Lucky Multiplier", 4, 9, 4, -3);
        check("Lucky Multiplier applies the supplied (possibly negative) multiplier",
                r.scoreDelta == -12);
        CricketLogic.PowerUpResult zero = CricketLogic.applyPowerUp("Lucky Multiplier", 4, 9, 4, 0);
        check("Lucky Multiplier can zero out the runs", zero.scoreDelta == 0);
    }

    private static void testPowerUpFusionRuns() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Fusion Runs", 4, 7, 4, 0);
        check("Fusion Runs adds both choices together", r.scoreDelta == 11);
    }

    private static void testPowerUpChaosBallOut() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Chaos Ball", 5, 5, 0, 0);
        check("Chaos Ball on an exact match wipes the score instead of dismissing",
                r.resetScore && !r.isOut);
    }

    private static void testPowerUpChaosBallTriple() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Chaos Ball", 4, 9, 4, 0);
        check("Chaos Ball triples the base runs on a non-match", r.scoreDelta == 12);
        check("Chaos Ball non-match does not wipe the score", !r.resetScore);
    }

    private static void testPowerUpShieldMode() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Shield Mode", 5, 9, 5, 0);
        check("Shield Mode halves the base runs (integer division)", r.scoreDelta == 2);
    }

    private static void testPowerUpUnknownFallsBackToBaseRuns() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp("Not A Real Power-Up", 4, 9, 4, 0);
        check("Unknown power-up name scores the base runs unchanged", r.scoreDelta == 4);
        check("Unknown power-up name is never a dismissal", !r.isOut);
    }

    private static void testPowerUpNullFallsBackToBaseRuns() {
        CricketLogic.PowerUpResult r = CricketLogic.applyPowerUp(null, 4, 9, 4, 0);
        check("null power-up scores the base runs unchanged", r.scoreDelta == 4);
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + description);
        } else {
            failed++;
            System.out.println("  FAIL  " + description);
        }
    }
}
