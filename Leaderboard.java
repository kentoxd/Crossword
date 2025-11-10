import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Leaderboard {
    private static final String LEADERBOARD_FILE = "crossword_leaderboard.txt";
    private static List<LeaderboardEntry> scores = new ArrayList<>();
    
    public static void addScore(String playerName, int score) {
        scores.add(new LeaderboardEntry(playerName, score));
        scores.sort((a, b) -> Integer.compare(b.score, a.score));
        
      
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        
        saveLeaderboard();
    }
    
    public static void loadLeaderboard() {
        scores.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    scores.add(new LeaderboardEntry(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException e) {
           
        }
    }
    
    private static void saveLeaderboard() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (LeaderboardEntry entry : scores) {
                writer.println(entry.playerName + "," + entry.score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void showLeaderboard(JFrame parent) {
        loadLeaderboard();
        
        JDialog dialog = new JDialog(parent, "Leaderboard", true);
        dialog.setLayout(new BorderLayout());
        
       
        JPanel leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        
        JLabel titleLabel = new JLabel("Top Scores");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardPanel.add(titleLabel);
        leaderboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        
        if (scores.isEmpty()) {
            JLabel noScoresLabel = new JLabel("No scores yet!");
            noScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            leaderboardPanel.add(noScoresLabel);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                LeaderboardEntry entry = scores.get(i);
                JLabel scoreLabel = new JLabel(String.format("%d. %s - %d points", 
                    i + 1, entry.playerName, entry.score));
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                leaderboardPanel.add(scoreLabel);
                leaderboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
       
        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dialog.dispose());
        
        leaderboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leaderboardPanel.add(closeButton);
        
        dialog.add(leaderboardPanel);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
    
    private static class LeaderboardEntry {
        final String playerName;
        final int score;
        
        LeaderboardEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }
    }
}