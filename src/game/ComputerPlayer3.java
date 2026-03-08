package game;

import java.util.*;

public class ComputerPlayer3 {

    public static Map<String, Integer> buildDPTable(Board board) {

        Map<String, Integer> dp = new HashMap<>();

        int N = board.size();

        int[][] goal = new int[N][N];
        int v = 1;

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                goal[i][j] = v++;

        Queue<int[][]> q = new ArrayDeque<>();

        q.add(goal);
        dp.put(encode(goal), 0);

        while (!q.isEmpty()) {

            int[][] state = q.poll();
            int dist = dp.get(encode(state));

            for (int m = 1; m <= board.totalMoves(); m++) {

                int[][] next = rotateCopy(state, m);
                String key = encode(next);

                if (!dp.containsKey(key)) {

                    dp.put(key, dist + 1);
                    q.add(next);
                }
            }
        }

        return dp;
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

    private static String encode(int[][] g) {

        StringBuilder sb = new StringBuilder();

        for (int[] r : g)
            for (int x : r)
                sb.append(x).append(',');

        return sb.toString();
    }
}