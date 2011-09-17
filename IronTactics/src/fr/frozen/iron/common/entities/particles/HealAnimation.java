package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.GameState;
import fr.frozen.game.ISpriteManager;

public class HealAnimation extends AnimatedParticle {

	public HealAnimation(GameState gs, float x, float y) {
		super(gs, x, y, ISpriteManager.getInstance().getAnimationSequence("healanimation"));
	}
}
