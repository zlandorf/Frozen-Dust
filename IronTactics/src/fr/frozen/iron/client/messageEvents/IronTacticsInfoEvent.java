package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class IronTacticsInfoEvent implements NetEvent {
	protected int nbPlayers;
	protected int nbGames;
	
	public IronTacticsInfoEvent(int nbPlayers, int nbGames) {
		this.nbPlayers = nbPlayers;
		this.nbGames = nbGames;
	}
	
	public int getNbPlayers() {
		return nbPlayers;
	}
	
	public int getNbGames() {
		return nbGames;
	}
}
