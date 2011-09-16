package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.skills.Skill;

public class Healer extends IronUnit {
	public Healer(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_HEALER, ownerId, x, y);
	}
	
	@Override
	protected void addInitialSkills() {
		addSkill(Skill.getSkill(Skill.HEAL));
	}
}
