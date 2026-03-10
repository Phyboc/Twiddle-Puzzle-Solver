package game;
import java.util.*;

public class BacktrackingPlayer extends AbstractPlayer {

    public BacktrackingPlayer(Board board) {
        super(board);
    }

    @Override
    public int getMove() {
        int[][] currentGrid = board.getGrid();
        
        // Iterative Deepening
        for (int maxDepth = 1; maxDepth <= 10; maxDepth++) { 
            Set<String> visited = new HashSet<>();
            List<Integer> path = new ArrayList<>();
            
            if (backtrack(currentGrid, 0, maxDepth, path, visited)) {
                return path.isEmpty() ? 1 : path.get(0);
            }
        }
        return 1;
    }

    private boolean backtrack(int[][] grid, int depth, int maxDepth, List<Integer> path, Set<String> visited) {
        if (isSolved(grid)) return true;
        if (depth + estimateRemaining(grid) > maxDepth) return false;

        String stateKey = encode(grid);
        if (visited.contains(stateKey)) return false;
        visited.add(stateKey);

        // --- HEURISTIC MOVE ORDERING ---
        // 1. Create a list of all potential moves with their projected score
        List<MoveOption> options = new ArrayList<>();
        for (int m = 1; m <= board.totalMoves(); m++) {
            int[][] nextGrid = rotateCopy(grid, m);
            options.add(new MoveOption(m, estimateRemaining(nextGrid), nextGrid));
        }

        // 2. Sort moves by estimated distance to goal (ascending)
        options.sort(Comparator.comparingInt(o -> o.score));

        // 3. Explore ordered moves
        for (MoveOption opt : options) {
            path.add(opt.moveId);
            if (backtrack(opt.grid, depth + 1, maxDepth, path, visited)) {
                return true; 
            }
            path.remove(path.size() - 1);
        }

        visited.remove(stateKey);
        return false;
    }

    // Helper class to store and sort moves
    private static class MoveOption {
        int moveId, score;
        int[][] grid;
        MoveOption(int id, int s, int[][] g) {
            this.moveId = id;
            this.score = s;
            this.grid = g;
        }
    }

    private int estimateRemaining(int[][] g) {
        int misplaced = 0;
        int v = 1;
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g[0].length; j++)
                if (g[i][j] != v++) misplaced++;
        return (int) Math.ceil(misplaced / 4.0);
    }

    // ... (Keep your existing isSolved, rotateCopy, and encode methods)


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
            for (int x : r) {
                sb.append(x).append(',');
            }
        }
        return sb.toString();

    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}