package game;
import java.util.*;

public class ComputerPlayer2 extends AbstractPlayer {

    private int finalgrid[][];
    private Map<String, Integer> bestH = new HashMap<>();
    private Set<String> visitedStates = new HashSet<>();
    private int lastMove = -1;
    
    // DP table for optimal solving
    private Map<String, Integer> dpTable = null;

    // 1 = Spatial
    // 2 = Cycle
    // 3 = Depth
    // 4 = DP-based optimal
    private int mode = 1;

    private static final int LOOKAHEAD = 3;

    public void setMode(int m) {
        this.mode = m;
    }
    
    public void setDPTable(Map<String, Integer> dpTable) {
        this.dpTable = dpTable;
    }

    public ComputerPlayer2(Board board) {
        super(board);
        this.finalgrid = new int[board.size()][board.size()];
        int val = 1;
        for (int i = 0; i < finalgrid.length; i++)
            for (int j = 0; j < finalgrid[0].length; j++)
                finalgrid[i][j] = val++;
    }

    @Override
    public int getMove() {
        if (Thread.currentThread().isInterrupted()) return 1;
        bestH.clear(); 
        int[][] grid = board.getGrid();
        visitedStates.add(encode(grid));   
        int move;
        if (mode == 2)
            move = solveCycleDC();
        else if (mode == 3)
            move = solveDepthDC();
        else
            move = solvewithdc();

        lastMove = move;   
        return move;
    }

    @Override
    public String getName() {
        return "Divide & Conquer Variants";
    }

// spatial d&c

    static class Rotator {
        int r1, r2, c1, c2;
        int h;
        int moveno;
        Rotator(int r1, int r2, int c1, int c2,int h){
            this.r1 = r1; this.r2 = r2;
            this.c1 = c1; this.c2 = c2;
            this.h = h;
        }
    }

    private int solvewithdc() {
        if (Thread.currentThread().isInterrupted()) return 1;
        int[][] grid = board.getGrid();
        Rotator r = rotation(grid, 0, grid.length - 1, 0, grid[0].length - 1);
        if (r == null) return 1;
        return r.moveno;
    }

    private Rotator rotation(int[][] matrix, int s1, int s2, int e1, int e2){
        if (Thread.currentThread().isInterrupted()) return null;
        if (s1 >= s2 || e1 >= e2) return null;

        if (((s2 - s1) == 1) && ((e2 - e1) == 1)) {
            int[][] next = rotate(matrix, s1, s2, e1, e2);
            int h = h1(next,s1,s2,e1,e2) + h2(next);

            String key = encode(next);
            if (bestH.containsKey(key) && bestH.get(key) <= h)
                return null;

            bestH.put(key, h);
            Rotator rot = new Rotator(s1,s2,e1,e2,h);
            rot.moveno = getMoveno(matrix,s1,s2,e1,e2);
            return rot;
        }

        List<Rotator> children = new ArrayList<>();
        children.add(rotation(matrix, s1, s2-1, e1, e2-1));
        children.add(rotation(matrix, s1+1, s2, e1, e2-1));
        children.add(rotation(matrix, s1, s2-1, e1+1, e2));
        children.add(rotation(matrix, s1+1, s2, e1+1, e2));
        children.removeIf(Objects::isNull);
        children.sort((a,b)->a.h-b.h);
        return Thread.currentThread().isInterrupted() ? null : (children.isEmpty()?null:children.get(0));
    }

// cycle d&c

    private int solveCycleDC() {
        int[][] grid = board.getGrid();
        List<List<Integer>> cycles = getCycles(grid);
        if (cycles.isEmpty()) return 1;

        List<Integer> largest = cycles.stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(null);

        int bestMove = 1;
        int bestScore = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) return 1;

            if (m == lastMove) continue; 
            int[][] next = rotateCopy(grid, m);
            String key = encode(next);

            if (visitedStates.contains(key)) continue; 

            int score = cycleScore(next, largest) + h2(next);

            if (bestH.containsKey(key) && bestH.get(key) <= score)
                continue;

            bestH.put(key, score);

            if (score == 0)
                return m;

            if (score < bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        return bestMove;
    }


    private List<List<Integer>> getCycles(int[][] grid) {
        int N = grid.length;
        boolean[] visited = new boolean[N*N];
        List<List<Integer>> cycles = new ArrayList<>();

        for (int i = 0; i < N*N; i++) {
            if (visited[i]) continue;
            List<Integer> cycle = new ArrayList<>();
            int cur = i;
            while (!visited[cur]) {
                visited[cur] = true;
                cycle.add(cur);
                int r = cur / N;
                int c = cur % N;
                cur = grid[r][c] - 1;
            }
            if (cycle.size() > 1)
                cycles.add(cycle);
        }
        return cycles;
    }

    private int cycleScore(int[][] g, List<Integer> cycle) {
        int N = g.length;
        int score = 0;
        for (int idx : cycle) {
            int r = idx / N;
            int c = idx % N;
            if (g[r][c] != finalgrid[r][c])
                score++;
        }
        return score;
    }

// depth d&c

    private int solveDepthDC() {
        int[][] grid = board.getGrid();
        int bestMove = 1;
        int bestScore = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) return 1;

            if (m == lastMove) continue;

            int[][] next = rotateCopy(grid, m);
            String key = encode(next);

            if (visitedStates.contains(key)) continue;

            int score = exploreDepth(next, LOOKAHEAD - 1, bestScore);

            if (score < bestScore) {
                bestScore = score;
                bestMove = m;
            }

            if (bestScore == 0)
                return bestMove;
        }

        return bestMove;
    }


    private int exploreDepth(int[][] state, int depth, int alpha) {

        int currentH = h2(state);

        if (Thread.currentThread().isInterrupted()) return Integer.MAX_VALUE;
        if (depth == 0 || currentH == 0)
            return currentH;

        String key = encode(state);

        if (bestH.containsKey(key) && bestH.get(key) <= currentH)
            return Integer.MAX_VALUE;

        bestH.put(key, currentH);

        int best = Integer.MAX_VALUE;

        for (int m = 1; m <= board.totalMoves(); m++) {
            if (Thread.currentThread().isInterrupted()) return Integer.MAX_VALUE;
            int[][] next = rotateCopy(state, m);
            int score = exploreDepth(next, depth - 1, best);

            best = Math.min(best, score);

            // Alpha-style pruning
            if (best <= alpha)
                break;
        }

        return best;
    }

// utilities

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

    private int[][] rotate(int[][] matrix, int r1, int r2, int c1, int c2) {
        int[][] copy = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            copy[i] = matrix[i].clone();

        copy[r1][c1] = matrix[r1][c2];
        copy[r2][c1] = matrix[r1][c1];
        copy[r2][c2] = matrix[r2][c1];
        copy[r1][c2] = matrix[r2][c2];
        return copy;
    }

    private int getMoveno(int[][] matrix, int s1, int s2, int e1, int e2){
        return s1 * (matrix.length-1) + e1 + 1;
    }

    private int h1(int[][] matrix,int s1,int s2,int e1,int e2){
        int sum=0;
        for(int i=s1;i<=s2;i++)
            for(int j=e1;j<=e2;j++)
                sum+=Math.abs(matrix[i][j]-finalgrid[i][j]);
        return sum;
    }

    private int h2(int[][] matrix){
        int count=0,v=1;
        for(int i=0;i<matrix.length;i++)
            for(int j=0;j<matrix[0].length;j++)
                if(matrix[i][j]!=v++)count++;
        return count;
    }

    private String encode(int[][] g){
        StringBuilder sb=new StringBuilder();
        for(int[] r:g)
            for(int x:r)
                sb.append(x).append(',');
        return sb.toString();
    }
}
