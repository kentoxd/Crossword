import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class JavaCrosswordGenerator {
    // Grid size - 18 rows x 17 columns
    private static final int ROWS = 18;
    private static final int COLS = 18;

    // Representation: '#' = black cell, '.' = empty, 'A'..'Z' letters when placed in solution
    private char[][] grid = new char[ROWS][COLS];
    private char[][] solution = new char[ROWS][COLS]; // final placed letters (only letters or '#')

    // Trie (dictionary)
    private final Trie trie = new Trie();

    // Words list with clues (DSA-themed)
    private final List<String> words = new ArrayList<>();
    private final Map<String, String> clues = new HashMap<>();

    // Placements done by the generator (for undo/redo & checking)
    private final Deque<Placement> undoStack = new ArrayDeque<>();
    private final Deque<Placement> redoStack = new ArrayDeque<>();

    // Swing UI
    private final JFrame frame = new JFrame("DSA Crossword Generator");
    private final JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS));
    private final JTextArea clueArea = new JTextArea();
    private final JTextField[][] cellFields = new JTextField[ROWS][COLS];
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private int score = 0;

    // Internal placements (used by generator)
    private final List<Placement> placed = new ArrayList<>();
    
    // Track current word being typed to prevent wrong navigation
    private Placement currentTypingWord = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JavaCrosswordGenerator app = new JavaCrosswordGenerator();
            app.setupDictionaryAndClues();
            app.buildUI();
            app.loadCrossword();
            app.frame.setVisible(true);
            
        });
    }

    private void setupDictionaryAndClues() {
        // DSA themed words + clues from your crossword puzzle
        
        // ACROSS clues:
        putWord("QUEUE", "A linear data structure where elements are processed in First In, First Out (FIFO) order");
        putWord("BUCKETSORT", "A sorting algorithm that distributes elements into groups (or \"bins\") before sorting them individually");
        putWord("SHELLSORT", "An in-place sorting algorithm that generalizes insertion sort by comparing elements separated by a gap");
        putWord("ARRAY", "A fixed-size collection of elements stored in contiguous memory locations");
        
        // DOWN clues:
        putWord("LINKEDLIST", "A linear data structure where each element (node) contains a reference to the next node");
        putWord("ALGORITHM", "A step-by-step procedure or formula for solving a problem");
        putWord("BUBBLESORT", "A simple sorting algorithm that repeatedly swaps adjacent elements if they are in the wrong order");
        putWord("STACK", "A linear data structure that follows the Last In, First Out (LIFO) principle");

        // Insert into trie
        for (String w : words) trie.insert(w);
    }

    private void putWord(String w, String clue) {
        words.add(w.toUpperCase());
        clues.put(w.toUpperCase(), clue);
    }

    // Load the single crossword puzzle
    private void loadCrossword() {
        // Clear everything
        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(grid[r], '#'); // default to black
            Arrays.fill(solution[r], '#');
        }
        
        // Clear UI fields
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            cellFields[r][c].setText("");
            cellFields[r][c].setBackground(Color.WHITE);
            cellFields[r][c].setEditable(false);
            cellFields[r][c].setVisible(true);
            cellFields[r][c].getParent().setBackground(Color.WHITE);
        }
        
        //Down;
        placed.add(new Placement("LINKEDLIST", 0, 7, Direction.DOWN));
        placed.add(new Placement("STACK", 13, 0, Direction.DOWN));
        placed.add(new Placement("BUBBLESORT", 7, 2, Direction.DOWN));
        placed.add(new Placement("ALGORITHM", 6, 9, Direction.DOWN));
        placed.add(new Placement("ALGORITHM", 1, 11, Direction.DOWN));



        //Across:
        placed.add(new Placement("ARRAY", 15, 0, Direction.ACROSS));
        placed.add(new Placement("BUCKETSORT", 9, 2, Direction.ACROSS));
        placed.add(new Placement("QUEUE",4, 5, Direction.ACROSS));
        placed.add(new Placement("SHELLSORTT", 13, 8, Direction.ACROSS));
        // placed.add(new Placement("WORD", row, col, Direction.DOWN));
        
        // Apply placements to solution
        for (Placement p : placed) {
            applyPlacementToSolution(p);
        }
        
        // Update UI
        updateUIFromSolution(false);
        updateClueArea();
        
        // Reset game state
        undoStack.clear();
        redoStack.clear();
        score = 0;
        currentTypingWord = null; // Reset current typing word
        updateScore();
        
        // Update title
        frame.setTitle("DSA Crossword Generator");
    }

    // --- UI building ---
    private void buildUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 900);
        frame.setLayout(new BorderLayout());

        // Grid panel
        gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rebuildGridUI();

        // Right panel: controls + clues
        JPanel rightPanel = new JPanel(new BorderLayout());
        clueArea.setEditable(false);
        clueArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        clueArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane clueScroll = new JScrollPane(clueArea);
        clueScroll.setPreferredSize(new Dimension(400, 700));

        JPanel topButtons = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton checkBtn = new JButton("Check");
        JButton revealBtn = new JButton("Reveal");
        JButton hintBtn = new JButton("Hint");
        JButton resetBtn = new JButton("Reset");

        checkBtn.addActionListener(_ -> checkAllWords());

        revealBtn.addActionListener(_ -> {
            // reveal the entire solution
            updateUIFromSolution(true);
            // compute final score
            computeScoreFromSolution();
        });

        hintBtn.addActionListener(_ -> showHint());

        resetBtn.addActionListener(_ -> {
            // Clear all user input
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    cellFields[r][c].setText("");
                    cellFields[r][c].setBackground(Color.WHITE);
                }
            }
            // Reset score and current typing word
            score = 0;
            currentTypingWord = null;
            updateScore();
        });

        topButtons.add(checkBtn);
        topButtons.add(revealBtn);
        topButtons.add(hintBtn);
        topButtons.add(resetBtn);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.add(scoreLabel);

        rightPanel.add(topButtons, BorderLayout.NORTH);
        rightPanel.add(clueScroll, BorderLayout.CENTER);
        rightPanel.add(scorePanel, BorderLayout.SOUTH);

        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
    }

    // Build the visible grid UI with text fields and black panels
    private void rebuildGridUI() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(ROWS, COLS, 1, 1));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                JPanel p = new JPanel(new BorderLayout());
                p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font("SansSerif", Font.BOLD, 18));
                tf.setBorder(null);
                // Input filter: only 1 letter A-Z
                ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                            throws BadLocationException {
                        if (text == null) return;
                        text = text.toUpperCase();
                        if (text.matches("[A-Z]") && (fb.getDocument().getLength() + text.length() - length) <= 1) {
                            super.replace(fb, offset, length, text, attrs);
                        } else if (text.isEmpty()) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }

                    @Override
                    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                            throws BadLocationException {
                        if (string == null) return;
                        string = string.toUpperCase();
                        if (string.matches("[A-Z]") && (fb.getDocument().getLength() + string.length()) <= 1) {
                            super.insertString(fb, offset, string, attr);
                        }
                    }
                });

                // Add Tab navigation and automatic navigation after typing
                final int currentRow = r;
                final int currentCol = c;
                
                // Add focus listener to set current typing word when cell is focused
                tf.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent e) {
                        // When clicking on a cell, prioritize ACROSS words first, then DOWN
                        // This gives consistent behavior at intersections
                        if (currentTypingWord == null || !currentTypingWord.coversCell(currentRow, currentCol)) {
                            Placement acrossWord = null;
                            Placement downWord = null;
                            
                            for (Placement p : placed) {
                                if (p.coversCell(currentRow, currentCol)) {
                                    if (p.dir == Direction.ACROSS) {
                                        acrossWord = p;
                                    } else {
                                        downWord = p;
                                    }
                                }
                            }
                            
                            // Prioritize across word at intersections
                            currentTypingWord = (acrossWord != null) ? acrossWord : downWord;
                        }
                    }
                });
                
                
                // Replace the keyPressed listener in rebuildGridUI() with this:
                tf.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                            e.consume(); // Prevent default tab behavior
                            // Tab switches to the next word, not next cell in same word
                            switchToNextWord(currentRow, currentCol);
                        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT || 
                                   e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                            e.consume(); // Prevent default arrow behavior
                            navigateToNextCell(currentRow, currentCol);
                        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT || 
                                   e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                            e.consume(); // Prevent default arrow behavior
                            navigateToPreviousCell(currentRow, currentCol);
                        }
                    }
                    
                    @Override
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        // Auto-navigate after typing a letter
                        if (e.getKeyChar() >= 'A' && e.getKeyChar() <= 'Z' || 
                            e.getKeyChar() >= 'a' && e.getKeyChar() <= 'z') {
                            SwingUtilities.invokeLater(() -> {
                                navigateToNextCell(currentRow, currentCol);
                            });
                        }
                    }
                });
                cellFields[r][c] = tf;
                p.add(tf, BorderLayout.CENTER);
                gridPanel.add(p);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }


    // apply placement directly to solution grid
    private void applyPlacementToSolution(Placement p) {
        int r = p.row, c = p.col;
        for (int i = 0; i < p.word.length(); i++) {
            char ch = p.word.charAt(i);
            if (p.dir == Direction.ACROSS) {
                solution[r][c + i] = ch;
            } else {
                solution[r + i][c] = ch;
            }
        }
        // push to undo stack (generator-level undo isn't user-facing)
        undoStack.push(p);
        // clear redo on new placement
        redoStack.clear();
    }

    // Update the visible UI from solution grid. If revealLetters true => show letters in textfields.
    private void updateUIFromSolution(boolean revealLetters) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char sol = solution[r][c];
                JTextField tf = cellFields[r][c];
                JPanel parent = (JPanel) tf.getParent();
                if (sol == '#') {
                    // black cell
                    tf.setText("");
                    tf.setEditable(false);
                    parent.setBackground(Color.BLACK);
                    tf.setVisible(false);
                } else {
                    // white cell
                    parent.setBackground(Color.WHITE);
                    tf.setVisible(true);
                    tf.setEditable(true);
                    if (revealLetters) {
                        tf.setText(String.valueOf(sol));
                        tf.setEditable(false);
                        tf.setBackground(new Color(210, 255, 210));
                    } else {
                        tf.setText("");
                        tf.setBackground(Color.WHITE);
                        tf.setEditable(true);
                    }
                }
            }
        }
    }

    // Update clue area based on placed words and their directions/locations
    private void updateClueArea() {
        StringBuilder sb = new StringBuilder();
        sb.append("DSA Crossword Puzzle\n");
        sb.append("Across & Down clues:\n\n");
        
        // Sort placed by row/col then direction
        List<Placement> list = new ArrayList<>(placed);
        list.sort(Comparator.comparingInt((Placement p) -> p.row).thenComparingInt(p -> p.col));
        
        for (Placement p : list) {
            sb.append(String.format("(%s) @ [%d,%d]\n", p.dir, p.row, p.col));
            sb.append("  -> ").append(clues.getOrDefault(p.word, "No clue available")).append("\n\n");
        }
        
        clueArea.setText(sb.toString());
    }
    private void switchToNextWord(int currentRow, int currentCol) {
        // Find the next word after the current one
        int currentIndex = -1;
        if (currentTypingWord != null) {
            currentIndex = placed.indexOf(currentTypingWord);
        }
        
        // Move to the next word in the list (wrap around)
        int nextIndex = (currentIndex + 1) % placed.size();
        Placement nextWord = placed.get(nextIndex);
        
        // Set as current typing word and focus on first cell
        currentTypingWord = nextWord;
        cellFields[nextWord.row][nextWord.col].requestFocus();
    }
    // When user clicks check: evaluate each placed word whether the letters in the grid match solution
    private void checkAllWords() {
        if (placed.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No puzzle loaded. Please select a puzzle first.");
            return;
        }
        int correctWords = 0;
        int validWords = 0;
        for (Placement p : placed) {
            boolean correct = true;
            StringBuilder userWord = new StringBuilder();
            for (int i = 0; i < p.word.length(); i++) {
                int rr = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
                int cc = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
                String text = cellFields[rr][cc].getText();
                char ch = (text != null && text.length() > 0) ? text.toUpperCase().charAt(0) : '?';
                userWord.append(ch);
                if (ch != p.word.charAt(i)) {
                    correct = false;
                }
            }
            
            // Use Trie to validate if user input is a valid word
            boolean isValidWord = trie.contains(userWord.toString());
            if (isValidWord) validWords++;
            
            // color feedback
            Color bg;
            if (correct) {
                bg = new Color(200, 255, 200); // green for correct
            } else if (isValidWord) {
                bg = new Color(255, 255, 200); // yellow for valid but not correct
            } else {
                bg = new Color(255, 220, 220); // red for invalid
            }
            
            for (int i = 0; i < p.word.length(); i++) {
                int rr = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
                int cc = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
                cellFields[rr][cc].setBackground(bg);
            }
            if (correct) correctWords++;
        }
        // score: +10 per correct word, +5 per valid word
        score = correctWords * 10 + validWords * 5;
        updateScore();
        JOptionPane.showMessageDialog(frame, 
            "Check complete. Correct words: " + correctWords + " / " + placed.size() + 
            "\nValid words: " + validWords + " / " + placed.size());
    }

    private void computeScoreFromSolution() {
        // used when revealing entire board
        score = placed.size() * 10;
        updateScore();
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    // Show hint using Trie startsWith functionality
    private void showHint() {
        if (placed.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No puzzle loaded. Please select a puzzle first.");
            return;
        }
        
        StringBuilder hintText = new StringBuilder();
        hintText.append("Hints for current puzzle:\n\n");
        
        for (Placement p : placed) {
            StringBuilder currentWord = new StringBuilder();
            boolean hasPartialInput = false;
            
            // Check what user has entered so far
            for (int i = 0; i < p.word.length(); i++) {
                int rr = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
                int cc = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
                String text = cellFields[rr][cc].getText();
                char ch = (text != null && text.length() > 0) ? text.toUpperCase().charAt(0) : '_';
                currentWord.append(ch);
                if (ch != '_') hasPartialInput = true;
            }
            
            String partial = currentWord.toString();
            hintText.append(String.format("(%s) @ [%d,%d]: ", p.dir, p.row, p.col));
            
            if (hasPartialInput) {
                // Use Trie to check if partial word is valid prefix
                if (trie.startsWith(partial)) {
                    hintText.append("✓ Valid prefix: ").append(partial).append("...\n");
                } else {
                    hintText.append("✗ Invalid prefix: ").append(partial).append("\n");
                }
            } else {
                hintText.append("Clue: ").append(clues.getOrDefault(p.word, "No clue available")).append("\n");
            }
        }
        
        JOptionPane.showMessageDialog(frame, hintText.toString(), "Hints", JOptionPane.INFORMATION_MESSAGE);
    }

    // Navigate to the next cell in the current word
    private void navigateToNextCell(int currentRow, int currentCol) {
        Placement targetWord = currentTypingWord;
        
        // If no current typing word, or current cell is not part of current typing word, find one
        if (targetWord == null || !targetWord.coversCell(currentRow, currentCol)) {
            // Prioritize ACROSS words at intersections for consistent navigation
            Placement acrossWord = null;
            Placement downWord = null;
            
            for (Placement p : placed) {
                if (p.coversCell(currentRow, currentCol)) {
                    if (p.dir == Direction.ACROSS) {
                        acrossWord = p;
                    } else {
                        downWord = p;
                    }
                }
            }
            
            targetWord = (acrossWord != null) ? acrossWord : downWord;
            currentTypingWord = targetWord;
        }
        
        if (targetWord != null) {
            // Calculate position within the word
            int wordPos = (targetWord.dir == Direction.ACROSS) ? 
                (currentCol - targetWord.col) : (currentRow - targetWord.row);
            
            // Move to next position in the word
            int nextPos = wordPos + 1;
            if (nextPos < targetWord.word.length()) {
                int nextRow = (targetWord.dir == Direction.ACROSS) ? targetWord.row : targetWord.row + nextPos;
                int nextCol = (targetWord.dir == Direction.ACROSS) ? targetWord.col + nextPos : targetWord.col;
                
                if (nextRow < ROWS && nextCol < COLS) {
                    cellFields[nextRow][nextCol].requestFocus();
                }
            }
        }
    }
    
    // Navigate to the previous cell in the current word
    private void navigateToPreviousCell(int currentRow, int currentCol) {
        Placement targetWord = currentTypingWord;
        
        // If no current typing word, or current cell is not part of current typing word, find one
        if (targetWord == null || !targetWord.coversCell(currentRow, currentCol)) {
            // Prioritize ACROSS words at intersections for consistent navigation
            Placement acrossWord = null;
            Placement downWord = null;
            
            for (Placement p : placed) {
                if (p.coversCell(currentRow, currentCol)) {
                    if (p.dir == Direction.ACROSS) {
                        acrossWord = p;
                    } else {
                        downWord = p;
                    }
                }
            }
            
            targetWord = (acrossWord != null) ? acrossWord : downWord;
            currentTypingWord = targetWord;
        }
        
        if (targetWord != null) {
            // Calculate position within the word
            int wordPos = (targetWord.dir == Direction.ACROSS) ? 
                (currentCol - targetWord.col) : (currentRow - targetWord.row);
            
            // Move to previous position in the word
            int prevPos = wordPos - 1;
            if (prevPos >= 0) {
                int prevRow = (targetWord.dir == Direction.ACROSS) ? targetWord.row : targetWord.row + prevPos;
                int prevCol = (targetWord.dir == Direction.ACROSS) ? targetWord.col + prevPos : targetWord.col;
                
                if (prevRow >= 0 && prevCol >= 0) {
                    cellFields[prevRow][prevCol].requestFocus();
                }
            }
        }
    }
    // --- Helper classes & enums ---

    // Direction for placement
    private enum Direction { ACROSS, DOWN }

    // Placement data
    private static class Placement {
        final String word;
        final int row, col;
        final Direction dir;

        Placement(String word, int row, int col, Direction dir) {
            this.word = word;
            this.row = row;
            this.col = col;
            this.dir = dir;
        }

        boolean coversCell(int r, int c) {
            if (dir == Direction.ACROSS) {
                return r == row && c >= col && c < col + word.length();
            } else {
                return c == col && r >= row && r < row + word.length();
            }
        }

        @Override
        public String toString() {
            return String.format("%s@[%d,%d]%s", word, row, col, dir);
        }
    }

    // --- Trie implementation for dictionary/prefix checks ---
    private static class Trie {
        private static class Node {
            Node[] next = new Node[26];
            boolean end = false;
        }

        private final Node root = new Node();

        void insert(String s) {
            Node cur = root;
            for (char ch : s.toCharArray()) {
                if (ch < 'A' || ch > 'Z') continue;
                int idx = ch - 'A';
                if (cur.next[idx] == null) cur.next[idx] = new Node();
                cur = cur.next[idx];
            }
            cur.end = true;
        }

        boolean contains(String s) {
            Node cur = root;
            for (char ch : s.toCharArray()) {
                if (ch < 'A' || ch > 'Z') return false;
                int idx = ch - 'A';
                if (cur.next[idx] == null) return false;
                cur = cur.next[idx];
            }
            return cur.end;
        }

        boolean startsWith(String pref) {
            Node cur = root;
            for (char ch : pref.toCharArray()) {
                if (ch < 'A' || ch > 'Z') return false;
                int idx = ch - 'A';
                if (cur.next[idx] == null) return false;
                cur = cur.next[idx];
            }
            return true;
        }
    }
}
