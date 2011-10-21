package fr.frozen.game;

import java.awt.Dimension;
import java.util.List;

import org.lwjgl.opengl.DisplayMode;

public class AppletEngine implements IGameEngine {

	protected GameEngine engine;
	
	public AppletEngine(GameEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public void addGameState(GameState gs) {
		engine.addGameState(gs);
	}

	@Override
	public GameState getCurrentGameState() {
		return engine.getCurrentGameState();
	}

	@Override
	public GameState getGameState(String name) {
		return engine.getCurrentGameState();
	}

	@Override
	public List<GameState> getGameStates() {
		return engine.getGameStates();
	}

	@Override
	public GameState getPreviousGameState() {
		return engine.getPreviousGameState();
	}

	@Override
	public Dimension getScreenSize() {
		return engine.getScreenSize();
	}

	@Override
	public boolean isKeyPressed(int keyCode) {
		return engine.isKeyPressed(keyCode);
	}

	@Override
	public void removeGameState(GameState gs) {
		engine.removeGameState(gs);
	}

	@Override
	public void setCurrentGameState(GameState gs) {
		engine.setCurrentGameState(gs);
	}

	@Override
	public boolean setDisplayMode(DisplayMode dm) {
		return true;
	}

	@Override
	public void setFullScreen(boolean fullscreen) {
		return;
	}

	@Override
	public void setHeight(int h) {
		engine.setHeight(h);
	}

	@Override
	public void setResolution(int x, int y) {
		engine.setResolution(x, y);
	}

	@Override
	public void setSize(int w, int h) {
		engine.setSize(w, h);
	}

	@Override
	public void setTitle(String title) {
		engine.setTitle(title);
	}

	@Override
	public void setVSync(boolean val) {
		engine.setVSync(val);
	}

	@Override
	public void setWidth(int w) {
		engine.setWidth(w);
	}

	@Override
	public void start() {
		engine.initGL();
		engine.setPreloaderFont();
		engine.buildAssets();
		engine.buildInitialGameStates();
		engine.gameLoop();
	}

	@Override
	public void stopGame() {
		engine.stopGame();
	}

	@Override
	public boolean toggleFullScreen() {
		return engine.toggleFullScreen();
	}

	@Override
	public boolean isGameRunning() {
		return engine.isGameRunning();
	}

	@Override
	public boolean isVsync() {
		return engine.isVsync();
	}
}
