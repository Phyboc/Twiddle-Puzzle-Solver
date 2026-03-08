package game;

import java.util.*;

public class ComputerPlayer3 extends AbstractPlayer {

    private Map<String, Integer> valueTable = new HashMap<>();
    private boolean tableBuilt = false;

    public ComputerPlayer3(Board board) {
        super(board);
    }

    @Override
    public String getName() {
        return "DP Solver (Reverse BFS)";
    }

    @Override
    public int getMove() {

        if (!tableBuilt) {
            buildValueTable();
            tableBuilt = true;
            System.out.println("DP table built. States stored: " + valueTable.size());
        }

        int[][] grid = board.getGrid();
        int bestMove = 1;
        int bestValue = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {

            int[][] next = rotateCopy(grid, m);
            String key = encode(next);

            Integer val = valueTable.get(key);

            if (val != null && val < bestValue) {
                bestValue = val;
                bestMove = m;
            }
        }

        return bestMove;
    }

    // =============================
    // BUILD FULL DP TABLE
    // =============================

    private void buildValueTable() {

        int N = board.getGrid().length;

        int[][] goal = new int[N][N];
        int v = 1;

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                goal[i][j] = v++;

        Queue<int[][]> q = new ArrayDeque<>();

        q.add(goal);
        valueTable.put(encode(goal), 0);

        while (!q.isEmpty()) {

            int[][] state = q.poll();
            int dist = valueTable.get(encode(state));

            for (int m = 1; m <= board.totalMoves(); m++) {

                int[][] next = rotateCopy(state, m);
                String key = encode(next);

                if (!valueTable.containsKey(key)) {

                    valueTable.put(key, dist + 1);
                    q.add(next);
                }
            }
        }
    }

    // =============================
    // ROTATION
    // =============================

    private int[][] rotateCopy(int[][] src, int move) {

        int N = src.length;

        int[][] g = new int[N][N];

        for (int i = 0; i < N; i++)
            g[i] = src[i].clone();

        int r = (move - 1) / (N - 1);
        int c = (move - 1) % (N - 1);

        int tmp = g[r][c];
        g[r][c] = g[r][c + 1];
        g[r][c + 1] = g[r + 1][c + 1];
        g[r + 1][c + 1] = g[r + 1][c];
        g[r + 1][c] = tmp;

        return g;
    }

    // =============================
    // ENCODE STATE
    // =============================

    private String encode(int[][] g) {

        StringBuilder sb = new StringBuilder();

        for (int[] r : g)
            for (int x : r)
                sb.append(x).append(',');

        return sb.toString();
    }
}