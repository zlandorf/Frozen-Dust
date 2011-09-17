package fr.frozen.iron.common.entities.particles;

import fr.frozen.game.GameState;
import fr.frozen.game.ISpriteManager;

public class ShieldBlockAnimation extends AnimatedParticle {
	public ShieldBlockAnimation(GameState gs, float x, float y) {
		super(gs, x, y, ISpriteManager.getInstance().getAnimationSequence("healanimation"));
		//TODO : add graphic animation for shield block on spritesheet
	}
}
