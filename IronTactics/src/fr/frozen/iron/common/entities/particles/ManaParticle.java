package fr.frozen.iron.common.entities.particles;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;

public class ManaParticle extends IntParticle {
	public ManaParticle(IronWorld world, float x, float y, int value) {
		super(world, x, y, value, 0xa9018b, IronUnit.MANA_COLOR);
	}
}
