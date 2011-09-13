package fr.frozen.game;

import java.io.IOException;
import java.util.Hashtable;

public class FontManager {

	private static FontManager instance = new FontManager();;

	private Hashtable<String, Font> fonts;
	
	
	private static FontManager getInstance() {
		return instance;
	}
	
	private FontManager() {
		fonts = new Hashtable<String, Font>();
	}
	
	public static Font getFont(String fontname) {
		return FontManager.getInstance().getFontAux(fontname);
	}
	
	private Font getFontAux(String fontname) {
		return fonts.get(fontname);
	}
	
	public static Font loadFont(String filename) {
		
		return FontManager.getInstance().loadFontAux(filename);
	}
	
	private Font loadFontAux(String filename) {
		Font font = null;
		try {
			font = new Font(filename);
			String []tmp = filename.split("/");
			String fontName = tmp[tmp.length - 1].replaceAll("\\.png", "");
			System.out.println("adding font : "+fontName);
			
			fonts.put(fontName,font);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return font;
	}
	
	public static void main(String []args) {
		
	}
}
