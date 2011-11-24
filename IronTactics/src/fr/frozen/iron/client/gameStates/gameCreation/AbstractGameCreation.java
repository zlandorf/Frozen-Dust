package fr.frozen.iron.client.gameStates.gameCreation;

import fr.frozen.game.GameState;
import fr.frozen.game.IGameEngine;
import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.ActionListener;
import fr.frozen.iron.client.components.Button;
import fr.frozen.iron.client.components.DropList;
import fr.frozen.iron.client.components.DropListItem;
import fr.frozen.iron.client.components.GUI;
import fr.frozen.iron.client.components.Label;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.protocol.Protocol;

public abstract class AbstractGameCreation extends GameState implements
		ActionListener {

	protected static String TXT_WAITING = "Waiting ...";
	protected static String TXT_RACE_NOT_CHOSEN = "Not chosen yet";
	protected static String TXT_HOST = "Host";

	protected GUI gui;

	protected ISprite backTex;

	protected IronPlayer host;
	protected IronPlayer other;

	protected Label hostName;
	protected Label otherName;

	protected DropList hostList;
	protected DropList otherList;

	abstract protected void leave();

	abstract protected void setPlayers();

	abstract protected void setReady();

	public AbstractGameCreation(IGameEngine engine, String name) {
		super(engine, name, false, false);

		gui = new GUI();

		int width = 200;
		int height = 30;
		hostName = new Label(TXT_HOST, 100, 100, width, height);
		otherName = new Label(TXT_WAITING, 100, 225, width, height);

		hostList = new DropList(TXT_RACE_NOT_CHOSEN, 350, 100, width, height);
		otherList = new DropList(TXT_RACE_NOT_CHOSEN, 350, 225, width, height);

		hostList.setEditable(false);
		otherList.setEditable(false);

		backTex = SpriteManager.getInstance().getSprite("backTex");

		Button button = new Button("Start", 570, 350, 0, 0);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setReady();
			}
		});

		Button button2 = new Button("Back to Lobby", 570, 450, 0, 0);
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				leave();
			}
		});

		gui.addComponent(hostName);
		gui.addComponent(otherName);
		gui.addComponent(hostList);
		gui.addComponent(otherList);
		gui.addComponent(button);
		gui.addComponent(button2);
	}

	protected void reInit() {
		host = null;
		other = null;

		hostName.setLabel(TXT_HOST);
		otherName.setLabel(TXT_WAITING);

		hostList.setLabel(TXT_RACE_NOT_CHOSEN);
		otherList.setLabel(TXT_RACE_NOT_CHOSEN);

		hostList.reInit();
		otherList.reInit();

		hostList.setEditable(false);
		otherList.setEditable(false);
	}

	@Override
	public void setActive(boolean val) {
		super.setActive(val);
		if (!val) {
			reInit();
		} else {
			setPlayers();
		}
	}

	protected void setList(DropList list) {
		list.setEditable(true);
		list.setLabel("choose your race");

		list.addItem(new DropListItem("orc", Protocol.ORC_RACE.ordinal()));
		list.addItem(new DropListItem("human", Protocol.HUMAN_RACE.ordinal()));
		list.addActionListener(this);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		gui.update(deltaTime);
	}

	@Override
	public void render(float deltaTime) {
		backTex.fillIn(0, 0, (float) gameEngine.getScreenSize().getWidth(),
				(float) gameEngine.getScreenSize().getWidth());
		super.render(deltaTime);
		gui.render(deltaTime);
	}
}
