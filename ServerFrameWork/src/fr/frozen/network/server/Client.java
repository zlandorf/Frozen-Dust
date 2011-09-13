package fr.frozen.network.server;

import java.nio.channels.SocketChannel;

public class Client {
	private int id;
	private SocketChannel channel;
	private String name;
	private IGameController currentGameSession;
	//score ?
	
	public Client(int id, SocketChannel channel, String name) {
		this.id = id;
		this.channel = channel;
		this.name = name;
	}
	
	public Client(int id, SocketChannel channel) {
		this(id,channel,"default");
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public SocketChannel getChannel() {
		return channel;
	}
	
	public void setCurrentGameSession(IGameController session) {
		currentGameSession = session;
	}
	
	public IGameController getCurrentGameSession() {
		return currentGameSession;
	}
	
	public String toString() {
		return name+" id = "+id;
	}
}
