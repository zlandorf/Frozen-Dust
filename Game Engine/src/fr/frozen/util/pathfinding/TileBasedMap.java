package fr.frozen.util.pathfinding;

public interface TileBasedMap {
	public boolean isBlocked(int x, int y);
	public int getWidth();
	public int getHeight();
	public int getCost(int x, int y);
}
