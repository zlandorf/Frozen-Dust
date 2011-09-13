package fr.frozen.iron.client;

public class GameInfo {
	protected int id;
	protected String host;
	
	public GameInfo(int id, String host) {
		this.id = id;
		this.host = host;
	}

	public int getId() {
		return id;
	}
	
	public String getHost() {
		return host;
	}
}
