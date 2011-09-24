package fr.paul.game.test;

import org.lwjgl.input.Keyboard;

import fr.frozen.game.AnimationSequence;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.SpriteManager;

public class TestObject extends GameObject {
	AnimationSequence asRunning;
	SpriteManager spriteManager;
	
	public TestObject(GameState gs, int x, int y, float scale) {
		super(gs,x,y,null);
		spriteManager = SpriteManager.getInstance();
		asRunning = spriteManager.getAnimationSequence("rebel_running");
		
		_sprite = spriteManager.getSprite("rebel");
		_sprite.setScale(scale);
	}

	@Override
	public void render(float deltaTime) {
		if (asRunning.animating())
			asRunning.getCurrentSprite().draw(_pos.x, _pos.y);
		else 
			super.render(deltaTime);
	}
	
	@Override
	public void update(float deltaTime) {
		asRunning.update(deltaTime);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			/*_pos.x = (float)((_pos.x - 600* GameEngine._tick) % Display.getDisplayMode().getWidth());
			_pos.setLocation(Math.abs(_pos.x), _pos.y);*/
			if (!asRunning.animating()) {
				asRunning.start();
			}
			asRunning.update(deltaTime);
		} else {
			asRunning.stop();
		}

		_sprite.setAngle(_sprite.getAngle() + 1);
		//_sprite.setScale(1 + deltaTime);
		
		/*if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			_pos.setLocation(_pos.x = (float)((_pos.x + 600* GameEngine._tick) % Display.getDisplayMode().getWidth()),
					_pos.y);

		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			_pos.setLocation(_pos.x,(float)((_pos.y + 600* GameEngine._tick) % Display.getDisplayMode().getHeight()));

		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			_pos.setLocation(_pos.x,Math.abs((float)((_pos.y - 600* GameEngine._tick) % Display.getDisplayMode().getHeight())));
			*/
	}
}
