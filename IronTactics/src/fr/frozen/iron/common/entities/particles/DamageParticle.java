package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.Font;
import fr.frozen.game.FontManager;
import fr.frozen.game.GameObject;
import fr.frozen.iron.common.IronWorld;

public class DamageParticle extends GameObject {
	
	protected static float PARTICLE_DURATION = 2;//s
	protected static float SPEED = 10;
	
	protected IronWorld world;
	protected int value;
	protected String str;
	protected float timeToLive = PARTICLE_DURATION;
	protected Font font;
	
	public DamageParticle(IronWorld world, float x, float y, int value) {
		super(null, x, y);
		this.world = world;
		this.value = value;
		str = new Integer(value).toString();
		if (value > 0) {
			str = "+"+str;
		} else if (value == 0) {
			str = " "+str;
		}
		font = FontManager.getFont("DamageFont");
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
		if (value < 0) {
			font.setColor(0.8f, 0, 0);
		} else if (value == 0) {
			font.setColor(1, 1, 1);	
		} else {
			font.setColor(0, 0.8f, 0);
		}
		font.setAlpha(alpha);
		font.glPrint(str, _pos.getX(), _pos.getY());
	}
	
	@Override
	public String toString() {
		return "DAMAGE PARTICLE OF "+str+" damage";
	}
}
