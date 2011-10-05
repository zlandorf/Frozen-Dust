package fr.frozen.game;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.newdawn.slick.Font;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.Effect;

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
	
	public static Font loadFont(String filename, Effect ... effects) {
		return loadFont(filename, 10, effects);
	}
	
	public static Font loadFont(String filename, int fontSize, Effect ... effects) {
		return loadFont(filename, fontSize, false, false, effects);
	}
	
	public static Font loadFont(String filename, int fontSize, boolean bold, boolean italic, Effect ... effects) {
		
		return instance.loadFontAux(filename, fontSize, bold, italic, effects);
	}
	
	private Font loadFontAux(String filename, int size, boolean bold, boolean italic, Effect ... effects) {
		UnicodeFont uFont = null;
		try {
			uFont = new UnicodeFont(FONT_DIR+filename , size, bold, italic);
			initFont(uFont, effects);
		} catch (SlickException e) {
			e.printStackTrace();
			Logger.getLogger(getClass()).error("did not manage to load font : "+filename);
		} 
		return uFont;
	}
	
	public static Font loadFont(java.awt.Font font, Effect ... effects) {
		return instance.loadFontAux(font, effects);
	}
	
	private Font loadFontAux(java.awt.Font font, Effect ... effects) {
		UnicodeFont uFont = new UnicodeFont(font);
		try {
			initFont(uFont, effects);
		} catch (SlickException e) {
			e.printStackTrace();
			Logger.getLogger(getClass()).error("did not manage to load font : "+font);
		} 
		return uFont;
	}
	
	@SuppressWarnings("unchecked")
	private void initFont(UnicodeFont uFont, Effect ... effects) throws SlickException {
		
		uFont.addAsciiGlyphs();   
		uFont.addGlyphs(400, 600); 
		for (Effect effect : effects) {
			uFont.getEffects().add(effect);
		}
		uFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		uFont.loadGlyphs();
	}
}
