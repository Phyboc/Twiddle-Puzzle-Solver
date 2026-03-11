package game;

import java.util.*;

public class TopDownDPPlayer extends AbstractPlayer {

    // The DP Memoization Table
    // Key format: "encodedGrid:remainingDepth"
    // Value: The best move to make from this state
    private Map<String, Integer> memo = new HashMap<>();

    public TopDownDPPlayer(Board board) {
        super(board);
    }

    @Override
    public int getMove() {
        int[][] currentGrid = board.getGrid();
        memo.clear(); // Clear memo for a fresh search from the current state
        
        // Iteratively increase the depth limit to ensure we find the shortest path
        for (int maxDepth = 1; maxDepth <= 15; maxDepth++) { 
            Set<String> visitedPath = new HashSet<>();
            int move = topDownDP(currentGrid, 0, maxDepth, visitedPath);
            if (move != -1) {
                return move;
            }
        }
        return 1; // Fallback move if no solution is found within depth 15
    }

    @Override
    public String getName() {
        return "Top-Down DP";
    }

    private int topDownDP(int[][] grid, int currentDepth, int maxDepth, Set<String> visited) {
        if (isSolved(grid)) {
            return 0; // 0 indicates the goal has been reached
        }
        
        // Prune branches that can't possibly reach the goal in the remaining depth
        if (currentDepth + estimateRemaining(grid) > maxDepth) {
            return -1; 
        }

        String stateKey = encode(grid);
        
        // DP Memoization Check: Have we seen this exact state with this much remaining depth?
        String memoKey = stateKey + ":" + (maxDepth - currentDepth);
        if (memo.containsKey(memoKey)) {
            return memo.get(memoKey);
        }

        // Cycle detection for the current recursive path
        if (visited.contains(stateKey)) return -1;
        visited.add(stateKey);

        int bestMoveForThisState = -1;

        // Try all possible Twiddle rotations
        for (int m = 1; m <= board.totalMoves(); m++) {
            int[][] nextGrid = rotateCopy(grid, m);
            
            // Recursive relation
            int result = topDownDP(nextGrid, currentDepth + 1, maxDepth, visited);
            
            if (result != -1) {
                bestMoveForThisState = m; // Found a valid path to the goal
                break; 
            }
        }

        visited.remove(stateKey);
        
        // DP Memoization Save: Store the best move for this specific sub-problem
        memo.put(memoKey, bestMoveForThisState);
        
        return bestMoveForThisState;
    }

    private int estimateRemaining(int[][] g) {
        int misplaced = 0;
        int v = 1;
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g[0].length; j++) {
                if (g[i][j] != v++) misplaced++;
            }
        }
        return (int) Math.ceil(misplaced / 4.0);
    }

    private boolean isSolved(int[][] g) {
        int v = 1;
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g[0].length; j++) {
                if (g[i][j] != v++) return false;
            }
        }
        return true;
    }

    private int[][] rotateCopy(int[][] src, int move) {
        int N = src.length;
        int[][] g = new int[N][N];
        for (int i = 0; i < N; i++) {
            g[i] = src[i].clone();
        }

        int r = (move - 1) / (N - 1);
        int c = (move - 1) % (N - 1);
        int tmp = g[r][c];
        g[r][c] = g[r][c + 1];
        g[r][c + 1] = g[r + 1][c + 1];
        g[r + 1][c + 1] = g[r + 1][c];
        g[r + 1][c] = tmp;
        return g;
    }

    private String encode(int[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int[] r : g) {
            for (int x : r) sb.append(x).append(',');
        }
        return sb.toString();
    }
}
