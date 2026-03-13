package game;

import java.util.HashSet;
import java.util.Set;

public class DPFlow {
    public static final int DEFAULT_MAX_RESHUFFLES = 2000;

    public static class Initialization {
        private final Session session;
        private final int reshuffles;
        private final long buildTimeNs;

        Initialization(Session session, int reshuffles, long buildTimeNs) {
            this.session = session;
            this.reshuffles = reshuffles;
            this.buildTimeNs = buildTimeNs;
        }

        public Session session() { return session; }
        public int reshuffles() { return reshuffles; }
        public long buildTimeNs() { return buildTimeNs; }
    }

    public static class Session {
        private final ComputerPlayer3.DPData data;
        private final Set<String> visited = new HashSet<>();
        private long lastMoveTimeNs = -1;

        Session(ComputerPlayer3.DPData data) {
            this.data = data;
        }

        public ComputerPlayer3.DPData data() { return data; }

        public boolean isSolvable(Board board) {
            return ComputerPlayer3.isSolvable(board, data);
        }

        public StepResult playNextMove(Board board) {
            if (!isSolvable(board))
                throw new IllegalStateException("Board cannot reach goal using current DP table");
            visited.add(encode(board.getGrid()));
            long start = System.nanoTime();
            int move = ComputerPlayer3.chooseMove(board, data, visited);
            lastMoveTimeNs = System.nanoTime() - start;
            board.executeMove(move);
            Integer remaining = ComputerPlayer3.distanceToGoal(board, data);
            return new StepResult(move, remaining);
        }

        public long getLastMoveTimeNs() { return lastMoveTimeNs; }
    }

    public static class StepResult {
        private final int move;
        private final Integer estimatedRemaining;

        StepResult(int move, Integer estimatedRemaining) {
            this.move = move;
            this.estimatedRemaining = estimatedRemaining;
        }

        public int move() { return move; }
        public Integer estimatedRemaining() { return estimatedRemaining; }
    }

    public static Initialization initialize(Board board) {
        return initialize(board, DEFAULT_MAX_RESHUFFLES);
    }

    public static Initialization initialize(Board board, int maxReshuffles) {
        long buildStart = System.nanoTime();
        ComputerPlayer3.DPData data = ComputerPlayer3.buildDPTable(board);
        long buildTimeNs = System.nanoTime() - buildStart;

        Session session = new Session(data);
        int reshuffles = 0;
        while (!session.isSolvable(board) && reshuffles < maxReshuffles) {
            board.randomize();
            reshuffles++;
        }
        return new Initialization(session, reshuffles, buildTimeNs);
    }

    private static String encode(int[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int[] r : g)
            for (int x : r)
                sb.append(x).append(',');
        return sb.toString();
    }
}