package fr.frozen.iron.client;

import java.awt.Font;

import org.apache.log4j.Logger;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.game.SoundManager;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.gameStates.Browser;
import fr.frozen.iron.client.gameStates.GameCreation;
import fr.frozen.iron.client.gameStates.Intro;
import fr.frozen.iron.client.gameStates.Lobby;
import fr.frozen.iron.client.gameStates.MainMenu;
import fr.frozen.iron.client.gameStates.OptionMenu;
import fr.frozen.iron.client.gameStates.game.MultiplayerGame;
import fr.frozen.iron.client.gameStates.game.SoloGame;
import fr.frozen.iron.client.messageEvents.NewSessionEvent;
import fr.frozen.iron.common.equipment.EquipmentManager;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
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
	protected void update(float deltaTime) {
		super.update(deltaTime);
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
				getGameState("lobby").setActive(false);
				break;

			case SESSION_GAME :
				newGameState = "multiGame";
				getGameState("lobby").setActive(false);
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

	public void switchToState(GameState gameState) {
		if (gameState == null) {
			Logger.getLogger(getClass().getName()).error("Game state not found");
			return;
		}

		gameState.setActive(true);
		gameState.setVisible(true);

		getCurrentGameState().setActive(false);
		getCurrentGameState().setVisible(false);
		setCurrentGameState(gameState);
	}

	public void switchToState(String newGameState) {
		switchToState(getGameState(newGameState));
	}

	public IronClient getNetClient() {
		return netClient;
	}

	public void connect() {
		//TODO: handle a possible restart of the network thread
		if (!netClient.isConnected())
			netClient.connect();
	}

	protected void setPreloaderFont() {
		preloaderFont = FontManager.loadAngelFont("componentFont.fnt", "componentFont.png");
	}

	@Override
	protected void buildAssets() {
		super.buildAssets();
		IronConfig.getInstance().initClientConfig();
		drawLoadingText("Loading images ...");
		SpriteManager.getInstance().loadImagesFromXml(IronConfig.getIronXMLParser());
		drawLoadingText("Loading sounds ...");
		SoundManager.getInstance().loadSoundsFromXml(IronConfig.getIronXMLParser());
		//this is because there is a bug where music plays even when music is off
		//so i do this to fix that
		//IronConfig.setVolume(IronConfig.getVolume());
		@SuppressWarnings("unused")
		EquipmentManager em = EquipmentManager.getInstance();//just to preload it
		drawLoadingText("Loading fonts ...");
		FontManager.addFont(FontManager.loadFont("visitor.ttf", 14), "statsFont");
		FontManager.addFont(FontManager.loadFont(new Font("Arial", Font.PLAIN, 15)), "chatFont");
		FontManager.addFont(FontManager.loadAngelFont("augusta.fnt", "augusta.png"), "defaultFont");
		FontManager.addFont(preloaderFont, "componentFont");
		FontManager.addFont(FontManager.loadAngelFont("DamageFont.fnt", "DamageFont.png"), "DamageFont");
	}

	protected void buildInitialGameStates() {
		Intro intro = new Intro(this);
		
		MainMenu menu = new MainMenu(this);
		OptionMenu optionMenu = new OptionMenu(this);

		Lobby lobby = new Lobby(this);
		Browser browser = new Browser(this);
		
		GameCreation gameCreation = new GameCreation(this);

		SoloGame soloGame = new SoloGame(this); 
		//SoloGame soloGame = new AIGame(this);
		MultiplayerGame multiGame = new MultiplayerGame(this);

		addGameState(intro);
		addGameState(menu);
		addGameState(lobby);
		addGameState(browser);
		addGameState(gameCreation);
		addGameState(multiGame);
		addGameState(soloGame);
		addGameState(optionMenu);

		netClient.addNetEventListener(gameCreation);
		netClient.addNetEventListener(browser);
		netClient.addNetEventListener(lobby);
		netClient.addNetEventListener(this);
		netClient.addNetEventListener(multiGame);

		setCurrentGameState(menu);
	}

	public void initIronTactics() {
		setTitle("Iron Tactics");
		setVSync(true);
		setFullScreen(false);
		setSize(800, 600);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		
		if (netClient.isConnected()) {
			Logger.getLogger(getClass()).debug("shutting down netClient");
			netClient.shutdown();
		}

		if (netClient.isAlive()) {
			Logger.getLogger(getClass()).debug("joining netClient thread");
			try {
				netClient.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.exit(0);
	}

	public static void main(String []args) {

		String host = IronConst.HOST;
		if (args.length == 1) {
			host = args[0];
		} else {
			System.out.println("you can add an argument to specify host address");
		}

		IronConfig.configClientLogger();
		IronTactics it = new IronTactics(host);
		it.initIronTactics();
		it.start();
	}
}
