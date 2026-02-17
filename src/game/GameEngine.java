package game;
public class GameEngine {

    private final Board board;
    private final Player human;
    private final Player computer;

    public GameEngine(Board board, Player human, Player computer) {
        this.board = board;
        this.human = human;
        this.computer = computer;
    }

    public void startGame() {
        System.out.println("Initial Board:");
        board.printBoard();

        while (!board.isSolved()) {

            // Human move
            int hMove = human.getMove();
            board.executeMove(hMove);
            System.out.println("After Human move (" + hMove + "):");
            board.printBoard();

            if (board.isSolved()) break;

            // Computer move
            int cMove = computer.getMove();
            board.executeMove(cMove);
            System.out.println("After Computer move (" + cMove + "):");
            board.printBoard();
        }

        System.out.println(" Puzzle Solved!");
    }
}
