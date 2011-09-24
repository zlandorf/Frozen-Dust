package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.GameState;
import fr.frozen.game.SpriteManager;

public class ShieldBlockAnimation extends AnimatedParticle {
	public ShieldBlockAnimation(GameState gs, float x, float y) {
		super(gs, x, y, SpriteManager.getInstance().getAnimationSequence("shieldblockanimation"));
	}
	
	@Override
	public void update(float deltaTime) {
		if (animation == null) return;
		
		animation.update(deltaTime);
		if (animation.isAnimationEnded()) {
			animation.getCurrentSprite().setAlpha(Math.max(0, animation.getCurrentSprite().getAlpha() - deltaTime * 1.5f));
			if (animation.getCurrentSprite().getAlpha() <= 0) {
				_gameState.removeGameObject(this);
			}
		}
	}
}
