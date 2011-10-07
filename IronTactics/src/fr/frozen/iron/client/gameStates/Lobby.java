package fr.frozen.iron.client.gameStates;

import java.util.Hashtable;

import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronPlayer;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.ChatWindow;
import fr.frozen.iron.client.components.ChatWindowMessage;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.TextField;
import fr.frozen.iron.client.messageEvents.ChatMessageEvent;
import fr.frozen.iron.client.messageEvents.IronTacticsInfoEvent;
import fr.frozen.iron.client.messageEvents.NameChangeEvent;
import fr.frozen.iron.client.messageEvents.NewPlayerEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class Lobby extends GameState implements NetEventListener {

	protected GUI gui;
	protected IronClient netClient;
	protected TextField textField;
	protected ChatWindow chatWindow;
	
	protected Hashtable<Integer, IronPlayer> players;
	
	protected ISprite backTex;
	
	public Lobby(GameEngine ge) {
		super(ge, "lobby", false, false);
		netClient = ((IronTactics)gameEngine).getNetClient();
		players = new Hashtable<Integer, IronPlayer>();
		gui = new GUI();
		
		backTex = SpriteManager.getInstance().getSprite("backTex");
		
		textField = new TextField(15, 560, 520, 25);
		chatWindow = new ChatWindow(15,15, 520,520);
		textField.addActionListener(new TextFieldListener());
		
		//TODO put the sprite shit in a button class directly
		Button button = new Button("Join", 600, 300, 0, 0);
		Button button2 = new Button("Create", 600, 400, 0, 0);
		Button button3 = new Button("Options", 600, 500, 0, 0);
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				joinGameBrowser();
			}
		});
		
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createGame();
			}
		});
		
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openOptions();
			}
		});
		
		gui.addComponent(textField);
		gui.addComponent(chatWindow);
		gui.addComponent(button);
		gui.addComponent(button2);
		gui.addComponent(button3);
	}

	private void openOptions() {
		setVisible(false);
		
		gameEngine.getGameState("optionMenu").setActive(true);
		gameEngine.getGameState("optionMenu").setVisible(true);
		gameEngine.setCurrentGameState(gameEngine.getGameState("optionMenu"));
	}
	
	public void joinGameBrowser() {
		/* when browsing the game list, the lobby is still active, but not visible */
		/* i am still in the lobby game session on the server side */
		setVisible(false);
		
		gameEngine.getGameState("browser").setActive(true);
		gameEngine.getGameState("browser").setVisible(true);
		gameEngine.setCurrentGameState(gameEngine.getGameState("browser"));
	}
	
	public void createGame() {
		netClient.sendEmptyMessage(Protocol.SESSION_CREATE_GAME_REQUEST);
	}
	
	@Override
	public void createGameObjects() {
		//GameObject o = new GameObject(this, 350, 250, ISpriteManager.getInstance().getSprite("avenger.png"));
		//addGameObject(o);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (isVisible())
			gui.update(deltaTime);
	}
	
	@Override
	public void render(float deltaTime) {
		backTex.fillIn(0, 0, (float)gameEngine.getScreenSize().getWidth(), (float)gameEngine.getScreenSize().getWidth());
		super.render(deltaTime);
		gui.render(deltaTime);
	}
	
	
	@Override
	public void setActive(boolean val) {
		boolean oldVal = isActive();
		super.setActive(val);
		if (!val) {
			players.clear();
			chatWindow.clearMessages();
		} else {
			if (!oldVal && val) {
				netClient.sendEmptyMessage(Protocol.IRONTACTICS_INFO_REQUEST);
				netClient.sendEmptyMessage(Protocol.SESSION_PLAYER_LIST_REQUEST);
			}
		}
	}
	
	@Override
	public void onNetEvent(NetEvent ne) {
		if (!active) return;

		if (ne instanceof ChatMessageEvent) {
			ChatMessageEvent cme = (ChatMessageEvent) ne;
			IronPlayer sender = players.get(cme.getSenderId());
			if (sender == null) return;
			chatWindow.addMessage(new ChatWindowMessage(ChatWindowMessage.CHAT_MESSAGE, sender.getName()+ " says : ", cme.getText()));
		}
		
		if (ne instanceof PlayerListReceivedEvent) {
			PlayerListReceivedEvent plre = (PlayerListReceivedEvent) ne;
			for (IronPlayer player : plre.getList()) {
				addPlayer(player);
			}
			chatWindow.addMessage(
					new ChatWindowMessage(ChatWindowMessage.SERVER_MESSAGE,
										  "There are "+(plre.getList().size() - 1)+" other players connected in the lobby."));
		}
		
		if (ne instanceof IronTacticsInfoEvent) {
			IronTacticsInfoEvent itie = (IronTacticsInfoEvent) ne;
			chatWindow.addMessage(
					new ChatWindowMessage(ChatWindowMessage.SERVER_MESSAGE,
										  "There are "+(itie.getNbPlayers() - 1)
										  +" other players connected to the game and "+itie.getNbGames()
										  + " games being played."));
		}
		
		if (ne instanceof NewPlayerEvent) {
			NewPlayerEvent npe = (NewPlayerEvent) ne;
			addPlayer(npe.getPlayer());
			chatWindow.addMessage(
					new ChatWindowMessage(ChatWindowMessage.SERVER_MESSAGE,
										  npe.getPlayer().getName()+ " logged into lobby"));
		}
		
		if (ne instanceof PlayerLogoutEvent) {
			PlayerLogoutEvent ploe = (PlayerLogoutEvent) ne;
			IronPlayer player = players.get(ploe.getId());
			if (player == null) return;
			chatWindow.addMessage(
					new ChatWindowMessage(ChatWindowMessage.SERVER_MESSAGE,
										  player.getName()+ " left : "+ploe.getReason()));
		}
		
		if (ne instanceof NameChangeEvent) {
			NameChangeEvent nce = (NameChangeEvent) ne;
			IronPlayer player = players.get(nce.getPlayerId());
			if (player == null) return;
			chatWindow.addMessage(
					new ChatWindowMessage(ChatWindowMessage.SERVER_MESSAGE,
										  player.getName()+ " changed name to "+nce.getName()));
			player.setName(nce.getName());
		}
	}
	
	public void addPlayer(IronPlayer player) {
		if (players.get(player.getId()) != null) {
			players.remove(player.getId()); //only one player per id
		}
		players.put(player.getId(), player);
	}
	
	class TextFieldListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = textField.getText();
			if (!text.isEmpty()) {
				netClient.sendMessage(Protocol.SESSION_CHAT_MESSAGE, text);
			}
			textField.setText("");
		}
	}
}
