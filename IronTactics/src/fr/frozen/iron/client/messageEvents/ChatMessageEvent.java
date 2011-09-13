package fr.frozen.iron.client.messageEvents;

import fr.frozen.network.client.NetEvent;

public class ChatMessageEvent implements NetEvent {
	protected int senderId;
	protected String text;
	
	public ChatMessageEvent(int senderId, String text) {
		this.senderId = senderId;
		this.text = text;
	}
	
	public int getSenderId() {
		return senderId;
	}
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
