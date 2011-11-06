package fr.frozen.iron.common;

public interface GameContext {
	public PlayerGameInfo getPlayerInfo(int clientId);
	public int getTurnPlayerId();//-1 if no one's turn and id otherwise
}
