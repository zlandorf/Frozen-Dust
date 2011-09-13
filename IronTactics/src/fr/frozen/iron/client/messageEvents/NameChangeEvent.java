package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class NameChangeEvent implements NetEvent {
	
	protected int playerId;
	protected String newName;
	
	public NameChangeEvent(int id, String name) {
		playerId = id;
		newName = name;
	}

	public int getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(int val) {
		playerId = val;
	}
	
	public String getName() {
		return newName;
	}
	
	public void setName(String val) {
		newName = val;
	}
}
