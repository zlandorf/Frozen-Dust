package fr.frozen.game;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Timer;
import org.newdawn.slick.Font;
import org.newdawn.slick.openal.SoundStore;

public class GameEngine implements IGameEngine {
  
	public static String LOADING_TEXT = "Loading resources, please wait ...";
	
	public static float _tick;
	protected float _lastTime;
	public static Timer _timer = null;
	
	
	protected volatile boolean _gameRunning = true;
  
	protected Dimension _screenSize;
	protected Dimension _resolution;
	protected boolean _fullScreen = false;
	protected String _title;
	protected boolean _vsync = false;

	protected List<GameState> _gameStatesToAdd;
	protected List<GameState> _gameStatesToRemove;
	
	protected List<GameState> _gameStates;
	protected Hashtable<String, GameState> _gameStatesByName;
	
	protected GameState currentGameState = null;
	protected GameState previousGameState = null;
	
	protected Font preloaderFont;
	
	public GameEngine(boolean fullScreen) {
		this();
		_fullScreen = fullScreen;
	}

	public GameEngine() {
		_gameStates = new Vector<GameState>();
		_gameStatesToAdd = new Vector<GameState>();
		_gameStatesToRemove = new Vector<GameState>();
		_gameStatesByName = new Hashtable<String, GameState>();
		_timer = new Timer();
		_resolution = new Dimension();
		_screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Keyboard.enableRepeatEvents(true);
	}
	
	@Override
	public boolean isGameRunning() {
		return _gameRunning;
	}
	
	@Override
	public void addGameState(GameState gs) {
		synchronized (_gameStatesToAdd) {
			_gameStatesToAdd.add(gs);
		}
	}
	
	@Override
	public synchronized void removeGameState(GameState gs){
		synchronized (_gameStatesToRemove) {
			_gameStatesToRemove.add(gs);
		}
	}
	
	@Override
	public void setCurrentGameState(GameState gs) {
		previousGameState = currentGameState;
		this.currentGameState = gs;
	}
	
	@Override
	public GameState getCurrentGameState() {
		return currentGameState;
	}
	
	@Override
	public GameState getPreviousGameState() {
		return previousGameState;
	}
	
	@Override
	public synchronized List<GameState> getGameStates() {
		return _gameStates;
	}
	
	@Override
	public synchronized GameState getGameState(String name) {
		return _gameStatesByName.get(name);
	}

	@Override
	public void start() {
		initDisplayMode();
		initGL();
		setPreloaderFont();
		buildAssets();
		buildInitialGameStates();
		gameLoop();
	}

	@Override	
	public boolean isKeyPressed(int keyCode) {
		// apparently, someone at decided not to use standard 
		// keycode, so we have to map them over:
		switch(keyCode) {
		case KeyEvent.VK_SPACE:
			keyCode = Keyboard.KEY_SPACE;
			break;
		case KeyEvent.VK_LEFT:
			keyCode = Keyboard.KEY_LEFT;
			break;
		case KeyEvent.VK_RIGHT:
			keyCode = Keyboard.KEY_RIGHT;
			break;
		}    
		
		return Keyboard.isKeyDown(keyCode);
	}
	
	//TODO check if i must destroy / recreate display ?!
	@Override
	public boolean setDisplayMode(DisplayMode dm) {
		try {
			Display.setDisplayMode(dm);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(getClass()).error("DisplayMode not supported");
		}
		return false;
	}

	@Override
	public void setVSync(boolean val) {
		_vsync = val;
		if (Display.isCreated() && Display.isActive()) {
			Display.setVSyncEnabled(_vsync);
		}
	}
	
	@Override
	public void setFullScreen(boolean fullscreen) {
		_fullScreen = fullscreen;
		try {
			Display.setFullscreen(_fullScreen);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setHeight(int h) {
		_screenSize.setSize(_screenSize.getWidth(), h);
	}

	@Override
	public void setResolution(int x, int y) {
		_resolution.setSize(x, y);
	}

	@Override
	public void setWidth(int w) {
		_screenSize.setSize(w,_screenSize.getHeight());
	}
	
	@Override
	public void setSize(int w, int h) {
		_screenSize.setSize(w,h);
	}
	
	@Override
	public void setTitle(String title) {
		_title = title;
		Display.setTitle(_title);
	}

	@Override
	public boolean toggleFullScreen() {
		// TODO Auto-generated method stub
		return false;
	} 
	
	@Override
	public Dimension getScreenSize() {
		return _screenSize;
	}
	
	@Override
	public void stopGame() {
		_gameRunning = false;
	}
	
	/* ----------------------  protected ------------------------- */
	
	protected boolean setDisplayMode() {
		Logger.getLogger(getClass()).debug("screen size wanted = "+_screenSize);
		try {
			DisplayMode[] adm = Display.getAvailableDisplayModes();
			DisplayMode finaldm;
			finaldm = adm[0];
			for (DisplayMode dm : adm) {
				if (dm.isFullscreenCapable() 
						&& dm.getWidth() == _screenSize.getWidth() 
						&& dm.getHeight() == _screenSize.getHeight()) {

					finaldm = dm;
					break;
				}
			}

			/*org.lwjgl.util.Display.setDisplayMode(adm, new String[] {
    					"width=" + (int)_screenSize.getWidth(),
    					"height=" + (int)_screenSize.getHeight(),
    					"freq=" + 60,
    					"bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
    					});*/

			Logger.getLogger(getClass()).debug("screen size found = ["+finaldm.getWidth()+","+finaldm.getHeight()+"]");
			Logger.getLogger(getClass()).debug("fullscreen "+_fullScreen);
			Display.setDisplayMode(finaldm);
			Display.setFullscreen(_fullScreen);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(getClass()).error("Unable to enter fullscreen, continuing in windowed mode");
		}

		//		try {
		//			// get modes
		//			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes((int)_screenSize.getWidth(), (int)_screenSize.getHeight(), -1, -1, -1, -1, 60, 60);
		//			
		//			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
		//			"width=" + (int)_screenSize.getWidth(),
		//			"height=" + (int)_screenSize.getHeight(),
		//			"freq=" + 60,
		//			"bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
		//			});
		//			return true;
		//			} catch (Exception e) {
		//			e.printStackTrace();
		//			System.out.println("Unable to enter fullscreen, continuing in windowed mode");
		//			}

		return false;
	}
	

	protected void setPreloaderFont() {
		preloaderFont = FontManager.loadFont(new java.awt.Font("Times new roman",java.awt.Font.PLAIN, 17));
	}

	protected void buildAssets() {
		if (preloaderFont != null) {
			drawLoadingText();
		}
		//function called to build game assets
		//has to be overridden
	}
	
	protected void drawLoadingText() {
		drawLoadingText(LOADING_TEXT);
	}
	
	protected void drawLoadingText(String text) {
		onPreRender();

		double x = getScreenSize().getWidth() / 2;
		double y = getScreenSize().getHeight() / 2;

		x -= preloaderFont.getWidth(text) / 2;
		y -= preloaderFont.getHeight(text) / 2;
		preloaderFont.drawString((int)x, (int)y, text);

		onPostRender();
	}
	
	protected void buildInitialGameStates() {
		//function called to build game assets
		//needs to be overridden
	}
	
	
	protected void initDisplayMode() {
		try {
			setDisplayMode();
			Display.setTitle(_title);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void initGL() {

			//Display.setVSyncEnabled(_vsync);
			//Mouse.setGrabbed(true);
  
			// enable textures since we're going to use these for our sprites
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			// disable the OpenGL depth test since we're rendering 2D graphics
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);//for alpha on images
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);//to be able to alpha textures
			
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, _screenSize.getWidth(), _screenSize.getHeight(), 0, -1, 1);

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glViewport(0, 0, (int)_screenSize.getWidth(), (int)_screenSize.getHeight());
	}


	protected void onPreRender() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glColor4f(1,1,1,1f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	protected void onPostRender() {
		Display.sync(60);
		Display.update();
	}
  
	protected void render() {
		if (Display.isVisible()) {
			for (GameState gs : _gameStates) {
				if (gs.isVisible())
					gs.render(_tick);
			}
		}
	}
	
	protected void heartBeat() {
		Timer.tick();
		_tick = _timer.getTime() - _lastTime;
		_lastTime = _timer.getTime();
	}
	
	protected void update(float deltaTime) {
		updateGameStates();
		for (GameState gs : _gameStates) {
			if (gs.isActive())
				gs.update(deltaTime);
		}
	}
	
	protected void updateGameStates() {
		
		synchronized (_gameStatesToAdd) {
			for (GameState gs : _gameStatesToAdd) {
				_gameStates.add(gs);
				_gameStatesByName.put(gs.getName(),gs);
			}
			_gameStatesToAdd.clear();
		}
		
		synchronized (_gameStatesToRemove) {
			for (GameState gs : _gameStatesToRemove) {
				_gameStates.remove(gs);
				_gameStatesByName.remove(gs.getName());
			}
			_gameStatesToRemove.clear();
		}
	}
	
	
	protected void gameLoop() {
		while (_gameRunning) {
			
			heartBeat();
			update(_tick);
			//check collisions
			onPreRender();
			render();
			onPostRender();
			
			SoundStore.get().poll((int)(_tick * 1000));
			//if (!Display.isActive()) Thread.yield();
			if(isCloseRequested()) {
				_gameRunning = false;
			}
		}
		cleanUp();
	}
	
	protected boolean isCloseRequested() {
		return Display.isCloseRequested();
	}
	
	protected void cleanUp() {
		Logger.getLogger(getClass()).debug("destroying display");
		Display.destroy();
		Logger.getLogger(getClass()).debug("destroying audio");
		AL.destroy();

		//System.exit(0);
	}

	@Override
	public boolean isVsync() {
		return _vsync;
	}
}
