package fr.frozen.iron.common.controller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import fr.frozen.iron.common.GameContext;
import fr.frozen.iron.common.GameObserver;
import fr.frozen.iron.common.IronMap;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.PlayerGameInfo;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.util.XMLParser;
import fr.frozen.util.pathfinding.Path;

public class GameController implements GameContext {

	protected int hostColor = 0xff0000;
	protected int otherColor = 0x0070b7;// 0xff3333;

	protected IronWorld world;

	protected List<IronPlayer> players;
	protected Hashtable<Integer, IronPlayer> playersById;
	protected Hashtable<IronPlayer, PlayerGameInfo> playerInfo;

	protected boolean gameStarted = false;
	protected boolean gameOver = false;
	protected int winnerId = -1;

	protected float timeLeftForTurn = 0;
	protected int turnPlayerId = -1;
	protected int turnIndex = 0; // switches from 0 to 1 to make it easier to
	// switch turns

	protected List<GameObserver> observers;
	protected Logger logger;

	protected int nextEntityId = 0;

	public GameController(IronWorld world, IronPlayer player1, int race1,
			IronPlayer player2, int race2) {
		logger = Logger.getLogger(getClass());

		this.world = world;
		this.world.setContext(this);

		observers = new Vector<GameObserver>();

		players = new ArrayList<IronPlayer>();
		players.add(player1);
		players.add(player2);

		playersById = new Hashtable<Integer, IronPlayer>();
		playersById.put(player1.getId(), player1);
		playersById.put(player2.getId(), player2);

		playerInfo = new Hashtable<IronPlayer, PlayerGameInfo>();
		playerInfo.put(player1, new PlayerGameInfo(Protocol.get(race1),
				hostColor));
		playerInfo.put(player2, new PlayerGameInfo(Protocol.get(race2),
				otherColor));
	}

	public IronWorld getWorld() {
		return world;
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public int getWinnerId() {
		return winnerId;
	}

	@Override
	public int getTurnPlayerId() {
		if (!isGameStarted() || isGameOver()) {
			return -1;
		}
		return turnPlayerId;
	}

	public void update(float deltaTime) {
		if (!gameStarted)
			return;
		boolean turnEnded = false;

		timeLeftForTurn -= deltaTime;
		if (timeLeftForTurn <= 0) {
			turnEnded = true;
		}

		if (!gameOver) {
			if (world.areAllUnitsDead(players.get(turnIndex ^ 1).getId())) {
				onGameOver(players.get(turnIndex), players.get(1 ^ turnIndex));
			}
		}

		turnEnded |= world.haveAllUnitsPlayed(turnPlayerId);

		if (turnEnded && !gameOver) {
			switchTurns();
		}
	}

	public void undoMove(IronUnit unit) {
		if (unit != null && !unit.isDead()
				&& unit.getOwnerId() == getTurnPlayerId() && unit.canUndo()
				&& !gameOver) {
			unit.undoMove();
			notifyUndoMove(unit);
		}
	}

	public void onGameOver(IronPlayer winner, IronPlayer loser) {
		winnerId = winner.getId();

		int nbWinnerUnits = 0;
		int nbLoserUnits = 0;

		for (IronUnit unit : world.getUnits()) {
			if (!unit.isDead()) {
				if (unit.getOwnerId() == winner.getId()) {
					nbWinnerUnits++;
				} else {
					nbLoserUnits++;
				}
			}
		}

		logger.info("[GameOver]" + winner + "("
				+ playerInfo.get(winner).getRace() + ") wins against " + loser
				+ "(" + playerInfo.get(loser).getRace() + ") -- Units left : ["
				+ nbWinnerUnits + "]vs[" + nbLoserUnits + "]");
		gameOver = true;
		notifyGameOver(winnerId);
	}

	public void switchTurns() {
		if (gameOver)
			return;

		turnIndex ^= 1;
		if (players.size() == 2)
			setTurn(players.get(turnIndex).getId());
	}

	public synchronized void setTurn(int playerId) {
		if (turnPlayerId != -1) {
			getPlayerInfo(turnPlayerId).setTurnToPlay(false);
			world.endTurn(playerId);
		}
		getPlayerInfo(playerId).setTurnToPlay(true);
		turnPlayerId = playerId;
		world.initTurn(playerId, false);

		timeLeftForTurn = IronConst.TURN_DURATION;

		notifyTurnChange(turnPlayerId);
	}

	public void abandon(int playerId) {
		if (!isGameOver()) {
			int winnerIndex = playerId == turnPlayerId ? 1 ^ turnIndex
					: turnIndex;

			logger.info(playersById.get(playerId) + " abandonned the game");
			onGameOver(players.get(winnerIndex), players.get(1 ^ winnerIndex));
		}
	}

	public void startGame() {
		turnIndex = (int) (System.currentTimeMillis() % 2);
		IronPlayer player = players.get(turnIndex);
		setTurn(player.getId());

		gameStarted = true;
		notifyStartGame(player.getId());

	}

	public void handleMove(int unitSrcId, int x, int y) {
		IronUnit unit = world.getUnitFromId(unitSrcId);
		if (unit == null || unit.hasPlayed() || unit.isDead())
			return;
		Path path = world.getPath(unitSrcId, x, y);
		if (path != null) {
			unit.move(x, y, path.getTotalMoveCost());
			notifyMove(unit, x, y, path);
		}
	}

	public void handleSkill(int unitSrcId, Skill skill, int x, int y) {
		IronUnit unitSrc = world.getUnitFromId(unitSrcId);
		if (unitSrc != null && !unitSrc.hasPlayed() && skill != null
				&& unitSrc.getSkills().contains(skill)) {
			
			List<int[]> res = skill.executeSkill(world, unitSrcId, x, y);

			if (res != null) {
				notifySkill(unitSrc, skill, x, y, res);
			}
		}
	}

	@Override
	public PlayerGameInfo getPlayerInfo(int playerId) {
		return playerInfo.get(playersById.get(playerId));
	}

	public void addGameObserver(GameObserver observer) {
		observers.add(observer);
	}

	public void removeGameObserver(GameObserver observer) {
		observers.remove(observer);
	}

	protected void notifyTurnChange(int playerId) {
		for (GameObserver observer : observers) {
			observer.onTurnChange(playerId);
		}
	}

	protected void notifyStartGame(int playerId) {
		for (GameObserver observer : observers) {
			observer.onGameStart(playerId);
		}
	}

	protected void notifyGameOver(int playerId) {
		for (GameObserver observer : observers) {
			observer.onGameOver(playerId);
		}
	}

	protected void notifyUndoMove(IronUnit unit) {
		for (GameObserver observer : observers) {
			observer.onMoveUndo(unit);
		}
	}

	protected void notifySkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res) {
		for (GameObserver observer : observers) {
			observer.onSkill(unit, skill, x, y, res);
		}
	}

	protected void notifyMove(IronUnit unit, int x, int y, Path path) {
		for (GameObserver observer : observers) {
			observer.onMove(unit, x, y, path);
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

		main: for (IronPlayer player : players) {

			String race = null;
			if (playerInfo.get(player) == null) {
				logger.error("player info not found");
				continue; // TODO error to handle here
			}
			race = IronUtil.getRaceStr(playerInfo.get(player).getRace());

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
							nextEntityId, player.getId(), x[i], y[i]);
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

	public void init() {
		world.setMap(new IronMap());
		world.getMap().generateMap();
		world.setUnits(createGameUnitsList());
	}
}
