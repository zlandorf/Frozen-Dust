package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class RaceChoiceEvent implements NetEvent {
	protected int clientId;
	protected int chosenRace;
	
	public RaceChoiceEvent(int clientId, int chosenRace) {
		this.clientId = clientId;
		this.chosenRace = chosenRace;
	}

	public int getClientId() {
		return clientId;
	}
	
	public int getChosenRace() {
		return chosenRace;
	}
}
