import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.event.*;

public class CricketNeo_Standard extends JFrame {
    private int userScore = 0, compScore = 0, target = -1;
    private boolean isUserBatting, isFirstInnings = true, isCrazyMode = false;
    private String userTossChoice;
    private Random rand = new Random();

    // GUI Components
    private JLabel lblHeader, lblStatus, lblMainDisplay, lblMiniScore;
    private JButton btnTheme, btnHelp;
    private JPanel cardPanel, tossPanel, modePanel, choicePanel, playPanel, endPanel;
    private CardLayout cl = new CardLayout();

    private List<JButton> gameButtons = new ArrayList<>();

    private Color darkBG = new Color(20, 20, 20);
    private Color lightBG = Color.WHITE;

    private JFrame frame;

    public CricketNeo_Standard() {
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
        addGameButton(modePanel, "NORMAL", 180, 50, 120, 50, e -> { isCrazyMode = false; setupChoice(); });
        addGameButton(modePanel, "CRAZY", 340, 50, 120, 50, e -> { isCrazyMode = true; setupChoice(); });

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

    private void playGame(int user) {
        int comp = rand.nextInt(10) + 1;
        StringBuilder msg = new StringBuilder("<html><center>");
        if (isUserBatting) {
            if (isOut(user, comp)) {
                handleOutSequence(msg, comp, userScore);
                return;
            } else {
                int runs = calculateRuns(user, comp);
                userScore += runs;
                msg.append("Comp bowled: ").append(comp)
                        .append(isCrazyMode && user == comp ? "<br>BONUS MULTIPLY!" : "")
                        .append("<br>You scored: ").append(runs)
                        .append("<br>Total: ").append(userScore);
                if (!isFirstInnings) {
                    msg.append("<br>Target: ").append(target);
                    if (userScore >= target) {
                        lblMainDisplay.setText("<html><center>Comp bowled: " + comp
                                + "<br><font color='orange'>TARGET REACHED!</font><br>GAME OVER</center></html>");
                        lblMiniScore.setText("Score: " + userScore + " - " + compScore);
                        cl.show(cardPanel, "END");
                        showFinalPopup(); // FIX: auto show results
                        return;
                    }
                }
            }
        } else {
            if (isOut(user, comp)) {
                handleOutSequence(msg, comp, compScore);
                return;
            } else {
                int runs = calculateRuns(user, comp);
                compScore += runs;
                msg.append("Comp put: ").append(comp)
                        .append(isCrazyMode && user == comp ? "<br>BONUS MULTIPLY!" : "")
                        .append("<br>Comp scored: ").append(runs)
                        .append("<br>Comp Total: ").append(compScore);
                if (!isFirstInnings) {
                    msg.append("<br>Target: ").append(target);
                    if (compScore >= target) {
                        lblMainDisplay.setText("<html><center>Comp put: " + comp
                                + "<br><font color='red'>TARGET REACHED BY COMP!</font><br>GAME OVER</center></html>");
                        lblMiniScore.setText("Score: " + userScore + " - " + compScore);
                        cl.show(cardPanel, "END");
                        showFinalPopup(); // FIX: auto show results
                        return;
                    }
                }
            }
        }
        lblMainDisplay.setText(msg.append("</center></html>").toString());
        lblMiniScore.setText("Score: " + userScore + " - " + compScore);
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
        String result;
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
            String shakeHands = "\uD83E\uDD1D";
            result = "<html><center>"
                    + "<span style='font-size:60px'>" + shakeHands + "</span><br><br>"
                    + "<b><font color='blue'>IT'S A DRAW!</font></b><br>"
                    + "Great Match!"
                    + "</center></html>";
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



    public static void main(String[] args) {
        SwingUtilities.invokeLater(CricketNeo_Standard::new);
    }
}
