package fr.frozen.iron.serverSessions;

import java.io.IOException;

import fr.frozen.iron.net.IronServer;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;
import fr.frozen.network.server.Client;

public class LobbySession extends BaseGameController {

	public LobbySession(IronServer server) {
		super("Lobby", server, Protocol.SESSION_LOBBY);
	}

	@Override
	public void handle(Message msg) {
		switch (Protocol.get(msg.getType())) {
		case SESSION_CREATE_GAME_REQUEST :
			server.createGameCreation(msg.getClientId());
			break;
		case SESSION_GAME_LIST_REQUEST :
			sendGameList(msg.getClientId());
			break;
		case SESSION_JOIN_GAME_REQUEST :
			handleJoinGameRequest(msg.getClientId(), msg.getData());
			break;
		default :
			super.handle(msg);
			break;
		}
	}
	
	public void handleJoinGameRequest(int clientId, byte[] data) {
		Client c = server.getClient(clientId);
		int gameId = IronUtil.byteArrayToInt(data);
		GameCreationSession gameRequested = server.getGameCreation(gameId);
		
		if (gameRequested == null 
				|| !gameRequested.slotAvailable()
				|| c.getCurrentGameSession() == null
				|| c.getCurrentGameSession().getSessionType() != Protocol.SESSION_LOBBY.ordinal()) {
			return;
		}
		c.getCurrentGameSession().removeClient(c, "joined game "+gameId);
		c.setCurrentGameSession(gameRequested);
		gameRequested.addClient(c);
		
		//server.notifySessionChange(c, gameRequested.getSessionType());
	}
	
	public void sendGameList(int destId) {
		Client dest = server.getClient(destId);

		if (dest == null) {
			logger.error("problem, client is null (in send game list) in "+sessionType);
			return;
		}
		logger.debug(dest+" requesting game list");
			
		try {
			byteArray.reset();
			
			for (GameCreationSession gameCreation : server.getGameCreations()) {
				if (!gameCreation.slotAvailable()) continue;
				byteArray.write(IronUtil.intToByteArray(gameCreation.getId()));
				byteArray.write(IronUtil.intToByteArray(gameCreation.getHost().getName().length()));
				byteArray.write(gameCreation.getHost().getName().getBytes());
			}

			byteArray.write(IronUtil.intToByteArray(-1));//end of list
			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.SESSION_GAME_LIST.ordinal(),
					byteArray.toByteArray());
			
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
