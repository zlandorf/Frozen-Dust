package fr.frozen.iron.common.entities.particles;

import org.newdawn.slick.Color;

import fr.frozen.iron.common.IronWorld;

public class DamageParticle extends IntParticle {

	public DamageParticle(IronWorld world, float x, float y, int value) {
		super(world, x, y, value, new Color(0.8f, 0, 0), new Color(0, 0.8f, 0));
	}
}
