package gui;

import game.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class TwiddleGUI extends JFrame {

    private static final Color BG_DARK       = new Color(10,  14,  26);
    private static final Color BG_MID        = new Color(16,  22,  42);
    private static final Color PANEL_BG      = new Color(22,  30,  56);
    private static final Color PANEL_BORDER  = new Color(45,  55,  90);
    private static final Color ACCENT        = new Color(245, 168,  30);
    private static final Color ACCENT_DIM    = new Color(180, 115,  10);
    private static final Color SOLVED_BG     = new Color(34,  90,  55);
    private static final Color SOLVED_TILE   = new Color(52, 211, 110);
    private static final Color TILE_BG       = new Color(30,  40,  72);
    private static final Color TILE_BORDER   = new Color(60,  75, 120);
    private static final Color TEXT_BRIGHT   = new Color(240, 240, 255);
    private static final Color TEXT_DIM      = new Color(130, 145, 185);
    private static final Color BTN_HUMAN     = new Color(60,  80, 160);
    private static final Color BTN_COMPUTER  = new Color(50,  65, 130);
    private static final Color HIGHLIGHT_ROTATE = new Color(255, 210, 70);

    private static final Font FONT_TITLE  = new Font("Georgia",     Font.BOLD,  22);
    private static final Font FONT_CARD   = new Font("Georgia",     Font.BOLD,  13);
    private static final Font FONT_TILE   = new Font("Courier New", Font.BOLD,  17);
    private static final Font FONT_LABEL  = new Font("Courier New", Font.PLAIN, 11);
    private static final Font FONT_BTN    = new Font("Georgia",     Font.BOLD,  11);
    private static final Font FONT_STATUS = new Font("Courier New", Font.PLAIN, 10);

    private static final int DEFAULT_AUTO_PLAY_DELAY_MS = 3500;
    private static final int ROTATE_FLASH_MS = 700;

    private static final String[] AI_METHODS = {
        "A*", "BFS", "Bidirectional BFS", "Spatial D&C",
        "Cycle D&C", "Depth D&C", "MDF DP", "Backtracking AI", "Top-Down DP"
    };

    private JComboBox<String> sizeBox;
    private JSpinner globalDelaySpinner;
    private JButton globalAutoPlayButton;
    private JPanel methodsContainer;
    private final List<MethodPanel> methodPanels = new ArrayList<>();

    public TwiddleGUI() {
        setContentPane(new GradientPanel());
        setTitle("Twiddle Puzzle — Method Arena");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1800, Math.max(1300, screen.width  - 40));
        int h = Math.min(920,  Math.max(720,  screen.height - 60));
        setSize(w, h);
        setLocationRelativeTo(null);

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 12, 12, 12));

        buildHeader();
        buildMethodsArea();
        rebuildBoards();

        setVisible(true);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TWIDDLE PUZZLE");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT);
        titleBlock.add(title);

        JLabel sub = new JLabel("9 solvers · 1 board · may the best algorithm win");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_DIM);
        titleBlock.add(sub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        sizeBox = new JComboBox<>(new String[]{"3 × 3", "4 × 4"});
        styleCombo(sizeBox);
        sizeBox.addActionListener(e -> rebuildBoards());

        JButton rulesBtn = makeBtn("Rules",     new Color(80, 60, 20), ACCENT);
        JButton resetBtn = makeBtn("New Board", BTN_COMPUTER,          TEXT_BRIGHT);
        globalAutoPlayButton = makeBtn("Global Auto Play", new Color(85, 110, 200), TEXT_BRIGHT);

        JLabel delayLabel = new JLabel("Auto Delay (s)");
        delayLabel.setFont(FONT_STATUS);
        delayLabel.setForeground(TEXT_DIM);
        globalDelaySpinner = new JSpinner(new SpinnerNumberModel(
            DEFAULT_AUTO_PLAY_DELAY_MS / 1000.0, 0.0, 5.0, 0.5));
        styleDelaySpinner(globalDelaySpinner, ACCENT);

        rulesBtn.addActionListener(e -> showRulesDialog());
        resetBtn.addActionListener(e -> {
            stopGlobalAutoPlay();
            rebuildBoards();
        });
        globalAutoPlayButton.addActionListener(e -> toggleGlobalAutoPlay());

        controls.add(sizeBox);
        controls.add(delayLabel);
        controls.add(globalDelaySpinner);
        controls.add(globalAutoPlayButton);
        controls.add(rulesBtn);
        controls.add(resetBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(controls,   BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ── Rules dialog ─────────────────────────────────────────────────────────

    private void showRulesDialog() {
        JDialog dlg = new JDialog(this, "Twiddle — Rules & Algorithms", true);
        dlg.setSize(640, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MID);
        dlg.setContentPane(root);

        JPanel stripe = new JPanel(new BorderLayout());
        stripe.setBackground(PANEL_BG);
        stripe.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel heading = new JLabel("How to Play & Algorithm Guide");
        heading.setFont(new Font("Georgia", Font.BOLD, 17));
        heading.setForeground(ACCENT);
        stripe.add(heading, BorderLayout.WEST);
        root.add(stripe, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_MID);
        body.setBorder(new EmptyBorder(16, 22, 16, 22));

        String[][] sections = {
            {
                "Objective",
                "Arrange the numbered tiles into ascending order (1, 2, 3 ... n^2) reading " +
                "left-to-right, top-to-bottom. The puzzle is solved when every tile sits " +
                "in its correct position."
            },
            {
                "The Twiddle Move",
                "A 'twiddle' rotates a 2x2 sub-grid of the board by 90 degrees clockwise. " +
                "On an n x n board there are (n-1)^2 such sub-grids, each numbered 1 to (n-1)^2. " +
                "Move k corresponds to the sub-grid whose top-left corner is at " +
                "row floor((k-1)/(n-1)), column (k-1) mod (n-1)."
            },
            {
                "Human Mode",
                "Numbered buttons appear below your board. Press a button to apply that " +
                "twiddle. Tiles that are already in their correct position are highlighted " +
                "in green. Try to beat the AIs!"
            },
            {
                "A* Search",
                "An informed best-first search guided by an admissible heuristic (misplaced " +
                "tiles / 4). Guaranteed to find the optimal (fewest-move) solution. " +
                "Can be slow on large boards due to state-space size."
            },
            {
                "BFS",
                "Breadth-first search explores all states level by level. Also optimal, " +
                "but consumes more memory than A* because no heuristic prunes the frontier."
            },
            {
                "Bidirectional BFS",
                "Runs two simultaneous BFS frontiers: one forward from the scrambled state, " +
                "one backward from the goal using inverse twiddle moves. They expand toward " +
                "each other and stop the moment they meet. Explores ~b^(d/2) states instead " +
                "of b^d — a dramatic speedup over standard BFS while still guaranteeing an " +
                "optimal solution."
            },
            {
                "Spatial D&C",
                "Divide & Conquer that splits the board into spatial regions and solves " +
                "each independently. Fast, but may not yield the minimum move count."
            },
            {
                "Cycle D&C",
                "Divide & Conquer based on permutation cycles. Decomposes the goal " +
                "permutation into independent cycles and solves each cycle in sequence."
            },
            {
                "Depth D&C",
                "Divide & Conquer that recurses by board depth (rows/columns). Tiles are " +
                "placed into their final rows first, then columns."
            },
            {
                "MDF DP",
                "Minimum-Depth-First Dynamic Programming. Pre-computes a lookup table of " +
                "optimal move sequences for reachable board states (max 2,000,000 states). " +
                "Very fast once built, but table construction can be time-consuming for 4x4."
            },
            {
                "Backtracking AI",
                "Iterative-deepening backtracking with move ordering and pruning. Explores " +
                "the move tree and backtracks when a dead-end is detected. Falls back to a " +
                "greedy best move if no solution is found within depth 10."
            },
            {
                "Top-Down DP",
                "Top-down memoised search keyed on (state, remaining-depth). Combines " +
                "iterative deepening with a memo table so repeated sub-problems are solved " +
                "only once. Bridges backtracking and dynamic programming."
            },
            {
                "Tips",
                "- Click 'New Board' to generate a fresh scrambled board shared by all solvers.\n" +
                "- Switch between 3x3 and 4x4 with the size selector — 4x4 is significantly harder.\n" +
                "- Green tile = correctly placed. Count your moves against each AI's total!"
            }
        };

        for (String[] sec : sections) {
            body.add(makeSectionLabel(sec[0]));
            body.add(Box.createVerticalStrut(4));
            body.add(makeBodyText(sec[1]));
            body.add(Box.createVerticalStrut(14));
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_MID);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        styleScrollBar(scroll);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        footer.setBackground(PANEL_BG);
        JButton close = makeBtn("Close", ACCENT_DIM, TEXT_BRIGHT);
        close.addActionListener(e -> dlg.dispose());
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.BOLD, 13));
        l.setForeground(ACCENT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextArea makeBodyText(String text) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(new Font("Courier New", Font.PLAIN, 12));
        ta.setForeground(TEXT_BRIGHT);
        ta.setBackground(BG_MID);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setOpaque(false);
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return ta;
    }

    private void styleScrollBar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(BG_MID);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(70, 85, 140);
                trackColor = BG_MID;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ── Methods viewport ─────────────────────────────────────────────────────

    private void buildMethodsArea() {
        // 9 AI panels + 1 Human panel = 10 total -> 2 rows x 5 columns
        methodsContainer = new JPanel(new GridLayout(2, 5, 10, 10));
        methodsContainer.setOpaque(false);
        add(methodsContainer, BorderLayout.CENTER);
    }

    private void rebuildBoards() {
        int n = sizeBox.getSelectedIndex() == 0 ? 3 : 4;
        Board seed = new Board(n);
        int[][] initial = copyGrid(seed.getGrid());

        stopGlobalAutoPlay();
        methodPanels.clear();
        methodsContainer.removeAll();
        for (String m : AI_METHODS) {
            MethodPanel panel = new MethodPanel(m, true, initial);
            methodPanels.add(panel);
            methodsContainer.add(panel.container);
        }
        MethodPanel humanPanel = new MethodPanel("Human", false, initial);
        methodPanels.add(humanPanel);
        methodsContainer.add(humanPanel.container);

        methodsContainer.revalidate();
        methodsContainer.repaint();
        updateGlobalAutoPlayButton();
    }

    private void toggleGlobalAutoPlay() {
        if (isAnyComputerAutoPlaying()) {
            stopGlobalAutoPlay();
        } else {
            startGlobalAutoPlay();
        }
    }

    private void startGlobalAutoPlay() {
        for (MethodPanel panel : methodPanels) {
            if (panel.isComputerPanel() && !panel.isSolvedBoard()) {
                panel.startAutoPlay();
            }
        }
        updateGlobalAutoPlayButton();
    }

    private void stopGlobalAutoPlay() {
        for (MethodPanel panel : methodPanels) {
            if (panel.isComputerPanel()) {
                panel.stopAutoPlay();
            }
        }
        updateGlobalAutoPlayButton();
    }

    private boolean isAnyComputerAutoPlaying() {
        for (MethodPanel panel : methodPanels) {
            if (panel.isComputerPanel() && panel.isAutoPlaying()) {
                return true;
            }
        }
        return false;
    }

    private void updateGlobalAutoPlayButton() {
        if (globalAutoPlayButton == null) return;
        globalAutoPlayButton.setText(isAnyComputerAutoPlaying() ? "Stop Global Auto" : "Global Auto Play");
    }

    // ── MethodPanel ──────────────────────────────────────────────────────────

    private class MethodPanel {
        final JPanel container;
        private final Board board;
        private final JLabel[][] cells;
        private final JLabel moveLabel, statusLabel;
        private final JButton computerButton;
        private final JButton autoPlayButton;
        private final JButton stopButton;
        private SwingWorker<MoveResult, Void> worker;
        private final List<JButton> humanBtns = new ArrayList<>();
        private final String method;
        private final boolean isComputer;
        private DPFlow.Initialization dpInit;
        private Timer autoPlayTimer;
        private Timer rotateFlashTimer;
        private boolean autoPlaying;

        private class MoveResult {
            final int move;
            final Integer remaining;
            final boolean alreadyApplied;

            MoveResult(int move, Integer remaining, boolean alreadyApplied) {
                this.move = move;
                this.remaining = remaining;
                this.alreadyApplied = alreadyApplied;
            }
        }

        MethodPanel(String method, boolean isComputer, int[][] initial) {
            this.method     = method;
            this.isComputer = isComputer;
            this.board      = new Board(initial.length);
            this.board.setGrid(copyGrid(initial));

            container = new RoundedPanel(14, PANEL_BG, PANEL_BORDER);
            container.setLayout(new BorderLayout(0, 6));
            container.setBorder(new EmptyBorder(10, 10, 10, 10));

            // ── Top bar ──
            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setOpaque(false);

            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            nameRow.setOpaque(false);
            JLabel dot = new JLabel("*");
            dot.setForeground(isComputer ? new Color(100, 160, 255) : ACCENT);
            dot.setFont(new Font("Courier New", Font.PLAIN, 10));
            JLabel nameLabel = new JLabel(method);
            nameLabel.setFont(FONT_CARD);
            nameLabel.setForeground(TEXT_BRIGHT);
            nameRow.add(dot);
            nameRow.add(nameLabel);

            moveLabel = new JLabel("0 moves");
            moveLabel.setFont(FONT_STATUS);
            moveLabel.setForeground(ACCENT);

            top.add(nameRow,   BorderLayout.WEST);
            top.add(moveLabel, BorderLayout.EAST);
            container.add(top, BorderLayout.NORTH);

            // ── Grid ──
            int size = board.size();
            cells = new JLabel[size][size];
            JPanel grid = new JPanel(new GridLayout(size, size, 3, 3));
            grid.setBackground(BG_DARK);
            grid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_BORDER, 1, true),
                new EmptyBorder(3, 3, 3, 3)
            ));
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    JLabel lbl = new JLabel("", SwingConstants.CENTER);
                    lbl.setFont(FONT_TILE);
                    lbl.setOpaque(true);
                    lbl.setBackground(TILE_BG);
                    lbl.setForeground(TEXT_BRIGHT);
                    lbl.setBorder(BorderFactory.createLineBorder(TILE_BORDER, 1, true));
                    cells[i][j] = lbl;
                    grid.add(lbl);
                }
            }
            container.add(grid, BorderLayout.CENTER);

            // ── Bottom ──
            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

            statusLabel = new JLabel("Ready");
            statusLabel.setFont(FONT_STATUS);
            statusLabel.setForeground(TEXT_DIM);
            statusLabel.setAlignmentX(LEFT_ALIGNMENT);
            bottom.add(statusLabel);
            bottom.add(Box.createVerticalStrut(5));

            if (isComputer) {
                computerButton = makeBtn("Solve Next", BTN_COMPUTER, TEXT_BRIGHT);
                autoPlayButton = makeBtn("Auto Play", new Color(70, 100, 180), TEXT_BRIGHT);
                stopButton     = makeBtn("Terminate", ACCENT_DIM, TEXT_BRIGHT);
                stopButton.setAlignmentX(LEFT_ALIGNMENT);
                stopButton.setEnabled(false);
                stopButton.addActionListener(e -> {
                    stopAutoPlay();
                    if (worker != null && !worker.isDone()) {
                        worker.cancel(true);
                        statusLabel.setText("Cancelled");
                    }
                });

                computerButton.setAlignmentX(LEFT_ALIGNMENT);
                computerButton.addActionListener(e -> runComputerMove());

                autoPlayButton.setAlignmentX(LEFT_ALIGNMENT);
                autoPlayButton.addActionListener(e -> {
                    if (autoPlaying) {
                        stopAutoPlay();
                        statusLabel.setForeground(TEXT_DIM);
                        statusLabel.setText("Auto play stopped");
                    } else {
                        startAutoPlay();
                    }
                });

                JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                btnRow.setOpaque(false);
                btnRow.add(computerButton);
                btnRow.add(autoPlayButton);
                bottom.add(btnRow);

                JPanel stopRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                stopRow.setOpaque(false);
                stopRow.add(stopButton);
                bottom.add(Box.createVerticalStrut(4));
                bottom.add(stopRow);
            } else {
                computerButton = null;
                autoPlayButton = null;
                stopButton = null;
                JPanel moveGrid = new JPanel(
                    new GridLayout(board.size() - 1, board.size() - 1, 3, 3));
                moveGrid.setOpaque(false);
                moveGrid.setAlignmentX(LEFT_ALIGNMENT);
                for (int i = 1; i <= board.totalMoves(); i++) {
                    final int mv = i;
                    JButton b = makeBtn(String.valueOf(i), BTN_HUMAN, TEXT_BRIGHT);
                    b.setFont(new Font("Courier New", Font.BOLD, 11));
                    b.addActionListener(e -> runHumanMove(mv));
                    humanBtns.add(b);
                    moveGrid.add(b);
                }
                bottom.add(moveGrid);
            }

            container.add(bottom, BorderLayout.SOUTH);
            refreshBoard();
        }

        private void runHumanMove(int move) {
            if (board.isSolved()) return;
            if (!isValidMove(move)) { statusLabel.setText("Invalid move: " + move); return; }
            applyMoveWithHighlight(move, "Move " + move + " applied");
        }

        private void runComputerMove() {
            runComputerMove(false);
        }

        private void runComputerMove(boolean fromAutoPlay) {
            if (board.isSolved()) return;
            if (worker != null && !worker.isDone()) return;

            statusLabel.setForeground(TEXT_DIM);
            statusLabel.setText(fromAutoPlay ? "Auto thinking..." : "Thinking...");
            computerButton.setEnabled(false);
            if (autoPlayButton != null) {
                autoPlayButton.setEnabled(!board.isSolved());
            }
            if (stopButton != null) stopButton.setEnabled(true);

            worker = new SwingWorker<MoveResult, Void>() {
                @Override protected MoveResult doInBackground() {
                    try {
                        if ("MDF DP".equals(method)) {
                            if (!ensureDP()) {
                                return null;
                            }

                            if (!dpInit.session().isSolvable(board))
                                return null;

                            DPFlow.StepResult step = dpInit.session().playNextMove(board);
                            return new MoveResult(step.move(), step.estimatedRemaining(), true);
                        } else {
                            final Player p = buildPlayer();
                            if (p == null) return null;
                            return new MoveResult(p.getMove(), null, false);
                        }
                    } catch (Exception ex) {
                        if (isCancelled()) return null;
                        throw ex;
                    }
                }

                @Override protected void done() {
                    try {
                        if (isCancelled()) {
                            statusLabel.setForeground(TEXT_DIM);
                            statusLabel.setText("Cancelled");
                            return;
                        }
                        MoveResult result = get();
                        if (result != null) {
                            if (!isValidMove(result.move)) {
                                statusLabel.setForeground(TEXT_DIM);
                                statusLabel.setText("Invalid move returned: " + result.move);
                                return;
                            }
                            if (result.alreadyApplied) {
                                refreshBoard(result.move);
                                moveLabel.setText(board.getMoves() + " moves");
                                statusLabel.setForeground(TEXT_DIM);
                                statusLabel.setText("Move " + result.move +
                                        (result.remaining != null ? " | ~" + result.remaining + " left" : ""));
                                checkSolved();
                            } else {
                                applyMoveWithHighlight(result.move, "Chose move " + result.move);
                            }
                        }
                    } catch (Exception ex) {
                        if (!isCancelled()) {
                            statusLabel.setForeground(TEXT_DIM);
                            statusLabel.setText("Calculation failed");
                        }
                    } finally {
                        if (!board.isSolved() && !autoPlaying) {
                            computerButton.setEnabled(true);
                        }
                        if (stopButton != null)
                            stopButton.setEnabled(autoPlaying);
                    }
                }
            };
            worker.execute();
        }

        private void startAutoPlay() {
            if (!isComputer || board.isSolved()) return;

            autoPlaying = true;
            computerButton.setEnabled(false);
            if (autoPlayButton != null) {
                autoPlayButton.setText("Stop Auto");
            }
            if (stopButton != null) stopButton.setEnabled(true);

            if (autoPlayTimer != null) {
                autoPlayTimer.stop();
            }

            int delayMs = restartAutoPlayTimer();
            if (autoPlayTimer == null) {
                return;
            }
            autoPlayTimer.setInitialDelay(0);
            autoPlayTimer.start();

            statusLabel.setForeground(TEXT_DIM);
            statusLabel.setText("Auto playing every " + formatDelaySeconds(delayMs) + "s");
            updateGlobalAutoPlayButton();
        }

        private int restartAutoPlayTimer() {
            if (autoPlayTimer != null) {
                autoPlayTimer.stop();
            }
            int delayMs = getAutoPlayDelayMs();
            autoPlayTimer = new Timer(delayMs, e -> {
                if (!autoPlaying) return;
                if (board.isSolved()) {
                    stopAutoPlay();
                    return;
                }
                if (worker != null && !worker.isDone()) return;
                runComputerMove(true);
            });
            return delayMs;
        }

        private void stopAutoPlay() {
            autoPlaying = false;
            if (autoPlayTimer != null) {
                autoPlayTimer.stop();
                autoPlayTimer = null;
            }
            if (autoPlayButton != null) {
                autoPlayButton.setText("Auto Play");
            }
            if (!board.isSolved() && computerButton != null) {
                computerButton.setEnabled(true);
            }
            if (stopButton != null && (worker == null || worker.isDone())) {
                stopButton.setEnabled(false);
            }
            updateGlobalAutoPlayButton();
        }

        private boolean isComputerPanel() {
            return isComputer;
        }

        private boolean isAutoPlaying() {
            return autoPlaying;
        }

        private boolean isSolvedBoard() {
            return board.isSolved();
        }

        private int getAutoPlayDelayMs() {
            if (globalDelaySpinner == null) return DEFAULT_AUTO_PLAY_DELAY_MS;
            Object value = globalDelaySpinner.getValue();
            double seconds = value instanceof Number ? ((Number) value).doubleValue() : 3.5;
            seconds = Math.max(0.0, Math.min(5.0, seconds));
            return (int) Math.round(seconds * 1000.0);
        }

        private String formatDelaySeconds(int delayMs) {
            return String.format(java.util.Locale.US, "%.1f", delayMs / 1000.0);
        }

        private boolean isValidMove(int move) {
            return move >= 1 && move <= board.totalMoves();
        }

        private Player buildPlayer() {
            switch (method) {
                case "A*":
                    { ComputerPlayer p = new ComputerPlayer(board); p.setAlgorithm(false); return p; }
                case "BFS":
                    { ComputerPlayer p = new ComputerPlayer(board); p.setAlgorithm(true); return p; }
                case "Bidirectional BFS":
                    return new BiDirBFSPlayer(board);
                case "Spatial D&C":
                    { ComputerPlayer2 p = new ComputerPlayer2(board); p.setMode(1); return p; }
                case "Cycle D&C":
                    { ComputerPlayer2 p = new ComputerPlayer2(board); p.setMode(2); return p; }
                case "Depth D&C":
                    { ComputerPlayer2 p = new ComputerPlayer2(board); p.setMode(3); return p; }
                case "Backtracking AI":
                    return new BacktrackingPlayer(board);
                case "Top-Down DP":
                    return new TopDownDPPlayer(board);
                default:
                    return null;
            }
        }

        private void runDPMove() {
            if (!ensureDP()) return;
            if (!dpInit.session().isSolvable(board)) {
                statusLabel.setText("Not solvable by DP");
                return;
            }
            DPFlow.StepResult step = dpInit.session().playNextMove(board);
            refreshBoard();
            moveLabel.setText(board.getMoves() + " moves");
            Integer rem = step.estimatedRemaining();
            statusLabel.setText("Move " + step.move() +
                (rem != null ? " | ~" + rem + " left" : ""));
            checkSolved();
        }

        private boolean ensureDP() {
            // make this method safe to call from either EDT or a background thread
            if (dpInit != null) return true;

            // show confirmation dialog on EDT and wait for result
            if (board.size() > 3) {
                final int[] ans = new int[1];
                try {
                    SwingUtilities.invokeAndWait(() ->
                        ans[0] = JOptionPane.showConfirmDialog(TwiddleGUI.this,
                            "MDF DP table for 4x4 may take time. Continue?",
                            "Build DP Table", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE)
                    );
                } catch (Exception e) {
                    return false;
                }
                if (ans[0] != JOptionPane.YES_OPTION) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Cancelled"));
                    return false;
                }
            }

            Cursor prev = getCursor();
            // update UI via EDT
            SwingUtilities.invokeLater(() -> setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
            SwingUtilities.invokeLater(() -> statusLabel.setText("Building DP table..."));

            try {
                dpInit = DPFlow.initialize(board);
            } catch (RuntimeException e) {
                if (Thread.currentThread().isInterrupted()) {
                    return false; // cancelled during build
                }
                throw e;
            } finally {
                SwingUtilities.invokeLater(() -> setCursor(prev));
            }

            if (dpInit.reshuffles() > 0) {
                SwingUtilities.invokeLater(() -> {
                    refreshBoard();
                    moveLabel.setText(board.getMoves() + " moves");
                    statusLabel.setText("Reshuffled");
                });
            }
            if (!dpInit.session().isSolvable(board)) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("No DP-solvable state found"));
                return false;
            }
            return true;
        }

        private void refreshBoard() {
            refreshBoard(-1);
        }

        private void refreshBoard(int highlightMove) {
            int[][] g = board.getGrid();
            int n = g.length;

            int hr = -1, hc = -1;
            if (highlightMove >= 1 && highlightMove <= board.totalMoves()) {
                hr = (highlightMove - 1) / (n - 1);
                hc = (highlightMove - 1) % (n - 1);
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int v = g[i][j];
                    boolean ok = (v == i * n + j + 1);
                    cells[i][j].setText(String.valueOf(v));
                    cells[i][j].setBackground(ok ? SOLVED_BG  : TILE_BG);
                    cells[i][j].setForeground(ok ? SOLVED_TILE : TEXT_BRIGHT);

                    boolean inHighlighted2x2 =
                        hr != -1 && (i == hr || i == hr + 1) && (j == hc || j == hc + 1);
                    if (inHighlighted2x2) {
                        cells[i][j].setBorder(BorderFactory.createLineBorder(HIGHLIGHT_ROTATE, 3, true));
                    } else {
                        cells[i][j].setBorder(BorderFactory.createLineBorder(TILE_BORDER, 1, true));
                    }
                }
            }
        }

        private void applyMoveWithHighlight(int move, String message) {
            board.executeMove(move);
            refreshBoard(move);
            moveLabel.setText(board.getMoves() + " moves");
            statusLabel.setForeground(TEXT_DIM);
            statusLabel.setText(message);

            if (rotateFlashTimer != null) {
                rotateFlashTimer.stop();
            }
            rotateFlashTimer = new Timer(ROTATE_FLASH_MS, e -> {
                refreshBoard();
                ((Timer) e.getSource()).stop();
            });
            rotateFlashTimer.setRepeats(false);
            rotateFlashTimer.start();

            checkSolved();
        }

        private void checkSolved() {
            if (!board.isSolved()) return;
            stopAutoPlay();
            statusLabel.setForeground(SOLVED_TILE);
            statusLabel.setText("Solved in " + board.getMoves() + " moves");
            if (computerButton != null) computerButton.setEnabled(false);
            if (autoPlayButton != null) autoPlayButton.setEnabled(false);
            if (stopButton != null) stopButton.setEnabled(false);
            humanBtns.forEach(b -> b.setEnabled(false));
            updateGlobalAutoPlayButton();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int[][] copyGrid(int[][] src) {
        int[][] c = new int[src.length][src[0].length];
        for (int i = 0; i < src.length; i++) c[i] = src[i].clone();
        return c;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(5, 12, 5, 12));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) b.setBackground(bg.brighter());
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(bg);
            }
        });
        return b;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(FONT_BTN);
        cb.setBackground(PANEL_BG);
        cb.setForeground(TEXT_BRIGHT);
        cb.setFocusable(false);
        cb.setBorder(BorderFactory.createLineBorder(PANEL_BORDER, 1, true));
    }

    private void styleDelaySpinner(JSpinner spinner, Color digitColor) {
        spinner.setPreferredSize(new Dimension(58, 24));
        spinner.setFont(FONT_STATUS);
        spinner.setBackground(PANEL_BG);
        spinner.setForeground(TEXT_BRIGHT);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(PANEL_BG);
            tf.setForeground(digitColor);
            tf.setCaretColor(digitColor);
            tf.setBorder(new EmptyBorder(2, 4, 2, 4));
        }
    }

    // ── Custom components ────────────────────────────────────────────────────

    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(
                0, 0, new Color(8, 12, 24),
                0, getHeight(), new Color(18, 24, 48)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color bg, border;
        RoundedPanel(int arc, Color bg, Color border) {
            this.arc = arc; this.bg = bg; this.border = border;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                    UIManager.put("control",               new Color(16, 22, 42));
                    UIManager.put("info",                  new Color(16, 22, 42));
                    UIManager.put("nimbusBase",            new Color(18, 24, 48));
                    UIManager.put("nimbusBlueGrey",        new Color(30, 40, 72));
                    UIManager.put("nimbusFocus",           new Color(245, 168, 30));
                    UIManager.put("text",                  new Color(240, 240, 255));
                    UIManager.put("OptionPane.background", new Color(16, 22, 42));
                    UIManager.put("Panel.background",      new Color(16, 22, 42));
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(TwiddleGUI::new);
    }
}