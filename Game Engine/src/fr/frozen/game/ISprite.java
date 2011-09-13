package fr.frozen.game;

public interface ISprite extends Cloneable {
	public float getWidth();
	public float getHeight();
	public Texture getTexture();
	public void setWidth(int val);
	public void setHeight(int val);
	public void setColor(int color);
	public void setMirrorX(boolean val);
	public void setMirrorY(boolean val);
	
	public boolean isMirrorX();
	public boolean isMirrorY();
	public void setAlpha(float val);
	public float getAlpha();
	
	public void setAngle(float val);
	public float getAngle();
	
	public void draw(float x, float y);
	public void draw(float x, float y, boolean mirrorX, boolean mirrorY);
	public void draw(float x, float y, float w, float h);
	public void draw(float x, float y, float w, float h, boolean mirrorX, boolean mirrorY);
	
	//only with a texture that takes up the whole image, it will not cycle properly otherwise
	public void fillIn(float xstart, float ystart, float xend, float yend);
	public void fillIn(float xstart, float ystart, float xend, float yend, boolean mirX, boolean mirY);
}
