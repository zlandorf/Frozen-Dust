package fr.frozen.iron.common;

import java.util.List;

import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.util.pathfinding.Path;

public interface GameObserver {

	public void onGameStart(int startPlayerId);

	public void onGameOver(int winnerId);

	public void onTurnChange(int newTurnPlayerId);

	public void onMoveUndo(IronUnit unit);

	public void onSkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res);
	
	public void onMove(IronUnit unit, int x, int y, Path path);
}
