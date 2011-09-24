package fr.paul.game.test;

import fr.frozen.game.AnimationSequence;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.SpriteManager;

public class TestObject2 extends GameObject {
	AnimationSequence as;
	SpriteManager spriteManager;

	public TestObject2(GameState gs, int x, int y) {
		super(gs,x,y,SpriteManager.getInstance().getSprite("rebel"));
		spriteManager = SpriteManager.getInstance();
		as = spriteManager.getAnimationSequence("rebel_shot");
		as.start();
	}

	@Override
	public void render(float deltaTime) {
		as.getCurrentSprite().draw(_pos.x, _pos.y);
	}
	
	@Override
	public void update(float deltaTime) {
		as.update(deltaTime);
		
//		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
//			_pos.x = (float)((_pos.x - 600* GameEngine._tick) % Display.getDisplayMode().getWidth());
//			System.out.println("_pos.x "+_pos.x);
//			_pos.setLocation(Math.abs(_pos.x), _pos.y);
//		}
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
//			_pos.setLocation(_pos.x = (float)((_pos.x + 600* GameEngine._tick) % Display.getDisplayMode().getWidth()),
//					_pos.y);
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
//			_pos.setLocation(_pos.x,(float)((_pos.y + 600* GameEngine._tick) % Display.getDisplayMode().getHeight()));
//
//		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
//			_pos.setLocation(_pos.x,Math.abs((float)((_pos.y - 600* GameEngine._tick) % Display.getDisplayMode().getHeight())));
	}
}
