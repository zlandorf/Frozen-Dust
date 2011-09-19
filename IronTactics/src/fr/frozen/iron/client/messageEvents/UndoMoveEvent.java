package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class UndoMoveEvent implements NetEvent {
	protected int unitId;
	
	public UndoMoveEvent(int unitId) {
		this.unitId = unitId;
	}
	
	public int getUnitId() {
		return unitId;
	}
}
