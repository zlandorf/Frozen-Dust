package fr.frozen.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.frozen.game.TextureLoader;


public class XMLParser {

//	private static XMLParser instance = new XMLParser();
//
//	public static XMLParser getInstance() {
//		return instance;
//	}

	private Document doc;

	public XMLParser() {
	}
	
	public XMLParser(String filename) {
		parseXml(filename);
	}

	public void parseXml(String filename) {
		try {
			URL url = TextureLoader.class.getClassLoader().getResource(filename);
	        
	        if (url == null) {
	            throw new IOException("Cannot find: "+filename);
	        }
			
			//File fXmlFile = new File(url.toURI());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(filename)));
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getAttributeValue(String tagsPath, String attributeName) {
		//the path is like : unitStats/footsoldier/orc
		String [] tags = tagsPath.split("/");
		if (tags.length == 0) return null;

		boolean success = true;
		Element element;

		Node node = doc.getElementsByTagName(tags[0]).item(0);
		if (node == null) success = false;


		for (int i = 1; i < tags.length && success; i++) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) node;
				node = element.getElementsByTagName(tags[i]).item(0);
				if (node == null) success = false;
			} else {
				success = false;
			}
		}

		if (success && node != null && node.hasAttributes()) {
			NamedNodeMap attributes = node.getAttributes();
			node = attributes.getNamedItem(attributeName);

			if (node != null) {
				return node.getNodeValue();
			}
		}
		return null;
	}

	public Element getElement(String tagsPath) {
		String [] tags = ("root/"+tagsPath).split("/");
		if (tags.length == 0) return null;

		boolean success = true;
		Element element;

		Node node = doc.getElementsByTagName(tags[0]).item(0);
		if (node == null) success = false;

		for (int i = 1; i < tags.length && success; i++) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) node;
				node = element.getElementsByTagName(tags[i]).item(0);
				if (node == null) success = false;
			} else {
				success = false;
			}
		}
		
		if (success && node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			return (Element) node;
		}
		return null;
	}
	
	public static void main(String []args) {
		XMLParser ic = new XMLParser("assets/Data/iron.cfg");
		System.out.println("---------------");
		String orcMaxHp = ic.getAttributeValue("unitstats/footsoldier/orc", "maxhp");
		String elfweaponid = ic.getAttributeValue("unitstats/footsoldier/elf", "weaponid");
		System.out.println("orc max hp = "+orcMaxHp);
		System.out.println("elf weapon id = "+elfweaponid);
	}
}
