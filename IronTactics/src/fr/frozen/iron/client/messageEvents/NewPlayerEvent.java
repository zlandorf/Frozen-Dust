package fr.frozen.iron.client.messageEvents;

import fr.frozen.iron.client.IronPlayer;
import fr.frozen.network.client.NetEvent;

public class NewPlayerEvent implements NetEvent {
	
	protected IronPlayer player;
	
	public NewPlayerEvent(IronPlayer player) {
		this.player = player;
	}

	public IronPlayer getPlayer() {
		return player;
	}
	
	public void setPlayer(IronPlayer player) {
		this.player = player;
	}
}