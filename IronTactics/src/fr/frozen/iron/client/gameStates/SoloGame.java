package fr.frozen.iron.client.gameStates;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.SkillInfo;

public class SoloGame extends AbstractGame {

	protected SoloGame(IGameEngine ge) {
		super(ge, "solo game");
	}

	@Override
	protected boolean canSelectUnit(IronUnit unit) {
		return unit != null && !unit.hasPlayed()
		&& unit.getOwnerId() == getClientId();
	}

	public int getClientId() {
		return turnPlayerId;
	}

	@Override
	protected String getNextTurnNotificationText(int nextTurnPlayerId) {
		return "It's "+ playersById.get(turnPlayerId).getName()+"'s turn";
	}
	
	@Override
	protected String getNextTurnText(int nextTurnPlayerId) {
		return playersById.get(turnPlayerId).getName()+"'s";
	}

	@Override
	protected String getWinnerText(int winnerId) {
		if (winnerId < 0) {
			return null;
		}
		
		return playersById.get(winnerId).getName()+" wins the game !";
	}

	@Override
	protected void leaveGame() {
		((IronTactics)gameEngine).switchToState("lobby");
	}

	@Override
	protected void requestEndTurn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void requestMove(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void requestSkill(SkillInfo skillInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void requestUndo() {
		// TODO Auto-generated method stub
		
	}

}
