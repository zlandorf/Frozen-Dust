package fr.frozen.iron.util;

import org.lwjgl.opengl.GL11;

public class IronGL {

	public static float[] getRgb(int color) {
		float [] rgb = new float[3];
		
		rgb[2] = color & 0xff;//blue
		rgb[2] /= 0xff;
		
		color >>= 8;
		rgb[1] = color & 0xff;//green
		rgb[1] /= 0xff;
		
		color >>= 8;
		rgb[0] = color & 0xff;//red
		rgb[0] /= 0xff;
		return rgb;
	}
	
	public static int getIntColor(float r, float g, float b) {
		int color = 0x0;
		
		color |= (0x000000ff & (byte)(r * 0xff));
		color <<= 8;
		color |= (0x000000ff & (byte)(g * 0xff));
		color <<= 8;
		color |= (0x000000ff & (byte)(b * 0xff));
		
		return color;
	}
	
	public static void drawRect(float x, float y, float w, float h, int icolor) {
		float []rgb = getRgb(icolor);
		drawRect(x, y, w, h, rgb[0], rgb[1],  rgb[2], 1);
	}
	
	public static void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPushMatrix();
		GL11.glTranslatef((int)x, (int)y, 0);
		
		GL11.glColor4f(r,g,b,a);
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, h);
			GL11.glVertex2f(w, h);
			GL11.glVertex2f(w, 0);
		}

		GL11.glEnd();
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public static void drawPoint(int x, int y) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();

		GL11.glBegin(GL11.GL_POINTS);
		{
			GL11.glVertex2i(x, y);
		}

		GL11.glEnd();
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public static void drawLine(float x1, float y1, float x2, float y2) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();
		GL11.glColor4f(.2f,.2f,.2f,1);
		
		GL11.glBegin(GL11.GL_LINES);
		{
			GL11.glVertex2f(x1, y1);
			GL11.glVertex2f(x2, y2);
		}

		GL11.glEnd();
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
