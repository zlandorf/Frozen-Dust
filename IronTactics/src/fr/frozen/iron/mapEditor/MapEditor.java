package fr.frozen.iron.mapEditor;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.frozen.game.FontManager;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.common.IronMap;
import fr.frozen.iron.common.Tile;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronGL;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.util.XMLParser;

public class MapEditor {
	private JFrame frame;
	Canvas display_parent;
	Thread gameThread;
	boolean running;

	volatile IronMap map;

	long lastTime;

	boolean mouseDown = false;

	int tilesChoiceX = 10;
	int tilesChoiceY = 500;

	int objectsChoiceX = 10;
	int objectsChoiceY = 550;

	List<Tile> tiles;
	List<Tile> objects;
	Tile currentTile;
	Tile currentObject;
	boolean showGrid = true;

	protected JFileChooser fileChooser;
	Font font;

	String posText = "";
	String objectTypeText = "";
	String tileTypeText ="";

	public MapEditor() {
		tiles = new ArrayList<Tile>();
		objects = new ArrayList<Tile>();
		initializeFrame();
		initializeMenu();

		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "custom filter to chose iron tactic maps";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				String extension = getExtension(f);
				if (extension != null) {
					if (extension.equals("map")) {
						return true;
					}
				}
				return false;
			}
		});

		frame.pack();
		frame.setVisible(true);
	}

	private String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	private void initializeMenu() {
		JMenuBar jMenuBar = new JMenuBar();
		JMenu menu = new JMenu("options");

		JMenuItem loadItem = new JMenuItem("Load Map");
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		loadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openNewMap();
			}
		});

		JMenuItem saveItem = new JMenuItem("Save Map");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveMap();
			}
		});

		JMenuItem resetItem = new JMenuItem("Reset map");
		resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		resetItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetMap();
			}
		});

		JMenuItem grid = new JMenuItem("Toggle grid");
		grid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				ActionEvent.CTRL_MASK));
		grid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleGrid();
			}
		});

		menu.add(loadItem);
		menu.add(saveItem);
		menu.add(resetItem);
		menu.add(grid);

		jMenuBar.add(menu);
		frame.setJMenuBar(jMenuBar);
	}

	protected synchronized void resetMap() {
		map = new IronMap();
		map.initSprites();
	}

	protected synchronized void saveMap() {
		File file = chooseFile();
		if (file != null && !file.isDirectory()) {
			if (map != null) {
				try {
					IronUtil.saveMap(map, file);
				} catch (IOException e) {
					System.out.println("failed to save map");
					e.printStackTrace();
				}
			} else {
				System.out.println("cannot save map, it is null");
			}
		}
	}

	protected synchronized void openNewMap() {
		File file = chooseFile();
		if (file != null && !file.isDirectory()) {
			try {
				map = IronUtil.loadMap(file);
				map.initSprites();
			} catch (IOException e) {
				System.out.println("failed to load map");
				e.printStackTrace();
			}
		}
	}

	private File chooseFile() {
		int returnVal = fileChooser.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	protected void toggleGrid() {
		showGrid = !showGrid;
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
		font = FontManager.loadFont("visitor.ttf", 20);
		initChosableTiles();

		resetMap();
		// map.generateMap();
		// map.initSprites();
	}

	private void initChosableTiles() {
		int tileCurrentX = 0, tileCurrentY = 0;
		int objectCurrentX = 0, objectCurrentY = 0;

		XMLParser xml = IronConfig.getIronXMLParser();
		Element element = xml.getElement("map");

		Tile newTile;

		{ // add the object "nothing" to remove
			// objects
			newTile = new Tile(new Point2D.Double(objectCurrentX,
					objectCurrentY), null);

			newTile.setType(Tile.TYPE_GRASS);
			newTile.setSubType(1);
			newTile.setOccupied(false);
			newTile.setObjectOverlap(0);
			newTile.setObjectSubType(0);
			newTile.findSprites();

			objects.add(newTile);

			objectCurrentX++;
		}

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

				if (isTile(name)) {
					int type = getTileType(name);
					if (type != -1 && nbtypes > 0) {
						for (int subType = 1; subType <= nbtypes; subType++) {
							newTile = new Tile(new Point2D.Double(tileCurrentX,
									tileCurrentY), null);

							newTile.setType(type);
							newTile.setSubType(subType);
							newTile.setOccupied(obstacle);

							newTile.findSprites();

							tiles.add(newTile);

							tileCurrentX++;
						}
					}
				} else {
					int type = getObjectType(name);
					if (type != -1 && nbtypes > 0) {
						for (int subType = 1; subType <= nbtypes; subType++) {
							newTile = new Tile(new Point2D.Double(
									objectCurrentX, objectCurrentY), null);

							newTile.setType(Tile.TYPE_GRASS);
							newTile.setSubType(1);
							newTile.setOccupied(obstacle);
							newTile.setObjectOverlap(type);
							newTile.setObjectSubType(subType);
							newTile.findSprites();

							objects.add(newTile);

							objectCurrentX++;
						}
					}
				}
			}
		}
	}

	private int getObjectType(String name) {
		for (int i = 0; i < Tile.object_names.length; i++) {
			if (name.equals(Tile.object_names[i])) {
				return i;
			}
		}
		return -1;
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
					if (currentTile != null) {
						addTile(x / IronConst.TILE_WIDTH, y
								/ IronConst.TILE_HEIGHT);
					}
					if (currentObject != null) {
						addObject(x / IronConst.TILE_WIDTH, y
								/ IronConst.TILE_HEIGHT);
					}
				}
				if (onTileChoice(x, y)) {
					choseTile((x - tilesChoiceX) / IronConst.TILE_WIDTH);
				}

				if (onOjectChoice(x, y)) {
					choseObject((x - objectsChoiceX) / IronConst.TILE_WIDTH);
				}
			}

			objectTypeText = "";
			tileTypeText = "";
			
			if (onMap(x, y)) {
				posText = "pos = " + (x / IronConst.TILE_WIDTH) + ", "
						+ (y / IronConst.TILE_HEIGHT);
			} else {
				posText = "";
			}
			if (onTileChoice(x, y)) {
				Tile tmp = tiles.get((x - tilesChoiceX) / IronConst.TILE_WIDTH);
				tileTypeText = Tile.tile_names[tmp.getType()] + "_"
						+ tmp.getSubType();
			} else if (onOjectChoice(x, y)) {
				Tile tmp = objects.get((x - objectsChoiceX)
						/ IronConst.TILE_WIDTH);
				objectTypeText = Tile.object_names[tmp.getObjectOverlap()] + "_"
						+ tmp.getObjectSubType();
			} else if (map != null && onMap(x, y)) {
				int mapX = x / IronConst.TILE_WIDTH;
				int mapY = y / IronConst.TILE_HEIGHT;
				Tile tmp = map.getTile(mapX, mapY);
				tileTypeText  = Tile.tile_names[tmp.getType()] + "_"
				+ tmp.getSubType();
				objectTypeText = Tile.object_names[tmp.getObjectOverlap()] + "_"
				+ tmp.getObjectSubType();
			}
		}
	}

	private void addObject(int x, int y) {
		map.getTile(x, y).setOccupied(currentObject.isOccupied());
		map.getTile(x, y).setObjectOverlap(currentObject.getObjectOverlap());
		map.getTile(x, y).setObjectSubType(currentObject.getObjectSubType());

		refreshMap();
	}

	private void choseObject(int i) {
		currentObject = objects.get(i);
		currentTile = null;
	}

	private boolean onOjectChoice(int x, int y) {
		x -= objectsChoiceX;
		y -= objectsChoiceY;
		return x >= 0 && x < objects.size() * IronConst.TILE_WIDTH && y >= 0
				&& y < IronConst.TILE_HEIGHT;
	}

	private void choseTile(int i) {
		currentTile = tiles.get(i);
		currentObject = null;
	}

	private boolean onTileChoice(int x, int y) {
		x -= tilesChoiceX;
		y -= tilesChoiceY;
		return x >= 0 && x < tiles.size() * IronConst.TILE_WIDTH && y >= 0
				&& y < IronConst.TILE_HEIGHT;
	}

	protected synchronized void render(float deltaTime) {

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glColor4f(1, 1, 1, 1f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		// ///////////////

		if (map != null) {
			map.render(deltaTime);
			if (showGrid)
				map.renderGrid();
		}

		// TILE CHOICE LIST
		GL11.glTranslatef(tilesChoiceX, tilesChoiceY, 0);
		for (Tile tile : tiles) {
			tile.render(deltaTime);
		}
		IronGL.drawGrid(0, 0, tiles.size() * IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT, IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT);

		if (currentTile != null) {
			int x = (int) (currentTile.getPos().getX() * IronConst.TILE_WIDTH);
			int y = (int) (currentTile.getPos().getY() * IronConst.TILE_HEIGHT);
			IronGL.drawHollowRect(x, y, IronConst.TILE_WIDTH,
					IronConst.TILE_HEIGHT, 0xff0000);
		}

		// /OBJECT CHOICE LIST
		GL11.glLoadIdentity();
		GL11.glTranslatef(objectsChoiceX, objectsChoiceY, 0);
		for (Tile tile : objects) {
			tile.render(deltaTime);
		}
		IronGL.drawGrid(0, 0, objects.size() * IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT, IronConst.TILE_WIDTH,
				IronConst.TILE_HEIGHT);

		// /
		if (currentObject != null) {
			int x = (int) (currentObject.getPos().getX() * IronConst.TILE_WIDTH);
			int y = (int) (currentObject.getPos().getY() * IronConst.TILE_HEIGHT);
			IronGL.drawHollowRect(x, y, IronConst.TILE_WIDTH,
					IronConst.TILE_HEIGHT, 0xff0000);
		}

		// to have all the grids behind
		for (Tile tile : objects) {
			tile.renderObject(deltaTime);
		}
		// /////////

		GL11.glLoadIdentity();
		font.drawString(650, 50, posText, Color.white);
		font.drawString(650, 80, tileTypeText, Color.white);
		font.drawString(650, 110, objectTypeText, Color.white);

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
		display_parent.setFocusable(false);
		// display_parent.requestFocus();
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

		refreshMap();
	}

	private void refreshMap() {
		map.resetOverLays();
		map.generateOverLays();
		map.resetSprites();
	}

	private boolean onMap(int x, int y) {
		return x >= 0 && x < IronConst.MAP_WIDTH * IronConst.TILE_WIDTH
				&& y >= 0 && y < IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT;
	}
}
