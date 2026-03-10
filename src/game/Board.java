package game;

import java.util.*;

public class Board {
    private final int N;
    private int[][] grid;
    private int moves = 0;
    private final int[][] TARGET;

    public Board(int n) {
        this.N = n;
        this.TARGET = buildTarget(n);
        generateRandomBoard();
    }

    private int[][] buildTarget(int n) {
        int[][] t = new int[n][n];
        int v = 1;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                t[i][j] = v++;
        return t;
    }

    private void generateRandomBoard() {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= N * N; i++) nums.add(i);
        Collections.shuffle(nums);

        grid = new int[N][N];
        int idx = 0;
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                grid[i][j] = nums.get(idx++);
    }

    public void executeMove(int move) {
        int r = (move - 1) / (N - 1);
        int c = (move - 1) % (N - 1);

        rotate2x2(r, c);
        moves++;
    }

    private void rotate2x2(int r, int c) {
        int tmp = grid[r][c];
        grid[r][c] = grid[r][c + 1];
        grid[r][c + 1] = grid[r + 1][c + 1];
        grid[r + 1][c + 1] = grid[r + 1][c];
        grid[r + 1][c] = tmp;
    }
    
    public void printBoard() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                System.out.printf("%4d", grid[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }


    public boolean isSolved() {
        return Arrays.deepEquals(grid, TARGET);
    }

    public void randomize() {
        moves = 0;
        generateRandomBoard();
    }

    public int[][] getGrid() {
        return grid;
    }

    public void setGrid(int[][] newGrid) {
        if (newGrid == null || newGrid.length != N || newGrid[0].length != N) {
            throw new IllegalArgumentException("Grid size mismatch");
        }

        int[][] copy = new int[N][N];
        for (int i = 0; i < N; i++) {
            if (newGrid[i].length != N) {
                throw new IllegalArgumentException("Grid size mismatch");
            }
            copy[i] = newGrid[i].clone();
        }

        this.grid = copy;
        this.moves = 0;
    }

    public int getMoves() {
        return moves;
    }

    public int size() {
        return N;
    }

    public int totalMoves() {
        return (N - 1) * (N - 1);
    }

    public void print() {
        for (int[] row : grid)
            System.out.println(Arrays.toString(row));
        System.out.println("Moves: " + moves + "\n");
    }
}
