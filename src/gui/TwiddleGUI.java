package gui;

import game.*;
import java.awt.*;
import javax.swing.*;

public class TwiddleGUI extends JFrame {

    private Board board;
    private JLabel[][] cells;
    private Player computer;

    private JComboBox<String> algoBox;
    private JComboBox<String> sizeBox;
    private JLabel moveLabel;

    private JPanel gridPanel;
    private JPanel buttonsPanel;

    public TwiddleGUI() {
        setTitle("Twiddle Puzzle");
        setSize(520, 720);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        sizeBox = new JComboBox<>(new String[]{"3 x 3", "4 x 4"});
        sizeBox.setBounds(40, 10, 200, 30);
        sizeBox.addActionListener(e -> resetBoard());
        add(sizeBox);

        setupControls();
        resetBoard();

        setVisible(true);
    }

    private void setupControls() {

        algoBox = new JComboBox<>(new String[]{
                "A*",
                "BFS",
                "Spatial D&C",
                "Cycle D&C",
                "Depth D&C"
        });
        algoBox.setBounds(40, 500, 200, 30);
        add(algoBox);

        moveLabel = new JLabel("Moves: 0");
        moveLabel.setBounds(260, 500, 150, 30);
        add(moveLabel);

        JButton comp = new JButton("Computer Move");
        comp.setBounds(40, 650, 440, 30);
        comp.addActionListener(e -> computerMove());
        add(comp);
    }

    private void resetBoard() {
        int N = sizeBox.getSelectedIndex() == 0 ? 3 : 4;

        if (gridPanel != null) remove(gridPanel);
        if (buttonsPanel != null) remove(buttonsPanel);

        board = new Board(N);

        setupGrid(N);
        setupMoveButtons(N);

        refreshBoard();
        updateMoves();

        repaint();
        revalidate();
    }

    private void setupGrid(int N) {
        cells = new JLabel[N][N];
        gridPanel = new JPanel(new GridLayout(N, N));
        gridPanel.setBounds(40, 50, 440, 440);

        Font f = new Font("Arial", Font.BOLD, 22);

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++) {
                JLabel l = new JLabel("", SwingConstants.CENTER);
                l.setFont(f);
                l.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                cells[i][j] = l;
                gridPanel.add(l);
            }

        add(gridPanel);
    }

    private void setupMoveButtons(int N) {
        buttonsPanel = new JPanel(new GridLayout(0, N - 1));
        buttonsPanel.setBounds(40, 550, 440, 80);

        for (int i = 1; i <= board.totalMoves(); i++) {
            final int mv = i;
            JButton b = new JButton("" + i);
            b.addActionListener(e -> humanMove(mv));
            buttonsPanel.add(b);
        }

        add(buttonsPanel);
    }

    // HUMAN MOVE
    private void humanMove(int mv) {
        board.executeMove(mv);
        refreshBoard();
        updateMoves();
        checkSolved();
    }

    // COMPUTER MOVE
    private void computerMove() {

        String choice = (String) algoBox.getSelectedItem();

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
        }

        int move = computer.getMove();
        board.executeMove(move);

        refreshBoard();
        updateMoves();
        checkSolved();
    }

    private void refreshBoard() {
        int[][] g = board.getGrid();
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g[0].length; j++)
                cells[i][j].setText("" + g[i][j]);
    }

    private void updateMoves() {
        moveLabel.setText("Moves: " + board.getMoves());
    }

    private void checkSolved() {
        if (board.isSolved())
            JOptionPane.showMessageDialog(this, "🎉 Puzzle Solved!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TwiddleGUI::new);
    }
}
