package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;

public class Healer extends IronUnit {
	public Healer(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_HEALER, ownerId, x, y);
	}
}
