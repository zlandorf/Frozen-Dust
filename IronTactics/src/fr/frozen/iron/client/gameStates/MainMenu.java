package fr.frozen.iron.client.gameStates;

import org.apache.log4j.Logger;

import fr.frozen.game.GameEngine;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.IronMenuButton;

public class MainMenu extends GameState {

	protected GUI gui;
	protected ISprite backTex;
	
	public MainMenu(final GameEngine ge) {
		super(ge, "mainMenu", false, false);
		gui = new GUI();

		Button multiplayer = new IronMenuButton("Multiplayer", 250);
		Button quit = new IronMenuButton("Quit", 390);
		Button option = new IronMenuButton("Options", 320);
		
		multiplayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((IronTactics)ge).connect();
			}
		});
		
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.getLogger(getClass()).info("ok, bye bye");
				ge.stopGame();
			}
		});
		
		option.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((IronTactics)ge).switchToState("optionMenu");
			}
		});
		
		gui.addComponent(multiplayer);
		//gui.addComponent(quit);
		gui.addComponent(option);
		
		backTex = SpriteManager.getInstance().getSprite("backTex");
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		gui.update(deltaTime);
	}
	
	@Override
	public void render(float deltaTime) {
		backTex.fillIn(0, 0, (float)gameEngine.getScreenSize().getWidth(), (float)gameEngine.getScreenSize().getHeight());
		super.render(deltaTime);
		gui.render(deltaTime);
	}
	
	
	@Override
	public void createGameObjects() {
		GameObject logo = new GameObject(this, 0, 0, SpriteManager.getInstance().getSprite("logo")) {
			@Override
			public void update(float deltaTime) {
				double x = gameEngine.getScreenSize().getWidth() / 2  - _sprite.getWidth() / 2;
				double y = 100 - _sprite.getHeight() / 2;
				_pos.set((float)x, (float)y);
			}
		};
		addGameObject(logo);
	}
}
