package fr.frozen.iron.client.messageEvents;

import java.util.List;

import fr.frozen.iron.client.GameInfo;
import fr.frozen.network.client.NetEvent;

public class GameListReceivedEvent implements NetEvent {
	protected List<GameInfo> list;
	
	public GameListReceivedEvent(List<GameInfo> list) {
		this.list = list;
	}

	public List<GameInfo> getList() {
		return list;
	}
}
