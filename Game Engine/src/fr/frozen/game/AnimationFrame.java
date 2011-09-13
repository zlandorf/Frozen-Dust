package fr.frozen.game;

public class AnimationFrame {
	private ISprite _sprite;
	private int _duration;
	
	public AnimationFrame(ISprite sprite, int duration) {
		_sprite = sprite;
		_duration = duration;
	}
	
	public AnimationFrame(AnimationFrame af) {
		this._sprite = new SpriteImpl((SpriteImpl)af._sprite);
		this._duration = af._duration;
	}
	
	public ISprite getSprite() {
		return _sprite;
	}

	public int getDuration() {
		return _duration;
	}
}