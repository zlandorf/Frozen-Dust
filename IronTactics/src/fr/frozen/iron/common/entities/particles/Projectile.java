package fr.frozen.iron.common.entities.particles;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.GameObject;
import fr.frozen.game.Sound;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class Projectile extends GameObject {
	
	
	protected IronWorld world;
	protected Vector2f distanceCovered = new Vector2f(0,0);
	protected Vector2f distanceToCover;
	protected Vector2f dir;
	protected float speed;
	
	protected GameObject damageParticle;
	protected List<Sound> impactSounds;
	
	public Projectile(IronWorld world, int x, int y, Vector2f vec, 
					  String spriteName, GameObject damageParticle, List<Sound> impactSounds) {
		this(world, x, y, vec, spriteName, IronConst.PROJECTILE_SPEED, damageParticle, impactSounds);
	}
	
	public Projectile(IronWorld world, int x, int y, Vector2f vec, 
				      String spriteName, float speed, GameObject damageParticle, List<Sound> impactSounds) {
		super(null, x, y);
		this.world = world;
		this.speed = speed;
		
		_sprite = SpriteManager.getInstance().getSprite(spriteName);
		_sprite.setAngle((float)IronUtil.getAngle(vec));
		
		dir = new Vector2f();
		vec.normalise(dir);
		distanceToCover = new Vector2f(Math.max(0,Math.abs(vec.getX()) - Math.abs(dir.getX() * 16)),
									   Math.max(0,Math.abs(vec.getY()) - Math.abs(dir.getY() * 16)));
		
		this.damageParticle = damageParticle;
		this.impactSounds = impactSounds;
	}
	
	@Override
	public void update(float deltaTime) {
		float offx = deltaTime * speed * dir.getX();
		float offy = deltaTime * speed * dir.getY();
		
		_pos.set(_pos.getX() + offx, _pos.getY() + offy);
		distanceCovered.set(distanceCovered.getX() + Math.abs(offx), distanceCovered.getY() + Math.abs(offy));

		if (distanceCovered.getX() >= distanceToCover.getX() && distanceCovered.getY() >= distanceToCover.getY()) {
			world.removeGameObject(this);
			onRemoval();
			return;
		}
	}
	
	public void onRemoval() {
		if (damageParticle != null) {
			world.addGameObject(damageParticle, "gfx");
		}
		for (Sound impactSound : impactSounds) {
			if (impactSound != null) {
				impactSound.playAsSoundEffect(false);
			}
		}
	}
}
