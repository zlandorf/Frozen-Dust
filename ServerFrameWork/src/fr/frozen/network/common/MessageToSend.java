package fr.frozen.network.common;

import java.nio.channels.SocketChannel;
import java.util.List;

public class MessageToSend {
	private SocketChannel channel;
	private List<SocketChannel> channels;
	private int type;
	private byte [] data;
	private boolean broadcast;
	
	public MessageToSend(SocketChannel channel, int type, byte[]data) {
		this.data = data;
		this.type = type;
		this.channel = channel;
		this.broadcast = false;
	}
	
	public MessageToSend(List<SocketChannel> channels, int type, byte[]data) {
		this.data = data;
		this.type = type;
		this.channels = channels;
		this.broadcast = true;
	}
	
	public SocketChannel getChannel() {
		return channel;
	}
	
	public List<SocketChannel> getChannels() {
		return channels;
	}
	
	public boolean isBroadCast() {
		return broadcast;
	}
	
	public int getType() {
		return type;
	}
	
	public byte[] getData() {
		return data;
	}
}
