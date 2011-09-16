package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.AnimatedObject;
import fr.frozen.game.GameState;
import fr.frozen.game.ISpriteManager;

public class HealAnimation extends AnimatedObject {

	public HealAnimation(GameState gs, float x, float y) {
		super(gs, x, y, ISpriteManager.getInstance().getAnimationSequence("healanimation"));
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (animation != null && animation.isAnimationEnded()) {
			_gameState.removeGameObject(this);
		}
	}
}
