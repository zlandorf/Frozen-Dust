package fr.frozen.iron.client.gameStates;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.Font;
import fr.frozen.game.FontManager;
import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.iron.util.IronConst;

public class Intro extends GameState {
	
	protected double timeSpent = 0;
	protected boolean started = false;
	protected boolean ended = false;
	
	protected Font font;
	protected Vector2f textPos;
	
	protected String text = "Iron Tactics";
	
	protected float scale = 2;
	
	public Intro(GameEngine ge) {
		super(ge, "intro", true, true);
		font = FontManager.getFont("Font");
		DisplayMode dm = Display.getDisplayMode();
		
		float x = (dm.getWidth() / 2) - (text.length() * 10 * scale / 2);
		float y = (dm.getHeight() / 2) - scale * 16 /2;
		textPos = new Vector2f(x, y);

	}

	@Override
	public void render(float deltaTime) {
		super.render(deltaTime);
		float alphaLevel = (float)(timeSpent / IronConst.INTRO_DURATION); 
		if (alphaLevel > 1) alphaLevel = 1;
		
		alphaLevel *= Math.PI;
		alphaLevel = (float) Math.abs(Math.sin(alphaLevel));
		
		font.setColor(1, 1, 1, alphaLevel);
		font.glPrint(text, textPos.getX(), textPos.getY(), 1,scale);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if (started) {
			timeSpent += deltaTime;
		} else {
			started = true;
		}
		
		if (timeSpent >= IronConst.INTRO_DURATION) {
			ended = true;
		}
		
		if (gameEngine.isKeyPressed(Keyboard.KEY_ESCAPE)) {
			ended = true;
		}
		
		if (ended) {
			setActive(false);
			setVisible(false);
			gameEngine.getGameState("mainMenu").setActive(true);
			gameEngine.getGameState("mainMenu").setVisible(true);
			gameEngine.removeGameState(this);
			GL11.glColor4f(1, 1, 1, 1);
		}
	}
	
	
	@Override
	public void createGameObjects() {
		//GameObject archer = new GameObject(this,300,200,ISpriteManager.getInstance().getSprite("avenger.png"));
		//archer.getSprite().setWidth(216);
		//archer.getSprite().setHeight(216);
		//addGameObject(archer);
	}
}
