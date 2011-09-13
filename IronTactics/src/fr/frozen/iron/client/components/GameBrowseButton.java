package fr.frozen.iron.client.components;

public class GameBrowseButton extends IronMenuButton {

	protected int gameId;
	
	public GameBrowseButton(int gameId, String label, int y) {
		super(label, y);
		this.gameId = gameId;
	}

	public int getGameId() {
		return gameId;
	}
	
}
