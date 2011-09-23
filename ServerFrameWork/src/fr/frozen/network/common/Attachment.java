package fr.frozen.network.common;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import fr.frozen.network.server.BaseServer;

public class Attachment {
	private static int HEADER_SIZE = 8;//type + payload
	
	public ByteBuffer readBuf;
	private IMessageProcessor processor;
	
	private int clientId = -1;
	private int type = -1;
	
	private int payLoadSize = -1;
	private boolean headerRead = false;
	private byte [] payLoad = null;
	private int payLoadOffset = 0;

	/* for the moment a net message is   [clientId 4o][payload 4o][data max BUFFER_SIZE o]*/
	
	
	public Attachment(int clientId, IMessageProcessor processor) {
		readBuf = ByteBuffer.allocateDirect(NetConstants.MAX_MSG_SIZE);
		this.processor = processor;
		this.clientId = clientId;
	}
	
	public ByteBuffer getBuff() {
		return readBuf;
	}
	
	public void checkForMessages() {
		boolean newMessage;
		while (readBuf.remaining() > 0) {
			readBuf.flip();
			newMessage = checkForMessage();
			readBuf.compact();

			if (newMessage) {
				createAndSendMessage();
				reset();
			} else {
				break;
			}
		}
	}
	
	public void createAndSendMessage() {
		Message msg = new Message(clientId, type, payLoad);
		processor.processMessage(msg);
	}
	
	public void reset() {
		//clientId = -1;
		payLoadSize = -1;
		headerRead = false;
		payLoad = null;
		payLoadOffset = 0;
	}
	
	public boolean checkForMessage() {
		if (!headerRead) {
			if (readBuf.remaining() < HEADER_SIZE) {
				return false;
			}
			type = readBuf.getInt();
			payLoadSize = readBuf.getInt();

			if (payLoadSize <= 0 || payLoadSize > NetConstants.MAX_MSG_SIZE) {
				Logger.getLogger(getClass()).error("problem with payloadsize");
				//TODO throw exception ?
				return false;
			}
			payLoad = new byte[payLoadSize];
			//TODO change this into only one array that I use, to not reallocate each time
			headerRead = true;
		}
		
		
		int remaining = readBuf.remaining();
		if (remaining == 0) return false;
		
		int nbToGet = Math.min(remaining, payLoadSize - payLoadOffset);
		readBuf.get(payLoad,payLoadOffset,nbToGet);
		payLoadOffset += nbToGet;
		
		if (payLoadOffset >= payLoadSize) {
			return true;
		} else {
			return false;
		}
	}
	
	//TODO unit testing here ??
	public static void main(String []args) {
		BaseServer server = new BaseServer(1234);
		Attachment attachement = new Attachment(0,server);
		byte []src = {0,0,0,1,0,0,0,2,'a','c',0,0,0,3,0,0,0,7,'a','b','c','d','x'};
		byte[]src2 = {'z','y'};
		attachement.readBuf.put(src);
		attachement.checkForMessages();
		attachement.readBuf.put(src2);
		//attachement.readBuf.put((byte)'z');
		//attachement.readBuf.put((byte)'y');
		attachement.checkForMessages();
	}
}
