package fr.frozen.game;

import org.lwjgl.util.vector.Vector2f;

public class GameObject {
	protected Vector2f _pos;
	protected ISprite _sprite = null;
	protected ISpriteManager _spriteManager;
	
	protected GameState _gameState;
	//animated object = sequence of sprites ?
	
	public GameObject(GameState gs) {
		this(gs,0,0);
	}
	
	public GameObject(GameState gs, float x, float y) {
		this(gs, x, y, null);
	}
	
	public GameObject(GameState gs, float x, float y, ISprite sprite) {
		
		_pos = new Vector2f(x, y);
		_gameState = gs;
		_sprite = sprite;
		_spriteManager = ISpriteManager.getInstance();
	}
	
	public Vector2f getPos() {
		return _pos;
	}

	public float getX() {
		return getPos().getX();
	}
	
	public float getY() {
		return getPos().getY();
	}
	
	public void setPos(int x, int y) {
		_pos.setX(x);
		_pos.setY(y);
	}
	
	public ISprite getSprite() {
		return _sprite;
	}
	
	public void setSprite(ISprite sprite) {
		_sprite = sprite;
	}
	
	public void update(float deltaTime) {
	}
	
	public void render(float deltaTime) {
		if (_sprite != null) {
			_sprite.draw(_pos.getX(),_pos.getY());
		}
	}
	
	public String toString() {
		return "pos x = "+_pos.getX()+"  y = "+_pos.getY(); 
	}
}
