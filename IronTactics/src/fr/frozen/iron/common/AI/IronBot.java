package fr.frozen.iron.common.AI;

import java.util.List;

import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.controller.IGameController;
import fr.frozen.iron.common.entities.IronUnit;

public class IronBot extends IronPlayer {

	protected IGameController controller;

	public IronBot(int Id, String name) {
		super(Id, name);
	}

	public void setController(IGameController controller) {
		this.controller = controller;
	}
	
	public void update(float deltaTime) {
		if (controller == null || !controller.isGameStarted()
				|| controller.isGameOver() || controller.getTurnPlayerId() != getId())
			return;

		List<IronUnit> myUnits = controller.getWorld().getPlayerUnits(getId());

		for (IronUnit unit : myUnits) {
			controller.handleMove(unit.getId(), (int) unit.getX(), (int) unit
					.getY() + 1);
		}
		controller.switchTurns();
	}
}
