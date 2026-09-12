import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.event.*;

public class CricketNeo_Grand extends JFrame {
    private int userScore = 0, compScore = 0, target = -1;
    private boolean isUserBatting, isFirstInnings = true, isCrazyMode = false, isRealMode = false, isHyperCrazyMode = false;
    private String userTossChoice;
    private Random rand = new Random();

    // GUI Components
    private JLabel lblHeader, lblStatus, lblMainDisplay, lblMiniScore, lblPowerUpBanner;
    private JButton btnTheme, btnHelp, Real, HyperCrazy;
    private JPanel cardPanel, tossPanel, modePanel, choicePanel, playPanel, endPanel;
    private CardLayout cl = new CardLayout();

    private List<JButton> gameButtons = new ArrayList<>();

    private Color darkBG = new Color(20, 20, 20);
    private Color lightBG = Color.WHITE;

    private JFrame frame;

    private int ballCounter = 0;
    private int nextTrigger = 0;
    private List<String> activePowerUps = new ArrayList<>();

    // HyperCrazy state
    private String activePowerUp = null;
    private int powerUpDuration = 0;

    private final String[] POWERUPS = {
            "Double Runs",
            "Reverse Scoring",
            "Sticky Wicket",
            "Lucky Multiplier",
            "Fusion Runs",
            "Chaos Ball",
            "Shield Mode"
    };

    // Normal cricket state
    private int wicketsLost = 0;
    private int ballsBowled = 0;
    private final int maxWickets = 0;

    private JLabel lblContext;

    public CricketNeo_Grand() {
        setTitle("CricketNeo - Hand Cricket Pro");
        setSize(750, 650);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        setLayout(null);
        getContentPane().setBackground(darkBG);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(
                new ImageIcon(getClass().getResource("resources/CricketNeo.png")).getImage()
        );

        // --- TOP UTILITY BUTTONS ---
        int btnSize = 49;
        int gap = 5;
        int rightMargin = 20;

        btnTheme = new JButton("🌙"); // FIX: dark mode starts with moon
        btnTheme.setBounds(750 - rightMargin - btnSize, 20, btnSize, 30);
        styleUtilityButton(btnTheme);
        btnTheme.addActionListener(e -> toggleTheme());
        add(btnTheme);

        btnHelp = new JButton("?");
        btnHelp.setBounds(750 - rightMargin - (2 * btnSize) - gap, 20, btnSize, 30);
        styleUtilityButton(btnHelp);
        btnHelp.addActionListener(e -> showInstructions());
        add(btnHelp);

        // --- HEADER & SCORE ---
        lblHeader = new JLabel("CRICKET NEO", SwingConstants.CENTER);
        lblHeader.setBounds(225, 10, 300, 40);
        lblHeader.setForeground(Color.CYAN);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        add(lblHeader);

        lblMiniScore = new JLabel("Score: 0 - 0", SwingConstants.CENTER);
        lblMiniScore.setBounds(225, 50, 300, 30);
        lblMiniScore.setForeground(Color.WHITE);
        lblMiniScore.setFont(new Font("Monospaced", Font.BOLD, 22));
        add(lblMiniScore);

        lblStatus = new JLabel("WELCOME PLAYER", SwingConstants.CENTER);
        lblStatus.setBounds(100, 110, 550, 30);
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(lblStatus);

        lblMainDisplay = new JLabel("<html><center>TIME FOR THE TOSS!<br>Choose ODD or EVEN below to start.</center></html>", SwingConstants.CENTER);
        lblMainDisplay.setBounds(50, 150, 650, 150);
        lblMainDisplay.setForeground(Color.YELLOW);
        lblMainDisplay.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblMainDisplay);

        // Compact banner for active power‑up
        lblPowerUpBanner = new JLabel("Active Power‑Up: None", SwingConstants.CENTER);
        lblPowerUpBanner.setBounds(100, 250, 550, 30);
        lblPowerUpBanner.setForeground(Color.MAGENTA);
        lblPowerUpBanner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPowerUpBanner.setVisible(false);
        add(lblPowerUpBanner);

        // Context line
        lblContext = new JLabel("Context: ---", SwingConstants.CENTER);
        lblContext.setBounds(100, 280, 550, 30);
        lblContext.setForeground(Color.ORANGE);
        lblContext.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblContext.setVisible(false);
        add(lblContext);

        // --- BUTTON CARD PANEL ---
        cardPanel = new JPanel(cl);
        cardPanel.setBounds(50, 350, 650, 200);
        cardPanel.setOpaque(false);

        tossPanel = createContainerPanel();
        addGameButton(tossPanel, "ODD", 180, 50, 120, 50, e -> startToss("o"));
        addGameButton(tossPanel, "EVEN", 340, 50, 120, 50, e -> startToss("e"));

        playPanel = createContainerPanel();
        for (int i = 0; i < 10; i++) {
            int val = i + 1;
            int x = (i % 5) * 125 + 15;
            int y = (i / 5) * 70 + 20;
            addGameButton(playPanel, String.valueOf(val), x, y, 100, 50, e -> handleInput(val));
        }

        modePanel = createContainerPanel();
        // Top row
        addGameButton(modePanel, "NORMAL", 180, 20, 120, 50, e -> { isCrazyMode = false; setupChoice(); });
        addGameButton(modePanel, "CRAZY", 340, 20, 120, 50, e -> { isCrazyMode = true; isRealMode = false; isHyperCrazyMode = false; setupChoice(); });
        // Bottom row
        JButton realBtn = new JButton("REAL");
        realBtn.setBounds(180, 100, 120, 50);
        realBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        realBtn.setBackground(new Color(182, 127, 241)); // purple
        realBtn.setForeground(Color.BLACK);
        realBtn.setFocusPainted(false);
        realBtn.addActionListener(e -> {
            isCrazyMode = false;
            isRealMode = true;
            isHyperCrazyMode = false;
            setupChoice();
        });
        modePanel.add(realBtn);

        JButton hyperBtn = new JButton("HYPERCRAZY");
        hyperBtn.setBounds(340, 100, 160, 50);
        hyperBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        hyperBtn.setBackground(new Color(182, 127, 241)); // purple
        hyperBtn.setForeground(Color.BLACK);
        hyperBtn.setFocusPainted(false);
        hyperBtn.addActionListener(e -> {
            isCrazyMode = false;
            isRealMode = false;
            isHyperCrazyMode = true;
            setupChoice();
            HyperCrazy();
        });
        modePanel.add(hyperBtn);

        choicePanel = createContainerPanel();
        addGameButton(choicePanel, "BATTING", 150, 50, 160, 60, e -> startMatch(true));
        addGameButton(choicePanel, "BOWLING", 330, 50, 160, 60, e -> startMatch(false));

        endPanel = createContainerPanel();
        addGameButton(endPanel, "RESULTS!", 100, 50, 140, 55, e -> showFinalPopup());
        addGameButton(endPanel, "RESTART", 260, 50, 140, 55, e -> restartGame());
        addGameButton(endPanel, "EXIT", 420, 50, 140, 55, e -> confirmExit());

        cardPanel.add(tossPanel, "TOSS");
        cardPanel.add(playPanel, "PLAY");
        cardPanel.add(modePanel, "MODE");
        cardPanel.add(choicePanel, "CHOICE");
        cardPanel.add(endPanel, "END");

        add(cardPanel);
        cl.show(cardPanel, "TOSS");
        setVisible(true);
    }

    private JPanel createContainerPanel() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        return p;
    }

    private void addGameButton(JPanel panel, String text, int x, int y, int w, int h, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, h);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(new Color(125, 236, 70));
        b.setForeground(Color.DARK_GRAY);
        b.setFocusPainted(false);
        b.addActionListener(al);
        gameButtons.add(b);
        panel.add(b);

    }

    private void styleUtilityButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(182, 127, 241));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
    }

    private void toggleTheme() {
        boolean isDark = getContentPane().getBackground().equals(darkBG);
        if (isDark) {
            getContentPane().setBackground(lightBG);
            lblMiniScore.setForeground(Color.BLACK);
            lblStatus.setForeground(Color.DARK_GRAY);
            lblHeader.setForeground(new Color(25, 100, 175));
            lblMainDisplay.setForeground(new Color(221, 173, 16));
            btnTheme.setText("🌙");
            btnTheme.setBackground(new Color(83, 13, 227));
            btnTheme.setForeground(Color.WHITE);
            btnHelp.setBackground(new Color(83, 13, 227));
            btnHelp.setForeground(Color.WHITE);
            Real.setBackground(new Color(83, 13, 227));
            Real.setForeground(Color.WHITE);
            HyperCrazy.setBackground(new Color(83, 13, 227));
            HyperCrazy.setForeground(Color.WHITE);
            for (JButton b : gameButtons) {
                b.setBackground(new Color(15, 168, 10));
                b.setForeground(Color.WHITE);
            }
        } else {
            getContentPane().setBackground(darkBG);
            lblMiniScore.setForeground(Color.WHITE);
            lblStatus.setForeground(Color.GRAY);
            lblHeader.setForeground(Color.CYAN);
            lblMainDisplay.setForeground(Color.YELLOW);
            btnTheme.setText("☀️");
            btnTheme.setBackground(new Color(182, 127, 241));
            btnTheme.setForeground(Color.BLACK);
            btnHelp.setBackground(new Color(182, 127, 241));
            btnHelp.setForeground(Color.BLACK);
            Real.setBackground(new Color(182, 127, 241));
            Real.setForeground(Color.BLACK);
            HyperCrazy.setBackground(new Color(182, 127, 241));
            HyperCrazy.setForeground(Color.BLACK);
            for (JButton b : gameButtons) {
                b.setBackground(new Color(125, 236, 70));
                b.setForeground(Color.DARK_GRAY);
            }
        }
    }

    private void startToss(String choice) {
        userTossChoice = choice;
        cl.show(cardPanel, "PLAY");
        lblStatus.setText("TOSS STEP 2");
        lblMainDisplay.setText("<html><center>You chose " + (choice.equals("o") ? "ODD" : "EVEN") + ".<br>Pick a number for the toss!</center></html>");
    }

    private void handleInput(int val) {
        if (isRealMode && !(val == 1 || val == 2 || val == 4 || val == 6)) {
            lblMainDisplay.setText("<html><center><font color='red'>Invalid choice in REAL mode!<br>Pick 1, 2, 4, or 6 only.</font></center></html>");
            return;
        }

        if (userTossChoice != null) handleTossLogic(val);
        else playGame(val);
    }

    private void handleTossLogic(int userNum) {
        int compNum = rand.nextInt(10) + 1;
        boolean isEven = (userNum + compNum) % 2 == 0;
        String result = isEven ? "e" : "o";
        String numbersPicked = "You chose: " + userNum + "<br>Comp chose: " + compNum;

        if (userTossChoice.equals(result)) {
            lblMainDisplay.setText("<html><center>" + numbersPicked
                    + "<br><font color='orange'>YOU WON THE TOSS!</font><br>Select Game Mode Below.</center></html>");
            cl.show(cardPanel, "MODE");
        } else {
            isCrazyMode = rand.nextBoolean();
            boolean compChoiceToBat = rand.nextBoolean();
            isUserBatting = !compChoiceToBat; // If Comp bats, User bowls.

            String tossLossMsg = "You chose: " + userNum + "\n"
                    + "Comp chose: " + compNum + "\n"
                    + "The sum was " + (userNum + compNum) + " (" + (isEven ? "EVEN" : "ODD") + ")\n\n"
                    + "COMP WON THE TOSS!\n"
                    + "Mode: " + (isCrazyMode ? "CRAZY" : "NORMAL") + "\n"
                    + "Comp chose to: " + (compChoiceToBat ? "BAT" : "BOWL");

            JOptionPane.showMessageDialog(this, tossLossMsg, "Toss Result", JOptionPane.INFORMATION_MESSAGE);
            startMatch(isUserBatting);
        }
        // FIX: reset only after toss is fully processed
        userTossChoice = null;
    }

    private void showPowerUpPopup(String powerUpName, int duration) {
        String message = "<html><center>" +
                "🎉 <b><font color='purple'>Power‑Up Activated!</font></b><br>" +
                "<font color='blue'>" + powerUpName + "</font><br>" +
                "Lasts for <b>" + duration + " balls</b>" +
                "</center></html>";

        JOptionPane.showMessageDialog(null, message,
                "HyperCrazy Power‑Up", JOptionPane.INFORMATION_MESSAGE);
    }

    public void HyperCrazy() {
        isHyperCrazyMode = true;
        ballCounter = 0;
        nextTrigger = 5 + (int)(Math.random() * 6); // 5–10 balls
        activePowerUp = null;
        powerUpDuration = 0;
        lblPowerUpBanner.setVisible(true);
        lblContext.setVisible(true);
    }

    public void playGame(int userChoice) {
        int compChoice = rand.nextInt(10) + 1;
        int runs = (userChoice == compChoice) ? 0 : userChoice;
        String contextMessage = "";

        if (isHyperCrazyMode) {
            ballCounter++;

            if (ballCounter >= nextTrigger) {
                int index = (int)(Math.random() * POWERUPS.length);
                activePowerUp = POWERUPS[index];
                powerUpDuration = 5 + (int)(Math.random() * 3);

                showPowerUpPopup(activePowerUp, powerUpDuration);

                ballCounter = 0;
                nextTrigger = 5 + (int)(Math.random() * 6);
            }

            if (activePowerUp != null && powerUpDuration > 0) {
                switch (activePowerUp) {
                    case "Double Runs":
                        userScore += runs * 2;
                        contextMessage = "Double Runs! Your " + runs + " doubled to " + (runs * 2);
                        break;

                    case "Reverse Scoring":
                        int mapped = 11 - userChoice;
                        userScore += mapped;
                        contextMessage = "Reverse Scoring! Your " + userChoice + " counted as " + mapped;
                        break;

                    case "Sticky Wicket":
                        if (userChoice == compChoice || Math.abs(userChoice - compChoice) == 1) {
                            contextMessage = "Sticky Wicket! OUT because your choice matched or was close.";
                            wicketsLost++;
                            if (wicketsLost >= maxWickets) {
                                lblMainDisplay.setText("All wickets lost! Game Over.");
                                return;
                            }
                        } else {
                            userScore += runs;
                            contextMessage = "Sticky Wicket active, but you survived!";
                        }
                        break;

                    case "Lucky Multiplier":
                        int[] multipliers = {-3, -2, -1, 0, 1, 2, 3};
                        int multiplier = multipliers[(int)(Math.random() * multipliers.length)];
                        userScore += runs * multiplier;
                        contextMessage = "Lucky Multiplier! Your " + runs + " × " + multiplier + " = " + (runs * multiplier);
                        break;

                    case "Fusion Runs":
                        userScore += userChoice + compChoice;
                        contextMessage = "Fusion Runs! Added both choices: " + userChoice + " + " + compChoice;
                        break;

                    case "Chaos Ball":
                        if (userChoice == compChoice) {
                            userScore = 0;
                            contextMessage = "Chaos Ball! OUT wiped all your runs!";
                        } else {
                            userScore += runs * 3;
                            contextMessage = "Chaos Ball! Your " + runs + " tripled to " + (runs * 3);
                        }
                        break;

                    case "Shield Mode":
                        userScore += runs / 2;
                        contextMessage = "Shield Mode! OUTs ignored, but runs halved.";
                        break;

                    default:
                        userScore += runs;
                        contextMessage = "Normal scoring.";
                }
                powerUpDuration--;
                if (powerUpDuration == 0) activePowerUp = null;

            } else if (userChoice == compChoice) {
                if (userChoice == compChoice) {
                    contextMessage = "OUT!";
                    wicketsLost++;
                    if (wicketsLost >= maxWickets) {
                        lblMainDisplay.setText("All wickets lost! Game Over.");
                        return;
                    }
                } else {
                    userScore += runs;
                    contextMessage = "Normal scoring.";
                }
            } else{
                userScore += runs;
                contextMessage = "No power‑up active. Normal scoring.";
            }
        }

        ballsBowled++;

        // ✅ Update GUI labels
        lblMainDisplay.setText("<html><center>You: " + userChoice + " | Comp: " + compChoice + "</center></html>");
        lblMiniScore.setText("Score: " + userScore + " - " + compScore);
        lblStatus.setText("Balls: " + ballsBowled + " | Wickets: " + wicketsLost);

        if (isHyperCrazyMode) {
            lblContext.setText("Context: " + contextMessage);
            lblPowerUpBanner.setText(
                    (activePowerUp != null ? "Active Power‑Up: " + activePowerUp + " (" + powerUpDuration + " balls left)" : "Active Power‑Up: None")
            );
        }
    }

    private void endInnings() {
        isHyperCrazyMode = false;
        JOptionPane.showMessageDialog(null,
                "Innings Over!\nFinal Score: " + userScore + "/" + wicketsLost,
                "Match Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleOutSequence(StringBuilder msg, int compMove, int finalScore) {
        msg.append("Comp chose: ").append(compMove).append("<br><font color='red'>OUT!</font>");
        if (isFirstInnings) {
            isUserBatting = !isUserBatting;
            isFirstInnings = false;
            target = finalScore + 1;
            msg.append("<br>Score: ").append(finalScore)
                    .append("<br><font color='yellow'>Target: </font>").append(target).append(". ")
                    .append("<br>").append(isUserBatting ? "You're Batting now." : "You're Bowling now.");
            lblStatus.setText(isUserBatting ? "BATTING" : "BOWLING");
            lblMainDisplay.setText(msg.append("</center></html>").toString());
        } else {
            msg.append("<br><br><font color='orange' size='6'>GAME OVER!</font>");
            lblMainDisplay.setText(msg.append("</center></html>").toString());
            cl.show(cardPanel, "END");
            showFinalPopup(); // FIX: auto show results
        }
    }

    private void showFinalPopup() {
        String result = ""; // initialize to avoid errors
        int diff = Math.abs(userScore - compScore);

        if (userScore > compScore) {
            String trophy = "\uD83C\uDFC6"; // 🏆
            result = "<html><center>"
                    + "<span style='font-size:60px'>" + trophy + "</span><br><br>"
                    + "<b><font color='blue'>CHAMPION!</font></b><br>"
                    + "You won by " + diff + " runs!"
                    + "</center></html>";
        } else if (compScore > userScore) {
            String sadFace = "😒";
            result = "<html><center>"
                    + "<span style='font-size:60px'>" + sadFace + "</span><br><br>"
                    + "<b><font color='blue'>DEFEAT!...</font></b><br>"
                    + "You lost by " + diff + " runs!"
                    + "</center></html>";
        } else {
            int choice = JOptionPane.showOptionDialog(
                    frame,
                    "<html><b><font color='blue'>IT'S A DRAW! Do you want a Super Over or will you declare Draw?</font></b></html>",
                    "Match Result",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[] { "Super Over", "Accept Draw" },
                    "Super Over"
            );

            if (choice == 0) {
                // Run Super Over and return early
                startSuperOver();
                return; // stop here, don’t use result
            } else {
                result = "It's a draw. Good Match!";
                cl.show(cardPanel, "END");
            }
        }

        JLabel label = new JLabel(result);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        JOptionPane.showMessageDialog(this, label, "Match Results", JOptionPane.PLAIN_MESSAGE);
    }


    private void showInstructions() {
        JOptionPane.showMessageDialog(this, "Welcome to Cricket Neo! 🏏\n\n"
                + "HOW TO START: THE TOSS\n"
                + "1. Choose 'ODD' or 'EVEN' then pick a number (1-10).\n"
                + "2. The sum determines the winner.\n"
                + "3. Winner chooses whether to BAT or BOWL first.\n\n"
                + "GAME MODES:\n"
                + "• NORMAL: Standard Hand Cricket.\n"
                + "• CRAZY: OUT if numbers differ by 1 (e.g. 4 vs 5).\n"
                + "  Exact match multiplies runs!", "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupChoice() {
        cl.show(cardPanel, "CHOICE");
        lblMainDisplay.setText("<html><center>Mode Set!<br>Pick your role.</center></html>");
    }

    private void startMatch(boolean userBats) {
        isUserBatting = userBats;
        cl.show(cardPanel, "PLAY");
        lblStatus.setText(isUserBatting ? "BATTING" : "BOWLING");
        lblMainDisplay.setText("<html><center>Match Started!</center></html>");
    }

    // FIXED: Crazy mode rules clarified
    private boolean isOut(int u, int c) {
        if (isCrazyMode) {
            return Math.abs(u - c) == 1; // OUT if difference is exactly 1
        } else {
            return u == c; // Normal mode OUT on exact match
        }
    }

    private int calculateRuns(int u, int c) {
        if (isCrazyMode && u == c) {
            return u * c; // BONUS multiply on exact match
        }
        return isUserBatting ? u : c;
    }

    private void restartGame() {
        userScore = 0;
        compScore = 0;
        target = -1;
        isFirstInnings = true;
        isCrazyMode = false; // FIX: reset crazy mode
        isRealMode = false;
        lblMiniScore.setText("Score: 0 - 0");
        lblMainDisplay.setText("<html><center>TOSS TIME!</center></html>");
        cl.show(cardPanel, "TOSS");
    }
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Did you like it?",
                "Exit",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(frame,
                    "We're glad you enjoyed it 🙂",
                    "Goodbye",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(frame,
                    "We'll keep working on it 🙁",
                    "Goodbye",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } else if (choice == JOptionPane.CANCEL_OPTION) {
            // Do nothing, just return to the game
        }
    }

    public void startSuperOver() {
        System.out.println("Match tied! Starting Super Over...");

        int userRuns = playSuperOver("You");
        int compRuns = playSuperOver("Computer");

        System.out.println("You scored " + userRuns);
        System.out.println("Computer scored " + compRuns);

        if (userRuns > compRuns) {
            JOptionPane.showMessageDialog(frame, "You win the Super Over!");
        } else if (compRuns > userRuns) {
            JOptionPane.showMessageDialog(frame, "Computer wins the Super Over!");
        } else {
            JOptionPane.showMessageDialog(frame, "Super Over tied again! Consider another Super Over or declare a draw.");
        }
    }

    private int playSuperOver(String player) {
        int runs = 0;
        for (int ball = 1; ball <= 6; ball++) {
            runs += simulateBall(player);
        }
        return runs;
    }

    private int simulateBall(String player) {
        // Simple random scoring logic for now
        int outcome = (int)(Math.random() * 7); // 0 to 6 runs
        return outcome;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CricketNeo_Grand::new);
    }

}
