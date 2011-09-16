package fr.frozen.iron.common.entities.particles;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.ISprite;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.util.IronConst;

public class BlindShotProjectile extends Projectile {

	protected String projectileName;
	protected float angle;
	protected float distToCover;
	protected float scale;
	
	protected ISprite spriteNormal;
	protected ISprite spriteDown;
	protected ISprite spriteUp;
	
	public BlindShotProjectile(IronWorld world, int x, int y, Vector2f vec,
			String spriteName) {
		super(world, x, y, vec, spriteName, IronConst.BLIND_PROJECTILE_SPEED);
		projectileName = spriteName;
		angle = _sprite.getAngle();
		
		scale = 1;
		
		spriteNormal = _sprite;
		spriteDown =  _spriteManager.getSprite(projectileName+"_down");
		spriteUp =  _spriteManager.getSprite(projectileName+"_up");
			
		spriteNormal.setAngle(angle);
		spriteDown.setAngle(angle);
		spriteUp.setAngle(angle);
		
		_sprite  = spriteUp;
		distToCover = distanceToCover.length();
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		float distCovered = distanceCovered.length();
		if (distToCover == 0) return;
		
		float percentDone = distCovered / distToCover;
		
		if (percentDone <= 0.33) {
			_sprite  = spriteUp;
			scale += scale * deltaTime * 1.5;
		} else if (percentDone <= 0.66){
			_sprite = spriteNormal;
		} else {
			_sprite = spriteDown;
			scale -= scale * deltaTime * 1.5;
		}
		_sprite.setScale(scale);
		System.out.println("scale = "+scale);
	}
}
