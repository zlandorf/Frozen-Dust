package fr.frozen.iron.client.gameStates.game;

import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.font.effects.OutlineEffect;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.IGameEngine;
import fr.frozen.game.ISprite;
import fr.frozen.game.Sound;
import fr.frozen.game.SoundManager;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.IronMenuButton;
import fr.frozen.iron.client.components.MouseListener;
import fr.frozen.iron.client.components.PopupList;
import fr.frozen.iron.common.GameObserver;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.controller.AbstractGameController;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.Bird;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.common.skills.SkillInfo;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronGL;
import fr.frozen.util.pathfinding.Path;

public abstract class AbstractGame extends GameState implements MouseListener,
		ActionListener, GameObserver {

	protected GUI gui;
	protected GUI ingameMenu;

	protected Button undoButton;

	protected IronWorld world;

	protected AbstractGameController controller;

	protected IronUnit selectedUnit = null;
	protected IronUnit hoveredUnit = null;

	protected PopupList popup;

	protected ISprite backTex;
	protected ISprite backTex2;

	protected Sound forestSound;

	protected boolean showIngameMenu = false;
	protected Font endFont;
	protected Font timeLeftFont;
	protected Font turnFont;

	protected float notifyNewTurnDuration = 5;// s
	protected float notifyNewTurnTimeLeft = notifyNewTurnDuration;

	protected AbstractGame(IGameEngine ge, String name) {
		super(ge, name, false, false);

		backTex = SpriteManager.getInstance().getSprite("backTex");
		backTex2 = SpriteManager.getInstance().getSprite("popupTex");
		forestSound = SoundManager.getInstance().getSound("forest_ambiance");

		endFont = FontManager.loadFont("default.ttf", 40, new OutlineEffect(3,
				java.awt.Color.black));
		turnFont = FontManager.loadFont("default.ttf", 30, new OutlineEffect(3,
				java.awt.Color.black));
		timeLeftFont = FontManager.getFont("defaultFont");

		createGui();
	}

	abstract protected String getWinnerText(int winnerId);

	abstract protected void leaveGame();

	abstract protected String getNextTurnNotificationText(int nextTurnPlayerId);

	abstract protected String getNextTurnText(int nextTurnPlayerId);

	protected void requestEndTurn() {
		controller.switchTurns();
	}

	protected void requestMove(IronUnit unit, int x, int y) {
		controller.handleMove(unit.getId(), x, y);
	}

	protected void requestSkill(SkillInfo skillInfo) {
		controller.handleSkill(skillInfo.getUnitId(), skillInfo.getSkill(),
				skillInfo.getX(), skillInfo.getY());
	}

	protected void requestUndo() {
		controller.undoMove();
	}

	abstract protected boolean canSelectUnit(IronUnit unit);

	abstract protected void initGame();

	protected void createGui() {
		gui = new GUI(this);
		ingameMenu = new GUI(this);

		popup = new PopupList(0, 0);
		popup.addActionListener(this);
		gui.addComponent(popup);

		Button button = new Button("End Turn", 650, 480, 0, 0);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestEndTurn();
			}
		});

		undoButton = new Button("Undo move", 650, 530, 0, 0);
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestUndo();
			}
		});

		Button menuButton = new Button("Menu", 650, 430, 0, 0);
		menuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showIngameMenu = true;
			}
		});

		button.setDim(140, 50);
		undoButton.setDim(140, 50);
		menuButton.setDim(140, 50);

		gui.addComponent(button);
		gui.addComponent(undoButton);
		gui.addComponent(menuButton);

		Button optionButton = new IronMenuButton("Options", 150);
		optionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openOptions();
			}
		});

		Button backButton = new IronMenuButton("Back to game", 250);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showIngameMenu = false;
			}
		});

		Button leaveButton = new IronMenuButton("Leave game", 350);
		leaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				leaveGame();
			}
		});

		ingameMenu.addComponent(leaveButton);
		ingameMenu.addComponent(optionButton);
		ingameMenu.addComponent(backButton);
	}

	protected void cleanUp() {
		controller.removeGameObserver(this);
		world.setContext(null);
		controller = null;
		world.reInit();
		world = null;

		selectedUnit = null;
		hoveredUnit = null;

		showIngameMenu = false;

		forestSound.stop();
	}

	private void openOptions() {
		setVisible(false);
		gameEngine.getGameState("optionMenu").setActive(true);
		gameEngine.getGameState("optionMenu").setVisible(true);
		gameEngine.setCurrentGameState(gameEngine.getGameState("optionMenu"));
	}

	@Override
	public synchronized void update(float deltaTime) {
		super.update(deltaTime);
		if (visible) {
			if (showIngameMenu) {
				ingameMenu.update(deltaTime);
			} else {
				gui.update(deltaTime);
			}
		}

		if (controller == null || !controller.isGameStarted())
			return;

		world.update(deltaTime);
		world.getMap().update(deltaTime);

		if (getGameObjectCollection("bird") == null
				|| getGameObjectCollection("bird").size() == 0) {
			if (Math.random() <= IronConst.BIRD_APPEARANCE_PROB) {
				Bird bird = new Bird(this);
				addGameObject(bird, "bird");
			}
		}
		if (controller.canUndo()) {
			undoButton.enable();
		} else {
			undoButton.disable();
		}

		controller.update(deltaTime);
	}

	protected void selectUnit(IronUnit unit) {
		if (selectedUnit != null) {
			selectedUnit.setSelected(false);
			selectedUnit = null;
		}

		if (canSelectUnit(unit)) {
			unit.setSelected(true);
			selectedUnit = unit;
		}
	}

	@Override
	public synchronized void render(float deltaTime) {
		// super.render(deltaTime);
		if (controller == null || !controller.isGameStarted())
			return;

		world.render(deltaTime, selectedUnit);

		if (hoveredUnit != null && !hoveredUnit.isDead()) {
			hoveredUnit.renderStatusBars(deltaTime);
		}

		if (getGameObjectCollection("bird") != null
				&& getGameObjectCollection("bird").size() >= 1) {
			GameObject bird = getGameObjectCollection("bird").get(0);
			bird.render(deltaTime);
		}

		renderGuiBackground(deltaTime);

		renderCountDown(deltaTime);
		renderWhosTurn(deltaTime);
		displayNotifyNewTurn(deltaTime);

		gui.render(deltaTime);

		if (hoveredUnit != null) {
			renderStatsInGui(deltaTime, hoveredUnit);
		} else if (selectedUnit != null) {
			renderStatsInGui(deltaTime, selectedUnit);
		}

		if (controller.isGameOver()) {
			String gameOverText = getWinnerText(controller.getWinnerId());

			int x = IronConst.MAP_WIDTH * IronConst.TILE_WIDTH / 2;
			x -= endFont.getWidth(gameOverText) / 2;
			int y = IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT / 2;
			y -= endFont.getHeight(gameOverText) / 2;
			endFont.drawString(x, y, gameOverText, Color.white);
		}

		if (showIngameMenu) {
			IronGL.drawRect(0, 0, Display.getDisplayMode().getWidth(), Display
					.getDisplayMode().getHeight(), 0, 0, 0, 0.75f);
			ingameMenu.render(deltaTime);
		}
	}

	public void displayNotifyNewTurn(float deltaTime) {
		if (controller == null || controller.isGameOver())
			return;

		notifyNewTurnTimeLeft -= deltaTime;
		if (notifyNewTurnTimeLeft > 0) {
			String text = getNextTurnNotificationText(controller
					.getTurnPlayerId());

			int x = IronConst.MAP_WIDTH * IronConst.TILE_WIDTH / 2;
			x -= turnFont.getWidth(text) / 2;
			int y = IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT / 2;
			y -= turnFont.getHeight(text) / 2;

			Color color = new Color(1, 1, 1, Math.min(1, notifyNewTurnTimeLeft));
			turnFont.drawString(x, y, text, color);
		}
	}

	public void renderWhosTurn(float deltaTime) {
		int x = 662;
		int y = 70;

		float width = 117;
		float height = 48;

		drawGuiBox(x, y, width, height);

		String text1 = "Turn :";
		int x2 = (int) (x + width / 2 - timeLeftFont.getWidth(text1) / 2);
		int y2 = y;

		timeLeftFont.drawString(x2, y2, text1, Color.white);

		String turnText = getNextTurnText(controller.getTurnPlayerId());

		if (controller.isGameOver()) {
			turnText = "--";
		}

		x2 = (int) (x + width / 2 - timeLeftFont.getWidth(turnText) / 2);
		y2 += timeLeftFont.getHeight(text1);

		timeLeftFont.drawString(x2, y2, turnText, Color.white);
	}

	public void renderStatsInGui(float deltaTime, IronUnit unit) {
		float x, y, w, h;
		x = IronConst.MAP_WIDTH * IronConst.TILE_WIDTH;
		x += 10;
		y = 130;

		w = Display.getDisplayMode().getWidth() - x - 8;
		h = 230;

		drawGuiBox(x, y, w, h);
		unit.renderStatsInGui(deltaTime, x, y, w, h);
	}

	public void drawGuiBox(float x, float y, float w, float h) {
		backTex2.setColor(0x4e4d4d);
		backTex2.fillIn(x, y, x + w, y + h);

		IronGL.drawHollowRect(x, y, w, h, 0x0);
		IronGL.drawHollowRect(x + 1, y + 1, w - 2, h - 2, 0x830000);
		IronGL.drawHollowRect(x + 2, y + 2, w - 4, h - 4, 0x0);
	}

	protected void renderGuiBackground(float deltaTime) {
		int screenWidth = Display.getDisplayMode().getWidth();
		int screenHeight = Display.getDisplayMode().getHeight();

		int x1 = 0;
		int y1 = IronConst.TILE_HEIGHT * IronConst.MAP_HEIGHT;
		int x2 = IronConst.TILE_WIDTH * IronConst.MAP_WIDTH;
		int y2 = 0;
		backTex.fillIn(x1, y1, screenWidth, screenHeight);
		backTex.fillIn(x2, y2, screenWidth, screenHeight);

		IronGL.drawLine(x1, y1, x2, y1, 0x0);
		IronGL.drawLine(x1, y1 + 1, x2 + 1, y1 + 1, 0x830000);
		IronGL.drawLine(x1, y1 + 2, x2 + 2, y1 + 2, 0x0);

		IronGL.drawLine(x2, y2, x2, y1, 0x0);
		IronGL.drawLine(x2 + 1, y2, x2 + 1, y1 + 1, 0x830000);
		IronGL.drawLine(x2 + 2, y2, x2 + 2, y1 + 2, 0x0);

	}

	protected void renderCountDown(float deltaTime) {
		float x = 662;
		float y = 15;

		float width = 117;
		float height = 48;

		drawGuiBox(x, y, width, height);

		float x2 = x + width / 2 - timeLeftFont.getWidth("TimeLeft :") / 2;
		float y2 = y;

		timeLeftFont.drawString((int) x2, (int) y2, "TimeLeft :", Color.white);

		String timeStr = "";
		if (controller.getTimeLeftForTurn() < 10) {
			timeStr = "0";
		}
		timeStr += String.valueOf((int) controller.getTimeLeftForTurn());

		if (controller.isGameOver()) {
			timeStr = "--";
		}

		x2 = x + width / 2 - timeLeftFont.getWidth(timeStr) / 2;
		y2 += timeLeftFont.getHeight("TimeLeft");

		timeLeftFont.drawString((int) x2, (int) y2, timeStr, Color.white);
	}

	@Override
	public synchronized void setActive(boolean val) {
		boolean oldVal = isActive();
		super.setActive(val);
		if (!val) {
			// clear everything for a future game
			cleanUp();
		} else if (!oldVal && val) {
			initGame();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof PopupList) {
			SkillInfo info = ((PopupList) e.getSource()).getSelectedSkillInfo();
			requestSkill(info);
		}
	}

	@Override
	public void onHover(int x, int y) {
		if (controller == null || !controller.isGameStarted() || showIngameMenu)
			return;

		x /= IronConst.TILE_WIDTH;
		y /= IronConst.TILE_HEIGHT;
		if (x < 0 || x >= IronConst.MAP_WIDTH || y < 0
				|| y >= IronConst.MAP_HEIGHT) {
			hoveredUnit = null;
			return;
		}
		hoveredUnit = world.getUnitAtXY(x, y);
	}

	@Override
	public void onLeftClick(int x, int y) {
		if (controller == null || !controller.isGameStarted() || showIngameMenu)
			return;

		x /= IronConst.TILE_WIDTH;
		y /= IronConst.TILE_HEIGHT;
		// out of bounds
		if (x < 0 || x >= IronConst.MAP_WIDTH || y < 0
				|| y >= IronConst.MAP_HEIGHT)
			return;

		if (popup.isVisible()) {
			popup.setVisible(false);
			return;
		}

		IronUnit unit = world.getUnitAtXY(x, y);

		if (unit != null) {
			selectUnit(unit);
		} else if (selectedUnit != null) {
			requestMove(selectedUnit, x, y);
		}
	}

	@Override
	public void onRightClick(int x, int y) {
		if (controller == null || !controller.isGameStarted() || showIngameMenu)
			return;

		if (selectedUnit != null) {
			if (!popup.isVisible()) {
				popup.setUnit(world, selectedUnit, x, y);
				popup.setVisible(!popup.isVisible());
			} else {
				IronUnit unit = world.getUnitAtXY(x / IronConst.TILE_WIDTH, y
						/ IronConst.TILE_HEIGHT);
				if (unit != null) {
					popup.setUnit(world, selectedUnit, x, y);
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

	@Override
	public void onRelease() {
	}

	@Override
	public void onGameOver(int winnerId) {
		selectUnit(null);
	}

	@Override
	public void onGameStart(int startPlayerId) {
		forestSound.playAsMusic(true);
	}

	@Override
	public void onMove(IronUnit unit, int x, int y, Path path) {
	}

	@Override
	public void onMoveUndo(IronUnit unit) {
	}

	@Override
	public void onSkill(IronUnit unit, Skill skill, int x, int y,
			List<int[]> res) {
		skill.executeClientSide(world, unit.getId(), x, y, res);
		if (unit.hasPlayed() &&  selectedUnit != null && selectedUnit.getId() == unit.getId()) {
			selectUnit(null);
		}
	}

	@Override
	public void onTurnChange(int newTurnPlayerId) {
		popup.setVisible(false);
		selectUnit(null);
		notifyNewTurnTimeLeft = notifyNewTurnDuration;
	}

}
