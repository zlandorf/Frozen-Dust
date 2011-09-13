package fr.frozen.iron.client.messageEvents;

import fr.frozen.iron.protocol.Protocol;
import fr.frozen.network.client.NetEvent;

public class NewSessionEvent implements NetEvent {
	
	protected Protocol type;

	public NewSessionEvent(Protocol type) {
		this.type = type;
	}

	
	public Protocol getType() {
		return type;
	}

	public void setType(Protocol type) {
		this.type = type;
	}
}
