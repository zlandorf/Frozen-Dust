package fr.frozen.iron.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import fr.frozen.game.ISprite;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.serverSessions.GameCreationSession;
import fr.frozen.iron.serverSessions.GameSession;
import fr.frozen.iron.serverSessions.LobbySession;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;
import fr.frozen.network.server.BaseServer;
import fr.frozen.network.server.Client;
import fr.frozen.network.server.IGameController;

public class IronServer extends BaseServer {

	protected LobbySession lobby;
	protected List<GameCreationSession> gameCreations;
	protected List<GameCreationSession> gameCreationsToAdd;
	protected List<GameCreationSession> gameCreationsToRemove;
	protected int gameCreationCurrentID = 0;
	protected ISprite backTex;
	
	public IronServer() {
		super(1234);
	}
	
	@Override
	public void buildInitialGameSessions() {
		lobby = new LobbySession(this);
		addGameSession(lobby);
		gameCreations = new ArrayList<GameCreationSession>();
		gameCreationsToAdd = new ArrayList<GameCreationSession>();
		gameCreationsToRemove = new ArrayList<GameCreationSession>();
	}
	
	public Client getClient(int id) {
		return clientsById.get(id);
	}
	
	public synchronized List<GameCreationSession> getGameCreations() {
		synchronized (gameCreations) {
			return gameCreations;
		}		
	}
	
	public synchronized GameCreationSession getGameCreation(int gameId) {
		synchronized (gameCreations) {
			for (GameCreationSession gcs : gameCreations) {
				if (gcs.getId() == gameId) 
					return gcs;
			}
		}
		return null;
	}
	
	public GameCreationSession createGameCreation(int hostId) {
		synchronized (gameCreations) {
			Client c = clientsById.get(hostId);
			GameCreationSession gc = new GameCreationSession(this, gameCreationCurrentID, c);
			gameCreationCurrentID++;
			addGameCreation(gc);
			
			if (c.getCurrentGameSession() != null)
				c.getCurrentGameSession().removeClient(c, "created a new game");
			c.setCurrentGameSession(gc);
			
			notifySessionChange(c, gc.getSessionType());
			logger.info("new gameCreation created by "+c);
			return gc;
		}
	}
	
	public void createGame(Client host, int hostRace, Client other, int otherRace) {
		GameSession newGame = new GameSession(host, hostRace, other, otherRace, this);
		addGameSession(newGame);
		host.setCurrentGameSession(newGame);
		other.setCurrentGameSession(newGame);
		
		notifySessionChange(host, newGame.getSessionType());
		notifySessionChange(other, newGame.getSessionType());
	}
	
	public IGameController getLobbySession() {
		return lobby;
	}
	
	public synchronized void addGameCreation(GameCreationSession gc) {
		gameCreationsToAdd.add(gc);
		addGameSession(gc);
	}
	
	public synchronized void removeGameCreation(GameCreationSession gc) {
		gameCreationsToRemove.add(gc);
		removeGameSession(gc);
	}
	
	@Override
	protected synchronized void updateGameSessionsList() {
		super.updateGameSessionsList();
		
		for (GameCreationSession session : gameCreationsToAdd) {
			gameCreations.add(session);
		}
		gameCreationsToAdd.clear();
		
		
		for (GameCreationSession session : gameCreationsToRemove) {
			gameCreations.remove(session);
		}
		gameCreationsToRemove.clear();
	}
	
	@Override
	public void processMessage(Message msg) {
		Client sender = clientsById.get(msg.getClientId());
		IGameController currentGameSession = sender.getCurrentGameSession();

		if (msg.getType() == Protocol.SERVER_C_SEND_PLAYER_NAME.getValue()) {
			String name = new String(msg.getData());
			if (name != null && name.length() > 0) {
				
				//TODO : handle the case where two people have the same name
				logger.info(sender+ " chose name : "+ name);
				sender.setName(name);
				if (currentGameSession == null) {
					sender.setCurrentGameSession(lobby);
					lobby.addClient(sender);
				} else {
					currentGameSession.notifyNameChange(sender);
				}
			}
		} else if (currentGameSession != null ){
			currentGameSession.enQueueMessage(msg);
		}
	}
	
	public void notifySessionChange(Client c, int sessionType) {
		MessageToSend newSessionMsg = new MessageToSend(c.getChannel(),
				Protocol.SERVER_S_NEW_SESSION.ordinal(),
				IronUtil.intToByteArray(sessionType));
		getWriter().addMsg(newSessionMsg);
	}
	
	@Override
	protected Client addNewClient(SocketChannel channel) throws IOException {
		
		Client player = super.addNewClient(channel);
		MessageToSend msg = new MessageToSend(player.getChannel(),
											  Protocol.CONNECTION_S_SEND_PLAYER_ID.ordinal(),
											  IronUtil.intToByteArray(player.getId()));
		msgWriter.addMsg(msg);
		return player;
	}
	
	public static void main(String []args) {
		IronConfig.configServerLogger();
		BaseServer server = new IronServer();
		server.start();
	}
}
