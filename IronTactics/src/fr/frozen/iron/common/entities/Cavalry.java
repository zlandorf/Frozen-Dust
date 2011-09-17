package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.skills.Skill;

public class Cavalry extends IronUnit {
	public Cavalry(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_CAVALRY, ownerId, x, y);
	}
	
	@Override
	protected void addInitialSkills() {
		addSkill(Skill.getSkill(Skill.CHARGE));
	}
}
