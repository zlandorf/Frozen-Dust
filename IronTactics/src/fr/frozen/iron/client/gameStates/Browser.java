package fr.frozen.iron.client.gameStates;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.GameInfo;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.Component;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.GameBrowseButton;
import fr.frozen.iron.client.messageEvents.GameListReceivedEvent;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class Browser extends GameState implements NetEventListener, ActionListener {

	protected static int Y_START = 50;
	protected static int BUTTON_HEIGHT = 50;
	protected static int PADDING = 15;
	
	protected GUI gui;
	protected IronClient netClient;

	protected List<GameBrowseButton> gameButtons;
	
	protected ISprite backTex;
	
	public Browser(GameEngine ge) {
		super(ge, "browser", false, false);
		
		netClient = ((IronTactics)gameEngine).getNetClient();
		gui = new GUI();
		
		gameButtons = new ArrayList<GameBrowseButton>();
		backTex = SpriteManager.getInstance().getSprite("backTex");
		
		
		ISprite spriteNormal = SpriteManager.getInstance().getSprite("buttonNormal");
		ISprite spriteHover = SpriteManager.getInstance().getSprite("buttonHover");
		
		Button button = new Button("Refresh", 600, 300, 0, 0);
		button.setDim((int)spriteNormal.getWidth(),(int)spriteNormal.getHeight());
		button.setHoverSprite(spriteHover);
		button.setNormalSprite(spriteNormal);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		
		
		
		Button button2 = new Button("Back to lobby", 600, 400, 0, 0);
		button2.setDim((int)spriteNormal.getWidth(),(int)spriteNormal.getHeight());
		button2.setHoverSprite(spriteHover);
		button2.setNormalSprite(spriteNormal);
		
		button2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				leave();
			}
		});
		
		gui.addComponent(button);
		gui.addComponent(button2);
	}
	
	private void leave() {
		setVisible(false);
		setActive(false);
		
		//gameEngine.getGameState("lobby").setActive(true);
		gameEngine.getGameState("lobby").setVisible(true);
		gameEngine.setCurrentGameState(gameEngine.getGameState("lobby"));
	}
	
	private void refresh() {
		removeButtons();
		netClient.sendEmptyMessage(Protocol.SESSION_GAME_LIST_REQUEST);
	}
	
	private void removeButtons() {
		synchronized(gameButtons) {
			for (Component c : gameButtons)
				gui.removeComponent(c);
			gameButtons.clear();
		}
	}
	
	@Override
	public void setActive(boolean val) {
		super.setActive(val);
		if (!val) {
			removeButtons();
		} else {
			netClient.sendEmptyMessage(Protocol.SESSION_GAME_LIST_REQUEST);
		}
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		gui.update(deltaTime);
	}
	
	@Override
	public void render(float deltaTime) {
		backTex.fillIn(0, 0, (float)gameEngine.getScreenSize().getWidth(), (float)gameEngine.getScreenSize().getWidth());
		super.render(deltaTime);
		gui.render(deltaTime);
	}

	@Override
	public void onNetEvent(NetEvent ne) {
		if (!active) return;
		
		if (ne instanceof GameListReceivedEvent) {
			GameListReceivedEvent glre = (GameListReceivedEvent) ne;
			synchronized (gameButtons) {
				for (GameInfo info : glre.getList()) {
					GameBrowseButton button = new GameBrowseButton(info.getId(), info.getHost()+" "+info.getId(), 
												Y_START + gameButtons.size() * (PADDING + BUTTON_HEIGHT));
					gameButtons.add(button);
					gui.addComponent(button);
					button.addActionListener(this);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof GameBrowseButton) {
			GameBrowseButton gbb = (GameBrowseButton) e.getSource();
			netClient.sendMessage(Protocol.SESSION_JOIN_GAME_REQUEST, IronUtil.intToByteArray(gbb.getGameId()));
		}
	}
}
