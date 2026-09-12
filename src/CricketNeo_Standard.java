import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.event.*;

public class CricketNeo_Standard extends JFrame {
    // ---- game state (unchanged rules; ballsThisInnings is display-only) ----
    private int userScore = 0, compScore = 0, target = -1;
    private boolean isUserBatting, isFirstInnings = true, isCrazyMode = false;
    private String userTossChoice;
    private Random rand = new Random();
    private int ballsThisInnings = 0;
    private boolean roleBatting = false;

    // ---- theme palette ----
    private boolean isDarkTheme = true;
    private Color cBg, cSurface, cInk, cMuted, cAccent, cGood, cLine;

    private static final Color LIGHT_BG = new Color(0xF6, 0xF5, 0xF1);
    private static final Color LIGHT_SURFACE = Color.WHITE;
    private static final Color LIGHT_INK = new Color(0x1A, 0x1D, 0x1A);
    private static final Color LIGHT_MUTED = new Color(0x72, 0x6B, 0x66);
    private static final Color LIGHT_ACCENT = new Color(0xB2, 0x3A, 0x2E);
    private static final Color LIGHT_GOOD = new Color(0x2F, 0x8F, 0x5B);
    private static final Color LIGHT_LINE = new Color(0xE1, 0xDC, 0xD2);

    private static final Color DARK_BG = new Color(0x17, 0x15, 0x13);
    private static final Color DARK_SURFACE = new Color(0x21, 0x1E, 0x1B);
    private static final Color DARK_INK = new Color(0xEF, 0xEC, 0xE4);
    private static final Color DARK_MUTED = new Color(0xA3, 0x9C, 0x94);
    private static final Color DARK_ACCENT = new Color(0xE2, 0x68, 0x5A);
    private static final Color DARK_GOOD = new Color(0x5F, 0xBB, 0x87);
    private static final Color DARK_LINE = new Color(0x3A, 0x36, 0x32);

    // ---- GUI components ----
    private JLabel lblHeader;
    private JButton btnTheme, btnHelp;
    private RoundedPanel dashboardCard, modeChipPill, needPill, roleDot;
    private JLabel lblPhase, lblModeChip, lblScoreBig, lblTargetText, lblNeedText, lblRoleText, lblBallsFaced, lblContext, lblTrayLabel;
    private JPanel dividerLine;
    private JPanel cardPanel, tossPanel, modePanel, choicePanel, playPanel, endPanel;
    private CardLayout cl = new CardLayout();

    private List<JButton> actionButtons = new ArrayList<>();
    private List<RoundButton> numberButtons = new ArrayList<>();

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
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(
                new ImageIcon(getClass().getResource("resources/CricketNeo.png")).getImage()
        );

        buildUtilityButtons();
        buildHeader();
        buildDashboard();
        buildCardPanel();

        applyTheme(isDarkTheme);
        updateScoreboard();
        cl.show(cardPanel, "TOSS");
        setVisible(true);
    }

    // ---------------------------------------------------------------
    // GUI construction
    // ---------------------------------------------------------------

    private void buildUtilityButtons() {
        int btnSize = 44, gap = 6, rightMargin = 20;

        btnTheme = new RoundButton("☾", 10);
        btnTheme.setBounds(750 - rightMargin - btnSize, 16, btnSize, 28);
        btnTheme.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        btnTheme.addActionListener(e -> toggleTheme());
        add(btnTheme);

        btnHelp = new RoundButton("?", 10);
        btnHelp.setBounds(750 - rightMargin - (2 * btnSize) - gap, 16, btnSize, 28);
        btnHelp.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnHelp.addActionListener(e -> showInstructions());
        add(btnHelp);
    }

    private void buildHeader() {
        lblHeader = new JLabel("CRICKET NEO", SwingConstants.CENTER);
        lblHeader.setBounds(0, 14, 750, 34);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblHeader);
    }

    private void buildDashboard() {
        dashboardCard = new RoundedPanel(16);
        dashboardCard.setLayout(null);
        dashboardCard.setBounds(40, 58, 670, 230);
        add(dashboardCard);

        lblPhase = new JLabel("TOSS");
        lblPhase.setBounds(20, 14, 300, 22);
        lblPhase.setFont(new Font("Consolas", Font.BOLD, 13));
        dashboardCard.add(lblPhase);

        modeChipPill = new RoundedPanel(12);
        modeChipPill.setLayout(new BorderLayout());
        modeChipPill.setBounds(390, 10, 260, 28);
        modeChipPill.setVisible(false);
        lblModeChip = new JLabel("", SwingConstants.CENTER);
        lblModeChip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeChipPill.add(lblModeChip, BorderLayout.CENTER);
        dashboardCard.add(modeChipPill);

        lblScoreBig = new JLabel("0 – 0");
        lblScoreBig.setBounds(20, 44, 210, 56);
        lblScoreBig.setFont(new Font("Consolas", Font.BOLD, 36));
        dashboardCard.add(lblScoreBig);

        lblTargetText = new JLabel("");
        lblTargetText.setBounds(235, 66, 190, 22);
        lblTargetText.setFont(new Font("Consolas", Font.BOLD, 13));
        lblTargetText.setVisible(false);
        dashboardCard.add(lblTargetText);

        needPill = new RoundedPanel(8);
        needPill.setLayout(new BorderLayout());
        needPill.setBounds(430, 60, 180, 30);
        needPill.setVisible(false);
        lblNeedText = new JLabel("", SwingConstants.CENTER);
        lblNeedText.setFont(new Font("Consolas", Font.BOLD, 12));
        needPill.add(lblNeedText, BorderLayout.CENTER);
        dashboardCard.add(needPill);

        roleDot = new RoundedPanel(6);
        roleDot.setBounds(20, 116, 12, 12);
        dashboardCard.add(roleDot);

        lblRoleText = new JLabel("—");
        lblRoleText.setBounds(38, 112, 150, 20);
        lblRoleText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dashboardCard.add(lblRoleText);

        lblBallsFaced = new JLabel("");
        lblBallsFaced.setBounds(200, 112, 400, 20);
        lblBallsFaced.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dashboardCard.add(lblBallsFaced);

        dividerLine = new JPanel();
        dividerLine.setBounds(20, 144, 630, 1);
        dashboardCard.add(dividerLine);

        lblContext = new JLabel("<html><center>Choose ODD or EVEN below to begin.</center></html>", SwingConstants.CENTER);
        lblContext.setBounds(20, 154, 630, 64);
        lblContext.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dashboardCard.add(lblContext);
    }

    private void buildCardPanel() {
        cardPanel = new JPanel(cl);
        cardPanel.setBounds(40, 302, 670, 308);
        cardPanel.setOpaque(false);

        tossPanel = createContainerPanel();
        addActionButton(tossPanel, "ODD", 185, 120, 140, 54, e -> startToss("o"));
        addActionButton(tossPanel, "EVEN", 345, 120, 140, 54, e -> startToss("e"));

        playPanel = createContainerPanel();
        lblTrayLabel = new JLabel("PLAY A BALL");
        lblTrayLabel.setBounds(25, 20, 300, 20);
        lblTrayLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        playPanel.add(lblTrayLabel);

        JPanel numPad = new JPanel(new GridLayout(2, 5, 12, 12));
        numPad.setOpaque(false);
        numPad.setBounds(25, 70, 620, 170);
        for (int i = 0; i < 10; i++) {
            int val = i + 1;
            RoundButton b = new RoundButton(String.valueOf(val), 10);
            b.setFont(new Font("Consolas", Font.BOLD, 20));
            b.addActionListener(e -> handleInput(val));
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    b.setBackground(mix(cSurface, cAccent, 0.35));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    b.setBackground(cSurface);
                }
            });
            numberButtons.add(b);
            numPad.add(b);
        }
        playPanel.add(numPad);

        modePanel = createContainerPanel();
        addActionButton(modePanel, "NORMAL", 185, 120, 140, 54, e -> {
            isCrazyMode = false;
            setupChoice();
        });
        addActionButton(modePanel, "CRAZY", 345, 120, 140, 54, e -> {
            isCrazyMode = true;
            setupChoice();
        });

        choicePanel = createContainerPanel();
        addActionButton(choicePanel, "BATTING", 155, 115, 170, 60, e -> startMatch(true));
        addActionButton(choicePanel, "BOWLING", 345, 115, 170, 60, e -> startMatch(false));

        endPanel = createContainerPanel();
        addActionButton(endPanel, "RESULTS!", 90, 125, 150, 56, e -> showFinalPopup());
        addActionButton(endPanel, "RESTART", 260, 125, 150, 56, e -> restartGame());
        addActionButton(endPanel, "EXIT", 430, 125, 150, 56, e -> confirmExit());

        cardPanel.add(tossPanel, "TOSS");
        cardPanel.add(playPanel, "PLAY");
        cardPanel.add(modePanel, "MODE");
        cardPanel.add(choicePanel, "CHOICE");
        cardPanel.add(endPanel, "END");

        add(cardPanel);
    }

    private JPanel createContainerPanel() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        return p;
    }

    private void addActionButton(JPanel panel, String text, int x, int y, int w, int h, ActionListener al) {
        RoundButton b = new RoundButton(text, 10);
        b.setBounds(x, y, w, h);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.addActionListener(al);
        actionButtons.add(b);
        panel.add(b);
    }

    /** Simple rounded-rectangle container, used for the dashboard card and its pill badges. */
    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    /** Rounded-rectangle button, used for every clickable control in the new GUI. */
    private static class RoundButton extends JButton {
        private final int radius;

        RoundButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------
    // Theming
    // ---------------------------------------------------------------

    private void toggleTheme() {
        applyTheme(!isDarkTheme);
    }

    private void applyTheme(boolean dark) {
        isDarkTheme = dark;
        cBg = dark ? DARK_BG : LIGHT_BG;
        cSurface = dark ? DARK_SURFACE : LIGHT_SURFACE;
        cInk = dark ? DARK_INK : LIGHT_INK;
        cMuted = dark ? DARK_MUTED : LIGHT_MUTED;
        cAccent = dark ? DARK_ACCENT : LIGHT_ACCENT;
        cGood = dark ? DARK_GOOD : LIGHT_GOOD;
        cLine = dark ? DARK_LINE : LIGHT_LINE;

        getContentPane().setBackground(cBg);

        btnTheme.setText(dark ? "☾" : "☀");
        btnTheme.setBackground(cSurface);
        btnTheme.setForeground(cInk);
        btnHelp.setBackground(cSurface);
        btnHelp.setForeground(cInk);

        lblHeader.setForeground(cAccent);

        dashboardCard.setBackground(cSurface);
        lblPhase.setForeground(cMuted);
        modeChipPill.setBackground(cBg);
        lblModeChip.setForeground(cInk);
        lblScoreBig.setForeground(cInk);
        lblTargetText.setForeground(cMuted);
        needPill.setBackground(cAccent);
        lblNeedText.setForeground(Color.WHITE);
        lblRoleText.setForeground(cInk);
        lblBallsFaced.setForeground(cMuted);
        dividerLine.setBackground(cLine);
        lblContext.setForeground(cMuted);
        refreshRoleDotColor();

        if (lblTrayLabel != null) {
            lblTrayLabel.setForeground(cMuted);
        }
        for (JButton b : actionButtons) {
            b.setBackground(cAccent);
            b.setForeground(Color.WHITE);
        }
        for (RoundButton b : numberButtons) {
            b.setBackground(cSurface);
            b.setForeground(cInk);
        }

        repaint();
    }

    /** Blends {@code base} toward {@code target} by {@code ratio} (0 = base, 1 = target). */
    private static Color mix(Color base, Color target, double ratio) {
        int r = (int) Math.round(base.getRed() * (1 - ratio) + target.getRed() * ratio);
        int g = (int) Math.round(base.getGreen() * (1 - ratio) + target.getGreen() * ratio);
        int b = (int) Math.round(base.getBlue() * (1 - ratio) + target.getBlue() * ratio);
        return new Color(r, g, b);
    }

    private void refreshRoleDotColor() {
        if (roleDot != null) {
            roleDot.setBackground(roleBatting ? cGood : cMuted);
            roleDot.repaint();
        }
    }

    private void setRole(String text, boolean batting) {
        lblRoleText.setText(text);
        roleBatting = batting;
        refreshRoleDotColor();
    }

    private String modeChipText() {
        return isCrazyMode
                ? "CRAZY · out on ±1, exact match = bonus"
                : "NORMAL · out on exact match";
    }

    private void updateScoreboard() {
        lblScoreBig.setText(userScore + " – " + compScore);

        boolean showTarget = !isFirstInnings && target > 0;
        lblTargetText.setVisible(showTarget);
        needPill.setVisible(showTarget);
        if (showTarget) {
            lblTargetText.setText("/ TARGET " + target);
            int battingScore = isUserBatting ? userScore : compScore;
            int need = Math.max(target - battingScore, 0);
            lblNeedText.setText("NEED " + need);
        }

        lblBallsFaced.setText(ballsThisInnings + (ballsThisInnings == 1 ? " ball faced" : " balls faced") + " this innings");
    }

    // ---------------------------------------------------------------
    // Game flow (mechanics unchanged from before; only what's displayed differs)
    // ---------------------------------------------------------------

    private void startToss(String choice) {
        userTossChoice = choice;
        cl.show(cardPanel, "PLAY");
        lblPhase.setText("TOSS");
        lblContext.setText("<html><center>You chose " + (choice.equals("o") ? "ODD" : "EVEN")
                + ".<br>Pick a number for the toss!</center></html>");
    }

    private void handleInput(int val) {
        if (userTossChoice != null) handleTossLogic(val);
        else playGame(val);
    }

    private void handleTossLogic(int userNum) {
        int compNum = rand.nextInt(10) + 1;
        boolean isEven = CricketLogic.isTossSumEven(userNum, compNum);
        String numbersPicked = "You chose: " + userNum + "<br>Comp chose: " + compNum;

        if (CricketLogic.userWonToss(userTossChoice, userNum, compNum)) {
            lblPhase.setText("SELECT MODE");
            lblContext.setText("<html><center>" + numbersPicked
                    + "<br><b>YOU WON THE TOSS!</b><br>Select your mode below.</center></html>");
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

            lblModeChip.setText(modeChipText());
            modeChipPill.setVisible(true);

            JOptionPane.showMessageDialog(this, tossLossMsg, "Toss Result", JOptionPane.INFORMATION_MESSAGE);
            startMatch(isUserBatting);
        }
        userTossChoice = null;
    }

    private void playGame(int user) {
        int comp = rand.nextInt(10) + 1;
        ballsThisInnings++;

        if (isUserBatting) {
            if (isOut(user, comp)) {
                handleOutSequence(comp, userScore);
                return;
            }
            int runs = calculateRuns(user, comp);
            userScore += runs;
            boolean bonus = isCrazyMode && user == comp;
            lblContext.setText("<html><center>Comp bowled: " + comp
                    + (bonus ? "<br><b>BONUS MULTIPLY!</b>" : "")
                    + "<br>You scored: " + runs + "</center></html>");
            updateScoreboard();
            if (!isFirstInnings && userScore >= target) {
                lblContext.setText("<html><center>Comp bowled: " + comp
                        + "<br><b>TARGET REACHED!</b><br>GAME OVER</center></html>");
                lblPhase.setText("MATCH OVER");
                updateScoreboard();
                cl.show(cardPanel, "END");
                showFinalPopup();
            }
        } else {
            if (isOut(user, comp)) {
                handleOutSequence(comp, compScore);
                return;
            }
            int runs = calculateRuns(user, comp);
            compScore += runs;
            boolean bonus = isCrazyMode && user == comp;
            lblContext.setText("<html><center>Comp put: " + comp
                    + (bonus ? "<br><b>BONUS MULTIPLY!</b>" : "")
                    + "<br>Comp scored: " + runs + "</center></html>");
            updateScoreboard();
            if (!isFirstInnings && compScore >= target) {
                lblContext.setText("<html><center>Comp put: " + comp
                        + "<br><b>TARGET REACHED BY COMP!</b><br>GAME OVER</center></html>");
                lblPhase.setText("MATCH OVER");
                updateScoreboard();
                cl.show(cardPanel, "END");
                showFinalPopup();
            }
        }
    }

    private void handleOutSequence(int compMove, int finalScore) {
        if (isFirstInnings) {
            isUserBatting = !isUserBatting;
            isFirstInnings = false;
            target = finalScore + 1;
            ballsThisInnings = 0;
            lblPhase.setText("INNINGS 2/2");
            setRole(isUserBatting ? "BATTING" : "BOWLING", isUserBatting);
            lblContext.setText("<html><center>Comp chose: " + compMove + "<br><b>OUT!</b>"
                    + "<br>Score: " + finalScore
                    + "<br>Target: " + target + ". "
                    + (isUserBatting ? "You're batting now." : "You're bowling now.")
                    + "</center></html>");
            updateScoreboard();
        } else {
            lblPhase.setText("MATCH OVER");
            lblContext.setText("<html><center>Comp chose: " + compMove + "<br><b>OUT!</b>"
                    + "<br><br><b>GAME OVER!</b></center></html>");
            updateScoreboard();
            cl.show(cardPanel, "END");
            showFinalPopup();
        }
    }

    private void showFinalPopup() {
        String result;
        int diff = Math.abs(userScore - compScore);

        if (userScore > compScore) {
            String trophy = "🏆"; // 🏆
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
            String shakeHands = "🤝";
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
        lblModeChip.setText(modeChipText());
        modeChipPill.setVisible(true);
        lblPhase.setText("SELECT ROLE");
        lblContext.setText("<html><center>Mode set!<br>Pick your role.</center></html>");
        cl.show(cardPanel, "CHOICE");
    }

    private void startMatch(boolean userBats) {
        isUserBatting = userBats;
        ballsThisInnings = 0;
        lblPhase.setText(isFirstInnings ? "INNINGS 1/2" : "INNINGS 2/2");
        setRole(isUserBatting ? "BATTING" : "BOWLING", isUserBatting);
        lblContext.setText("<html><center>Match Started!</center></html>");
        updateScoreboard();
        cl.show(cardPanel, "PLAY");
    }

    private boolean isOut(int u, int c) {
        return CricketLogic.isOut(isCrazyMode, u, c);
    }

    private int calculateRuns(int u, int c) {
        return CricketLogic.calculateRuns(isCrazyMode, isUserBatting, u, c);
    }

    private void restartGame() {
        userScore = 0;
        compScore = 0;
        target = -1;
        isFirstInnings = true;
        isCrazyMode = false;
        ballsThisInnings = 0;
        lblPhase.setText("TOSS");
        modeChipPill.setVisible(false);
        setRole("—", false);
        lblContext.setText("<html><center>Toss time! Choose ODD or EVEN.</center></html>");
        updateScoreboard();
        cl.show(cardPanel, "TOSS");
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Did you like it?",
                "Exit",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "We're glad you enjoyed it 🙂",
                    "Goodbye",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "We'll keep working on it 🙁",
                    "Goodbye",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        // CANCEL_OPTION: do nothing, just return to the game
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CricketNeo_Standard::new);
    }
}
