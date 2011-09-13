package fr.frozen.iron.common.entities.particles;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.GameObject;
import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.util.IronUtil;

public class Projectile extends GameObject {
	protected static float SPEED = 45;
	
	protected IronWorld world;
	protected Vector2f distanceCovered = new Vector2f(0,0);
	protected Vector2f distanceToCover;
	protected Vector2f dir;
	
	public Projectile(IronWorld world, int x, int y, Vector2f vec, String spriteName) {
		super(null, x, y);
		this.world = world;

		
		/*double angle = IronUtil.getAngle(vec, new Vector2f(0,-1));
		if (vec.getX() < 0) angle *= -1;
		
		angle = Math.toDegrees(angle);*/
		_sprite = ISpriteManager.getInstance().getSprite(spriteName);
		_sprite.setAngle((float)IronUtil.getAngle(vec));
		
		dir = new Vector2f();
		vec.normalise(dir);
		//dir = new Vector2f((float)(vec.getX() / distance), (float)(vec.getY() / distance));//makes dir a director vector
		distanceToCover = new Vector2f(Math.max(0,Math.abs(vec.getX()) - Math.abs(dir.getX() * 16)),
									   Math.max(0,Math.abs(vec.getY()) - Math.abs(dir.getY() * 16)));
	}
	
	@Override
	public void update(float deltaTime) {
		float offx = deltaTime * SPEED * dir.getX();
		float offy = deltaTime * SPEED * dir.getY();
		
		_pos.set(_pos.getX() + offx, _pos.getY() + offy);
		distanceCovered.set(distanceCovered.getX() + Math.abs(offx), distanceCovered.getY() + Math.abs(offy));

		if (distanceCovered.getX() >= distanceToCover.getX() && distanceCovered.getY() >= distanceToCover.getY()) {
			world.removeGameObject(this);
			return;
		}
	}
}
