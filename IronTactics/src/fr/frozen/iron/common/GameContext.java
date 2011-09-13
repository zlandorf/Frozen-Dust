package fr.frozen.iron.common;

public interface GameContext {
	public PlayerGameInfo getPlayerInfo(int clientId);
	public int getClientId();//-1 if server, otherwise the client's id
}
