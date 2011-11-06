package fr.frozen.iron.client.messageEvents;

import java.util.List;

import fr.frozen.iron.common.IronPlayer;
import fr.frozen.network.client.NetEvent;

public class PlayerListReceivedEvent implements NetEvent {
	protected List<IronPlayer> playerList;
	
	public PlayerListReceivedEvent(List<IronPlayer> list) {
		playerList = list;
	}
	
	public List<IronPlayer> getList() {
		return playerList;
	}
	
	public void setList(List<IronPlayer> list) {
		playerList = list;
	}
}
