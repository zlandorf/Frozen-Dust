package fr.frozen.iron.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameEngine;
import fr.frozen.game.SoundManager;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.gameStates.Browser;
import fr.frozen.iron.client.gameStates.Game;
import fr.frozen.iron.client.gameStates.GameCreation;
import fr.frozen.iron.client.gameStates.Intro;
import fr.frozen.iron.client.gameStates.Lobby;
import fr.frozen.iron.client.gameStates.MainMenu;
import fr.frozen.iron.client.gameStates.OptionMenu;
import fr.frozen.iron.client.messageEvents.NewSessionEvent;
import fr.frozen.iron.common.equipment.EquipmentManager;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.network.client.ConnectEvent;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class IronTactics extends GameEngine implements NetEventListener {
	
	protected IronClient netClient;
	
	public IronTactics(String host) {
		super();
		setVSync(true);
		netClient = new IronClient(host, 1234);
	}

	@Override
	protected void render() {
		super.render();
	}
	
	@Override
	protected void update() {
		super.update();
	}

	public void changeUserName(String userName) {
		if (userName == null || userName.equals("")) return;
		if (netClient.isConnected()) {
			netClient.sendMessage(Protocol.SERVER_C_SEND_PLAYER_NAME, userName);
		}
	}
	
	@Override
	public void onNetEvent(NetEvent ne) {
		
		if (ne instanceof ConnectEvent) {
			ConnectEvent ce = (ConnectEvent) ne;
			if (ce.getStatus()) {
				Logger.getLogger(getClass()).info("IronTactics connected successfully");
				netClient.sendMessage(Protocol.SERVER_C_SEND_PLAYER_NAME, IronConfig.getUserName());
				netClient.start();
			} else {
				Logger.getLogger(getClass()).error("IronTactics failed to connect");
			}
		}
		
		
		if (ne instanceof NewSessionEvent) {
			NewSessionEvent nse = (NewSessionEvent) ne;
			String newGameState = null;

			switch (nse.getType()) {
			case SESSION_LOBBY : 
				newGameState = "lobby";
				break;
				
			case SESSION_GAME_CREATION :
				newGameState = "gameCreation";
				break;
				
			case SESSION_GAME :
				newGameState = "game";
				break;
				
			default : 
				Logger.getLogger(getClass()).error("game session not handled : "+nse.getType());
				break;
			}
			Logger.getLogger(getClass()).info("switching to "+newGameState);
			if (newGameState != null && !newGameState.equals(getCurrentGameState().getName())) {
				switchToState(newGameState);
			}
		}
	}
	
	public void switchToState(String newGameState) {
		if (getGameState(newGameState) == null) return;
		
		getGameState(newGameState).setActive(true);
		getGameState(newGameState).setVisible(true);

		getCurrentGameState().setActive(false);
		getCurrentGameState().setVisible(false);
		setCurrentGameState(getGameState(newGameState));
	}

	public IronClient getNetClient() {
		return netClient;
	}
	
	public void connect() {
		//TODO: handle a possible restart of the network thread
		if (!netClient.isConnected())
			netClient.connect();
	}
	
	@Override
	protected void buildAssets() {
		SpriteManager.getInstance().loadImagesFromXml(IronConfig.getIronXMLParser());
		SoundManager.getInstance().loadSoundsFromXml(IronConfig.getIronXMLParser());
		@SuppressWarnings("unused")
		EquipmentManager em = EquipmentManager.getInstance();//just to preload it
		FontManager.loadFont("Data/Font.png");
		FontManager.loadFont("Data/DamageFont.png");
		FontManager.loadFont("Data/StatsFont.png",7);
	}
	
	protected void buildInitialGameStates() {
		Intro intro = new Intro(this);
		MainMenu menu = new MainMenu(this);
		Lobby lobby = new Lobby(this);
		Browser browser = new Browser(this);
		GameCreation gameCreation = new GameCreation(this);
		Game game = new Game(this);
		OptionMenu optionMenu = new OptionMenu(this);
		
		addGameState(intro);
		addGameState(menu);
		addGameState(lobby);
		addGameState(browser);
		addGameState(gameCreation);
		addGameState(game);
		addGameState(optionMenu);
		
		netClient.addNetEventListener(gameCreation);
		netClient.addNetEventListener(browser);
		netClient.addNetEventListener(lobby);
		netClient.addNetEventListener(this);
		netClient.addNetEventListener(game);
		
		setCurrentGameState(menu);
	}
	
	public static void main(String []args) {
		String host = "";
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (args.length == 1) {
			host = args[0];
		} else {
			System.out.println("you can add an argument to specify host address");
		}
		
		IronConfig.configClientLogger();
		IronTactics it = new IronTactics(host);
		it.setTitle("Iron Tactics");
		it.setVSync(true);
		it.setFullScreen(false);
		it.setSize(800, 600);
		it.start();
	}

}
