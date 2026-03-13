package game;

public interface Player {
    int getMove();
    String getName();
}
abstract class AbstractPlayer implements Player {
    protected Board board;
    private long lastSolveTimeNs = -1;
 
    AbstractPlayer(Board board) {
        this.board = board;
    }
 
    public final int getTimedMove() {
        long start = System.nanoTime();
        int move = getMove();
        lastSolveTimeNs = System.nanoTime() - start;
        return move;
    }
 
    public long getLastSolveTimeNs() {
        return lastSolveTimeNs;
    }
}


