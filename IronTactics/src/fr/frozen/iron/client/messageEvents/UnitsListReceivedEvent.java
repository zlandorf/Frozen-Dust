package fr.frozen.iron.client.messageEvents;

import java.util.List;

import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.network.client.NetEvent;

public class UnitsListReceivedEvent implements NetEvent {
	protected List<IronUnit> list;
	
	public UnitsListReceivedEvent(List<IronUnit> list) {
		this.list = list;
	}

	public List<IronUnit> getUnitsList() {
		return list;
	}
}
