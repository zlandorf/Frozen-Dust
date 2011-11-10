package fr.frozen.iron.serverSessions;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import fr.frozen.iron.net.IronServer;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;
import fr.frozen.network.server.Client;

public class GameCreationSession extends BaseServerSession {

	protected int id;
	
	protected Client host;
	protected int hostRace = - 1;
	protected boolean hostReady = false;
	
	protected Client other = null;
	protected int otherRace = - 1;
	protected boolean otherReady = false;
	
	public GameCreationSession(IronServer server, int id, Client host) {
		super("gameCreation", server, Protocol.SESSION_GAME_CREATION);
		this.host = host;
		this.id = id;
	}
	
	public boolean slotAvailable() {
		return other == null;
	}
	
	public int getId() {
		return id;
	}
	
	public Client getHost() {
		return host;
	}
	
	@Override
	public void handle(Message msg) {
		switch (Protocol.get(msg.getType())) {
		case GAME_CREATION_RACE_REQUEST :
			handleRaceRequest(msg);
			break;
		case GAME_CREATION_READY :
			handleGameReady(msg);
			break;
		default :
			super.handle(msg);
			break;
		}
	}
	
	protected void handleGameReady(Message msg) {
		//TODO : send message to tell opponent that the player is ready
		if (msg.getClientId() == host.getId() && hostRace != -1) {
			hostReady = true;
		} else if (other != null && msg.getClientId() == other.getId() && otherRace != -1) {
			otherReady = true;
		} 
		
		if (hostReady && otherReady) { //both are ready
			server.createGame(host, hostRace, other, otherRace);
			server.removeGameCreation(this);
		}
	}
	
	protected void handleRaceRequest(Message msg) {
		Client c = server.getClient(msg.getClientId());
		int raceChosen = IronUtil.byteArrayToInt(msg.getData());
		logger.info(c+" chose "+Protocol.get(raceChosen));
		//TODO: check here that race is legit
		if (host.getId() == msg.getClientId()) {
			hostRace = raceChosen;
			hostReady = false;
		} else {
			otherRace = raceChosen;
			otherReady = false;
		}
		
		List<SocketChannel> channels = new ArrayList<SocketChannel>();
		if (host != null) {
			channels.add(host.getChannel());
		}
		if (other != null) {
			channels.add(other.getChannel());
		}
		if (channels.size() == 0) return;
		
		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(msg.getClientId()));
			byteArray.write(IronUtil.intToByteArray(raceChosen));
			
			MessageToSend msgToSend = new MessageToSend(channels,
					Protocol.GAME_CREATION_RACE.ordinal(),
					byteArray.toByteArray());
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void addClient(Client c) {
		if (other == null) {
			other = c;
			server.notifySessionChange(c, sessionType.ordinal());
			
			SocketChannel channel = host.getChannel();
			
			if (channel != null)  {
				try {
					byteArray.reset();
					byteArray.write(IronUtil.intToByteArray(Protocol.SESSION_NEW_PLAYER.ordinal()));
					byteArray.write(IronUtil.intToByteArray(c.getId()));
					byteArray.write(c.getName().getBytes());

					MessageToSend msgToSend = new MessageToSend(channel,
							Protocol.SESSION_S_SERVER_MESSAGE.ordinal(),
							byteArray.toByteArray());
					server.getWriter().addMsg(msgToSend);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void removeClient(Client c, String reason) {
		if (c.getId() == host.getId()) {
			if (other != null) {
				other.setCurrentGameSession(server.getLobbySession());
				server.getLobbySession().addClient(other);
			}
			server.removeGameCreation(this);
		} else {
			if (c.getId() == other.getId()) {
				other = null;
				otherRace = -1;
				otherReady = false;
				
				SocketChannel channel = host.getChannel();
				if (channel != null) {
					try {
						byteArray.reset();
						byteArray.write(IronUtil.intToByteArray(Protocol.SESSION_PLAYER_LOGOUT.ordinal()));
						byteArray.write(IronUtil.intToByteArray(c.getId()));
						byteArray.write(reason.getBytes());

						MessageToSend msgToSend = new MessageToSend(channel,
								Protocol.SESSION_S_SERVER_MESSAGE.ordinal(),
								byteArray.toByteArray());
						server.getWriter().addMsg(msgToSend);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public void sendPlayerList(int destId) {
		Client dest = server.getClient(destId);

		if (dest == null) {
			logger.error("problem, client is null (in send playerlist");
			return;
		}
		logger.debug(dest+" requesting player list in gameCreation");
			
		try {
			byteArray.reset();
			//host is first
			//other after
			
			byteArray.write(IronUtil.intToByteArray(host.getId()));
			//byteArray.write(IronUtil.intToByteArray(hostRace));
			byteArray.write(IronUtil.intToByteArray(host.getName().length()));
			byteArray.write(host.getName().getBytes());
			

			if (other != null) {
				byteArray.write(IronUtil.intToByteArray(other.getId()));
				//byteArray.write(IronUtil.intToByteArray(otherRace));
				byteArray.write(IronUtil.intToByteArray(other.getName().length()));
				byteArray.write(other.getName().getBytes());
			} 
			//TODO: find a way to send already chosen races
			//maybe send a race choice message after this one
			byteArray.write(IronUtil.intToByteArray(-1));
			
			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.SESSION_PLAYER_LIST.ordinal(),
					byteArray.toByteArray());
			
			server.getWriter().addMsg(msgToSend);
			
			
			if (other != null && destId == other.getId() && hostRace != -1) {
				byteArray.reset();
				byteArray.write(IronUtil.intToByteArray(host.getId()));
				byteArray.write(IronUtil.intToByteArray(hostRace));
				
				MessageToSend msgToSendBis = new MessageToSend(dest.getChannel(),
						Protocol.GAME_CREATION_RACE.ordinal(),
						byteArray.toByteArray());
				server.getWriter().addMsg(msgToSendBis);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}