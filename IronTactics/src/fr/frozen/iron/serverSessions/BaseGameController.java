package fr.frozen.iron.serverSessions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.iron.net.IronServer;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;
import fr.frozen.network.server.Client;
import fr.frozen.network.server.IGameController;

public class BaseGameController implements IGameController {

	protected String sessionName;
	protected Protocol sessionType;
	
	protected List<Message> incomingMessages;
	protected List<MessageToSend> outgoingMessages;
	protected List<Client> clients;
	
	protected ByteArrayOutputStream byteArray;
	
	protected IronServer server;
	protected Logger logger = Logger.getLogger(getClass());
	
	public BaseGameController(String name, IronServer server, Protocol sessionType) {
		this.sessionName = name;
		this.server = server;
		this.sessionType = sessionType;

		incomingMessages = new ArrayList<Message>();
		outgoingMessages = new ArrayList<MessageToSend>();
		clients = new ArrayList<Client>();
		
		byteArray = new ByteArrayOutputStream();
	}
	
	public void handle(Message msg) {
		switch (Protocol.get(msg.getType())) {
		case SESSION_CHAT_MESSAGE : 
			handleChatMessage(msg);
			break;
		case SESSION_PLAYER_LIST_REQUEST :
			sendPlayerList(msg.getClientId());
			break;
		case SERVER_C_REQUEST_SESSION : 
			if (Protocol.get(IronUtil.byteArrayToInt(msg.getData())) == Protocol.SESSION_LOBBY) {
				IGameController lobby = server.getLobbySession();
				Client client = server.getClient(msg.getClientId());
				
				if (!lobby.equals(client.getCurrentGameSession())) {
					client.getCurrentGameSession().removeClient(client, "left game.");
					client.setCurrentGameSession(lobby);
					
					lobby.addClient(client);
				}
			}
			break;
		default :
			logger.error("not handled in session "+sessionName+"  "+Protocol.get(msg.getType()));
			break;
		}
	}
	
	public synchronized List<SocketChannel> getAllChannels() {
		if (clients.size() == 0) return null;
		List<SocketChannel> channels = new ArrayList<SocketChannel>();
		
		for (Client player : clients) {
			channels.add(player.getChannel());
		}
		return channels;
	}
	
	@Override
	public synchronized void addClient(Client p) {
		logger.info("adding "+p+" to "+sessionType);
		List<SocketChannel> channels = getAllChannels();
		
		if (channels != null)  {
			try {
				byteArray.reset();
				byteArray.write(IronUtil.intToByteArray(Protocol.SESSION_NEW_PLAYER.ordinal()));
				byteArray.write(IronUtil.intToByteArray(p.getId()));
				byteArray.write(p.getName().getBytes());

				MessageToSend msgToSend = new MessageToSend(channels,
						Protocol.SESSION_S_SERVER_MESSAGE.ordinal(),
						byteArray.toByteArray());
				server.getWriter().addMsg(msgToSend);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		clients.add(p);
		server.notifySessionChange(p, sessionType.ordinal());
	}
	
	@Override
	public synchronized void removeClient(Client p, String reason) { //TODO add a reason to this method, to know why he disconnected
		logger.info("removing "+p+" from "+sessionType);
		clients.remove(p);
		List<SocketChannel> channels = getAllChannels();
		if (channels == null) return;

		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(Protocol.SESSION_PLAYER_LOGOUT.ordinal()));
			byteArray.write(IronUtil.intToByteArray(p.getId()));
			byteArray.write(reason.getBytes());
			
			MessageToSend msgToSend = new MessageToSend(channels,
					Protocol.SESSION_S_SERVER_MESSAGE.ordinal(),
					byteArray.toByteArray());
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendPlayerList(int destId) { //TODO handle the size of an attachment buffer !!
		
		Client dest = server.getClient(destId);

		if (dest == null) {
			logger.error("problem, client is null (in send playerlist");
			return;
		}
			
		try {
			byteArray.reset();
			
			for (Client client : clients) {
				
				byteArray.write(IronUtil.intToByteArray(client.getId()));
				byteArray.write(IronUtil.intToByteArray(client.getName().length()));
				byteArray.write(client.getName().getBytes());
			}

			byteArray.write(IronUtil.intToByteArray(-1));//end of list
			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.SESSION_PLAYER_LIST.ordinal(),
					byteArray.toByteArray());
			
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleChatMessage(Message msg) {
		List<SocketChannel> channels = getAllChannels();
		if (channels == null) return;
		
		String chatMessage = msg.getClientId() + new String(msg.getData());
		logger.info("new chat message : "+chatMessage);
		
		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(msg.getClientId()));
			byteArray.write(msg.getData());
			
			MessageToSend msgToSend = new MessageToSend(channels, msg.getType(), byteArray.toByteArray());
			//outgoingMessages.add(msgToSend);
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void notifyNameChange(Client client) {
		List<SocketChannel> channels = getAllChannels();
		if (channels == null) return;
		
		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(Protocol.SESSION_NAME_CHANGE.ordinal()));
			byteArray.write(IronUtil.intToByteArray(client.getId()));
			byteArray.write(client.getName().getBytes());

			MessageToSend msgToSend = new MessageToSend(channels,
					Protocol.SESSION_S_SERVER_MESSAGE.ordinal(),
					byteArray.toByteArray());
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void enQueueMessage(Message msg) {
		incomingMessages.add(msg);
	}

	@Override
	public synchronized List<MessageToSend> getOutgoingMessages() {
		return outgoingMessages;
	}

	@Override
	public void update(float delta) {
		outgoingMessages.clear();
		synchronized (this) {
			for (Message msg : incomingMessages) {
				handle(msg);
			}
			incomingMessages.clear();
		}
	}
	
	@Override
	public int getSessionType() {
		return sessionType.ordinal();
	}
}
