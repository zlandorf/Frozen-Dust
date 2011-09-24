package fr.frozen.game;

import org.newdawn.slick.openal.Audio;

public class Sound {
	
	protected Audio audioClip;
	protected SoundManager soundManager;
	protected float gain = 1.f;
	protected float pitch = 1.f;
	
	public Sound(Audio audioClip, SoundManager soundManager) {
		this.audioClip = audioClip;
		this.soundManager = soundManager;
	}
	
	public float getGain() {
		return gain;
	}

	public void setGain(float gain) {
		this.gain = gain;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public Audio getAudioClip() {
		return audioClip;
	}
	
	public boolean isPlaying() {
		return audioClip.isPlaying();
	}
	
	public void stop() {
		audioClip.stop();
	}
	
	public void playAsSoundEffect(boolean loop) {
		audioClip.playAsSoundEffect(soundManager.getGlobalPitch() * getPitch(), soundManager.getGlobalGain() * getGain(), loop);
	}
	
	public void playAsMusic(boolean loop) {
		audioClip.playAsMusic(soundManager.getGlobalPitch() * getPitch(), soundManager.getGlobalGain() * getGain(), loop);
	}
	
}
