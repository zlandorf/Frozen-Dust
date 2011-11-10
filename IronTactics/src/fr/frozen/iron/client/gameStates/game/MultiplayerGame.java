package fr.frozen.iron.client.gameStates.game;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.ChatWindow;
import fr.frozen.iron.client.components.ChatWindowMessage;
import fr.frozen.iron.client.components.TextField;
import fr.frozen.iron.client.messageEvents.ChatMessageEvent;
import fr.frozen.iron.client.messageEvents.GameInfoReceivedEvent;
import fr.frozen.iron.client.messageEvents.MapRecievedEvent;
import fr.frozen.iron.client.messageEvents.NameChangeEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
import fr.frozen.iron.client.messageEvents.UnitsListReceivedEvent;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.controller.ClientGameController;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class MultiplayerGame extends AbstractGame implements NetEventListener {
	
	protected IronClient netClient;
	protected TextField textField;
	protected ChatWindow chatWindow;

	protected Hashtable<Integer, IronPlayer> playersById;
	protected List<IronPlayer> players;
	
	@Override
	protected void createGui() {
		super.createGui();
		
		textField = new TextField(5, 575, 500, 20);
		chatWindow = new ChatWindow(5, 500, 500, 70);
		textField.addActionListener(new TextFieldListener());
		gui.addComponent(textField);
		gui.addComponent(chatWindow);
	}

	public MultiplayerGame(IGameEngine ge) {
		super(ge, "multiGame");
		netClient = ((IronTactics) gameEngine).getNetClient();
		playersById = new Hashtable<Integer, IronPlayer>();
		players = new ArrayList<IronPlayer>();
	}

	@Override
	protected boolean canSelectUnit(IronUnit unit) {
		return unit != null && !unit.hasPlayed()
				&& unit.getOwnerId() == getClientId();
	}

	@Override
	protected void leaveGame() {
		netClient.sendMessage(Protocol.SERVER_C_REQUEST_SESSION, IronUtil
				.intToByteArray(Protocol.SESSION_LOBBY.ordinal()));
	}

	@Override
	protected void cleanUp() {
		netClient.removeNetEventListener((ClientGameController)controller);
		controller.removeGameObserver(this);
		super.cleanUp();
		players.clear();
		playersById.clear();
		chatWindow.clearMessages();
	}

	@Override
	protected void requestEndTurn() {
		netClient.sendEmptyMessage(Protocol.GAME_END_TURN_REQUEST);
	}

	public int getClientId() {
		return netClient.getClientId();
	}

	@Override
	protected void initGame() {
		world = new IronWorld();
		controller = new ClientGameController(world, netClient);
		controller.addGameObserver(this);
	}
	
	@Override
	public void onSkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res) {
	}
	
	@Override
	public synchronized void onNetEvent(NetEvent ne) {
		// order : playerList -> playerList Info -> game map -> game units list
		if (!active)
			return;

		if (ne instanceof ChatMessageEvent) {
			ChatMessageEvent cme = (ChatMessageEvent) ne;
			IronPlayer sender = playersById.get(cme.getSenderId());
			if (sender == null)
				return;
			chatWindow.addMessage(new ChatWindowMessage(
					ChatWindowMessage.CHAT_MESSAGE, sender.getName()
							+ " says : ", cme.getText()));
		}

		if (ne instanceof PlayerListReceivedEvent) {
			PlayerListReceivedEvent plre = (PlayerListReceivedEvent) ne;
			if (plre.getList().size() != 2)
				Logger.getLogger(getClass()).error(
						"PROBLEM WITH NUMBER OF PLAYERS");
			for (IronPlayer player : plre.getList()) {
				players.add(player);
				playersById.put(player.getId(), player);
			}
			
			controller.setPlayersList(players);
			controller.setPlayersMap(playersById);
			
			netClient.sendEmptyMessage(Protocol.GAME_PLAYER_INFO_REQUEST);
		}

		if (ne instanceof GameInfoReceivedEvent) {
			GameInfoReceivedEvent gire = (GameInfoReceivedEvent) ne;
			controller.setInfoMap(gire.getInfo());
			netClient.sendEmptyMessage(Protocol.GAME_MAP_REQUEST);
		}

		if (ne instanceof MapRecievedEvent) {
			world.setMap(((MapRecievedEvent) ne).getMap());
			netClient.sendEmptyMessage(Protocol.GAME_UNIT_LIST_REQUEST);
		}

		if (ne instanceof UnitsListReceivedEvent) {
			UnitsListReceivedEvent nlre = (UnitsListReceivedEvent) ne;
			for (IronUnit unit : nlre.getUnitsList()) {
				unit.setWorld(world);
				// unit.findSprite();
			}
			world.setUnits(nlre.getUnitsList());
			netClient.sendEmptyMessage(Protocol.GAME_READY);
		}

		

		if (ne instanceof PlayerLogoutEvent) {
			PlayerLogoutEvent ploe = (PlayerLogoutEvent) ne;
			IronPlayer player = playersById.get(ploe.getId());
			if (player == null)
				return;
			chatWindow.addMessage(new ChatWindowMessage(
					ChatWindowMessage.SERVER_MESSAGE, player.getName()
							+ " left : " + ploe.getReason()));
		}

		if (ne instanceof NameChangeEvent) {
			NameChangeEvent nce = (NameChangeEvent) ne;
			IronPlayer player = playersById.get(nce.getPlayerId());
			if (player == null)
				return;
			chatWindow.addMessage(new ChatWindowMessage(
					ChatWindowMessage.SERVER_MESSAGE, player.getName()
							+ " changed name to " + nce.getName()));
			player.setName(nce.getName());
		}
	}

	@Override
	public synchronized void setActive(boolean val) {
		boolean oldVal = isActive();
		super.setActive(val);
		if (val && !oldVal) {
			// launching a new game, we need to start over again
			netClient.sendEmptyMessage(Protocol.SESSION_PLAYER_LIST_REQUEST);
		}
	}

	class TextFieldListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = textField.getText();
			if (!text.isEmpty()) {
				netClient.sendMessage(Protocol.SESSION_CHAT_MESSAGE, text);
				chatWindow.resetScrollIndex();
			}
			textField.setText("");
		}
	}

	@Override
	protected String getNextTurnNotificationText(int id) {
		if (id == netClient.getClientId()) {
			return "It's your turn";
		}
		return "It's your opponent's turn";
	}

	@Override
	protected String getNextTurnText(int id) {
		if (id == netClient.getClientId()) {
			return "Your's";
		}
		return "Opponent's";
	}

	@Override
	protected String getWinnerText(int id) {
		if (id == netClient.getClientId()) {
			return "Victory !";
		}
		return "You Lose !";
	}
}
