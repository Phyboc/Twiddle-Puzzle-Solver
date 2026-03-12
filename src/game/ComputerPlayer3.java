package game;
import java.util.*;

public class ComputerPlayer3 {
    private static final int DEFAULT_MAX_STATES = 2_000_000;

    public static class DPData {
        private final Map<String, Integer> distance;
        private final Map<String, Integer> bestMove;
        private final boolean truncated;

        DPData(Map<String, Integer> distance, Map<String, Integer> bestMove, boolean truncated) {
            this.distance = distance;
            this.bestMove = bestMove;
            this.truncated = truncated;
        }

        public int stateCount() {
            return distance.size();
        }
        public boolean isTruncated() {
            return truncated;
        }
    }

    public static DPData buildDPTable(Board board) {
        return buildDPTable(board, DEFAULT_MAX_STATES);
    }

    public static DPData buildDPTable(Board board, int maxStates) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, Integer> bestMove = new HashMap<>();

        int N = board.size();
        int[][] goal = new int[N][N];
        int v = 1;
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                goal[i][j] = v++;
        Queue<int[][]> q = new ArrayDeque<>();
        q.add(goal);
        distance.put(encode(goal), 0);
        boolean truncated = false;
        while (!q.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
            int[][] state = q.poll();
            int dist = distance.get(encode(state));

            for (int m = 1; m <= board.totalMoves(); m++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException(new InterruptedException());
                }
                int[][] prev = rotateInverseCopy(state, m);
                String key = encode(prev);
                if (!distance.containsKey(key)) {
                    if (distance.size() >= maxStates) {
                        truncated = true;
                        return new DPData(distance, bestMove, truncated);
                    }
                    distance.put(key, dist + 1);
                    bestMove.put(key, m);
                    q.add(prev);
                }
            }
        }
        return new DPData(distance, bestMove, truncated);
    }

    public static boolean isSolvable(Board board, DPData data) {
        return data.distance.containsKey(encode(board.getGrid()));
    }
    public static Integer distanceToGoal(Board board, DPData data) {
        return data.distance.get(encode(board.getGrid()));
    }

    public static int chooseMove(Board board, DPData data, Set<String> visited) {
        int[][] grid = board.getGrid();
        String key = encode(grid);

        Integer direct = data.bestMove.get(key);
        if (direct != null)
            return direct;
        int bestMove = -1;
        int bestDist = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) break;
            int[][] next = rotateCopy(grid, m);
            String nextKey = encode(next);
            if (visited != null && visited.contains(nextKey)) continue;
            Integer d = data.distance.get(nextKey);
            if (d != null && d < bestDist) {
                bestDist = d;
                bestMove = m;
            }
        }
        if (bestMove != -1)
            return bestMove;
        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) break;
            int[][] next = rotateCopy(grid, m);
            Integer d = data.distance.get(encode(next));
            if (d != null && d < bestDist) {
                bestDist = d;
                bestMove = m;
            }
        }
        return bestMove == -1 ? 1 : bestMove;
    }

    private static int[][] rotateCopy(int[][] src, int move) {
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

    private static int[][] rotateInverseCopy(int[][] src, int move) {
        int N = src.length;
        int[][] g = new int[N][N];
        for (int i = 0; i < N; i++)
            g[i] = src[i].clone();
        int r = (move - 1) / (N - 1);
        int c = (move - 1) % (N - 1);

        int tmp = g[r][c];
        g[r][c] = g[r + 1][c];
        g[r + 1][c] = g[r + 1][c + 1];
        g[r + 1][c + 1] = g[r][c + 1];
        g[r][c + 1] = tmp;
        return g;
    }

    private static String encode(int[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int[] r : g)
            for (int x : r)
                sb.append(x).append(',');
        return sb.toString();
    }
}