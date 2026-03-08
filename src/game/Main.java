package game;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter board size N: ");
        int N = sc.nextInt();

        Board board = new Board(N);

        System.out.println("Choose mode:");
        System.out.println("1. Human vs Computer");
        System.out.println("2. Computer Only (step-by-step)");

        int mode = sc.nextInt();

        System.out.println("Choose AI:");
        System.out.println("1. BFS");
        System.out.println("2. A*");
        System.out.println("3. Spatial D&C");
        System.out.println("4. Cycle D&C");
        System.out.println("5. Depth D&C");
        System.out.println("6. MDF DP");

        int choice = sc.nextInt();

        Player computer;

        switch (choice) {

            case 1:
                ComputerPlayer bfs = new ComputerPlayer(board);
                bfs.setAlgorithm(true);
                computer = bfs;
                break;

            case 2:
                ComputerPlayer aStar = new ComputerPlayer(board);
                aStar.setAlgorithm(false);
                computer = aStar;
                break;

            case 3:
                ComputerPlayer2 spatial = new ComputerPlayer2(board);
                spatial.setMode(1);
                computer = spatial;
                break;

            case 4:
                ComputerPlayer2 cycle = new ComputerPlayer2(board);
                cycle.setMode(2);
                computer = cycle;
                break;

            case 5:
                ComputerPlayer2 depth = new ComputerPlayer2(board);
                depth.setMode(3);
                computer = depth;
                break;

            case 6:

                Map<String,Integer> dp = ComputerPlayer3.buildDPTable(board);

                System.out.println("DP table built. States: " + dp.size());

                runDPGame(board, dp, sc);
                return;
                
            default:
                System.out.println("Invalid choice.");
                return;
        }

        if (mode == 1) {
            Player human = new HumanPlayer(board);
            new GameEngine(board, human, computer).startGame();
        } else {
            runComputerOnly(board, computer, sc);
        }
    }

    private static void runComputerOnly(Board board, Player computer, Scanner sc) {

        System.out.println("\nInitial Board:");
        board.print();

        sc.nextLine();

        while (!board.isSolved()) {

            System.out.print("\nPress ENTER for computer move (or q to quit): ");
            String input = sc.nextLine();

            if (input.equalsIgnoreCase("q")) break;

            int move = computer.getMove();

            System.out.println("Computer chooses move: " + move);
            board.executeMove(move);
            board.print();
        }

        if (board.isSolved())
            System.out.println("🎉 Puzzle Solved!");
        else
            System.out.println("Stopped by user.");
    }
    
    private static void runDPGame(Board board, Map<String,Integer> dp, Scanner sc) {

        System.out.println("\nInitial Board:");
        board.print();

        Set<String> visited = new HashSet<>();

        sc.nextLine();

        while (!board.isSolved()) {

            System.out.print("\nPress ENTER for next move (or q to quit): ");
            String input = sc.nextLine();

            if (input.equalsIgnoreCase("q"))
                break;

            int[][] grid = board.getGrid();

            visited.add(encode(grid));

            int bestMove = -1;
            int bestCost = Integer.MAX_VALUE;

            for (int m = 1; m <= board.totalMoves(); m++) {

                int[][] next = rotateCopy(grid, m);
                String key = encode(next);

                if (visited.contains(key))
                    continue;

                Integer cost = dp.get(key);

                if (cost != null && cost < bestCost) {

                    bestCost = cost;
                    bestMove = m;
                }
            }

            // fallback if all neighbors visited
            if (bestMove == -1) {
                for (int m = 1; m <= board.totalMoves(); m++) {
                    int[][] next = rotateCopy(grid, m);
                    String key = encode(next);

                    Integer cost = dp.get(key);

                    if (cost != null && cost < bestCost) {
                        bestCost = cost;
                        bestMove = m;
                    }
                }
            }

            System.out.println("Computer chooses move: " + bestMove);

            board.executeMove(bestMove);
            board.print();
        }

        if (board.isSolved())
            System.out.println("Puzzle solved!");
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
