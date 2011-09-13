package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class PlayerLogoutEvent implements NetEvent {
	protected int id;
	protected String reason;
	
	public PlayerLogoutEvent(int id, String reason) {
		this.id = id;
		this.reason = reason;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
