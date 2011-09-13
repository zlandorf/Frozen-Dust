package fr.frozen.game;

public class AnimatedObject extends GameObject {

	protected AnimationSequence animation;
	
	public AnimatedObject(GameState gs) {
		this(gs,0,0);
	}

	public AnimatedObject(GameState gs, float x, float y) {
		this(gs,x,y, null);
	}
	
	public AnimatedObject(GameState gs, float x, float y, AnimationSequence sequence) {
		super(gs,x,y);
		animation = sequence;
		if (animation != null) animation.start();
	}
	
	public void update(float deltaTime) {
		if (animation != null) animation.update(deltaTime);
	}
	
	public void render(float deltaTime) {
		if (animation != null && animation.getCurrentSprite() != null) {
			animation.getCurrentSprite().draw(_pos.getX(),_pos.getY());
		}
	}
	
}
