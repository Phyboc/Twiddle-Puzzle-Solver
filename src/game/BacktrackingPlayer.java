package game;
import java.util.*;

public class BacktrackingPlayer extends AbstractPlayer {

    private Random random = new Random();

    public BacktrackingPlayer(Board board) {
        super(board);
    }

    @Override
    public int getMove() {
        int[][] currentGrid = board.getGrid();
        
        //Iterative Deepening 
        for (int maxDepth = 1; maxDepth <= 10; maxDepth++) {
            if (Thread.currentThread().isInterrupted()) break; 
            Set<String> visited = new HashSet<>();
            List<Integer> path = new ArrayList<>();
            
            if (backtrack(currentGrid, 0, maxDepth, path, visited)) {
                // If a solution is found, take the first move of that sequence
                return path.isEmpty() ? 1 : path.get(0);
            }
        }

        //No path found? Then return current best move
        return getBestGreedyMove(currentGrid);
    }

    private boolean backtrack(int[][] grid, int depth, int maxDepth, List<Integer> path, Set<String> visited) {
        if (Thread.currentThread().isInterrupted()) return false;
        if (isSolved(grid)) return true;
        
        //Pruning: if depth+estimateRemaining > maxdepth stop searching this path
        if (depth + estimateRemaining(grid) > maxDepth) return false;

        String stateKey = encode(grid);
        if (visited.contains(stateKey)) return false;
        visited.add(stateKey);

        //Move Ordering
        List<MoveOption> options = new ArrayList<>();
        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) break;
            int[][] nextGrid = rotateCopy(grid, m);
            options.add(new MoveOption(m, estimateRemaining(nextGrid), nextGrid));
        }

        // Sort: try moves that look "better" first to find solution faster
        options.sort(Comparator.comparingInt(o -> o.score));

        for (MoveOption opt : options) {
            if (Thread.currentThread().isInterrupted()) break;
            path.add(opt.moveId);
            if (backtrack(opt.grid, depth + 1, maxDepth, path, visited)) {
                return true; 
            }
            path.remove(path.size() - 1);
        }

        visited.remove(stateKey);
        return false;
    }

    //Get the best move that reduces number of misplaced tiles.
	//If there are multiple equally good moves then pick randomly 
    private int getBestGreedyMove(int[][] grid) {
        List<Integer> bestMoves = new ArrayList<>();
        int minScore = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {
            int[][] next = rotateCopy(grid, m);
            int score = estimateRemaining(next);

            if (score < minScore) {
                minScore = score;
                bestMoves.clear();
                bestMoves.add(m);
            } else if (score == minScore) {
                bestMoves.add(m);
            }
        }
        
       
        return bestMoves.get(random.nextInt(bestMoves.size()));
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
            for (int x : r) {
                sb.append(x).append(',');
            }
        }
        return sb.toString();
    }

    private static class MoveOption {
        int moveId, score;
        int[][] grid;
        MoveOption(int id, int s, int[][] g) {
            this.moveId = id;
            this.score = s;
            this.grid = g;
        }
    }

    @Override
    public String getName() {
        return "Hybrid Backtracking AI";
    }
}