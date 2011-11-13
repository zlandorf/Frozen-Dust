package fr.frozen.iron.client.gameStates.game;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.AI.IronBot;
import fr.frozen.iron.common.controller.SoloGameController;
import fr.frozen.iron.protocol.Protocol;

public class AIGame extends SoloGame {
	
	protected IronBot bot;

	public AIGame(IGameEngine ge) {
		super(ge);
	}

	protected void initGame() {
		world = new IronWorld();

		bot = new IronBot(0, "Bot");
		IronPlayer player = new IronPlayer(1, "Human");

		controller = new SoloGameController(world, bot, Protocol.HUMAN_RACE
				.getValue(), player, Protocol.ORC_RACE.getValue());

		
		bot.setController(controller);
		
		controller.init();
		controller.getWorld().getMap().initSprites();
		controller.addGameObserver(this);
		controller.startGame();
	}
	
	@Override
	public synchronized void update(float deltaTime) {
		super.update(deltaTime);
		bot.update(deltaTime);
	}
}
