package fr.frozen.iron.common.entities.particles;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.AnimatedObject;
import fr.frozen.game.GameState;
import fr.frozen.game.ISprite;
import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class Bird extends AnimatedObject {
	protected static int BIRD_SPEED = 100; 
	protected Vector2f dir;
	protected float angle = 0;
	
	public Bird(GameState gs) {
		super(gs,0, 0, ISpriteManager.getInstance().getAnimationSequence("bird_fly"));
		init();
	}
	
	private void init() {
		boolean vertical = System.nanoTime() % 2 == 1;
		int xstart,ystart,xend, yend;
		
		if (vertical) {
			ystart = System.nanoTime() % 2 == 1 ? IronConst.MAP_HEIGHT* IronConst.TILE_HEIGHT : -32;
			yend = ystart == -32 ? IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT : -32;
			
			xstart = (int)((Math.random() * IronConst.MAP_WIDTH) * IronConst.TILE_WIDTH);
			xend = (int)((Math.random() * IronConst.MAP_WIDTH) * IronConst.TILE_HEIGHT);
		} else {
			xstart = System.nanoTime() % 2 == 1 ? IronConst.MAP_WIDTH * IronConst.TILE_WIDTH : -32;
			xend = xstart == -32 ? IronConst.MAP_WIDTH * IronConst.TILE_WIDTH : -32;
			
			ystart = (int)((Math.random() * IronConst.MAP_HEIGHT) * IronConst.TILE_WIDTH);
			yend = (int)((Math.random() * IronConst.MAP_HEIGHT) * IronConst.TILE_HEIGHT);
		}
		
		Vector2f vec = new Vector2f(xend - xstart, yend - ystart); 
		angle = (float)IronUtil.getAngle(vec);
		dir = new Vector2f();
		vec.normalise(dir);

		setPos(xstart, ystart);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		_pos.setX(_pos.getX() + dir.getX() * BIRD_SPEED * deltaTime);
		_pos.setY(_pos.getY() + dir.getY() * BIRD_SPEED * deltaTime);
		
		if (getX() <=  -32 || getX() > 32 + IronConst.MAP_WIDTH * IronConst.TILE_WIDTH 
			|| getY() <=  -32 || getY() > 32 + IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT) {
			_gameState.removeGameObject(this);
		}
	}
	
	@Override
	public void render(float deltaTime) {
		ISprite sprite = animation.getCurrentSprite();
		sprite.setAngle(angle);
		sprite.draw(getX(), getY());
	}
}
