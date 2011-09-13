package fr.frozen.iron.common.entities;

import fr.frozen.iron.common.IronWorld;

public class Archer extends IronUnit {

	public Archer(IronWorld world, int id, int ownerId, float x, float y) {
		super(world, id, IronUnit.TYPE_ARCHER, ownerId, x, y);
	}

}
