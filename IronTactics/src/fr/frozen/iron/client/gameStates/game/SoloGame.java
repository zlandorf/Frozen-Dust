package fr.frozen.iron.client.gameStates.game;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.controller.SoloGameController;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.protocol.Protocol;

public class SoloGame extends AbstractGame {

	public SoloGame(IGameEngine ge) {
		super(ge, "soloGame");
	}

	protected void initGame() {
		world = new IronWorld();

		IronPlayer player1 = new IronPlayer(0, "Player 1");
		IronPlayer player2 = new IronPlayer(1, "Player 2");

		controller = new SoloGameController(world, player1, Protocol.HUMAN_RACE
				.getValue(), player2, Protocol.ORC_RACE.getValue());

		controller.init();
		controller.getWorld().getMap().initSprites();
		controller.addGameObserver(this);
		controller.startGame();
	}
	
	@Override
	protected boolean canSelectUnit(IronUnit unit) {
		return unit != null && !unit.hasPlayed()
				&& unit.getOwnerId() == controller.getTurnPlayerId();
	}

	@Override
	protected String getNextTurnNotificationText(int nextTurnPlayerId) {
		return "It's " + controller.getTurnPlayer().getName() + "'s turn";
	}

	@Override
	protected String getNextTurnText(int nextTurnPlayerId) {
		return controller.getTurnPlayer().getName() + "'s";
	}

	@Override
	protected String getWinnerText(int winnerId) {
		if (winnerId < 0) {
			return null;
		}
		return controller.getWinner().getName() + " is victorious !";
	}

	@Override
	protected void leaveGame() {
		((IronTactics) gameEngine).switchToState("mainMenu");
	}

	@Override
	protected void requestUndo() {
		// TODO : do undoMove without arguments, and controller handles who gets
		// his movement undone !
		controller.undoMove();
	}
}
