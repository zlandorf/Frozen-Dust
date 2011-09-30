package fr.frozen.iron.common.entities.particles;

import org.newdawn.slick.Color;

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
	
	public IntParticle(IronWorld world, float x, float y, int value, Color negColor, Color posColor) {
		super(null, x, y);
		this.world = world;
		this.value = value;
		str = new Integer(value).toString();
		if (value > 0) {
			str = "+"+str;
		} else if (value == 0) {
			str = " "+str;
		}
		System.out.println("value = "+value);
		this.negColor = negColor;
		this.posColor = posColor;
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
		Color color = new Color(colortmp.getRed(), colortmp.getGreen(), colortmp.getBlue(), alpha);
		
		FontManager.getFont("DamageFont").drawString(_pos.getX(), _pos.getY(), str, color);
	}
	
	@Override
	public String toString() {
		return "DAMAGE PARTICLE OF "+str+" damage";
	}
}
