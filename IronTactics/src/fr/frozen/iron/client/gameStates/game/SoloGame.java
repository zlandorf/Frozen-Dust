package fr.frozen.iron.client.gameStates.game;

import java.util.List;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.common.GameObserver;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.controller.GameController;
import fr.frozen.iron.common.controller.SoloGameController;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.common.skills.SkillInfo;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConst;
import fr.frozen.util.pathfinding.Path;

public class SoloGame extends AbstractGame implements GameObserver {

	protected GameController controller;

	public SoloGame(IGameEngine ge) {
		super(ge, "soloGame");
	}

	protected void initGame() {
		world = new IronWorld();

		IronPlayer player1 = new IronPlayer(0, "Player 1");
		IronPlayer player2 = new IronPlayer(1, "Player 2");

		players.add(player1);
		players.add(player2);
		
		playersById.put(player1.getId(), player1);
		playersById.put(player2.getId(), player2);
		
		controller = new SoloGameController(world, player1, Protocol.HUMAN_RACE
				.getValue(), player2, Protocol.ORC_RACE.getValue());

		controller.init();
		controller.getWorld().getMap().initSprites();
		controller.addGameObserver(this);
		controller.startGame();
	}
	
	@Override
	public synchronized void update(float deltaTime) {
		super.update(deltaTime);
		if (controller != null) 
			controller.update(deltaTime);
	}

	@Override
	public synchronized void setActive(boolean val) {
		super.setActive(val);
		if (val) {
			initGame();
		}
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		controller.removeGameObserver(this);
		controller = null;
		world.setContext(null);
		world = null;
	}

	@Override
	protected boolean canSelectUnit(IronUnit unit) {
		return unit != null && !unit.hasPlayed()
				&& unit.getOwnerId() == controller.getTurnPlayerId();
	}

	@Override
	protected String getNextTurnNotificationText(int nextTurnPlayerId) {
		return "It's " + playersById.get(turnPlayerId).getName() + "'s turn";
	}

	@Override
	protected String getNextTurnText(int nextTurnPlayerId) {
		return playersById.get(turnPlayerId).getName() + "'s";
	}

	@Override
	protected String getWinnerText(int winnerId) {
		if (winnerId < 0) {
			return null;
		}

		return playersById.get(winnerId).getName() + " is victorious !";
	}

	@Override
	protected void leaveGame() {
		((IronTactics) gameEngine).switchToState("mainMenu");
	}

	@Override
	protected void requestEndTurn() {
		controller.switchTurns();
	}

	@Override
	protected void requestMove(IronUnit unit, int x, int y) {
		controller.handleMove(unit.getId(), x, y);
	}

	@Override
	protected void requestSkill(SkillInfo skillInfo) {
		controller.handleSkill(skillInfo.getUnitId(), skillInfo.getSkill(),
				skillInfo.getX(), skillInfo.getY());
	}

	@Override
	protected void requestUndo() {
		// TODO : do undoMove without arguments, and controller handles who gets
		// his movement undone !
		controller.undoMove(lastUnitMoved);
	}

	@Override
	public void onGameOver(int winnerId) {
		gameOver = true;
		this.winnerId = winnerId;
	}

	@Override
	public void onGameStart(int startPlayerId) {
		worldReady = true;
	}

	@Override
	public void onMove(IronUnit unit, int x, int y, Path path) {
	}

	@Override
	public void onMoveUndo(IronUnit unit) {
	}

	@Override
	public void onSkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res) {
		skill.executeClientSide(world, unit.getId(), x, y, res);
	}

	@Override
	public void onTurnChange(int newTurnPlayerId) {
		turnPlayerId = newTurnPlayerId;

		timeLeftForTurn = IronConst.TURN_DURATION;

		if (selectedUnit != null) {
			selectedUnit.setSelected(false);
		}
		selectedUnit = null;
		lastUnitMoved = null;

		notifyNewTurnTimeLeft = notifyNewTurnDuration;
	}

}
