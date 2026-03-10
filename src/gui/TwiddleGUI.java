package gui;

import game.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class TwiddleGUI extends JFrame {

    private static final Color BG_TOP = new Color(239, 246, 255);
    private static final Color BG_BOTTOM = new Color(219, 234, 254);
    private static final Color TILE_BG = new Color(255, 255, 255);
    private static final Color TILE_OK_BG = new Color(220, 252, 231);
    private static final Color TILE_BORDER = new Color(148, 163, 184);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color DARK_BUTTON = new Color(51, 65, 85);
    private static final Color TEXT_MAIN = new Color(17, 24, 39);
    private static final Color TEXT_SUBTLE = new Color(71, 85, 105);

    private static final String[] AI_METHODS = new String[] {
        "A*", "BFS", "Spatial D&C", "Cycle D&C", "Depth D&C", "MDF DP", "Backtracking AI"
    };

    private JComboBox<String> sizeBox;
    private JPanel methodsContainer;

    public TwiddleGUI() {
        setContentPane(new GradientPanel());
        setTitle("Twiddle Puzzle Method Comparison");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(1600, Math.max(1180, screen.width - 40));
        int height = Math.min(880, Math.max(700, screen.height - 60));
        setSize(width, height);
        setLocationRelativeTo(null);

        setupHeader();
        setupMethodsViewport();
        rebuildBoards();

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 10, 10, 10));
        setVisible(true);
    }

    private void setupHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Twiddle Puzzle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_MAIN);
        textPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("7 AI solvers vs 1 human board - all start from the same state");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SUBTLE);
        textPanel.add(subtitleLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        sizeBox = new JComboBox<>(new String[] {"3 x 3", "4 x 4"});
        styleComboBox(sizeBox);
        sizeBox.addActionListener(e -> rebuildBoards());

        JButton resetButton = createActionButton("New Shared Board", ACCENT, Color.WHITE);
        resetButton.addActionListener(e -> rebuildBoards());

        controls.add(sizeBox);
        controls.add(resetButton);

        header.add(textPanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void setupMethodsViewport() {
        methodsContainer = new JPanel(new GridLayout(2, 4, 8, 8));
        methodsContainer.setOpaque(false);
        add(methodsContainer, BorderLayout.CENTER);
    }

    private void rebuildBoards() {
        int n = sizeBox.getSelectedIndex() == 0 ? 3 : 4;
        Board seedBoard = new Board(n);
        int[][] initial = copyGrid(seedBoard.getGrid());

        methodsContainer.removeAll();

        for (String method : AI_METHODS) {
            methodsContainer.add(new MethodPanel(method, true, initial).container);
        }
        methodsContainer.add(new MethodPanel("Human", false, initial).container);

        methodsContainer.revalidate();
        methodsContainer.repaint();
    }

    private int[][] copyGrid(int[][] src) {
        int[][] c = new int[src.length][src[0].length];
        for (int i = 0; i < src.length; i++) {
            c[i] = src[i].clone();
        }
        return c;
    }

    private class MethodPanel {
        private final String method;
        private final boolean computerControlled;
        private final Board board;
        private final JPanel container;
        private final JLabel[][] cells;
        private final JLabel moveLabel;
        private final JLabel statusLabel;
        private final JButton computerButton;
        private final List<JButton> humanMoveButtons = new ArrayList<>();

        private DPFlow.Initialization dpInit;

        MethodPanel(String method, boolean computerControlled, int[][] initialGrid) {
            this.method = method;
            this.computerControlled = computerControlled;

            this.board = new Board(initialGrid.length);
            this.board.setGrid(copyGrid(initialGrid));

            container = new JPanel(new BorderLayout(8, 8));
            container.setOpaque(false);
                container.setPreferredSize(new Dimension(250, 280));
            container.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));

            JPanel top = new JPanel(new BorderLayout(4, 4));
            top.setOpaque(false);

            JLabel nameLabel = new JLabel(method);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setForeground(TEXT_MAIN);
            top.add(nameLabel, BorderLayout.NORTH);

            moveLabel = new JLabel("Moves: 0");
            moveLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            moveLabel.setForeground(TEXT_MAIN);
            top.add(moveLabel, BorderLayout.WEST);

            container.add(top, BorderLayout.NORTH);

            int size = board.size();
            cells = new JLabel[size][size];
            JPanel gridPanel = new JPanel(new GridLayout(size, size, 4, 4));
            gridPanel.setBackground(new Color(248, 250, 252));
            gridPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
            gridPanel.setPreferredSize(new Dimension(160, 160));

            Font tileFont = new Font("Segoe UI", Font.BOLD, 16);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    JLabel label = new JLabel("", SwingConstants.CENTER);
                    label.setFont(tileFont);
                    label.setOpaque(true);
                    label.setBackground(TILE_BG);
                    label.setForeground(TEXT_MAIN);
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                            new EmptyBorder(2, 2, 2, 2)
                    ));
                    cells[i][j] = label;
                    gridPanel.add(label);
                }
            }

            container.add(gridPanel, BorderLayout.CENTER);

            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

            statusLabel = new JLabel("Status: Ready");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            statusLabel.setForeground(TEXT_SUBTLE);
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bottom.add(statusLabel);
            bottom.add(Box.createVerticalStrut(6));

            if (computerControlled) {
                computerButton = createActionButton("Computer Move", DARK_BUTTON, Color.WHITE);
                computerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                computerButton.addActionListener(e -> runComputerMove());
                bottom.add(computerButton);
            } else {
                computerButton = null;
                JPanel moveButtons = new JPanel(new GridLayout(board.size() - 1, board.size() - 1, 4, 4));
                moveButtons.setOpaque(false);
                moveButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

                for (int i = 1; i <= board.totalMoves(); i++) {
                    final int move = i;
                    JButton button = createActionButton(String.valueOf(i), DARK_BUTTON, Color.WHITE);
                    button.addActionListener(e -> runHumanMove(move));
                    humanMoveButtons.add(button);
                    moveButtons.add(button);
                }
                bottom.add(moveButtons);
            }

            container.add(bottom, BorderLayout.SOUTH);
            refreshBoard();
        }

        private void runHumanMove(int move) {
            if (!computerControlled && !board.isSolved()) {
                board.executeMove(move);
                refreshBoard();
                updateMoves();
                statusLabel.setText("Status: Human chose move " + move);
                checkSolved();
            }
        }

        private void runComputerMove() {
            if (!computerControlled || board.isSolved()) {
                return;
            }

            if ("MDF DP".equals(method)) {
                runDPMove();
                return;
            }

            Player computer = buildComputerPlayer();
            if (computer == null) {
                statusLabel.setText("Status: Unknown method");
                return;
            }

            statusLabel.setText("Status: " + method + " is thinking...");
            computerButton.setEnabled(false);

            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() {
                    return computer.getMove();
                }

                @Override
                protected void done() {
                    try {
                        int move = get();
                        board.executeMove(move);
                        refreshBoard();
                        updateMoves();
                        statusLabel.setText("Status: " + method + " chose move " + move);
                        checkSolved();
                    } catch (Exception e) {
                        statusLabel.setText("Status: Calculation failed");
                    } finally {
                        if (!board.isSolved()) {
                            computerButton.setEnabled(true);
                        }
                    }
                }
            }.execute();
        }

        private Player buildComputerPlayer() {
            switch (method) {
                case "A*": {
                    ComputerPlayer p = new ComputerPlayer(board);
                    p.setAlgorithm(false);
                    return p;
                }
                case "BFS": {
                    ComputerPlayer p = new ComputerPlayer(board);
                    p.setAlgorithm(true);
                    return p;
                }
                case "Spatial D&C": {
                    ComputerPlayer2 p = new ComputerPlayer2(board);
                    p.setMode(1);
                    return p;
                }
                case "Cycle D&C": {
                    ComputerPlayer2 p = new ComputerPlayer2(board);
                    p.setMode(2);
                    return p;
                }
                case "Depth D&C": {
                    ComputerPlayer2 p = new ComputerPlayer2(board);
                    p.setMode(3);
                    return p;
                }
                case "Backtracking AI":
                    return new BacktrackingPlayer(board);
                default:
                    return null;
            }
        }

        private void runDPMove() {
            if (!ensureDPReady()) {
                return;
            }

            if (!dpInit.session().isSolvable(board)) {
                statusLabel.setText("Status: Board not solvable by current DP table");
                return;
            }

            DPFlow.StepResult step = dpInit.session().playNextMove(board);
            refreshBoard();
            updateMoves();

            Integer remaining = step.estimatedRemaining();
            if (remaining != null) {
                statusLabel.setText("Status: MDF DP move " + step.move() + " | Remaining: " + remaining);
            } else {
                statusLabel.setText("Status: MDF DP move " + step.move());
            }
            checkSolved();
        }

        private boolean ensureDPReady() {
            if (dpInit != null) {
                return true;
            }

            if (board.size() > 3) {
                int answer = JOptionPane.showConfirmDialog(
                        TwiddleGUI.this,
                        "MDF DP can be heavy for 4x4 boards and may take time. Continue?",
                        "Build DP Table",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (answer != JOptionPane.YES_OPTION) {
                    statusLabel.setText("Status: MDF DP build cancelled");
                    return false;
                }
            }

            Cursor prev = getCursor();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                statusLabel.setText("Status: Building DP table...");
                dpInit = DPFlow.initialize(board);
            } finally {
                setCursor(prev);
            }

            if (dpInit.reshuffles() > 0) {
                refreshBoard();
                updateMoves();
                statusLabel.setText("Status: Reshuffled to DP-solvable state");
            }

            if (!dpInit.session().isSolvable(board)) {
                statusLabel.setText("Status: Could not find DP-solvable board");
                return false;
            }

            return true;
        }

        private void refreshBoard() {
            int[][] g = board.getGrid();
            for (int i = 0; i < g.length; i++) {
                for (int j = 0; j < g[0].length; j++) {
                    int value = g[i][j];
                    cells[i][j].setText(String.valueOf(value));
                    cells[i][j].setBackground(isCorrectPosition(i, j, value, g.length) ? TILE_OK_BG : TILE_BG);
                }
            }
        }

        private void updateMoves() {
            moveLabel.setText("Moves: " + board.getMoves());
        }

        private void checkSolved() {
            if (!board.isSolved()) {
                return;
            }

            statusLabel.setText("Status: Solved in " + board.getMoves() + " moves");
            if (computerButton != null) {
                computerButton.setEnabled(false);
            }
            for (JButton b : humanMoveButtons) {
                b.setEnabled(false);
            }
        }
    }

    private boolean isCorrectPosition(int row, int col, int value, int size) {
        return value == row * size + col + 1;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_MAIN);
        comboBox.setFocusable(false);
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
            new EmptyBorder(3, 6, 3, 6)
        );
        comboBox.setBorder(border);
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            LinearGradientPaint paint = new LinearGradientPaint(
                    new Point2D.Float(0, 0),
                    new Point2D.Float(0, getHeight()),
                    new float[] {0f, 1f},
                    new Color[] {BG_TOP, BG_BOTTOM}
            );
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TwiddleGUI::new);
    }
}
