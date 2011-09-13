package fr.frozen.network.common;

public class Message {
	private int clientId;
	private int type;
	private byte [] data;
	
	public Message(int clientId, int type, byte[] data) {
		this.clientId = clientId;
		this.data = data;
		this.type = type;
	}

	public int getClientId() {
		return clientId;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getType() {
		return type;
	}
	
	public String toString() {
		return "clientId = "+ clientId + " type = "+type + " data = "+ new String(data);
	}
}
