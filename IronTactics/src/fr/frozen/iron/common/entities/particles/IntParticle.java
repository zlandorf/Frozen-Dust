package fr.frozen.iron.common.entities.particles;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameObject;
import fr.frozen.iron.common.IronWorld;

public class IntParticle extends GameObject {
	
	protected static float PARTICLE_DURATION = 2;//s
	protected static float SPEED = 10;
	
	protected IronWorld world;
	protected int value;
	protected String str;
	protected float timeToLive = PARTICLE_DURATION;
	protected Color negColor;
	protected Color posColor;
	
	protected Font damageFont;
	
	protected int width;
	protected int height;
	
	public IntParticle(IronWorld world, float x, float y, int value, int negColor, int posColor) {
		super(null, x, y);
		this.world = world;
		this.value = value;
		str = new Integer(value).toString();
		if (value > 0) {
			str = "+"+str;
		} else if (value == 0) {
			str = " "+str;
		}
		damageFont = FontManager.getFont("DamageFont");
		width = damageFont.getWidth(str);
		height = damageFont.getHeight(str);
		
		setPosWithNoCollision((int)(getX() -  width / 2), (int)getY());
		
		this.negColor = new Color(negColor);
		this.posColor = new Color(posColor);
	}
	
	private void setPosWithNoCollision(int x, int y) {
		setPos(x, y);
		if (world.getGameObjectCollection("gfx") == null) return;
		
		for (GameObject go : world.getGameObjectCollection("gfx")) {
			if (this.equals(go)) continue;
			if (go instanceof IntParticle) {
				IntParticle otherParticle = (IntParticle) go;
				if (collides(otherParticle)) {
					int offY = (int)(otherParticle.height - Math.abs(getY() - otherParticle.getY()));
					otherParticle.setPos((int)otherParticle.getX(),
										(int)(otherParticle.getY() - offY /2));
					
					setPos((int)getX(),(int)(getY() + offY /2));
					break;
				}
			}
		}
		
	}
	
	private boolean collides(IntParticle ip) {
		int x1, y1, w1, h1;
		int x2, y2, w2, h2;
		
		x1 = (int)getX();
		y1 = (int)getY();
		
		x2 = (int)ip.getX();
		y2 = (int)ip.getY();
		
		w1 = width;
		h1 = height;
		
		w2 = ip.width;
		h2 = ip.height;
		
		if ((x2 >= x1 + w1)
		    || (x2 + w2 <= x1)
			|| (y2 >= y1 + h1)
			|| (y2 + h2 <= y1)) {
			return false; 
		}
		
		return true;
	}
	
	@Override
	public void update(float deltaTime) {
		timeToLive -= deltaTime;
		if (timeToLive <= 0) {
			world.removeGameObject(this);
			return;
		}
		
		_pos.setY(_pos.getY() - deltaTime * SPEED);
	}
	
	@Override
	public void render(float deltaTime) {
		float alpha = Math.max(0, Math.min(timeToLive, 1));
		Color colortmp;
		if (value < 0) {
			colortmp = negColor;
		} else if (value == 0) {
			colortmp = Color.white;
		} else {
			colortmp = posColor;
		}
		Color color = new Color(colortmp.getRed(), colortmp.getGreen(), colortmp.getBlue(), (int)(alpha * 255));
		
		damageFont.drawString(_pos.getX(), _pos.getY(), str, color);
	}
	
	@Override
	public String toString() {
		return "DAMAGE PARTICLE OF "+str+" damage";
	}
}
