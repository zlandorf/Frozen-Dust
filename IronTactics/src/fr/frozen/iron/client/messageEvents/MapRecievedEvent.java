package fr.frozen.iron.client.messageEvents;

import fr.frozen.iron.common.IronMap;
import fr.frozen.network.client.NetEvent;

public class MapRecievedEvent implements NetEvent {
	protected IronMap map;

	public MapRecievedEvent(IronMap map) {
		this.map = map;
	}

	public IronMap getMap() {
		return map;
	}

	public void setMap(IronMap map) {
		this.map = map;
	}
}
