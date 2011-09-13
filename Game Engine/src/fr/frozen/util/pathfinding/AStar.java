package fr.frozen.util.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AStar implements PathFinder {

	protected TileBasedMap map;
	protected boolean diagMovAllowed;
	
	List<Step> openList;
	List<Step> closedList;
	
	public AStar(TileBasedMap map, boolean diagMovAllowed) {
		this.map = map;
		this.diagMovAllowed = diagMovAllowed;
		
		openList = new ArrayList<Step>();
		closedList = new ArrayList<Step>();
	}
	
	@Override
	public Path findPath(Mover mover, int xSrc, int ySrc, int xDst, int yDst, int maxDistance) {
		if (map.isBlocked(xDst, yDst)) return null;
		//if (getHeuristic(xSrc, ySrc, xDst, yDst) > maxDistance) return null; //TODO : find something else if diag mvmt
		
		boolean targetReached = false;
		Step current = null;
		Step last = null;
		openList.clear();
		closedList.clear();
		
		openList.add(new Step(xSrc, ySrc, null, 0, 0, closedList.size()));
		
		main : while (openList.size() > 0 && !targetReached) {
			current = getNextStepFromOpenList();
			closedList.add(current);
			
			for (Step s : getSurroundingSteps(current, xDst, yDst)) {
				
				if (s.G > maxDistance) continue;
				
				if (s.x == xDst && s.y == yDst) {
					closedList.add(s);
					targetReached = true;
					last = s;
					break main;
				}
				
				Step tmp = getStepFromList(closedList, s.x, s.y);
				if (tmp != null) continue;
				tmp = getStepFromList(openList, s.x, s.y);
				if (tmp != null) {
					if (tmp.G > s.G) {
						tmp.parent = s.parent;
						tmp.G = s.G;
						tmp.F = s.F;
					}
				} else {
					openList.add(s);
				}
			}
		} // while
		if (!targetReached) return null;
		
		Path res = new Path();
		current = last;
		openList.clear();
		while (current != null) {
			openList.add(current);
			current = current.parent;
		}
		
		for (int i = openList.size() - 1; i >= 0; i--) {
			res.addStep(openList.get(i).x, openList.get(i).y, openList.get(i).cost);
		}
		return res;
	}
	
	private int getHeuristic(int x1, int y1, int x2, int y2) {
		return (Math.abs(x1 - x2) + Math.abs(y1 - y2)) * 10;
	}
	
	private void addToListIfPossible(List<Step> list, Step parent, int x, int y, int xDst, int yDst, boolean diag) {
		if (map.isBlocked(x, y)) return;
		
		list.add(new Step(x, y,
				parent,
				(int)(map.getCost(x, y) * (diag ? 1.4 : 1)),
				getHeuristic(x, y, xDst, yDst), closedList.size()));
	}
	
	private List<Step> getSurroundingSteps(Step parent, int xDst, int yDst) {
		List <Step> tmp = new ArrayList<Step>();
		
		if (parent.x > 0) {
			addToListIfPossible(tmp, parent, parent.x - 1, parent.y, xDst, yDst, false);
		}
		if (parent.y > 0) {
			addToListIfPossible(tmp, parent, parent.x, parent.y - 1, xDst, yDst, false);
		}
		if (parent.x < map.getWidth() - 1 ) {
			addToListIfPossible(tmp, parent, parent.x + 1, parent.y, xDst, yDst, false);
		}
		if (parent.y < map.getHeight() - 1) {
			addToListIfPossible(tmp, parent, parent.x, parent.y + 1, xDst, yDst, false);
		}
		
		if (diagMovAllowed) {
			if (parent.x > 0 && parent.y > 0) {
				addToListIfPossible(tmp, parent, parent.x - 1, parent.y - 1, xDst, yDst, true);
			}
			if (parent.x > 0 && parent.y < map.getHeight() - 1) {
				addToListIfPossible(tmp, parent, parent.x - 1, parent.y + 1, xDst, yDst, true);
			}
			if (parent.x < map.getWidth() - 1 && parent.y < map.getHeight() - 1) {
				addToListIfPossible(tmp, parent, parent.x + 1, parent.y + 1, xDst, yDst, true);
			}
			if (parent.x < map.getWidth() - 1 && parent.y > 0) {
				addToListIfPossible(tmp, parent, parent.x + 1, parent.y - 1, xDst, yDst, true);
			}
		}
		
		return tmp;
	}
	
	private Step getStepFromList(List<Step> list, int x, int y) {
		for (Step s : list) {
			if (s.x == x && s.y == y) return s;
		}
		return null;
	}
	
	private Step getNextStepFromOpenList() {
		Collections.sort(openList);
		Step tmp = openList.get(0);
		openList.remove(0);
		
		return tmp;
	}
}



class Step implements Comparable<Step> {
	int x, y, G, H, F, cost;
	int xfactor;//to see wich step was added last, the last one has better chance of being the good step
	//the last one should have a bigger score
	Step parent;
	
	public Step(int x, int y, Step parent, int cost, int H, int xfactor) {
		this.x = x;
		this.y = y;
		this.G = parent != null ? parent.G + cost : cost;
		this.H = H;
		this.F = G + H;
		this.xfactor = xfactor;
		this.parent = parent;
		this.cost = cost;
	}



	@Override
	public int compareTo(Step o) {
		if (this.F < o.F) return -1;
		if (this.F == o.F) {
			if (this.xfactor > o.xfactor) return -1;
			if (this.xfactor < o.xfactor) return 1;
			return 0;
		}
		return 1;
	}
}