package fr.paul.game.test.path;

import fr.frozen.util.pathfinding.AStar;
import fr.frozen.util.pathfinding.Mover;
import fr.frozen.util.pathfinding.Path;
import fr.frozen.util.pathfinding.PathFinder;
import fr.frozen.util.pathfinding.TileBasedMap;

public class PathTest {
	public static int [][] mapData = 
	   {{0,0,0,0,0,0,0,0,0,0},
    	{0,1,1,1,0,0,0,0,0,0},
		{0,1,0,1,0,0,0,0,0,0},
		{0,1,0,0,1,0,0,0,0,0},
		{0,1,0,0,1,0,0,0,0,0},
		{0,1,0,0,1,0,0,0,0,0},
		{0,1,1,0,0,1,0,0,0,0},
		{0,1,0,1,0,0,1,0,0,0},
		{0,1,0,1,1,0,0,1,0,0},
		{0,0,0,0,0,0,0,0,1,0}};
	
	
	public static void main(String[] args) {
		TestMap map = new TestMap(mapData);
		PathFinder pathFinder = new AStar(map, false);
		Path path = pathFinder.findPath(null, 4, 2, 4, 6, 320);
		
		if (path == null) {
			System.out.println("no path !");
		} else {
			for (int i = 0; i < path.getNbSteps(); i++) {
				map.data[path.getX(i)][path.getY(i)] = 2;
			}

			map.dump();
		}
	}
}

class TestMap implements TileBasedMap {

	int [][] data;
	
	public TestMap(int [][] data) {
		this.data = data;
	}
	
	@Override
	public int getCost(int x, int y) {
		return 10;
	}

	@Override
	public int getHeight() {
		return data[0].length;
	}

	@Override
	public int getWidth() {
		return data.length;
	}

	@Override
	public boolean isBlocked(int x, int y) {
		return data[x][y] == 1;
	}
	
	public void dump() {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				System.out.print(data[i][j]+" ");
			}
			System.out.println();
		}
	}
}

class TestMover implements Mover {
}
