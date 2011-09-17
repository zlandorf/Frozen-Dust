package fr.frozen.iron.common.entities.particles;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.util.IronGL;

public class DamageParticle extends IntParticle {

	public DamageParticle(IronWorld world, float x, float y, int value) {
		super(world, x, y, value, IronGL.getIntColor(0.8f, 0, 0), IronGL.getIntColor(0, 0.8f, 0));
	}
}
