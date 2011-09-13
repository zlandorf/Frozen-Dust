package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class GameActionEvent implements NetEvent {
	int unitId;
	int type;
	byte [] data;
	
	public GameActionEvent(int unitId, int type, byte[] data) {
		this.unitId = unitId;
		this.type = type;
		this.data = data;
	}

	public int getUnitId() {
		return unitId;
	}

	public int getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}
}
