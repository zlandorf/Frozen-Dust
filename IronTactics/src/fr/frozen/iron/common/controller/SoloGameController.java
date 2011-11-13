package fr.frozen.iron.common.controller;

import java.util.List;

import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;

public class SoloGameController extends AbstractGameController {

	public SoloGameController(IronWorld world, IronPlayer player1, int race1,
			IronPlayer player2, int race2) {
		super(world, player1, race1, player2, race2);
	}

	
	@Override
	public void handleSkill(int unitSrcId, Skill skill, int x, int y) {
		IronUnit unitSrc = world.getUnitFromId(unitSrcId);
		if (unitSrc != null && !unitSrc.hasPlayed() && skill != null
				&& unitSrc.getSkills().contains(skill)) {
			
			List<int[]> res = skill.computeSkill(world, unitSrcId, x, y);

			if (res != null) {
				setLastUnitMoved(null);
				notifySkill(unitSrc, skill, x, y, res);
			}
		}
	}

	@Override
	protected boolean isAddParticles() {
		return true;
	}
}
