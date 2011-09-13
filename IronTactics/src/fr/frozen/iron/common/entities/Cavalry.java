package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;

public class Cavalry extends IronUnit {
	public Cavalry(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_CAVALRY, ownerId, x, y);
	}
}
