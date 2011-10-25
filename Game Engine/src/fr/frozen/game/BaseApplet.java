package fr.frozen.game;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

@SuppressWarnings("serial")
public abstract class BaseApplet extends Applet {
	
	protected Canvas display_parent;
	protected Thread gameThread;
	protected AppletEngine engine;
	
	protected BaseApplet() {
	}
	
	protected abstract void createEngine();
	
	public void startLWJGL() {
		gameThread = new Thread() {
			public void run() {
				try {
					Display.setParent(display_parent);
					Dimension dim = engine.getScreenSize();
					DisplayMode dm = new DisplayMode((int)dim.getWidth(), (int)dim.getHeight());
					Display.setDisplayMode(dm);
					Display.setVSyncEnabled(engine.isVsync());
					Display.create();
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				engine.start();
			}
		};
		gameThread.start();
		Logger.getLogger(getClass()).debug("applet engine started");
	}
 
 
	private void stopLWJGL() {
		Logger.getLogger(getClass()).debug("applet stopped, so stopping game");
		engine.stopGame();
		try {
			Logger.getLogger(getClass()).debug("Waiting for appletEngine to end (ie join)");
			gameThread.join();
			Logger.getLogger(getClass()).debug("appletEngine joined");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
 
	public void start() {
		Logger.getLogger(getClass()).debug("applet start");
	}
 
	public void stop() {
		Logger.getLogger(getClass()).debug("applet stop");
	}
 
	public void destroy() {
		Logger.getLogger(getClass()).debug("destroying applet, ie removing display");
		remove(display_parent);
		super.destroy();
	}
 
	public void init() {
		Logger.getLogger(getClass()).debug("applet init");
		createEngine();
		setLayout(new BorderLayout());
		try {
			display_parent = new Canvas() {
				public final void addNotify() {
					super.addNotify();
					startLWJGL();
				}
				public final void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};
			display_parent.setSize(getWidth(),getHeight());
			add(display_parent);
			display_parent.setFocusable(true);
			display_parent.requestFocus();
			display_parent.setIgnoreRepaint(true);
			setVisible(true);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("unable to create display");
			throw new RuntimeException("Unable to create display");
		}
	}
}