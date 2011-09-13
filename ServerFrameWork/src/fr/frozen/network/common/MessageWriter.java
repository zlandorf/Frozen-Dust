package fr.frozen.network.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Vector;

public class MessageWriter extends Thread {
	
	private List<MessageToSend> msgsToSend;
	private boolean run = true;
	private ByteBuffer writeBuff;
	
	public MessageWriter() {
		msgsToSend = new Vector<MessageToSend>();
		writeBuff = ByteBuffer.allocateDirect(NetConstants.MAX_MSG_SIZE);
	}
	
	public synchronized void addMsg(MessageToSend msg) {
		msgsToSend.add(msg);
		notifyAll();
	}
	
	private void prepareBuff(int type, byte[] data) {
		writeBuff.clear();
		writeBuff.putInt(type);
		writeBuff.putInt(data.length);
		writeBuff.put(data);
		writeBuff.flip();
	}
	
	public synchronized void end() {
		msgsToSend.clear();
		run = false;
		notifyAll();
	}
	
	@Override
	public void run() {
		while (run) {
			synchronized (this) {
				if (msgsToSend.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
						System.err.println("Cannot wait");
						e.printStackTrace();
					}
				} else {
					for (MessageToSend msg : msgsToSend) {
						prepareBuff(msg.getType(), msg.getData());
						try {
							if (!msg.isBroadCast()) {
								if (msg.getChannel() == null) {
									System.err.println("channel for writing null");
									return;
								}
								msg.getChannel().write(writeBuff);
							} else {
								if (msg.getChannels() == null) {
									System.err.println("list of channels null");
									return;
								}
								for (SocketChannel channel : msg.getChannels()) {
									channel.write(writeBuff);
									writeBuff.rewind();
								}
							}
							
							//System.out.println("message wrote !");
						} catch (IOException e) {
							System.out.println("problem when writing");
							e.printStackTrace();
						}
					}
					msgsToSend.clear();
				}
			}//syncho
		}//while
	}
}
