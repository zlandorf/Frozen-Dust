package fr.frozen.game;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.lwjgl.openal.AL;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.openal.SoundStore;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.frozen.util.XMLParser;

public class SoundManager {
	
	private static final String SOUNDS_DIRECTORY = "sounds/";
	
	private static SoundManager instance = null;
	public static SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}
		return instance;
	}
	
	protected Hashtable<String, Sound> audioClips;
	
	protected SoundManager() {
		audioClips = new Hashtable<String, Sound>();
	}
	
	public float getMusicVolume() {
		return SoundStore.get().getMusicVolume();
	}
	
	public float getSoundVolume() {
		return SoundStore.get().getSoundVolume();
	}

	public void setMusicVolume(float globalVolume) {
		SoundStore.get().setMusicVolume(globalVolume);
	}
	
	public void setSoundVolume(float globalVolume) {
		SoundStore.get().setSoundVolume(globalVolume);
	}

	public void setMusicPitch(float globalPitch) {
		SoundStore.get().setMusicPitch(globalPitch);
	}
	
	public void setMusicOn(boolean on) {
		SoundStore.get().setMusicOn(on);
	}
	
	public void setSoundsOn(boolean on) {
		SoundStore.get().setSoundsOn(on);
	}

	protected synchronized void addAudioClip(Audio audioClip, String name) {
		if (audioClip != null) {
			audioClips.put(name.replaceAll("\\..{3,4}",""), new Sound(audioClip));
			Logger.getLogger(getClass()).debug("sound added : "+name);
		}		
	}
	
	public void loadSound(String type, String filename) {
		try {
			Audio audioClip = AudioLoader.getAudio(type, getClass().getClassLoader().getResourceAsStream(SOUNDS_DIRECTORY+filename));
			addAudioClip(audioClip, filename);
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("filename could not be loaded");
		}
	}
	
	public synchronized Sound getSound(String soundName) {
		Sound audioClip = audioClips.get(soundName);
		return audioClip;
	}
	
	public boolean loadSoundsFromXml(String filename) {
		return loadSoundsFromXml(new XMLParser(filename));
	}
	
	public boolean loadSoundsFromXml(XMLParser parser) {
		Element soundsNode = parser.getElement("assets/sounds");
		boolean success = true;
		
		if (soundsNode != null && soundsNode.hasChildNodes()) {
			for (int i = 0; i < soundsNode.getChildNodes().getLength(); i++) {
				if (soundsNode.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE) continue;
				success &= getSoundFromNode(soundsNode.getChildNodes().item(i));
			}
		}
		
		String soundsAdded = "sounds added = ";
		soundsAdded += getArrayStr(audioClips.keySet().toArray());
		
		Logger.getLogger(getClass()).info(soundsAdded);
		
		if (success) {
			Logger.getLogger(getClass()).info("XML sound Loading success");
		} else {
			Logger.getLogger(getClass()).error("XML sound Loading failure");
		}
		
		return success;
	}
	
	protected String getArrayStr(Object[] array) {
		String str = "[";
		for (int i = 0; i < array.length; i++) {
			str += array[i];
			if (i < array.length - 1) {
				str += ",";
			}
		}
		return str+"]";
	}
	
	protected boolean getSoundFromNode(Node node) {
		if (node == null || !node.hasAttributes()) {
			return false;
		}
		String filename, name;
		NamedNodeMap attributes = node.getAttributes();
		
		Node attrnode = attributes.getNamedItem("filename");
		if (attrnode == null) {
			Logger.getLogger(getClass()).error("attribute not found : filename");
			return false;
		}
		filename = attrnode.getNodeValue();
		
		attrnode = attributes.getNamedItem("name");
		if (attrnode == null) {
			Logger.getLogger(getClass()).error("attribute not found : name");
			return false;
		}
		name = attrnode.getNodeValue();
		
		if (filename == null || filename.equals("") || name == null || name.equals("")) {
			Logger.getLogger(getClass()).error("filename or name have a bad value in getSoundFromNode");
			return false;
		}
		
		String type = filename.substring(filename.lastIndexOf(".") + 1);
		if (type == null || type.equals("")) {
			Logger.getLogger(getClass()).error("sound type not found in "+filename);
			return false;
		}
		
		type = type.toUpperCase();
		
		Logger.getLogger(getClass()).debug("soundfile = "+filename+ "  audio type = "+type+"  name = "+name);
		try {
			Audio audioClip = AudioLoader.getAudio(type, getClass().getClassLoader().getResourceAsStream(SOUNDS_DIRECTORY+filename));
			addAudioClip(audioClip, name);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("failed to load "+filename);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String []args) {
		SoundManager.getInstance().loadSound("OGG","sword.ogg");
		//SoundManager.getInstance().getSound("sword").playAsSoundEffect(false);
		Sound sound = SoundManager.getInstance().getSound("sword");
		//SoundManager.getInstance().seSoundsOn(false);
		for (int i = 0; i < 5; i++) {
			try {
				SoundManager.getInstance().setSoundVolume(0.0f);
				//SoundManager.getInstance().getSound("sword").playAsSoundEffect(false);
				//sound.setGain(0.5f);
				//sound.getAudioClip().playAsMusic(1.f,1.f,false);
				Thread.sleep(200);
				sound.setGain(1f);
				sound.playAsSoundEffect(false);
				sound.playAsSoundEffect(false);
				sound.playAsSoundEffect(false);
				sound.playAsSoundEffect(false);
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		AL.destroy();
	}
}
