package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class GameOverEvent implements NetEvent {
	protected int winnerId;
	
	public GameOverEvent(int winnerId) {
		this.winnerId = winnerId;
	}
	
	public int getWinnerId() {
		return winnerId;
	}
}
