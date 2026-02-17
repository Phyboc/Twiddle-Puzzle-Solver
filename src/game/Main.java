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
}
