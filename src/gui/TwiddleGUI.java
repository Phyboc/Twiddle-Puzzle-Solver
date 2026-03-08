package gui;

import game.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
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

    private Board board;
    private JLabel[][] cells;
    private Player computer;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JComboBox<String> algoBox;
    private JComboBox<String> sizeBox;
    private JLabel moveLabel;
    private JLabel statusLabel;

    private JPanel gridPanel;
    private JPanel buttonsPanel;
    private JButton computerMoveButton;

    private DPFlow.Initialization dpInit;

    public TwiddleGUI() {
        setContentPane(new GradientPanel());
        setTitle("Twiddle Puzzle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(620, Math.max(560, screen.width - 80));
        int height = Math.min(860, Math.max(700, screen.height - 80));
        setSize(width, height);
        setLocationRelativeTo(null);

        setupHeaderAndControls();
        resetBoard();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });

        setVisible(true);
    }

    private void setupHeaderAndControls() {
        titleLabel = new JLabel("Twiddle Puzzle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_MAIN);
        add(titleLabel);

        subtitleLabel = new JLabel("Rotate 2x2 blocks and restore order");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SUBTLE);
        add(subtitleLabel);

        sizeBox = new JComboBox<>(new String[]{"3 x 3", "4 x 4"});
        styleComboBox(sizeBox);
        sizeBox.addActionListener(e -> resetBoard());
        add(sizeBox);

        algoBox = new JComboBox<>(new String[]{
                "A*",
                "BFS",
                "Spatial D&C",
                "Cycle D&C",
                "Depth D&C",
                "MDF DP"
        });
        styleComboBox(algoBox);
        add(algoBox);

        moveLabel = new JLabel("Moves: 0");
        moveLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        moveLabel.setForeground(TEXT_MAIN);
        add(moveLabel);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SUBTLE);
        add(statusLabel);

        computerMoveButton = createActionButton("Computer Move", ACCENT, Color.WHITE);
        computerMoveButton.addActionListener(e -> computerMove());
        add(computerMoveButton);
    }

    private void resetBoard() {
        int n = sizeBox.getSelectedIndex() == 0 ? 3 : 4;

        if (gridPanel != null) {
            remove(gridPanel);
        }
        if (buttonsPanel != null) {
            remove(buttonsPanel);
        }

        board = new Board(n);
        dpInit = null;
        statusLabel.setText("Status: Ready");

        setupGrid(n);
        setupMoveButtons();

        refreshBoard();
        updateMoves();
        relayout();

        repaint();
        revalidate();
    }

    private void setupGrid(int n) {
        cells = new JLabel[n][n];
        gridPanel = new JPanel(new GridLayout(n, n, 8, 8));
        gridPanel.setBackground(new Color(248, 250, 252));
        gridPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        Font tileFont = new Font("Segoe UI", Font.BOLD, 24);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(tileFont);
                label.setOpaque(true);
                label.setBackground(TILE_BG);
                label.setForeground(TEXT_MAIN);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                        new EmptyBorder(6, 6, 6, 6)
                ));
                cells[i][j] = label;
                gridPanel.add(label);
            }
        }

        add(gridPanel);
    }

    private void setupMoveButtons() {
        int cols = board.size() - 1;
        int total = board.totalMoves();
        int rows = (int) Math.ceil(total / (double) cols);

        buttonsPanel = new JPanel(new GridLayout(rows, cols, 8, 8));
        buttonsPanel.setOpaque(false);

        for (int i = 1; i <= total; i++) {
            final int move = i;
            JButton button = createActionButton(String.valueOf(i), DARK_BUTTON, Color.WHITE);
            button.addActionListener(e -> humanMove(move));
            buttonsPanel.add(button);
        }

        add(buttonsPanel);
    }

    private void relayout() {
        int contentW = getContentPane().getWidth();
        int contentH = getContentPane().getHeight();

        int sidePadding = 30;
        int fullW = Math.max(420, contentW - sidePadding * 2);

        titleLabel.setBounds(sidePadding, 12, fullW, 34);
        subtitleLabel.setBounds(sidePadding, 44, fullW, 20);
        sizeBox.setBounds(sidePadding, 70, 200, 34);

        int rows = board == null ? 3 : (int) Math.ceil(board.totalMoves() / (double) (board.size() - 1));
        int moveButtonHeight = 32;
        int moveGap = 8;
        int movePanelHeight = rows * moveButtonHeight + (rows - 1) * moveGap;

        int bottomSectionHeight = 34 + 6 + 20 + 8 + movePanelHeight + 10 + 36;
        int gridTop = 116;
        int gridBottomMax = contentH - 18 - bottomSectionHeight;

        int gridSize = Math.min(fullW, gridBottomMax - gridTop);
        gridSize = Math.max(220, gridSize);

        int gridX = (contentW - gridSize) / 2;
        gridPanel.setBounds(gridX, gridTop, gridSize, gridSize);

        int controlsY = gridTop + gridSize + 12;
        algoBox.setBounds(sidePadding, controlsY, Math.min(240, fullW / 2), 34);
        moveLabel.setBounds(contentW - sidePadding - 210, controlsY, 210, 34);

        statusLabel.setBounds(sidePadding, controlsY + 36 + 4, fullW, 20);

        buttonsPanel.setBounds(sidePadding, controlsY + 36 + 4 + 24 + 6, fullW, movePanelHeight);

        int computerY = buttonsPanel.getY() + movePanelHeight + 10;
        computerMoveButton.setBounds(sidePadding, computerY, fullW, 36);
    }

    private void humanMove(int move) {
        board.executeMove(move);
        refreshBoard();
        updateMoves();
        checkSolved();
    }

    private void computerMove() {
        String choice = (String) algoBox.getSelectedItem();

        if ("MDF DP".equals(choice)) {
            runDPMove();
            return;
        }

        switch (choice) {
            case "A*":
                ComputerPlayer aStar = new ComputerPlayer(board);
                aStar.setAlgorithm(false);
                computer = aStar;
                break;
            case "BFS":
                ComputerPlayer bfs = new ComputerPlayer(board);
                bfs.setAlgorithm(true);
                computer = bfs;
                break;
            case "Spatial D&C":
                ComputerPlayer2 spatial = new ComputerPlayer2(board);
                spatial.setMode(1);
                computer = spatial;
                break;
            case "Cycle D&C":
                ComputerPlayer2 cycle = new ComputerPlayer2(board);
                cycle.setMode(2);
                computer = cycle;
                break;
            case "Depth D&C":
                ComputerPlayer2 depth = new ComputerPlayer2(board);
                depth.setMode(3);
                computer = depth;
                break;
            default:
                return;
        }

        int move = computer.getMove();
        board.executeMove(move);

        refreshBoard();
        updateMoves();
        statusLabel.setText("Status: " + choice + " chose move " + move);
        checkSolved();
    }

    private void runDPMove() {
        if (!ensureDPReady()) {
            return;
        }

        if (!dpInit.session().isSolvable(board)) {
            statusLabel.setText("Status: Current board cannot reach goal with current DP policy");
            JOptionPane.showMessageDialog(this,
                    "This board configuration cannot reach the goal using the current DP table.",
                    "DP Solver",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DPFlow.StepResult step = dpInit.session().playNextMove(board);

        refreshBoard();
        updateMoves();

        Integer remaining = step.estimatedRemaining();
        if (remaining != null) {
            statusLabel.setText("Status: MDF DP chose move " + step.move() + " | Estimated remaining: " + remaining);
        } else {
            statusLabel.setText("Status: MDF DP chose move " + step.move());
        }
        checkSolved();
    }

    private boolean ensureDPReady() {
        if (dpInit != null) {
            return true;
        }

        if (board.size() > 3) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "MDF DP can be heavy for 4x4 boards and may take time. Continue?",
                    "Build DP Table",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                statusLabel.setText("Status: MDF DP build cancelled");
                return false;
            }
        }

        Cursor prev = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            statusLabel.setText("Status: Building DP table...");
            repaint();

            dpInit = DPFlow.initialize(board);
            ComputerPlayer3.DPData dpData = dpInit.session().data();

            String msg = "DP table built. States: " + dpData.stateCount();
            if (dpData.isTruncated()) {
                msg += " (truncated)";
            }
            statusLabel.setText("Status: " + msg);
        } finally {
            setCursor(prev);
        }

        if (!dpInit.session().isSolvable(board)) {
            JOptionPane.showMessageDialog(this,
                    "Could not find a solvable random board using current DP table.\n"
                            + "Try 3x3 or increase DP state limit in ComputerPlayer3.",
                    "DP Solver",
                    JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Status: DP table cannot solve current randomized board");
            return false;
        }

        if (dpInit.reshuffles() > 0) {
            refreshBoard();
            updateMoves();
            statusLabel.setText("Status: Board reshuffled to a DP-solvable state");
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

    private boolean isCorrectPosition(int row, int col, int value, int size) {
        return value == row * size + col + 1;
    }

    private void updateMoves() {
        moveLabel.setText("Moves: " + board.getMoves());
    }

    private void checkSolved() {
        if (board.isSolved()) {
            JOptionPane.showMessageDialog(this, "Puzzle solved!");
        }
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_MAIN);
        comboBox.setFocusable(false);
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        );
        comboBox.setBorder(border);
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
                    new float[]{0f, 1f},
                    new Color[]{BG_TOP, BG_BOTTOM}
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
