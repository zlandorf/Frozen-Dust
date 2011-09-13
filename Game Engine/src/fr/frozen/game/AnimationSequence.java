package fr.frozen.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

public class AnimationSequence {
	private String _name;
	private List<AnimationFrame> _frames;
	private int _currentFrame;
	private boolean _loop;
	private boolean _animating;
	private long _lastTime;
	
	public AnimationSequence(AnimationSequence sequence) {
		this._name = sequence._name;
		this._frames = new ArrayList<AnimationFrame>();
		for (AnimationFrame frame : sequence._frames) {
			_frames.add(new AnimationFrame(frame));
		}
		this._currentFrame = sequence._currentFrame;
		this._loop = sequence._loop;
		this._animating = sequence._animating;
		this._lastTime = sequence._lastTime;
	}
	
	public AnimationSequence(String name, boolean loop) {
		_name = name;
		_frames = new ArrayList<AnimationFrame>();
		_currentFrame = 0;
		_loop = loop;
		_animating = false;
	}
	
	public List<AnimationFrame> getFrames() {
		return _frames;
	}
	
	public void setLoop(boolean val) {
		_loop = val;
	}
	
	public boolean isLooping() {
		return _loop;
	}
	
	public void createFramesFromSpriteSheet(String sheetName, int nbFrames, Vector2f start, Vector2f []dim, Vector2f []offSets, int duration) {
		createFramesFromSpriteSheet(ISpriteManager.getInstance().getSprite(sheetName),
									nbFrames, start, dim, offSets, duration);
	}
	
	public void createFramesFromSpriteSheet(ISprite sheet, int nbFrames, Vector2f start, Vector2f []dim, Vector2f []offSets, int duration) {
		//TRYING FOR STRIP HERE !!
		AnimationFrame frame;
		for (int i = 0; i < nbFrames; i++) {
			frame = new AnimationFrame(ISpriteManager.getInstance().getSubSprite(sheet, start, dim[i], offSets[i]), duration);
			_frames.add(frame);
			start.setX(dim[i].getX() + start.getX());
		}
	}
	
	public void start() {
		_lastTime = System.currentTimeMillis();
		_currentFrame = 0;
		_animating = true;
	}
	
	public boolean animating() {
		return _animating;
	}
	
	public void stop() {
		_animating = false;
	}
	
	public void update(float deltaTime) {
		if (!_animating) return;
		
		long time = System.currentTimeMillis();
		long timePassed = time - _lastTime;
		_lastTime = time;
		
		while (timePassed > _frames.get(_currentFrame).getDuration()) {
			timePassed -= _frames.get(_currentFrame).getDuration();
			if (_currentFrame == _frames.size() - 1 && !_loop) {
				//notify that animation sequence is ended
				stop();
				return;
			}
			_currentFrame++;
			_currentFrame %= _frames.size();
		}
		_lastTime -= timePassed;//extra time must be counted for next turn
		
	}
	
	public ISprite getCurrentSprite() {
		return _frames.get(_currentFrame).getSprite();
	}
}
