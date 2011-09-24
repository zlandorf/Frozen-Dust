package fr.frozen.game;

import org.newdawn.slick.openal.Audio;

public class Sound {
	
	protected Audio audioClip;
	protected SoundManager soundManager;
	
	public Sound(Audio audioClip, SoundManager soundManager) {
		this.audioClip = audioClip;
		this.soundManager = soundManager;
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
		audioClip.playAsSoundEffect(soundManager.getGlobalPitch(), soundManager.getGlobalGain(), loop);
	}
	
	public void playAsMusic(boolean loop) {
		audioClip.playAsMusic(soundManager.getGlobalPitch(), soundManager.getGlobalGain(), loop);
	}
	
}
