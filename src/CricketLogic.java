/**
 * Pure hand-cricket rules, extracted from {@link CricketNeo_Standard} and
 * {@link CricketNeo_Grand} so they can be shared and unit tested without
 * involving any Swing/AWT component.
 */
public final class CricketLogic {

    private CricketLogic() {
    }

    /** REAL mode only allows the boundary values a real cricket shot can score. */
    public static boolean isValidRealModeChoice(int choice) {
        return choice == 1 || choice == 2 || choice == 4 || choice == 6;
    }

    /**
     * @param crazyMode true if CRAZY (or HyperCrazy) rules are active
     * @return true if this ball is a dismissal
     */
    public static boolean isOut(boolean crazyMode, int userChoice, int compChoice) {
        if (crazyMode) {
            return Math.abs(userChoice - compChoice) == 1;
        }
        return userChoice == compChoice;
    }

    /**
     * Runs scored off a single ball that is not a dismissal.
     * In CRAZY mode an exact match between the two choices is a bonus,
     * multiplying the two numbers together instead of being an out.
     */
    public static int calculateRuns(boolean crazyMode, boolean isUserBatting, int userChoice, int compChoice) {
        if (crazyMode && userChoice == compChoice) {
            return userChoice * compChoice;
        }
        return isUserBatting ? userChoice : compChoice;
    }

    /** True if the sum of the two toss numbers is even. */
    public static boolean isTossSumEven(int userNum, int compNum) {
        return (userNum + compNum) % 2 == 0;
    }

    /**
     * @param userTossChoice "o" for odd or "e" for even
     * @return true if the user's odd/even call matches the actual parity of the sum
     */
    public static boolean userWonToss(String userTossChoice, int userNum, int compNum) {
        String actual = isTossSumEven(userNum, compNum) ? "e" : "o";
        return userTossChoice.equals(actual);
    }

    /** Outcome of applying a HyperCrazy power-up to a single ball. */
    public static final class PowerUpResult {
        public final int scoreDelta;
        public final boolean resetScore;
        public final boolean isOut;
        public final String message;

        public PowerUpResult(int scoreDelta, boolean resetScore, boolean isOut, String message) {
            this.scoreDelta = scoreDelta;
            this.resetScore = resetScore;
            this.isOut = isOut;
            this.message = message;
        }
    }

    /**
     * Applies a named HyperCrazy power-up to one ball.
     *
     * @param baseRuns        the runs the ball would score under plain scoring rules
     *                         (see {@link #calculateRuns})
     * @param luckyMultiplier the multiplier to use when {@code powerUp} is
     *                        "Lucky Multiplier" (caller supplies it so this method
     *                        stays deterministic/testable); ignored otherwise
     */
    public static PowerUpResult applyPowerUp(String powerUp, int userChoice, int compChoice,
                                              int baseRuns, int luckyMultiplier) {
        if (powerUp == null) {
            return new PowerUpResult(baseRuns, false, false, "Normal scoring.");
        }
        switch (powerUp) {
            case "Double Runs":
                return new PowerUpResult(baseRuns * 2, false, false,
                        "Double Runs! Your " + baseRuns + " doubled to " + (baseRuns * 2));

            case "Reverse Scoring":
                int mapped = 11 - userChoice;
                return new PowerUpResult(mapped, false, false,
                        "Reverse Scoring! Your " + userChoice + " counted as " + mapped);

            case "Sticky Wicket":
                if (userChoice == compChoice || Math.abs(userChoice - compChoice) == 1) {
                    return new PowerUpResult(0, false, true,
                            "Sticky Wicket! OUT because your choice matched or was close.");
                }
                return new PowerUpResult(baseRuns, false, false, "Sticky Wicket active, but you survived!");

            case "Lucky Multiplier":
                return new PowerUpResult(baseRuns * luckyMultiplier, false, false,
                        "Lucky Multiplier! Your " + baseRuns + " x " + luckyMultiplier
                                + " = " + (baseRuns * luckyMultiplier));

            case "Fusion Runs":
                return new PowerUpResult(userChoice + compChoice, false, false,
                        "Fusion Runs! Added both choices: " + userChoice + " + " + compChoice);

            case "Chaos Ball":
                if (userChoice == compChoice) {
                    return new PowerUpResult(0, true, false, "Chaos Ball! OUT wiped all your runs!");
                }
                return new PowerUpResult(baseRuns * 3, false, false,
                        "Chaos Ball! Your " + baseRuns + " tripled to " + (baseRuns * 3));

            case "Shield Mode":
                return new PowerUpResult(baseRuns / 2, false, false,
                        "Shield Mode! OUTs ignored, but runs halved.");

            default:
                return new PowerUpResult(baseRuns, false, false, "Normal scoring.");
        }
    }
}
