package fr.frozen.game;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.util.XMLParser;

public abstract class ISpriteManager {
	private static ISpriteManager instance = null;
	public static ISpriteManager getInstance() {
		if (instance == null) {
			instance = new SpriteManagerImpl();
		}
		return instance;
	}
	
	public abstract boolean isSpriteLoaded(String name);
	public abstract boolean isAnimationLoaded(String name);
	
	public abstract boolean loadSprite(String filename);
	public abstract boolean loadSprite(String filename, String spritename);
	public abstract ISprite getSprite(String name);
	public abstract ISprite getSubSprite(String sheetName, Vector2f pos, Vector2f dim, Vector2f offSet);
	public abstract ISprite getSubSprite(ISprite sheet, Vector2f pos, Vector2f dim, Vector2f offSet);
	
	public abstract AnimationSequence getAnimationSequence(String name);
	
	public abstract boolean loadImagesFromXml(String filename);
	public abstract boolean loadImagesFromXml(XMLParser parser);
}
