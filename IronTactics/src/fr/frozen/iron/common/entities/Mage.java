package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;

public class Mage extends IronUnit {
	public Mage(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_MAGE, ownerId, x, y);
	}
}
