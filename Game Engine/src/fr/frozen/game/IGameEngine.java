package fr.frozen.game;

import java.awt.Dimension;
import java.util.List;

import org.lwjgl.opengl.DisplayMode;

public interface IGameEngine {
	
	public void addGameState(GameState gs);
	public void removeGameState(GameState gs);
	public List<GameState> getGameStates();
	public GameState getGameState(String name);
	public void setCurrentGameState(GameState gs);
	public GameState getCurrentGameState();
	
	public void setTitle(String title);
	public void setResolution(int x, int y);
	public void setWidth(int w);
	public void setHeight(int h);
	public void setSize(int w, int h);
	public Dimension getScreenSize();
	
	public boolean setDisplayMode(DisplayMode dm);
	public boolean toggleFullScreen();
	public void setFullScreen(boolean fullscreen);
	public void setVSync(boolean val);
	
	
	public void start();
	public void stopGame();
	
	public boolean isKeyPressed(int keyCode);
	//public void stop();	
}
