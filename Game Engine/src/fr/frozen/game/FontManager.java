package fr.frozen.game;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.newdawn.slick.Font;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class FontManager {
	public static String FONT_DIR = "Data/";
	private static FontManager instance = new FontManager();
	
	private Hashtable<String, Font> fonts;

	private FontManager() {
		fonts = new Hashtable<String, Font>();
	}
	
	public static void addFont(Font font, String fontName) {
		if (font == null) return;
		if (getFont(fontName) != null) {
			Logger.getLogger(instance.getClass()).warn("font with name "+fontName+" already added");
		} else {
			instance.fonts.put(fontName, font);
			Logger.getLogger(instance.getClass()).info("adding font : "+fontName);
		}
	}
	
	public static Font getFont(String fontName) {
		return instance.fonts.get(fontName);
	}
	
	public static Font loadFont(String filename) {
		return loadFont(filename, 10);
	}
	
	public static Font loadFont(String filename, int fontSize) {
		return loadFont(filename, fontSize, false, false);
	}
	
	public static Font loadFont(String filename, int fontSize, boolean bold, boolean italic) {
		
		return instance.loadFontAux(filename, fontSize, bold, italic);
	}
	
	@SuppressWarnings("unchecked")
	private Font loadFontAux(String filename, int size, boolean bold, boolean italic) {
		UnicodeFont uFont = null;
		try {
			uFont = new UnicodeFont(FONT_DIR+filename , size, bold, italic);
			uFont.addAsciiGlyphs();   
			uFont.addGlyphs(400, 600); 
			uFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
			uFont.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
			Logger.getLogger(getClass()).error("did not manage to load font : "+filename);
		} 
		return uFont;
	}
}
