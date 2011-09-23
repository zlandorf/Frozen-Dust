package fr.paul.game.test;

import org.lwjgl.input.Keyboard;

import fr.frozen.game.AnimationSequence;
import fr.frozen.game.GameObject;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.ISpriteManager;

public class TestShooter extends GameObject {
	private AnimationSequence fire;
	private AnimationSequence reload;
	private ISprite idle;
	
	private int ammo = 5;
	
	private float scale = 0;
	private float scaletmp = 0;
	
	public TestShooter(GameState gs, int x, int y) {
		super(gs,x,y,null);
		fire = ISpriteManager.getInstance().getAnimationSequence("rebel_fire");
		reload = ISpriteManager.getInstance().getAnimationSequence("rebel_reload");
		
		_sprite = idle =  ISpriteManager.getInstance().getSprite("rebel_fire_idle");
	
	}

	@Override
	public void render(float deltaTime) {
		if (reload.animating()) {
			_sprite = reload.getCurrentSprite();
		} else if (fire.animating()) {
			_sprite = fire.getCurrentSprite();
		} else {
			_sprite = idle;
		}
		_sprite.setScale(1 + scale);
		super.render(deltaTime);
	}
	
	@Override
	public void update(float deltaTime) {
		
		scaletmp += deltaTime;
		scaletmp %= 2;
		
		if (scaletmp > 1) {
			scale = 1 - (scaletmp % 1);
		} else {
			scale = scaletmp;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (!reload.animating() && !fire.animating() && ammo > 0) {
				fire.start();
				ammo--;
			}
		}
		
		if (ammo == 0 && !reload.animating() && !fire.animating()) {
			reload.start();
		}
		
		if (fire.animating()) {
			fire.update(deltaTime);
		}
		if (reload.animating()) {
			reload.update(deltaTime);
			ammo = 5;
		}
		
	}
}
