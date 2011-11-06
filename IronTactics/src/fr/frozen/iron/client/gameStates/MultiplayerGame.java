package fr.frozen.iron.client.gameStates;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronPlayer;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.ChatWindow;
import fr.frozen.iron.client.components.ChatWindowMessage;
import fr.frozen.iron.client.components.TextField;
import fr.frozen.iron.client.messageEvents.ChatMessageEvent;
import fr.frozen.iron.client.messageEvents.GameActionEvent;
import fr.frozen.iron.client.messageEvents.GameInfoReceivedEvent;
import fr.frozen.iron.client.messageEvents.GameOverEvent;
import fr.frozen.iron.client.messageEvents.GameTurnEvent;
import fr.frozen.iron.client.messageEvents.MapRecievedEvent;
import fr.frozen.iron.client.messageEvents.NameChangeEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
import fr.frozen.iron.client.messageEvents.UndoMoveEvent;
import fr.frozen.iron.client.messageEvents.UnitsListReceivedEvent;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.common.skills.SkillInfo;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class MultiplayerGame extends AbstractGame implements NetEventListener {

	protected IronClient netClient;
	protected TextField textField;
	protected ChatWindow chatWindow;

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
		super(ge, "multi game");
		netClient = ((IronTactics) gameEngine).getNetClient();
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
	protected void reInit() {
		super.reInit();

		chatWindow.clearMessages();
		forestSound.stop();
	}

	@Override
	protected void requestEndTurn() {
		netClient.sendEmptyMessage(Protocol.GAME_END_TURN_REQUEST);
	}

	@Override
	public int getClientId() {
		return netClient.getClientId();
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
			netClient.sendEmptyMessage(Protocol.GAME_PLAYER_INFO_REQUEST);
		}

		if (ne instanceof GameInfoReceivedEvent) {
			GameInfoReceivedEvent gire = (GameInfoReceivedEvent) ne;
			playerInfo = gire.getInfo();
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

		if (ne instanceof GameTurnEvent) {
			GameTurnEvent gte = (GameTurnEvent) ne;

			setTurn(gte.getPlayerId());

			if (!worldReady) {
				worldReady = true;
				forestSound.playAsMusic(true);
			}
		}
		/* in game actions */

		if (ne instanceof GameActionEvent) {
			try {
				handleGameAction((GameActionEvent) ne);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (ne instanceof UndoMoveEvent) {
			IronUnit unit = world.getUnitFromId(((UndoMoveEvent) ne)
					.getUnitId());
			if (unit != null) {
				unit.undoMove();
			}
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

		if (ne instanceof GameOverEvent) {
			GameOverEvent goe = (GameOverEvent) ne;
			winnerId = goe.getWinnerId();
			gameOver = true;
		}
	}

	public void setTurn(int playerId) {
		popup.setVisible(false);
		if (turnPlayerId != -1) {
			playerInfo.get(turnPlayerId).setTurnToPlay(false);
			world.endTurn(turnPlayerId);
		}

		playerInfo.get(playerId).setTurnToPlay(true);
		world.initTurn(playerId, true);

		turnPlayerId = playerId;

		timeLeftForTurn = IronConst.TURN_DURATION;

		if (selectedUnit != null) {
			selectedUnit.setSelected(false);
		}
		selectedUnit = null;
		lastUnitMoved = null;

		notifyNewTurnTimeLeft = notifyNewTurnDuration;
	}

	protected void handleGameAction(GameActionEvent gae) throws IOException {
		IronUnit unitSrc = world.getUnitFromId(gae.getUnitId());
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(gae
				.getData()));

		int x, y;

		switch (gae.getType()) {
		case IronUnit.ACTION_MOVE:
			x = is.readInt();
			y = is.readInt();
			int moveCost = is.readInt();
			unitSrc.move(x, y, moveCost);
			if (unitSrc.hasPlayed()) {
				unitSrc.setSelected(false);
				selectedUnit = null;
			}
			if (unitSrc.getOwnerId() == netClient.getClientId()) {
				lastUnitMoved = unitSrc;
			}
			break;
		case IronUnit.ACTION_SKILL:
			Skill skill = Skill.getSkill(is.readInt());
			x = is.readInt();
			y = is.readInt();

			List<int[]> values = new ArrayList<int[]>();
			while (true) {
				int dstId = is.readInt();
				if (dstId == -1)
					break;
				int value = is.readInt();

				values.add(new int[] { dstId, value });
			}
			skill.executeClientSide(world, unitSrc.getId(), x, y, values);

			if (unitSrc.hasPlayed()) {
				unitSrc.setSelected(false);
				selectedUnit = null;
			}
			break;
		default:
			Logger.getLogger(getClass()).error(
					"Action not recognised " + gae.getType());
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

	@Override
	protected void requestMove(int x, int y) {
		byte[] data = new byte[16];

		System.arraycopy(IronUtil.intToByteArray(selectedUnit.getId()), 0,
				data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_MOVE), 0,
				data, 4, 4);
		System.arraycopy(IronUtil.intToByteArray(x), 0, data, 8, 4);
		System.arraycopy(IronUtil.intToByteArray(y), 0, data, 12, 4);

		netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
	}

	@Override
	protected void requestUndo() {
		if (lastUnitMoved == null) {
			return;
		}

		byte[] data = new byte[4];
		System.arraycopy(IronUtil.intToByteArray(lastUnitMoved.getId()), 0,
				data, 0, 4);

		netClient.sendMessage(Protocol.GAME_UNDO_REQUEST, data);
	}

	@Override
	protected void requestSkill(SkillInfo info) {
		int unitId = info.getUnitId();
		int x = info.getX();
		int y = info.getY();
		int skillType = info.getSkill().getSkillType();

		byte[] data = new byte[20];

		System.arraycopy(IronUtil.intToByteArray(unitId), 0, data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_SKILL), 0,
				data, 4, 4);
		System.arraycopy(IronUtil.intToByteArray(skillType), 0, data, 8, 4);
		System.arraycopy(IronUtil.intToByteArray(x), 0, data, 12, 4);
		System.arraycopy(IronUtil.intToByteArray(y), 0, data, 16, 4);

		netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
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
		if (turnPlayerId == netClient.getClientId()) {
			return "It's your turn";
		}
		return "It's your opponent's turn";
	}

	@Override
	protected String getNextTurnText(int id) {
		if (turnPlayerId == netClient.getClientId()) {
			return "Your's";
		}
		return "Opponent's";
	}

	@Override
	protected String getWinnerText(int id) {
		if (winnerId == netClient.getClientId()) {
			return "Victory !";
		}
		return "You Lose !";
	}
}
