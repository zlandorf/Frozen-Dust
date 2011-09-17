package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.AnimatedObject;
import fr.frozen.game.AnimationSequence;
import fr.frozen.game.GameState;

public class AnimatedParticle extends AnimatedObject {
	
	
	public AnimatedParticle(GameState gs, float x, float y,
			AnimationSequence sequence) {
		super(gs, x, y, sequence);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (animation != null && animation.isAnimationEnded()) {
			_gameState.removeGameObject(this);
		}
	}
}
