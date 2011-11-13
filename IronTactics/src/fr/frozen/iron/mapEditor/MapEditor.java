package fr.frozen.iron.mapEditor;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.frozen.game.SpriteManager;
import fr.frozen.iron.common.IronMap;
import fr.frozen.iron.common.Tile;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronGL;
import fr.frozen.util.XMLParser;

public class MapEditor {
	private JFrame frame;
	Canvas display_parent;
	Thread gameThread;
	boolean running;

	volatile IronMap map;

	long lastTime;

	boolean mouseDown = false;
	
	int tilesChoiceX = 5; 
	int tilesChoiceY = 500;

	List<Tile> tiles;
	Tile currentTile;

	public MapEditor() {
		tiles = new ArrayList<Tile>();
		initializeFrame();
		frame.pack();
		frame.setVisible(true);
	}

	public void startLWJGL() {
		gameThread = new Thread() {
			public void run() {
				running = true;
				try {
					Display.setParent(display_parent);
					Display.setVSyncEnabled(true);
					Display.setDisplayMode(new DisplayMode(800, 600));
					Display.create();
					initGL();
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				init();
				gameLoop();
			}
		};
		gameThread.start();
	}

	protected void init() {

		SpriteManager.getInstance().loadImagesFromXml(
				IronConfig.getIronXMLParser());

		initChosableTiles();

		map = new IronMap();
		map.initSprites();
		// map.generateMap();
		// map.initSprites();
	}

	private void initChosableTiles() {
		int currentX = 0, currentY = 0;

		XMLParser xml = IronConfig.getIronXMLParser();
		Element element = xml.getElement("map");
		if (element != null) {
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.TEXT_NODE)
					continue;

				NamedNodeMap map = node.getAttributes();

				String name = node.getNodeName();
				int nbtypes = Integer.parseInt(map.getNamedItem("nbtypes")
						.getNodeValue());
				boolean obstacle = Integer.parseInt(map
						.getNamedItem("obstacle").getNodeValue()) == 1;

				if (isTile(name) && nbtypes > 0) {
					int type = getTileType(name);
					if (type != -1) {
						for (int subType = 1; subType <= nbtypes; subType++) {
							Tile newTile = new Tile(new Point2D.Double(
									currentX, currentY), null);

							newTile.setType(type);
							newTile.setSubType(subType);
							newTile.setOccupied(obstacle);

							newTile.findSprites();

							tiles.add(newTile);
							
							currentX ++;
						}
					}
				} else {
					System.out.println("not tile "+name);
					System.out.println("nb types ="+nbtypes);
				}
			}
		}
	}

	private int getTileType(String name) {
		for (int i = 0; i < Tile.tile_names.length; i++) {
			if (name.equals(Tile.tile_names[i])) {
				return i;
			}
		}
		return -1;
	}

	private boolean isTile(String name) {
		for (String tile_name : Tile.tile_names) {
			if (name.equals(tile_name)) {
				return true;
			}
		}
		return false;
	}

	protected void gameLoop() {
		lastTime = System.currentTimeMillis();
		long time;
		float deltaTime;
		while (running) {
			time = System.currentTimeMillis();
			deltaTime = (time - lastTime) / 1000f;

			update(deltaTime);
			render(deltaTime);

			lastTime = time;
		}
		Display.destroy();
	}

	private void update(float deltaTime) {
		Mouse.poll();
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
			int x = Mouse.getX();
			int y = (int) Display.getDisplayMode().getHeight() - Mouse.getY();
			if (eventButton >= 0) {
				if (Mouse.getEventButtonState()) {
					mouseDown = true;
				} else { // mouse released
					mouseDown = false;
				}
			}

			if (mouseDown) {
				if (onMap(x, y)) {
					addTile(x / IronConst.TILE_WIDTH, y / IronConst.TILE_HEIGHT);
				}
				if (onTileChoice(x, y)) {
					choseTile((x - tilesChoiceX) / IronConst.TILE_WIDTH);
				}
			}
		}

		/*
		 * if (clikedOnMap(e.getX(), e.getY())) { addTile(e.getX() /
		 * IronConst.TILE_WIDTH, e.getY() / IronConst.TILE_HEIGHT); }
		 */
	}

	private void choseTile(int i) {
		currentTile = tiles.get(i);
	}

	private boolean onTileChoice(int x, int y) {
		x -= tilesChoiceX;
		y -= tilesChoiceY;
		return x >= 0 && x < tiles.size() * IronConst.TILE_WIDTH && y >= 0
				&& y < IronConst.TILE_HEIGHT; 
	}

	protected void render(float deltaTime) {

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glColor4f(1, 1, 1, 1f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		// ///////////////

		map.render(deltaTime);
		map.renderGrid();
		// sprite.draw(10,10);

		GL11.glTranslatef(tilesChoiceX, tilesChoiceY, 0);
		for (Tile tile : tiles) {
			tile.render(deltaTime);
		}
		IronGL.drawGrid(0, 0, tiles.size() * IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT, IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT);

		// /////////
		Display.sync(60);
		Display.update();
	}

	protected void initGL() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 600, 0, -1, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, 800, 600);
	}

	private void stopLWJGL() {
		running = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	private void initializeFrame() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.remove(display_parent);
				frame.dispose();
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().setPreferredSize(new Dimension(800, 600));

		display_parent = new Canvas() {
			public void addNotify() {
				super.addNotify();
				startLWJGL();
			}

			public void removeNotify() {
				stopLWJGL();
				super.removeNotify();
			}
		};
		display_parent.setFocusable(true);
		display_parent.requestFocus();
		display_parent.setIgnoreRepaint(true);

		display_parent.setPreferredSize(new Dimension(800, 600));
		frame.getContentPane().add(display_parent);
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		MapEditor editor = new MapEditor();
	}

	private void addTile(int x, int y) {
		map.getTile(x, y).setType(currentTile.getType());
		map.getTile(x, y).setSubType(currentTile.getSubType());
		map.getTile(x, y).setOccupied(currentTile.isOccupied());
		map.getTile(x, y).setObjectOverlap(0);
		
		map.resetOverLays();
		map.generateOverLays();
		map.resetSprites();
	}

	private boolean onMap(int x, int y) {
		return x >= 0 && x < IronConst.MAP_WIDTH * IronConst.TILE_WIDTH
				&& y >= 0 && y < IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT;
	}
}
