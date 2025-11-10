import javax.swing.*;
import java.awt.*;

public class MenuScreen extends JFrame {
    private final JavaCrosswordGenerator mainGame;
    private Image backgroundImage;

    public MenuScreen() {
        super("DSA Crossword - Main Menu");
        this.mainGame = new JavaCrosswordGenerator(this);
        setupUI();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        try {
            backgroundImage = new ImageIcon("GameBg.jpg").getImage();
        } catch (Exception e) {
            backgroundImage = null;
        }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(100, 150, 255),
                            0, getHeight(), new Color(50, 100, 200));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        panel.setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("DSA Crossword Puzzle");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton playButton = createMenuButton("Play Game");
        JButton tutorialButton = createMenuButton("Tutorial");
        JButton leaderboardButton = createMenuButton("Leaderboard");
        JButton exitButton = createMenuButton("Exit");

        playButton.addActionListener(e -> startGame());
        tutorialButton.addActionListener(e -> showTutorial());
        leaderboardButton.addActionListener(e -> showLeaderboard());
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playButton);
        buttonPanel.add(tutorialButton);
        buttonPanel.add(leaderboardButton);
        buttonPanel.add(exitButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 40, 0);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 50, 20, 50);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(new Color(50, 120, 220));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 140, 240));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 120, 220));
            }
        });

        return button;
    }

    private void startGame() {
        this.setVisible(false);
        mainGame.setupDictionaryAndClues();
        mainGame.buildUI();
        mainGame.loadDefaultCrossword();
        mainGame.getFrame().setVisible(true);
    }

    private void showTutorial() {
        JDialog tutorial = new JDialog(this, "How to Play", true);
        tutorial.setSize(500, 400);
        tutorial.setLocationRelativeTo(this);

        JTextArea tutorialText = new JTextArea();
        tutorialText.setEditable(false);
        tutorialText.setWrapStyleWord(true);
        tutorialText.setLineWrap(true);
        tutorialText.setFont(new Font("Arial", Font.PLAIN, 14));
        tutorialText.setMargin(new Insets(10, 10, 10, 10));

        tutorialText.setText(
            "Welcome to DSA Crossword Puzzle!\n\n" +
            "How to Play:\n" +
            "1. Click on any cell to start typing\n" +
            "2. Use arrow keys to move between cells\n" +
            "3. Press TAB to switch between words\n" +
            "4. Use the 'Check' button to verify your answers\n\n" +
            "Hints System:\n" +
            "- You have 3 hints per game\n" +
            "- Each hint reveals one correct letter\n" +
            "- Use hints wisely!\n\n" +
            "Scoring:\n" +
            "- 10 points for each correct word\n" +
            "- 5 points for each valid word\n" +
            "- Try to get on the leaderboard!\n\n" +
            "Good luck and have fun!"
        );

        JScrollPane scroll = new JScrollPane(tutorialText);
        tutorial.add(scroll);

        JButton closeButton = new JButton("Got it!");
        closeButton.addActionListener(e -> tutorial.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        tutorial.add(buttonPanel, BorderLayout.SOUTH);
        tutorial.setVisible(true);
    }

    private void showLeaderboard() {
        Leaderboard.showLeaderboard(this);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MenuScreen menu = new MenuScreen();
            menu.setVisible(true);
        });
    }
}