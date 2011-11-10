package fr.frozen.iron.common.controller;

import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;


public class ServerGameController extends AbstractGameController {

	public ServerGameController(IronWorld world, IronPlayer player1, int race1,
			IronPlayer player2, int race2) {
		super(world, player1, race1, player2, race2);
	}

	@Override
	protected boolean isAddParticles() {
		// TODO Auto-generated method stub
		return false;
	}

}
