package game;

import java.util.*;

public class ComputerPlayer extends AbstractPlayer {

    private boolean useBFS = false;

    public ComputerPlayer(Board board) {
        super(board);
    }

    public void setAlgorithm(boolean bfs) {
        this.useBFS = bfs;
    }

    @Override
    public int getMove() {
        return useBFS ? solveBFS() : solveAStar();
    }

    @Override
    public String getName() {
        return useBFS ? "BFS Computer" : "A* Computer";
    }

    private static class State {
        int[][] grid;
        int g, h, f;
        int move;
        State parent;

        State(int[][] g, int cost, int mv, State p) {
            grid = g;
            this.g = cost;
            move = mv;
            parent = p;
            h = heuristic(g);
            f = this.g + h;
        }
    }

    private int solveAStar() {
        PriorityQueue<State> open =
                new PriorityQueue<>(Comparator.comparingInt(s -> s.f));

        Map<String, Integer> closed = new HashMap<>();

        State start = new State(copy(board.getGrid()), 0, -1, null);
        open.add(start);

        while (!open.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) return 1;
            State cur = open.poll();
            String key = encode(cur.grid);

            if (closed.containsKey(key) && closed.get(key) <= cur.g)
                continue;

            closed.put(key, cur.g);

            if (isSolved(cur.grid))
                return extractFirstMove(cur);

            for (int m = 1; m <= board.totalMoves(); m++) {
                if (Thread.currentThread().isInterrupted()) return 1;
                int[][] next = rotate(cur.grid, m);
                open.add(new State(next, cur.g + 1, m, cur));
            }
        }
        return 1;
    }

    private int solveBFS() {
        Queue<State> q = new LinkedList<>();
        Set<String> seen = new HashSet<>();

        State start = new State(copy(board.getGrid()), 0, -1, null);
        q.add(start);
        seen.add(encode(start.grid));

        while (!q.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) return 1;
            State cur = q.poll();

            if (isSolved(cur.grid))
                return extractFirstMove(cur);

            for (int m = 1; m <= board.totalMoves(); m++) {
                if (Thread.currentThread().isInterrupted()) return 1;
                int[][] next = rotate(cur.grid, m);
                String key = encode(next);
                if (!seen.contains(key)) {
                    seen.add(key);
                    q.add(new State(next, 0, m, cur));
                }
            }
        }
        return 1;
    }

    private static int heuristic(int[][] g) {
        int v = 1, mis = 0;
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g[0].length; j++)
                if (g[i][j] != v++) mis++;
        return (int) Math.ceil(mis / 4.0);
    }

    private int extractFirstMove(State s) {
        while (s.parent != null && s.parent.parent != null)
            s = s.parent;
        return s.move;
    }

    private boolean isSolved(int[][] g) {
        int v = 1;
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g[0].length; j++)
                if (g[i][j] != v++) return false;
        return true;
    }

    private int[][] rotate(int[][] src, int move) {
        int N = src.length;
        int[][] g = copy(src);

        int r = (move - 1) / (N - 1);
        int c = (move - 1) % (N - 1);

        int tmp = g[r][c];
        g[r][c] = g[r][c + 1];
        g[r][c + 1] = g[r + 1][c + 1];
        g[r + 1][c + 1] = g[r + 1][c];
        g[r + 1][c] = tmp;

        return g;
    }

    private int[][] copy(int[][] src) {
        int[][] c = new int[src.length][src[0].length];
        for (int i = 0; i < src.length; i++)
            c[i] = src[i].clone();
        return c;
    }

    private String encode(int[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int[] r : g)
            for (int x : r)
                sb.append(x).append(',');
        return sb.toString();
    }
}
