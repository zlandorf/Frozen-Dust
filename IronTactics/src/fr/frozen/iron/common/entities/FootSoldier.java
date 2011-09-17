package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.skills.Skill;

public class FootSoldier extends IronUnit {

	public FootSoldier(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_FOOTSOLDIER, ownerId, x, y);
	}
	
	@Override
	protected void addInitialSkills() {
		addSkill(Skill.getSkill(Skill.SHIELD_BLOCK));
	}
}
