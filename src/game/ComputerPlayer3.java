package game;

import java.util.*;

public class ComputerPlayer3 extends AbstractPlayer {
	private class MDP{
		int grid[][];
		int cost;
		public MDP(int[][] grid, int cost) {
			this.grid = grid;
			this.cost = cost;
		}
		public MDP(int[][] grid) {
			this.grid = grid;
			this.cost = Integer.MAX_VALUE;
		}
		
	}
	private int finalgrid[][];
	private ArrayList<MDP> dp;
	public ComputerPlayer3(Board board) {
		super(board);
		this.finalgrid = new int[board.size()][board.size()];
		this.dp = new ArrayList<>();
        int val = 1;
        for (int i = 0; i < finalgrid.length; i++)
            for (int j = 0; j < finalgrid[0].length; j++)
                finalgrid[i][j] = val++;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getMove() {
		// TODO Auto-generated method stub
		int mvno = solvehdp();
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Heuristic Dynamic Programming";
	}
	
	private int solvehdp() {
		MDP mdp = new MDP(board.getGrid());
		dp.add(mdp);
		int initial_cost = getcost(mdp);
	}
	private int getcost(MDP mdp) {
		int grid[][] = mdp.grid;
		int cost = mdp.cost;
	}
	
	private String encode(int[][] g){
        StringBuilder sb=new StringBuilder();
        for(int[] r:g)
            for(int x:r)
                sb.append(x).append(',');
        return sb.toString();
    }


}
