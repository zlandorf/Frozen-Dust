package fr.frozen.util.pathfinding;

public interface PathFinder {
	public Path findPath(Mover mover, int xSrc, int ySrc, int xDst, int yDst, int maxDistance);
}
