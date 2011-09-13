package fr.frozen.game;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class SpriteImpl implements ISprite {
	private Texture texture;
	private Vector2f texPos;
	private Vector2f offSet;
	
	private float width;
	private float height;
	private float alpha = 1;

	private boolean mirrorX = false;
	private boolean mirrorY = false;
	
	private float r = 1;
	private float g = 1;
	private float b = 1;
	
	private float angle = 0;
	
	public SpriteImpl(Texture texture, Vector2f texturePos, Vector2f offSet) {
		this.texture = texture;
		width = texture.getImageWidth();
		height = texture.getImageHeight();
		texPos = new Vector2f(texturePos.x,texturePos.y);
		if (offSet == null) {
			this.offSet = new Vector2f(0,0);
		} else {
			this.offSet = offSet;
		}
	}
	
	public SpriteImpl(Texture texture) {
		this(texture,new Vector2f(0,0), new Vector2f(0,0));
	}

	public SpriteImpl(SpriteImpl sprite) {
		//TODO see if there is not a better way to do this
		this.texture = sprite.texture;
		this.width = sprite.width;
		this.height = sprite.height;
		this.mirrorX = sprite.mirrorX;
		this.mirrorY = sprite.mirrorY;
		this.texPos = new Vector2f(sprite.texPos.x,sprite.texPos.y);
		this.offSet = new Vector2f(sprite.offSet.getX(), sprite.offSet.getY());
		
		this.r = sprite.r;
		this.g = sprite.g;
		this.b = sprite.b;
		
		this.alpha = sprite.alpha;

		this.angle = sprite.angle;
	}
	
	@Override
	public Texture getTexture() {
		return texture;
	}
	
	@Override
	public float getWidth() {
		return width;
	}
	
	public void setColor(int color) {
		b = color & 0xff;
		b = b / 0xff;
		
		color >>= 8;
		g = color & 0xff;
		g = g / 0xff;
		
		color >>= 8;
		r = color & 0xff;
		r = r / 0xff;
	}

	@Override
	public void setAlpha(float val) {
		if (val < 0 || val > 1) {
			System.err.println("BAD ALPHA VALUE : "+val);
			//TODO : remplacer par un assert ou un throw ?
			return;
		}
		alpha = val;
	}
	
	@Override
	public float getAngle() {
		return angle;
	}
	
	@Override
	public void setAngle(float val) {
		angle = val;
	}

	@Override
	public boolean isMirrorX() {
		return mirrorX;
	}

	@Override
	public boolean isMirrorY() {
		return mirrorY;
	}

	@Override
	public void setMirrorX(boolean val) {
		mirrorX = val;
	}

	@Override
	public void setMirrorY(boolean val) {
		mirrorY = val;
	}
	
	@Override
	public float getAlpha() {
		return alpha;
	}
	
	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public void setWidth(int val) {
		width = val;
	}
	
	@Override
	public void setHeight(int val) {
		height = val;
	}
	
	@Override
	public void draw(float x, float y) {
		draw(x, y, width, height, mirrorX, mirrorY);
	}
	
	@Override
	public void draw(float x, float y, float w, float h) {
		draw(x, y, w, h, mirrorX, mirrorY);
	}
	
	@Override
	public void draw(float x, float y, boolean mirX, boolean mirY) {
		draw(x, y, width, height, mirX, mirY);
	}
	
	@Override
	public void draw(float x, float y, float w, float h, boolean mirX, boolean mirY) {
		// store the current model matrix
		GL11.glPushMatrix();
		// bind to the appropriate texture for this sprite
		texture.bind();
    
		// translate to the right location and prepare to draw
		GL11.glTranslatef((int)x + (int)offSet.getX(), (int)y + (int)offSet.getY(), 0);
		GL11.glTranslatef(width / 2, height / 2, 0);
		GL11.glRotatef(angle, 0, 0, 1);
		GL11.glTranslatef(- width / 2, - height / 2, 0);
		//GL11.glTranslatef((int)(x - w / 2), ((int)y - h), 0);
    	GL11.glColor4f(r,g,b,alpha);
		// draw a quad textured to match the sprite
    	GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(mirX ? texture.getWidth() + texPos.x : texPos.x, mirY ? texture.getHeight() + texPos.y: texPos.y);
			GL11.glVertex2f(0, 0);
			GL11.glTexCoord2f(mirX ? texture.getWidth() + texPos.x: texPos.x, mirY ?  texPos.y : texture.getHeight() + texPos.y);
			GL11.glVertex2f(0, (int)h);
			GL11.glTexCoord2f(mirX ? texPos.x : texture.getWidth() + texPos.x, mirY ?  texPos.y : texture.getHeight() + texPos.y);
			GL11.glVertex2f((int)w,(int)h);
			GL11.glTexCoord2f(mirX ? texPos.x : texture.getWidth() + texPos.x, mirY ? texture.getHeight() + texPos.y : texPos.y);
			GL11.glVertex2f((int)w,0);
			
		}
		GL11.glEnd();
		GL11.glPopMatrix();
	}
	
	
	
	@Override
	public void fillIn(float xstart, float ystart, float xend, float yend) {
		fillIn(xstart, ystart, xend, yend, mirrorX, mirrorY);
	}
		
	@Override
	public void fillIn(float xstart, float ystart, float xend, float yend, boolean mirX, boolean mirY) {
		// store the current model matrix
		float repeatX = Math.abs(xend - xstart) / width;
		float repeatY = Math.abs(yend - ystart) / height;
		
		GL11.glPushMatrix();
		// bind to the appropriate texture for this sprite
		texture.bind();
    	GL11.glColor4f(r,g,b,alpha);
    	GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(mirX ? repeatX : 0, mirY ? repeatY : 0);
			GL11.glVertex2f((int)xstart, (int)ystart);
			
			GL11.glTexCoord2f(mirX ? repeatX : 0, mirY ?  0 : repeatY);
			GL11.glVertex2f((int)xstart, (int)yend);
			
			GL11.glTexCoord2f(mirX ? 0 : repeatX, mirY ?  0 : repeatY);
			GL11.glVertex2f((int)xend,(int)yend);
			
			GL11.glTexCoord2f(mirX ? 0 : repeatX, mirY ? repeatY : 0);
			GL11.glVertex2f((int)xend,(int)ystart);
		}
		GL11.glEnd();
		GL11.glPopMatrix();
	}
}
