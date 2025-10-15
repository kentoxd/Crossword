import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class JavaCrosswordGenerator {
    private static final int ROWS = 18;
    private static final int COLS = 18;

    private char[][] solution = new char[ROWS][COLS];
    private final Trie trie = new Trie();
    private final List<String> words = new ArrayList<>();
    private final Map<String, String> clues = new HashMap<>();
    
    
    private final JFrame frame = new JFrame("DSA Crossword Generator");
    private final JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS));
    private final JPanel coordinateGridPanel = new JPanel(new BorderLayout());
    private final JTextArea clueArea = new JTextArea();
    private final JTextField[][] cellFields = new JTextField[ROWS][COLS];
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private int score = 0;

    private final List<Placement> placed = new ArrayList<>();
    private Placement currentTypingWord = null;
    
    // User action undo/redo
    private final Deque<UserAction> userUndoStack = new ArrayDeque<>();
    private final Deque<UserAction> userRedoStack = new ArrayDeque<>();
    private boolean isUndoRedoAction = false; // Flag to prevent recording during undo/redo

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JavaCrosswordGenerator app = new JavaCrosswordGenerator();
            app.setupDictionaryAndClues();
            app.buildUI();
            app.loadDefaultCrossword();
            app.frame.setVisible(true);
        });
    }

    private void setupDictionaryAndClues() {
        putWord("QUEUE", "A linear data structure where elements are processed in First In, First Out (FIFO) order");
        putWord("BUCKETSORT", "A sorting algorithm that distributes elements into groups (or \"bins\") before sorting them individually");
        putWord("SHELLSORT", "An in-place sorting algorithm that generalizes insertion sort by comparing elements separated by a gap");
        putWord("ARRAY", "A fixed-size collection of elements stored in contiguous memory locations");
        putWord("LINKEDLIST", "A linear data structure where each element (node) contains a reference to the next node");
        putWord("ALGORITHM", "A step-by-step procedure or formula for solving a problem");
        putWord("BUBBLESORT", "A simple sorting algorithm that repeatedly swaps adjacent elements if they are in the wrong order");
        putWord("STACK", "A linear data structure that follows the Last In, First Out (LIFO) principle");
        putWord("TREE", "A hierarchical data structure with nodes connected by edges");
        putWord("HASH", "A data structure that maps keys to values for efficient lookup");
        putWord("GRAPH", "A non-linear data structure consisting of vertices and edges");
        putWord("HEAP", "A complete binary tree that satisfies the heap property");
        putWord("SEARCH", "Algorithm to find elements in a data structure");
        putWord("SORT", "Algorithm to order elements in a specific sequence");
        putWord("LIST", "A linear collection of elements");
        putWord("BINARY", "Base-2 number system or tree with two children");
        putWord("BACKTRACK", "Algorithm technique that tries solutions and reverts if unsuccessful");
        putWord("TRIE", "A tree-like data structure for storing strings with common prefixes");
        putWord("BUBBLE", "Simple sorting algorithm that repeatedly swaps adjacent elements");
        putWord("ALGORITHM", "A step-by-step procedure for solving a problem");
        putWord("QUICKSORT", "Efficient sorting algorithm using divide and conquer");
        putWord("MERGESORT", "Stable sorting algorithm using divide and conquer");
        putWord("HEAPSORT", "Sorting algorithm using heap data structure");
        putWord("BUCKETSORT", "Sorting algorithm that distributes elements into buckets");
        putWord("SHELLSORT", "In-place sorting algorithm with gap-based comparisons");

        for (String w : words) trie.insert(w);
    }

    private void putWord(String w, String clue) {
        words.add(w.toUpperCase());
        clues.put(w.toUpperCase(), clue);
    }

    private void loadDefaultCrossword() {
        // Load Level 1 - Easy crossword with proper intersections
        loadLevel(1);
    }
    
    private void loadLevel(int levelNumber) {
        clearGrid();
        placed.clear();
        currentTypingWord = null;
        
        // Hardcoded levels with proper intersections
        switch (levelNumber) {
            case 1:
                loadLevel1();
                break;
            case 2:
                loadLevel2();
                break;
            case 3:
                loadLevel3();
                break;
            case 4:
                loadLevel4();
                break;
            default:
                JOptionPane.showMessageDialog(frame, "Level " + levelNumber + " not available!");
                return;
        }
        
        for (Placement p : placed) {
            applyPlacementToSolution(p);
        }
        
        updateUIFromSolution(false);
        updateClueArea();
        computeScoreFromSolution();
        frame.setTitle("DSA Crossword - Level " + levelNumber);
    }
    
    private void loadLevel1() {
        // Level 1: Easy - Basic Data Structures
        // STACK (across) intersects with TREE (down) at 'T'
        // ARRAY (across) intersects with TREE (down) at 'R'
        placed.add(new Placement("STACK", 5, 5, Direction.ACROSS));
        placed.add(new Placement("TREE", 5, 5, Direction.DOWN));
        placed.add(new Placement("ARRAY", 7, 5, Direction.ACROSS));
        placed.add(new Placement("HEAP", 5, 7, Direction.DOWN));
    }
    
    private void loadLevel2() {
        // Level 2: Medium - Algorithms
        // HASH (across) intersects with HEAP (down) at 'H'
        // SEARCH (across) intersects with HEAP (down) at 'E'
        placed.add(new Placement("HASH", 4, 4, Direction.ACROSS));
        placed.add(new Placement("HEAP", 4, 4, Direction.DOWN));
        placed.add(new Placement("SEARCH", 5, 4, Direction.ACROSS));
        placed.add(new Placement("SORT", 4, 6, Direction.DOWN));
        placed.add(new Placement("LIST", 7, 4, Direction.ACROSS));
    }
    
    private void loadLevel3() {
        // Level 3: Hard - Advanced Data Structures
        // BINARY (across) intersects with BACKTRACK (down) at 'B'
        // GRAPH (across) intersects with BACKTRACK (down) at 'A'
        placed.add(new Placement("BINARY", 3, 3, Direction.ACROSS));
        placed.add(new Placement("BACKTRACK", 3, 3, Direction.DOWN));
        placed.add(new Placement("GRAPH", 4, 3, Direction.ACROSS));
        placed.add(new Placement("TRIE", 3, 5, Direction.DOWN));
        placed.add(new Placement("QUEUE", 6, 3, Direction.ACROSS));
        placed.add(new Placement("BUBBLE", 3, 7, Direction.DOWN));
    }
    
    private void loadLevel4() {
        // Level 4: Expert - Complex Algorithms
        // ALGORITHM (across) intersects with BACKTRACK (down) at 'A'
        // QUICKSORT (across) intersects with BACKTRACK (down) at 'K'
        placed.add(new Placement("ALGORITHM", 2, 2, Direction.ACROSS));
        placed.add(new Placement("BACKTRACK", 2, 2, Direction.DOWN));
        placed.add(new Placement("QUICKSORT", 3, 2, Direction.ACROSS));
        placed.add(new Placement("MERGESORT", 4, 2, Direction.ACROSS));
        placed.add(new Placement("HEAPSORT", 5, 2, Direction.ACROSS));
        placed.add(new Placement("BUCKETSORT", 2, 4, Direction.DOWN));
        placed.add(new Placement("SHELLSORT", 2, 6, Direction.DOWN));
    }

    private void buildUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 1000);
        frame.setResizable(false); // Make GUI fixed size
        frame.setLayout(new BorderLayout());

        gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rebuildGridUI();

        JPanel rightPanel = new JPanel(new BorderLayout());
        clueArea.setEditable(false);
        clueArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        clueArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane clueScroll = new JScrollPane(clueArea);
        clueScroll.setPreferredSize(new Dimension(400, 700));

        JPanel topButtons = new JPanel(new GridLayout(5, 2, 6, 6));
        JButton checkBtn = new JButton("Check");
        JButton revealBtn = new JButton("Reveal");
        JButton hintBtn = new JButton("Hint");
        JButton resetBtn = new JButton("Reset");
        JButton level1Btn = new JButton("Level 1 (Easy)");
        JButton level2Btn = new JButton("Level 2 (Medium)");
        JButton level3Btn = new JButton("Level 3 (Hard)");
        JButton level4Btn = new JButton("Level 4 (Expert)");
        JButton undoBtn = new JButton("Undo (Ctrl+Z)");
        JButton redoBtn = new JButton("Redo (Ctrl+Y)");

        checkBtn.addActionListener(_ -> checkAllWords());
        revealBtn.addActionListener(_ -> {
            updateUIFromSolution(true);
            computeScoreFromSolution();
        });
        hintBtn.addActionListener(_ -> showHint());
        resetBtn.addActionListener(_ -> {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (cellFields[r][c].isEditable()) {
                        cellFields[r][c].setText("");
                        cellFields[r][c].setBackground(Color.WHITE);
                    }
                }
            }
            resetGameState();
        });
        
        level1Btn.addActionListener(_ -> loadLevel(1));
        level2Btn.addActionListener(_ -> loadLevel(2));
        level3Btn.addActionListener(_ -> loadLevel(3));
        level4Btn.addActionListener(_ -> loadLevel(4));
        
        undoBtn.addActionListener(_ -> undoLastAction());
        redoBtn.addActionListener(_ -> redoLastAction());

        topButtons.add(checkBtn);
        topButtons.add(revealBtn);
        topButtons.add(hintBtn);
        topButtons.add(resetBtn);
        topButtons.add(level1Btn);
        topButtons.add(level2Btn);
        topButtons.add(level3Btn);
        topButtons.add(level4Btn);
        topButtons.add(undoBtn);
        topButtons.add(redoBtn);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.add(scoreLabel);

        rightPanel.add(topButtons, BorderLayout.NORTH);
        rightPanel.add(clueScroll, BorderLayout.CENTER);
        rightPanel.add(scorePanel, BorderLayout.SOUTH);

        frame.add(coordinateGridPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        
        // Add keyboard shortcuts
        addKeyboardShortcuts();
    }
    
    private void addKeyboardShortcuts() {
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke("control Z");
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke("control Y");
        
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(undoKeyStroke, "undo");
        frame.getRootPane().getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                undoLastAction();
            }
        });
        
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(redoKeyStroke, "redo");
        frame.getRootPane().getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                redoLastAction();
            }
        });
    }

    private void rebuildGridUI() {
        coordinateGridPanel.removeAll();
        
        // Create a single panel with all components using a precise grid layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Create the crossword grid first
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(ROWS, COLS, 1, 1));
        
        // Store cell panels for later access
        JPanel[][] cellPanels = new JPanel[ROWS][COLS];
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                JPanel p = new JPanel(new BorderLayout());
                p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                p.setPreferredSize(new Dimension(35, 35));
                p.setMinimumSize(new Dimension(35, 35));
                p.setMaximumSize(new Dimension(35, 35));
                
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font("SansSerif", Font.BOLD, 18));
                tf.setBorder(null);
                
                final int currentRow = r;
                final int currentCol = c;
                
                // Document listener to track changes for undo/redo
                tf.getDocument().addDocumentListener(new DocumentListener() {
                    private String oldValue = "";
                    
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        handleChange();
                    }
                    
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        handleChange();
                    }
                    
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        handleChange();
                    }
                    
                    private void handleChange() {
                        if (!isUndoRedoAction && tf.isEditable()) {
                            String newValue = tf.getText();
                            char oldChar = oldValue.isEmpty() ? ' ' : oldValue.charAt(0);
                            char newChar = newValue.isEmpty() ? ' ' : newValue.charAt(0);
                            
                            if (oldChar != newChar) {
                                recordUserAction(currentRow, currentCol, oldChar, newChar);
                                oldValue = newValue;
                            }
                        }
                    }
                });
                
                // Input filter
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

                tf.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent e) {
                        if (currentTypingWord == null || !currentTypingWord.coversCell(currentRow, currentCol)) {
                            Placement acrossWord = null;
                            Placement downWord = null;
                            
                            for (Placement p : placed) {
                                if (p.coversCell(currentRow, currentCol)) {
                                    if (p.dir == Direction.ACROSS) acrossWord = p;
                                    else downWord = p;
                                }
                            }
                            currentTypingWord = (acrossWord != null) ? acrossWord : downWord;
                        }
                    }
                });
                
                tf.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                            e.consume();
                            switchToNextWord(currentRow, currentCol);
                        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT || 
                                   e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                            e.consume();
                            navigateToNextCell(currentRow, currentCol);
                        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT || 
                                   e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                            e.consume();
                            navigateToPreviousCell(currentRow, currentCol);
                        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                            // Handle backspace - move to previous cell after deleting
                            SwingUtilities.invokeLater(() -> {
                                if (tf.getText().isEmpty()) {
                                    navigateToPreviousCell(currentRow, currentCol);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void keyTyped(java.awt.event.KeyEvent e) {
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
                cellPanels[r][c] = p;
                gridPanel.add(p);
            }
        }
        
        // Add corner cell (empty)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel cornerLabel = new JLabel("", JLabel.CENTER);
        cornerLabel.setPreferredSize(new Dimension(35, 35));
        cornerLabel.setMinimumSize(new Dimension(35, 35));
        cornerLabel.setMaximumSize(new Dimension(35, 35));
        cornerLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cornerLabel.setBackground(Color.LIGHT_GRAY);
        cornerLabel.setOpaque(true);
        mainPanel.add(cornerLabel, gbc);
        
        // Add column labels (0-17)
        for (int c = 0; c < COLS; c++) {
            gbc.gridx = c + 1; gbc.gridy = 0;
            JLabel colLabel = new JLabel(String.valueOf(c), JLabel.CENTER);
            colLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            colLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            colLabel.setBackground(Color.LIGHT_GRAY);
            colLabel.setOpaque(true);
            colLabel.setPreferredSize(new Dimension(35, 35));
            colLabel.setMinimumSize(new Dimension(35, 35));
            colLabel.setMaximumSize(new Dimension(35, 35));
            mainPanel.add(colLabel, gbc);
        }
        
        // Add row labels (0-17) and grid cells
        for (int r = 0; r < ROWS; r++) {
            // Row label
            gbc.gridx = 0; gbc.gridy = r + 1;
            JLabel rowLabel = new JLabel(String.valueOf(r), JLabel.CENTER);
            rowLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            rowLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            rowLabel.setBackground(Color.LIGHT_GRAY);
            rowLabel.setOpaque(true);
            rowLabel.setPreferredSize(new Dimension(35, 35));
            rowLabel.setMinimumSize(new Dimension(35, 35));
            rowLabel.setMaximumSize(new Dimension(35, 35));
            mainPanel.add(rowLabel, gbc);
            
            // Grid cells for this row
            for (int c = 0; c < COLS; c++) {
                gbc.gridx = c + 1; gbc.gridy = r + 1;
                mainPanel.add(cellPanels[r][c], gbc);
            }
        }
        
        coordinateGridPanel.add(mainPanel, BorderLayout.CENTER);
        coordinateGridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        coordinateGridPanel.revalidate();
        coordinateGridPanel.repaint();
    }

    private void applyPlacementToSolution(Placement p) {
        for (int i = 0; i < p.word.length(); i++) {
            int r = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
            int c = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
            solution[r][c] = p.word.charAt(i);
        }
    }

    private void updateUIFromSolution(boolean revealLetters) {
        isUndoRedoAction = true; // Prevent recording these changes
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char sol = solution[r][c];
                JTextField tf = cellFields[r][c];
                JPanel parent = (JPanel) tf.getParent();
                
                if (sol == '#') {
                    tf.setText("");
                    tf.setEditable(false);
                    parent.setBackground(Color.BLACK);
                    tf.setVisible(false);
                } else {
                    parent.setBackground(Color.WHITE);
                    tf.setVisible(true);
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
        
        isUndoRedoAction = false;
    }

    private void updateClueArea() {
        StringBuilder sb = new StringBuilder();
        sb.append("DSA Crossword Puzzle\n");
        sb.append("====================\n\n");
        
        List<Placement> acrossWords = new ArrayList<>();
        List<Placement> downWords = new ArrayList<>();
        
        for (Placement p : placed) {
            if (p.dir == Direction.ACROSS) acrossWords.add(p);
            else downWords.add(p);
        }
        
        acrossWords.sort(Comparator.comparingInt((Placement p) -> p.row).thenComparingInt(p -> p.col));
        downWords.sort(Comparator.comparingInt((Placement p) -> p.col).thenComparingInt(p -> p.row));
        
        if (!acrossWords.isEmpty()) {
            sb.append("ACROSS:\n");
            for (int i = 0; i < acrossWords.size(); i++) {
                Placement p = acrossWords.get(i);
                sb.append(String.format("%d. [%d,%d] %s\n", i+1, p.row, p.col, 
                    clues.getOrDefault(p.word, "No clue")));
            }
            sb.append("\n");
        }
        
        if (!downWords.isEmpty()) {
            sb.append("DOWN:\n");
            for (int i = 0; i < downWords.size(); i++) {
                Placement p = downWords.get(i);
                sb.append(String.format("%d. [%d,%d] %s\n", i+1, p.row, p.col,
                    clues.getOrDefault(p.word, "No clue")));
            }
        }
        
        clueArea.setText(sb.toString());
    }

    private void switchToNextWord(int currentRow, int currentCol) {
        int currentIndex = (currentTypingWord != null) ? placed.indexOf(currentTypingWord) : -1;
        int nextIndex = (currentIndex + 1) % placed.size();
        Placement nextWord = placed.get(nextIndex);
        currentTypingWord = nextWord;
        cellFields[nextWord.row][nextWord.col].requestFocus();
    }

    private void checkAllWords() {
        if (placed.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No puzzle loaded.");
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
                char ch = (text != null && !text.isEmpty()) ? text.charAt(0) : '?';
                userWord.append(ch);
                if (ch != p.word.charAt(i)) correct = false;
            }
            
            boolean isValidWord = trie.contains(userWord.toString());
            if (isValidWord) validWords++;
            
            Color bg = correct ? new Color(200, 255, 200) : 
                      isValidWord ? new Color(255, 255, 200) : 
                      new Color(255, 220, 220);
            
            for (int i = 0; i < p.word.length(); i++) {
                int rr = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
                int cc = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
                cellFields[rr][cc].setBackground(bg);
            }
            
            if (correct) correctWords++;
        }
        
        score = correctWords * 10 + validWords * 5;
        updateScore();
        JOptionPane.showMessageDialog(frame, 
            String.format("Correct: %d/%d\nValid: %d/%d\nScore: %d", 
                correctWords, placed.size(), validWords, placed.size(), score));
    }

    private void computeScoreFromSolution() {
        score = placed.size() * 10;
        updateScore();
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private void showHint() {
        if (placed.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No puzzle loaded.");
            return;
        }
        
        StringBuilder hintText = new StringBuilder("Hints:\n\n");
        
        for (Placement p : placed) {
            StringBuilder currentWord = new StringBuilder();
            boolean hasInput = false;
            
            for (int i = 0; i < p.word.length(); i++) {
                int rr = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
                int cc = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
                String text = cellFields[rr][cc].getText();
                char ch = (!text.isEmpty()) ? text.charAt(0) : '_';
                currentWord.append(ch);
                if (ch != '_') hasInput = true;
            }
            
            hintText.append(String.format("%s [%d,%d]: ", p.dir, p.row, p.col));
            
            if (hasInput) {
                String partial = currentWord.toString();
                if (trie.startsWith(partial.replace("_", ""))) {
                    hintText.append("✓ ").append(partial);
                } else {
                    hintText.append("✗ ").append(partial);
                }
            } else {
                hintText.append(p.word.length()).append(" letters");
            }
            hintText.append("\n");
        }
        
        JOptionPane.showMessageDialog(frame, hintText.toString(), "Hints", JOptionPane.INFORMATION_MESSAGE);
    }

    private void navigateToNextCell(int currentRow, int currentCol) {
        Placement targetWord = currentTypingWord;
        
        if (targetWord == null || !targetWord.coversCell(currentRow, currentCol)) {
            for (Placement p : placed) {
                if (p.coversCell(currentRow, currentCol)) {
                    targetWord = p;
                    currentTypingWord = p;
                    break;
                }
            }
        }
        
        if (targetWord != null) {
            int wordPos = (targetWord.dir == Direction.ACROSS) ? 
                (currentCol - targetWord.col) : (currentRow - targetWord.row);
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
    
    private void navigateToPreviousCell(int currentRow, int currentCol) {
        Placement targetWord = currentTypingWord;
        
        if (targetWord == null || !targetWord.coversCell(currentRow, currentCol)) {
            for (Placement p : placed) {
                if (p.coversCell(currentRow, currentCol)) {
                    targetWord = p;
                    currentTypingWord = p;
                    break;
                }
            }
        }
        
        if (targetWord != null) {
            int wordPos = (targetWord.dir == Direction.ACROSS) ? 
                (currentCol - targetWord.col) : (currentRow - targetWord.row);
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

    // CONSTRAINT SATISFACTION
    private boolean canPlaceWord(String word, int row, int col, Direction dir) {
        if (dir == Direction.ACROSS) {
            if (col + word.length() > COLS) return false;
            if (col > 0 && solution[row][col-1] != '#') return false; // Check left
            if (col + word.length() < COLS && solution[row][col + word.length()] != '#') return false; // Check right
        } else {
            if (row + word.length() > ROWS) return false;
            if (row > 0 && solution[row-1][col] != '#') return false; // Check above
            if (row + word.length() < ROWS && solution[row + word.length()][col] != '#') return false; // Check below
        }
        
        int intersections = 0;
        for (int i = 0; i < word.length(); i++) {
            int r = (dir == Direction.ACROSS) ? row : row + i;
            int c = (dir == Direction.ACROSS) ? col + i : col;
            
            char existing = solution[r][c];
            if (existing != '#') {
                if (existing != word.charAt(i)) return false;
                intersections++;
            }
            
            // Check perpendicular conflicts
            if (dir == Direction.ACROSS) {
                if (r > 0 && solution[r-1][c] != '#' && existing == '#') return false;
                if (r < ROWS-1 && solution[r+1][c] != '#' && existing == '#') return false;
            } else {
                if (c > 0 && solution[r][c-1] != '#' && existing == '#') return false;
                if (c < COLS-1 && solution[r][c+1] != '#' && existing == '#') return false;
            }
        }
        
        return placed.isEmpty() || intersections > 0;
    }
    
    private List<Position> findValidPlacements(String word) {
        List<Position> positions = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                for (Direction dir : Direction.values()) {
                    if (canPlaceWord(word, r, c, dir)) {
                        int score = calculatePlacementScore(word, r, c, dir);
                        positions.add(new Position(r, c, dir, score));
                    }
                }
            }
        }
        positions.sort((a, b) -> Integer.compare(b.score, a.score));
        return positions;
    }
    
    private void removeWordFromGrid(Placement p) {
        for (int i = 0; i < p.word.length(); i++) {
            int r = (p.dir == Direction.ACROSS) ? p.row : p.row + i;
            int c = (p.dir == Direction.ACROSS) ? p.col + i : p.col;
            
            boolean usedByOther = false;
            for (Placement other : placed) {
                if (other != p && other.coversCell(r, c)) {
                    usedByOther = true;
                    break;
                }
            }
            if (!usedByOther) {
                solution[r][c] = '#';
            }
        }
    }

    // BACKTRACKING
    public boolean generatePuzzleBacktrack(List<String> wordsToPlace) {
        clearGrid();
        placed.clear();
        currentTypingWord = null;
        
        List<String> sorted = new ArrayList<>(wordsToPlace);
        sorted.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        return backtrack(sorted, 0);
    }
    
    private boolean backtrack(List<String> words, int idx) {
        if (idx >= words.size()) return true;
        
        String word = words.get(idx);
        List<Position> positions = findValidPlacements(word);
        
        for (Position pos : positions) {
            Placement p = new Placement(word, pos.row, pos.col, pos.dir);
            placed.add(p);
            applyPlacementToSolution(p);
            
            if (backtrack(words, idx + 1)) return true;
            
            placed.remove(p);
            removeWordFromGrid(p);
        }
        
        return false;
    }

    // HEURISTICS
    private int calculatePlacementScore(String word, int row, int col, Direction dir) {
        int score = 0;
        
        int intersections = 0;
        for (int i = 0; i < word.length(); i++) {
            int r = (dir == Direction.ACROSS) ? row : row + i;
            int c = (dir == Direction.ACROSS) ? col + i : col;
            if (solution[r][c] != '#') intersections++;
        }
        score += intersections * 10;
        
        int centerRow = ROWS / 2;
        int centerCol = COLS / 2;
        int distFromCenter = Math.abs(row - centerRow) + Math.abs(col - centerCol);
        score += Math.max(0, 20 - distFromCenter);
        
        score += word.length() * 2;
        
        int vowels = 0;
        for (char c : word.toCharArray()) {
            if ("AEIOU".indexOf(c) >= 0) vowels++;
        }
        score += vowels * 3;
        
        if (!placed.isEmpty()) {
            long across = placed.stream().filter(p -> p.dir == Direction.ACROSS).count();
            long down = placed.stream().filter(p -> p.dir == Direction.DOWN).count();
            if ((dir == Direction.ACROSS && across < down) || (dir == Direction.DOWN && down < across)) {
                score += 5;
            }
        }
        
        return score;
    }

    // RANDOMIZATION
    public boolean generateRandomPuzzle(List<String> wordsToPlace, int seed) {
        Random random = new Random(seed);
        clearGrid();
        placed.clear();
        currentTypingWord = null;
        
        List<String> shuffled = new ArrayList<>(wordsToPlace);
        Collections.shuffle(shuffled, random);
        shuffled.sort((a, b) -> {
            int diff = Integer.compare(b.length(), a.length());
            return diff != 0 ? diff : random.nextInt(3) - 1;
        });
        
        return backtrackRandom(shuffled, 0, random);
    }
    
    private boolean backtrackRandom(List<String> words, int idx, Random random) {
        if (idx >= words.size()) return true;
        
        String word = words.get(idx);
        List<Position> positions = findValidPlacements(word);
        
        if (positions.isEmpty()) return false;
        
        Collections.shuffle(positions, random);
        
        for (Position pos : positions) {
            Placement p = new Placement(word, pos.row, pos.col, pos.dir);
            placed.add(p);
            applyPlacementToSolution(p);
            
            if (backtrackRandom(words, idx + 1, random)) return true;
            
            placed.remove(p);
            removeWordFromGrid(p);
        }
        
        return false;
    }

    // UNDO/REDO FOR USER INPUT
    private void recordUserAction(int row, int col, char previousChar, char newChar) {
        if (previousChar == newChar) return;
        UserAction action = new UserAction(row, col, previousChar, newChar);
        userUndoStack.push(action);
    }
    
    public void undoLastAction() {
        if (userUndoStack.isEmpty()) {
            return;
        }
        
        isUndoRedoAction = true;
        UserAction action = userUndoStack.pop();
        userRedoStack.push(action);
        
        JTextField field = cellFields[action.row][action.col];
        field.setText(action.previousChar == ' ' ? "" : String.valueOf(action.previousChar));
        
        isUndoRedoAction = false;
    }
    
    public void redoLastAction() {
        if (userRedoStack.isEmpty()) {
            return;
        }
        
        isUndoRedoAction = true;
        UserAction action = userRedoStack.pop();
        userUndoStack.push(action);
        
        JTextField field = cellFields[action.row][action.col];
        field.setText(action.newChar == ' ' ? "" : String.valueOf(action.newChar));
        
        isUndoRedoAction = false;
    }
    
    
    private void resetGameState() {
        score = 0;
        currentTypingWord = null;
        userUndoStack.clear();
        userRedoStack.clear();
        updateScore();
    }
    
    private void clearGrid() {
        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(solution[r], '#');
        }
    }

    // HELPER CLASSES
    private enum Direction { ACROSS, DOWN }
    
    private static class Position {
        final int row, col;
        final Direction dir;
        final int score;
        
        Position(int row, int col, Direction dir, int score) {
            this.row = row;
            this.col = col;
            this.dir = dir;
            this.score = score;
        }
    }
    
    private static class UserAction {
        final int row, col;
        final char previousChar, newChar;
        
        UserAction(int row, int col, char previousChar, char newChar) {
            this.row = row;
            this.col = col;
            this.previousChar = previousChar;
            this.newChar = newChar;
        }
    }

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
    }

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