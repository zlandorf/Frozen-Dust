package fr.frozen.iron.client.gameStates;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.IGameEngine;
import fr.frozen.game.ISprite;
import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronPlayer;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.ChatWindow;
import fr.frozen.iron.client.components.ChatWindowMessage;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.MouseListener;
import fr.frozen.iron.client.components.PopupList;
import fr.frozen.iron.client.components.TextField;
import fr.frozen.iron.client.messageEvents.ChatMessageEvent;
import fr.frozen.iron.client.messageEvents.GameActionEvent;
import fr.frozen.iron.client.messageEvents.GameInfoReceivedEvent;
import fr.frozen.iron.client.messageEvents.GameTurnEvent;
import fr.frozen.iron.client.messageEvents.MapRecievedEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.UnitsListReceivedEvent;
import fr.frozen.iron.common.GameContext;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.PlayerGameInfo;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.Bird;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.common.skills.SkillInfo;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class Game extends GameState implements NetEventListener, MouseListener, ActionListener, GameContext {

	protected GUI gui;
	protected IronClient netClient;
	protected TextField textField;
	protected ChatWindow chatWindow;
	
	protected IronWorld world;
	protected boolean worldReady = false; 
	
	protected List<IronPlayer> players;
	protected Hashtable<Integer, PlayerGameInfo> playerInfo;
	
	protected int turnPlayerId = -1;
	protected float timeLeftForTurn = 0;
	/*protected TextField textField;
	protected ChatWindow chatWindow;*/
	
	protected IronUnit selectedUnit = null;
	protected IronUnit hoveredUnit = null;
	
	protected PopupList popup;
	
	public Game(IGameEngine ge) {
		super(ge, "game", false, false);
		netClient = ((IronTactics)gameEngine).getNetClient();
		gui = new GUI(this);
		
		
		players = new ArrayList<IronPlayer>();
		//playerInfo = new Hashtable<Integer, PlayerGameInfo>();
		
		world = new IronWorld(this);
		popup = new PopupList(world, 0, 0);
		popup.addActionListener(this);
		gui.addComponent(popup);
		
		textField = new TextField(5, 580, 500, 20);
		chatWindow = new ChatWindow(5,505, 500,70);
		textField.addActionListener(new TextFieldListener());
		gui.addComponent(textField);
		gui.addComponent(chatWindow);
		
		
		ISprite spriteNormal = ISpriteManager.getInstance().getSprite("buttonNormal");
		ISprite spriteHover = ISpriteManager.getInstance().getSprite("buttonHover");
		Button button = new Button("End Turn", 590, 500, 0, 0);
		button.setDim((int)spriteNormal.getWidth(),(int)spriteNormal.getHeight());
		button.setHoverSprite(spriteHover);
		button.setNormalSprite(spriteNormal);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				requestEndTurn();
			}
		});
		
		gui.addComponent(button);
	}
	
	protected void requestEndTurn() {
		netClient.sendEmptyMessage(Protocol.GAME_END_TURN_REQUEST);
	}
	
	
	@Override
	public int getClientId() {
		return netClient.getClientId();
	}

	@Override
	public synchronized void onNetEvent(NetEvent ne) {
		//order : playerList -> playerList Info -> game map -> game units list
		
		if (!active) return;
		
		if (ne instanceof ChatMessageEvent) {
			ChatMessageEvent cme = (ChatMessageEvent) ne;
			IronPlayer sender = players.get(cme.getSenderId());
			if (sender == null) return;
			chatWindow.addMessage(new ChatWindowMessage(ChatWindowMessage.CHAT_MESSAGE, sender.getName()+ " says : ", cme.getText()));
		}
		
		
		if (ne instanceof PlayerListReceivedEvent) {
			PlayerListReceivedEvent plre = (PlayerListReceivedEvent) ne;
			if (plre.getList().size() != 2) System.err.println("PROBLEM WITH NUMBER OF PLAYERS");
			for (IronPlayer player : plre.getList())
				players.add(player);
			
			netClient.sendEmptyMessage(Protocol.GAME_PLAYER_INFO_REQUEST);
		}
		
		if (ne instanceof GameInfoReceivedEvent) {
			GameInfoReceivedEvent gire = (GameInfoReceivedEvent) ne;
			playerInfo = gire.getInfo();
			netClient.sendEmptyMessage(Protocol.GAME_MAP_REQUEST);
		}
		
		
		if (ne instanceof MapRecievedEvent) {
			world.setMap(((MapRecievedEvent)ne).getMap());
			netClient.sendEmptyMessage(Protocol.GAME_UNIT_LIST_REQUEST);
		}
		
		if (ne instanceof UnitsListReceivedEvent) {
			UnitsListReceivedEvent nlre = (UnitsListReceivedEvent) ne;
			for (IronUnit unit : nlre.getUnitsList()) {
				unit.setWorld(world);
				//unit.findSprite();
			}
			world.setUnits(nlre.getUnitsList());
			netClient.sendEmptyMessage(Protocol.GAME_READY);
		}

		if (ne instanceof GameTurnEvent) {
			GameTurnEvent gte = (GameTurnEvent) ne;
			
			setTurn(gte.getPlayerId());

			if (!worldReady)
				worldReady = true;
		}
		/* in game actions */
		
		if (ne instanceof GameActionEvent) {
			try {
				handleGameAction((GameActionEvent)ne);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	}
	
	
	protected void handleGameAction(GameActionEvent gae) throws IOException {
		IronUnit unitSrc = world.getUnitFromId(gae.getUnitId());
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(gae.getData()));
		
		int x, y;
		
		switch (gae.getType()) {
		case IronUnit.ACTION_MOVE :
			x = is.readInt();
			y = is.readInt();
			int moveCost = is.readInt();
			unitSrc.move(x, y, moveCost);
			if (unitSrc.hasPlayed()) {
				unitSrc.setSelected(false);
				selectedUnit = null;
			}
			break;
		case IronUnit.ACTION_SKILL :
			Skill skill = Skill.getSkill(is.readInt());
			x = is.readInt();
			y = is.readInt();

			List<int[]> values = new ArrayList<int[]>();
			while (true) {
				int dstId = is.readInt();
				if (dstId == -1) break;
				int value = is.readInt();
				
				values.add(new int[]{dstId, value});
			}
			
			skill.executeClientSide(world, unitSrc.getId(), x, y, values);
			break;
		default :
			System.out.println("Action not recognised "+gae.getType());
		}
	}
	

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		gui.update(deltaTime);

		if (!worldReady) return;
		world.update(deltaTime);
		
		if (getGameObjectCollection("bird") == null || getGameObjectCollection("bird").size() == 0) {
			if (Math.random() <= IronConst.BIRD_APPEARANCE_PROB) {
				Bird bird = new Bird(this);
				addGameObject(bird, "bird");
			}
		}
		
		timeLeftForTurn -= deltaTime;
		timeLeftForTurn = Math.max(0, timeLeftForTurn);
	}
	
	protected void selectUnit(IronUnit unit) {
		if (selectedUnit != null) {
			selectedUnit.setSelected(false);
			selectedUnit = null;
		}
		
		if (unit != null && !unit.hasPlayed() && unit.getOwnerId() == getClientId()) {
			unit.setSelected(true);
			selectedUnit = unit;
		}
	}

	@Override
	public void render(float deltaTime) {
		//super.render(deltaTime);
		world.render(deltaTime, selectedUnit);

		if (hoveredUnit != null && !hoveredUnit.isDead()) {
			hoveredUnit.renderStatusBars(deltaTime);
		}
		
		if (getGameObjectCollection("bird") != null && getGameObjectCollection("bird").size() >= 1) {
			GameObject bird = getGameObjectCollection("bird").get(0);
			bird.render(deltaTime);
		}
		
		if (worldReady) {
			renderWorldReady(deltaTime);
		}
		gui.render(deltaTime);
		if (hoveredUnit != null) {
			hoveredUnit.renderStatsInGui(deltaTime);
		}
	}
	
	protected void renderWorldReady(float deltaTime) {
		float x = 660;
		float y = 20;
		FontManager.getFont("DamageFont").setColor(1, 1, 1, 1);
		FontManager.getFont("DamageFont").glPrint("TimeLeft:", x, y, 0, 1.3f);
		
		x += 44;//4 * 11
		y += 20;
		
		String timeStr = "";
		if (timeLeftForTurn < 10) {
			timeStr = "0";
		}
		FontManager.getFont("DamageFont").glPrint(timeStr+(int)timeLeftForTurn, x, y, 0, 1.3f);
	}
	
	@Override
	public void setActive(boolean val) {
		super.setActive(val);
		if (!val) {
			//players.clear();
		} else {
			netClient.sendEmptyMessage(Protocol.SESSION_PLAYER_LIST_REQUEST);
		}
	}
	
	@Override
	public PlayerGameInfo getPlayerInfo(int clientId) {
		return playerInfo.get(clientId);
	}
	
	public IronPlayer getPlayer(int playerId) {
		for (IronPlayer player : players) {
			if (player.getId() == playerId)
				return player;
		}
		return null;
	}
	
	
	protected void requestMove(int x, int y) {
		byte []data = new byte[16];
		
		System.arraycopy(IronUtil.intToByteArray(selectedUnit.getId()), 0, data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_MOVE), 0, data, 4, 4);
		System.arraycopy(IronUtil.intToByteArray(x), 0, data, 8, 4);
		System.arraycopy(IronUtil.intToByteArray(y), 0, data, 12, 4);
		
		netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof PopupList) {
			SkillInfo info = ((PopupList)e.getSource()).getSelectedSkillInfo();
			int unitId = info.getUnitId();
			int x = info.getX();
			int y = info.getY();
			int skillType = info.getSkill().getSkillType();

			byte []data = new byte[20];
			
			System.arraycopy(IronUtil.intToByteArray(unitId), 0, data, 0, 4);
			System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_SKILL), 0, data, 4, 4);
			System.arraycopy(IronUtil.intToByteArray(skillType), 0, data, 8, 4);
			System.arraycopy(IronUtil.intToByteArray(x), 0, data, 12, 4);
			System.arraycopy(IronUtil.intToByteArray(y), 0, data, 16, 4);
			
			netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
			
			selectedUnit.setSelected(false);
			selectedUnit = null;
		}
	}

	@Override
	public void onHover(int x, int y) {
		if (!worldReady) return;
		
		x /= IronConst.TILE_WIDTH;
		y /= IronConst.TILE_HEIGHT;
		if (x < 0 || x >= IronConst.MAP_WIDTH || y < 0 || y >= IronConst.MAP_HEIGHT) {
			hoveredUnit = null;
			return;
		}
		hoveredUnit = world.getUnitAtXY(x, y);
	}

	@Override
	public void onLeftClick(int x, int y) {
		x /= IronConst.TILE_WIDTH;
		y /= IronConst.TILE_HEIGHT;
		//out of bounds
		if (x < 0 || x >= IronConst.MAP_WIDTH || y < 0 || y >= IronConst.MAP_HEIGHT) return;

		if (popup.isVisible()) {
			popup.setVisible(false);
			return;
		}
		
		IronUnit unit = world.getUnitAtXY(x,y);
		
		if (unit != null ) {
			selectUnit(unit);
		} else if (selectedUnit != null) {
			requestMove(x,y);
		}
	}

	@Override
	public void onRightClick(int x, int y) {
		//int x = Mouse.getX();
		//int y = (Display.getDisplayMode().getHeight() - Mouse.getY());
		//if (x < 0 || x >= Display.getDisplayMode().getWidth() || y < 0 || y >= Display.getDisplayMode().getHeight()) return;
		
		if (selectedUnit != null) {
			if (!popup.isVisible()) {
				popup.setUnit(selectedUnit, x, y);
				popup.setVisible(!popup.isVisible());
			} else {
				IronUnit unit = world.getUnitAtXY(x / IronConst.TILE_WIDTH, y / IronConst.TILE_HEIGHT);
				if (unit != null) {
					popup.setUnit(selectedUnit, x, y);
					popup.setVisible(true);
				} else {
					popup.setVisible(false);
				}
			}
		} else {
			popup.setVisible(false);
		}
	}

	@Override
	public void onExit() {
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
