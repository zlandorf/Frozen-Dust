package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class GameTurnEvent implements NetEvent {

	protected int playerId;
	
	public GameTurnEvent(int playerId) {
		this.playerId = playerId;
	}
	
	public int getPlayerId() {
		return playerId;
	}
}
