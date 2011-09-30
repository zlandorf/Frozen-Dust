package fr.frozen.iron.common.entities.particles;

import org.newdawn.slick.Color;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;

public class ManaParticle extends IntParticle {
	public ManaParticle(IronWorld world, float x, float y, int value) {
		super(world, x, y, value, new Color(0xffa9018b), new Color(0xff000000 & IronUnit.MANA_COLOR));
	}
}
