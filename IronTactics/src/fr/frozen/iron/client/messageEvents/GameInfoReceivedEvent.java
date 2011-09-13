package fr.frozen.iron.client.messageEvents;

import java.util.Hashtable;

import fr.frozen.iron.common.PlayerGameInfo;
import fr.frozen.network.client.NetEvent;

public class GameInfoReceivedEvent implements NetEvent {

	protected Hashtable<Integer, PlayerGameInfo> info;
	
	public GameInfoReceivedEvent(Hashtable<Integer, PlayerGameInfo> info) {
		this.info = info;
	}
	
	public Hashtable<Integer, PlayerGameInfo> getInfo() {
		return info;
	}
}
