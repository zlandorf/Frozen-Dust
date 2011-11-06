package fr.frozen.iron.serverSessions;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.frozen.iron.common.GameController;
import fr.frozen.iron.common.GameObserver;
import fr.frozen.iron.common.IronMap;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.net.IronServer;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;
import fr.frozen.network.server.Client;
import fr.frozen.util.XMLParser;
import fr.frozen.util.pathfinding.Path;

public class GameSession extends BaseGameController implements GameObserver {

	protected Hashtable<Client, Integer> playerRaces;

	protected IronWorld world;
	protected GameController controller;

	protected int nextEntityId = 0;

	protected int lastReadyId = -1;
	protected int nbReady = 0;

	public GameSession(Client host, int hostRace, Client other, int otherRace,
			IronServer server) {
		super("Game session", server, Protocol.SESSION_GAME);
		clients.add(host);
		clients.add(other);

		logger.info("[New game] " + host + "(" + Protocol.get(hostRace)
				+ ") vs " + other + "(" + Protocol.get(otherRace) + ")");

		playerRaces = new Hashtable<Client, Integer>();
		playerRaces.put(host, hostRace);
		playerRaces.put(other, otherRace);

		world = new IronWorld();
		controller = new GameController(world, new IronPlayer(host.getId(),
				host.getName()), hostRace, new IronPlayer(other.getId(), other
				.getName()), otherRace);

		world.setMap(new IronMap());
		world.getMap().generateMap();
		world.setUnits(createGameUnitsList());

		controller.addGameObserver(this);
	}

	// TODO USE THE CONTROLLER !!

	@Override
	public synchronized void removeClient(Client c, String reason) {
		super.removeClient(c, reason);
		if (clients.size() == 0) {
			server.removeGameSession(this);
			//avoid having leaks ?
			controller = null;
			world.setContext(null);
			world = null;
			logger.debug(this + " removed");
		} else {
			controller.abandon(c.getId());
		}
	}

	@Override
	public synchronized void onTurnChange(int playerId) {

		List<SocketChannel> channels = getAllChannels();
		if (channels == null) {
			return;// TODO handle this type of error ?
		}
		server.getWriter().addMsg(
				new MessageToSend(channels, Protocol.GAME_TURN.getValue(),
						IronUtil.intToByteArray(playerId)));
	}

	@Override
	public synchronized void update(float deltaTime) {
		super.update(deltaTime);
		if (controller != null) {
			controller.update(deltaTime);
		}
	}

	protected List<IronUnit> createGameUnitsList() {
		List<IronUnit> list = new ArrayList<IronUnit>();
		IronUnit unit = null;
		XMLParser parser = IronConfig.getIronXMLParser();
		int nblines = 2;

		String[] line = new String[nblines];

		int nb = 0;// 0 is when its top player, and 1 bottom player
		int[] x = new int[nblines];
		int[] y = new int[nblines];

		main: for (Client client : clients) {

			String race = null;
			if (playerRaces.get(client) == null)
				continue; // TODO error to handle here
			race = IronUtil.getRaceStr(playerRaces.get(client));

			if (race == null) {
				logger.error("race not found");
				continue; // TODO error to handle here
			}

			for (int i = 0; i < nblines; i++) {
				line[i] = parser.getAttributeValue("deployement/" + race,
						"line" + (i + 1));
				if (line[i] == null) {
					logger
							.error("probs at parsing deployement line "
									+ (i + 1));
					continue main;
				}
			}

			for (int i = 0; i < nblines; i++) {
				x[i] = IronConst.MAP_WIDTH / 2 - line[i].length() / 2;
				y[i] = nb == 0 ? i : IronConst.MAP_HEIGHT - (1 + i);
			}

			for (int i = 0; i < nblines; i++) {
				for (int j = 0; j < line[i].length(); j++) {
					unit = IronUnit.getUnit(line[i].charAt(j), world,
							nextEntityId, client.getId(), x[i], y[i]);
					x[i]++;
					if (unit != null) {
						list.add(unit);
						nextEntityId++;
						unit = null;
					}
				}
			}
			nb++;
		}
		return list;
	}

	@Override
	public synchronized void onMoveUndo(IronUnit unit) {
		List<SocketChannel> channels = getAllChannels();
		if (channels == null) {
			return;// TODO handle this type of error ?
		}
		MessageToSend msgToSend = new MessageToSend(channels,
				Protocol.GAME_UNDO.ordinal(), IronUtil.intToByteArray(unit
						.getId()));
		server.getWriter().addMsg(msgToSend);
	}

	@Override
	public void handle(Message msg) {

		// TODO : add security everywhere in case there is false data sending
		switch (Protocol.get(msg.getType())) {
		case GAME_END_TURN_REQUEST:
			synchronized (this) {
				if (controller.getTurnPlayerId() == msg.getClientId()) {
					controller.switchTurns();
				}
			}
			break;

		case GAME_UNDO_REQUEST:
			IronUnit unit = world.getUnitFromId(IronUtil.byteArrayToInt(msg
					.getData()));
			if (msg.getClientId() == controller.getTurnPlayerId()) {
				controller.undoMove(unit);
			}
			break;

		case GAME_ACTION_REQUEST:
			try {
				if (controller.getTurnPlayerId() == msg.getClientId()) {
					handleActionRequest(msg.getData());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case GAME_MAP_REQUEST:
			sendMap(msg.getClientId());
			break;
		case GAME_PLAYER_INFO_REQUEST:
			sendPlayerInfo(msg.getClientId());
			break;
		case GAME_UNIT_LIST_REQUEST:
			sendGameUnitList(msg.getClientId());
			break;
		case GAME_READY:
			if (msg.getClientId() != lastReadyId) {
				lastReadyId = msg.getClientId();
				nbReady++;

				if (nbReady == 2) {
					controller.startGame();
				}
			}
			break;
		default:
			super.handle(msg);
			break;
		}
	}

	public void handleActionRequest(byte[] data) throws IOException {
		DataInput is = new DataInputStream(new ByteArrayInputStream(data));

		int unitSrcId = is.readInt();
		int actionType = is.readInt();
		int x, y;

		if (world.getUnitFromId(unitSrcId).hasPlayed())
			return;

		switch (actionType) {
		case IronUnit.ACTION_MOVE:
			x = is.readInt();
			y = is.readInt();
			controller.handleMove(unitSrcId, x, y);
			break;
		case IronUnit.ACTION_SKILL:
			int skillType = is.readInt();
			x = is.readInt();
			y = is.readInt();
			Skill skill = Skill.getSkill(skillType);
			controller.handleSkill(unitSrcId, skill, x, y);
			break;
		default:
			logger.error("ACTION NOT SUPPORTED :" + actionType);
		}
	}

	@Override
	public synchronized void onSkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res) {

		if (res == null)
			return;

		try {
			List<SocketChannel> channels = getAllChannels();
			if (channels == null) {
				return;// TODO handle this type of error ?
			}

			// goes as follows :
			// unitID - actionType - skillType - x - y - list of [dstId - value]
			// ended by -1

			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(unit.getId()));
			byteArray.write(IronUtil.intToByteArray(IronUnit.ACTION_SKILL));
			byteArray.write(IronUtil.intToByteArray(skill.getSkillType()));
			byteArray.write(IronUtil.intToByteArray(x));
			byteArray.write(IronUtil.intToByteArray(y));

			for (int[] couple : res) {
				byteArray.write(IronUtil.intToByteArray(couple[0]));// targetId
				byteArray.write(IronUtil.intToByteArray(couple[1]));// value of
				// heal/damage
			}

			byteArray.write(IronUtil.intToByteArray(-1));

			MessageToSend msgToSend = new MessageToSend(channels,
					Protocol.GAME_ACTION.ordinal(), byteArray.toByteArray());
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			logger
					.error("error while trying to send packet to notify skill execution");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void onMove(IronUnit unit, int x, int y, Path path) {
		if (path == null)
			return;

		List<SocketChannel> channels = getAllChannels();
		if (channels == null) {
			return;// TODO handle this type of error ?
		}
		// goes as follows
		// unitId - actionType (here ACTION_MOVE) - x - y - moveCost

		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(unit.getId()));
			byteArray.write(IronUtil.intToByteArray(IronUnit.ACTION_MOVE));
			byteArray.write(IronUtil.intToByteArray(x));
			byteArray.write(IronUtil.intToByteArray(y));
			byteArray.write(IronUtil.intToByteArray(path.getTotalMoveCost()));

			server.getWriter().addMsg(
					new MessageToSend(channels,
							Protocol.GAME_ACTION.getValue(), byteArray
									.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMap(int clientId) {
		Client dest = server.getClient(clientId);

		if (dest == null) {
			logger.error("problem, client is null (in sendMap");
			return;
		}
		try {
			byteArray.reset();
			byteArray.write(world.getMap().serialize());

			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.GAME_MAP_SEND.ordinal(), byteArray.toByteArray());

			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendPlayerInfo(int clientId) {
		Client dest = server.getClient(clientId);

		if (dest == null || clients.size() != 2) {
			logger.error("problem, in send playerInfo");
			return;
		}
		try {
			byteArray.reset();
			for (Client c : clients) {
				byteArray.write(IronUtil.intToByteArray(c.getId()));
				byteArray
						.write(controller.getPlayerInfo(c.getId()).serialize());
			}
			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.GAME_PLAYER_INFO_SEND.ordinal(), byteArray
							.toByteArray());

			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendGameUnitList(int clientId) {
		Client dest = server.getClient(clientId);

		if (dest == null) {
			logger.error("problem, client is null (in sendGameUnit");
			return;
		}
		try {
			byteArray.reset();
			for (IronUnit unit : world.getUnits()) {
				byteArray.write(unit.serialize());
			}
			byteArray.write(IronUtil.intToByteArray(-1));// end of list

			MessageToSend msgToSend = new MessageToSend(dest.getChannel(),
					Protocol.GAME_UNIT_LIST_SEND.ordinal(), byteArray
							.toByteArray());

			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void onGameOver(int winnerId) {
		List<SocketChannel> channels = getAllChannels();
		if (channels == null)
			return;

		try {
			byteArray.reset();
			byteArray.write(IronUtil.intToByteArray(winnerId));

			MessageToSend msgToSend = new MessageToSend(channels,
					Protocol.GAME_OVER.ordinal(), byteArray.toByteArray());
			server.getWriter().addMsg(msgToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void onGameStart(int playerId) {
		// TODO Auto-generated method stub

	}
}
