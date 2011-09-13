package fr.frozen.util.pathfinding;

import java.util.ArrayList;
import java.util.List;

public class Path {
	protected List<int[]> path;
	protected int totalMoveCost;
	
	
	public Path() {
		path = new ArrayList<int[]>();
		totalMoveCost = 0;
	}
	
	public void addStep(int x, int y, int cost) {
		int []e = {x, y, cost};
		totalMoveCost += cost;
		path.add(e);
	}
	
	public int getTotalMoveCost() {
		return totalMoveCost;
	}
	
	public int getNbSteps() {
		return path.size();
	}
	
	public int getX(int index) {
		if (index < 0 || index >= getNbSteps()) return -1;
		
		return path.get(index)[0];
	}
	
	public int getY(int index) {
		if (index < 0 || index >= getNbSteps()) return -1;
		
		return path.get(index)[1];
	}
	
	public int getCost(int index) {
		if (index < 0 || index >= getNbSteps()) return -1;
		
		return path.get(index)[2];
	}
	
	public String toString() {
		String str = "path : ";
		for (int[] tab : path) {
			str += "["+tab[0]+","+tab[1]+"] ";
		}
		return str;
	}
}
