package fr.frozen.iron.client.gameStates;

import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.IronMenuButton;
import fr.frozen.iron.client.components.Label;
import fr.frozen.iron.client.components.TextField;
import fr.frozen.iron.util.IronConfig;

public class OptionMenu extends GameState {
	protected GUI gui;
	protected ISprite backTex;
	protected TextField textField;
	protected Label showGridLabel;
	
	public OptionMenu(final GameEngine ge) {
		super(ge, "optionMenu", false, false);
		gui = new GUI();

		textField = new TextField(300, 200, 200, 25);
		textField.setText(IronConfig.getUserName());
		Button apply = new IronMenuButton("apply", 250);
		//320
		Button showGrid = new IronMenuButton("ShowGrid",320);
		showGridLabel = new Label(IronConfig.isShowGrid() ? "on" : "off",
								  525, 310, 30, 50);
		
		Button back = new IronMenuButton("Back", 390);
		
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeName();
			}
		});
		
		showGrid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleShowGrid();
			}
		});
		
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeName();
			}
		});
		
		gui.addComponent(apply);
		gui.addComponent(back);
		gui.addComponent(textField);
		gui.addComponent(showGrid);
		gui.addComponent(showGridLabel);
		
		backTex = ISpriteManager.getInstance().getSprite("backTex");
	}
	
	protected void toggleShowGrid() {
		IronConfig.setShowGrid(!IronConfig.isShowGrid());
		showGridLabel.setLabel(IronConfig.isShowGrid() ? "on" : "off");
	}
	
	protected void quit() {
		//TODO switch back to old state, and not mainMenu !
		((IronTactics)gameEngine).switchToState("mainMenu");
	}
	
	protected void changeName() {
		String text = textField.getText();
		if (text == null || text.equals("") || text.length() > 20) return;
		if (gameEngine instanceof IronTactics) {
			((IronTactics)gameEngine).changeUserName(text);
			IronConfig.setUserName(text);
		}
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
}
