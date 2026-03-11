package game;

import java.util.*;

public class BidirectionalBFSPlayer extends AbstractPlayer {

    public BidirectionalBFSPlayer(Board board) {
        super(board);
    }

    @Override
    public String getName() {
        return "Bidirectional BFS";
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    @Override
    public int getMove() {
        int[][] start = board.getGrid();

        // Already solved — shouldn't happen in normal GUI flow, but guard anyway
        if (isSolved(start)) return 1;

        // Each map: encoded_state → Node (parent encoding + move that produced it)
        Map<String, Node> fwdVisited = new HashMap<>();
        Map<String, Node> bwdVisited = new HashMap<>();

        // Use ArrayDeque for BFS — faster than LinkedList
        Deque<int[][]> fwdQueue = new ArrayDeque<>();
        Deque<int[][]> bwdQueue = new ArrayDeque<>();

        int[][] goal = buildGoal(board.size());

        // Seed both frontiers
        String startKey = encode(start);
        String goalKey  = encode(goal);

        fwdVisited.put(startKey, new Node(null, -1));
        bwdVisited.put(goalKey,  new Node(null, -1));

        fwdQueue.add(start);
        bwdQueue.add(goal);

        // Edge case: start == goal
        if (startKey.equals(goalKey)) return 1;

        while (!fwdQueue.isEmpty() || !bwdQueue.isEmpty()) {

            // Always expand the smaller frontier to keep both balanced
            if (!fwdQueue.isEmpty() &&
                    (bwdQueue.isEmpty() || fwdQueue.size() <= bwdQueue.size())) {

                String hit = expandForward(fwdQueue, fwdVisited, bwdVisited);
                if (hit != null) return extractFirstMove(hit, fwdVisited, startKey);

            } else if (!bwdQueue.isEmpty()) {

                String hit = expandBackward(bwdQueue, bwdVisited, fwdVisited);
                if (hit != null) return extractFirstMove(hit, fwdVisited, startKey);
            }
        }

        // No solution found (should not happen on a valid Twiddle board)
        return 1;
    }

    // ── Forward expansion (apply normal forward moves) ────────────────────────

    private String expandForward(Deque<int[][]> queue,
                                  Map<String, Node> fwdVisited,
                                  Map<String, Node> bwdVisited) {
        int[][] state = queue.poll();
        String stateKey = encode(state);

        for (int m = 1; m <= board.totalMoves(); m++) {
            int[][] next = rotateCopy(state, m);
            String key   = encode(next);

            if (!fwdVisited.containsKey(key)) {
                fwdVisited.put(key, new Node(stateKey, m));
                queue.add(next);

                // Intersection found — backward frontier already reached this state
                if (bwdVisited.containsKey(key)) return key;
            }
        }
        return null;
    }

    // ── Backward expansion (apply INVERSE moves from goal side) ──────────────

    private String expandBackward(Deque<int[][]> queue,
                                   Map<String, Node> bwdVisited,
                                   Map<String, Node> fwdVisited) {
        int[][] state = queue.poll();
        String stateKey = encode(state);

        for (int m = 1; m <= board.totalMoves(); m++) {
            // rotateInverseCopy is the key: instead of asking
            // "where do we go FROM here?", we ask
            // "which state transitions INTO here via move m?"
            int[][] prev = rotateInverseCopy(state, m);
            String key   = encode(prev);

            if (!bwdVisited.containsKey(key)) {
                bwdVisited.put(key, new Node(stateKey, m));
                queue.add(prev);

                // Intersection found — forward frontier already reached this state
                if (fwdVisited.containsKey(key)) return key;
            }
        }
        return null;
    }

    // ── Path reconstruction ───────────────────────────────────────────────────
    //
    //  fwdVisited encodes:  start → ... → meeting_point
    //  We walk back from meeting_point to start and return the FIRST move taken.

    private int extractFirstMove(String meetingKey,
                                  Map<String, Node> fwdVisited,
                                  String startKey) {
        // Reconstruct the forward path from start to meeting point
        List<Integer> moves = new ArrayList<>();
        String cur = meetingKey;

        while (true) {
            Node node = fwdVisited.get(cur);
            if (node == null || node.move == -1) break; // reached the root
            moves.add(node.move);
            cur = node.parentKey;
        }

        if (moves.isEmpty()) return 1; // meeting point IS the start (shouldn't happen)

        // moves is in reverse order (meeting→start), so the first real move is last
        return moves.get(moves.size() - 1);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** Forward (clockwise) twiddle — same rotation as Board.executeMove */
    private int[][] rotateCopy(int[][] src, int move) {
        int N   = src.length;
        int[][] g = deepCopy(src);
        int r   = (move - 1) / (N - 1);
        int c   = (move - 1) % (N - 1);

        int tmp    = g[r][c];
        g[r][c]    = g[r][c + 1];
        g[r][c+1]  = g[r+1][c+1];
        g[r+1][c+1]= g[r+1][c];
        g[r+1][c]  = tmp;
        return g;
    }

    /**
     * Inverse (counter-clockwise) twiddle.
     * Identical to ComputerPlayer3.rotateInverseCopy — reproduced here so this
     * class has no dependency on ComputerPlayer3.
     *
     * If forward is:  TL→TR→BR→BL→TL
     * Inverse is:     TL→BL→BR→TR→TL
     */
    private int[][] rotateInverseCopy(int[][] src, int move) {
        int N   = src.length;
        int[][] g = deepCopy(src);
        int r   = (move - 1) / (N - 1);
        int c   = (move - 1) % (N - 1);

        int tmp    = g[r][c];
        g[r][c]    = g[r+1][c];
        g[r+1][c]  = g[r+1][c+1];
        g[r+1][c+1]= g[r][c+1];
        g[r][c+1]  = tmp;
        return g;
    }

    private int[][] buildGoal(int n) {
        int[][] goal = new int[n][n];
        int v = 1;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                goal[i][j] = v++;
        return goal;
    }

    private boolean isSolved(int[][] g) {
        int v = 1;
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g[0].length; j++)
                if (g[i][j] != v++) return false;
        return true;
    }

    private int[][] deepCopy(int[][] src) {
        int[][] c = new int[src.length][];
        for (int i = 0; i < src.length; i++) c[i] = src[i].clone();
        return c;
    }

    private String encode(int[][] g) {
        StringBuilder sb = new StringBuilder(g.length * g.length * 3);
        for (int[] row : g)
            for (int x : row)
                sb.append(x).append(',');
        return sb.toString();
    }

    // ── Inner record ──────────────────────────────────────────────────────────

    /** Stores how we arrived at a given state during BFS. */
    private static class Node {
        final String parentKey; // encoded state we came from (null at root)
        final int move;         // move that produced this state (-1 at root)

        Node(String parentKey, int move) {
            this.parentKey = parentKey;
            this.move      = move;
        }
    }
}