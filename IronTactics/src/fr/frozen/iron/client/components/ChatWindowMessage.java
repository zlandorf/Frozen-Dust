package fr.frozen.iron.client.components;

public class ChatWindowMessage {
	
	public static int CHAT_MESSAGE = 0;
	public static int SERVER_MESSAGE = 1;
	
	protected int type;//0 chat, 1 server
	protected String message;
	protected String prefix;
	
	public ChatWindowMessage(int type, String message) {
		this(type, "", message);
	}
	
	public ChatWindowMessage(int type, String prefix, String message) {
		this.type = type;
		this.message = message;
		this.prefix = prefix;
	}
	
	public int getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getFullMessage() {
		return prefix + message;
	}
}
