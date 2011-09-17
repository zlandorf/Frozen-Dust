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
	protected ISprite spriteGrounded;
	
	protected boolean coveredDist = false;
	protected float timeGrounded = IronConst.TIME_PROJECTILE_GROUNDED;
	protected float timeSpent = 0;
	
	protected int xdst, ydst;
	protected boolean missed;
	
	
	public BlindShotProjectile(IronWorld world, int x, int y, Vector2f vec,
			String spriteName, int xdst, int ydst, boolean missed) {
		super(world, x, y, vec, spriteName, IronConst.BLIND_PROJECTILE_SPEED);
		
		this.xdst = xdst;
		this.ydst = ydst;
		this.missed = missed;
		
		projectileName = spriteName;
		angle = _sprite.getAngle();
		
		scale = 1;
		
		spriteNormal = _sprite;
		spriteDown =  _spriteManager.getSprite(projectileName+"_down");
		spriteUp =  _spriteManager.getSprite(projectileName+"_up");
		spriteGrounded = _spriteManager.getSprite(projectileName+"_grounded");
			
		spriteNormal.setAngle(angle);
		spriteDown.setAngle(angle);
		spriteUp.setAngle(angle);
		
		_sprite  = spriteUp;
		distToCover = distanceToCover.length();
	}

	@Override
	public void update(float deltaTime) {
		
		if (!coveredDist) {
			float offx = deltaTime * speed * dir.getX();
			float offy = deltaTime * speed * dir.getY();
			
			_pos.set(_pos.getX() + offx, _pos.getY() + offy);
			distanceCovered.set(distanceCovered.getX() + Math.abs(offx), distanceCovered.getY() + Math.abs(offy));
			
			if (distanceCovered.getX() >= distanceToCover.getX() && distanceCovered.getY() >= distanceToCover.getY()) {
				coveredDist = true;
				if (!missed) {
					world.removeGameObject(this);
				} else {
					_sprite = spriteGrounded;
					setPos(xdst, ydst);
				}
				return;
			}
			
			float distCovered = distanceCovered.length();
			if (distToCover == 0) return;
			
			float percentDone = distCovered / distToCover;
			
			if (percentDone <= 0.33) {
				_sprite  = spriteUp;
				scale += scale * deltaTime * 1.65;
			} else if (percentDone <= 0.66){
				_sprite = spriteNormal;
			} else {
				_sprite = spriteDown;
				scale -= scale * deltaTime * 1.65;
			}
			_sprite.setScale(scale);
		} else { //we have covered the distance
			timeSpent += deltaTime;
			if (timeSpent >= timeGrounded) {
				world.removeGameObject(this);
			} else {
				if (timeSpent > timeGrounded / 2 && timeGrounded > 0) {
					float alpha = 1 - timeSpent / timeGrounded;
					alpha *= 2;
					_sprite.setAlpha(alpha);
				}
			}
		}
		
		if (_sprite.getAlpha() <= 0) {
			world.removeGameObject(this);
		}
	}
}
