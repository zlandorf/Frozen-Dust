package fr.frozen.game;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.lwjgl.openal.AL;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
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
	
	protected Hashtable<String, Audio> audioClips;

	public SoundManager() {
		audioClips = new Hashtable<String, Audio>();
	}
	
	protected synchronized void addAudioClip(Audio audioClip, String name) {
		if (audioClip != null) {
			audioClips.put(name.replaceAll("\\..{3,4}", ""), audioClip);
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
	
	public synchronized Audio getSound(String soundName) {
		Audio audioClip = audioClips.get(soundName);
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
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("failed to load "+filename);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String []args) {
		SoundManager.getInstance().loadSound("OGG","sword.ogg");
		SoundManager.getInstance().getSound("sword").playAsSoundEffect(1.0f, 1.0f, false);
		try {
			Thread.sleep(1000);
			SoundManager.getInstance().getSound("sword").playAsSoundEffect(1.0f, 1.0f, false);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		AL.destroy();
	}
}
