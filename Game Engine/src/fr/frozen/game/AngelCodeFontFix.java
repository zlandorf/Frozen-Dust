package fr.frozen.game;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.TextureImpl;

public class AngelCodeFontFix extends AngelCodeFont {

	public AngelCodeFontFix(String fntFile, String imgFile)
			throws SlickException {
		super(fntFile, imgFile);
	}

	@Override
	public void drawString(float x, float y, String text, Color col,
			int startIndex, int endIndex) {
		TextureImpl.bindNone();
		super.drawString(x,y,text,col,startIndex, endIndex);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
