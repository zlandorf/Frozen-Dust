package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;

public class Special extends IronUnit {
	public Special(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_SPECIAL, ownerId, x, y);
	}
}
