package fr.frozen.game;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.util.vector.Vector2f;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.frozen.util.XMLParser;

public class SpriteManagerImpl extends ISpriteManager {
	
	private static final String IMAGES_DIRECTORY = "images/";
	
	private HashMap<String, ISprite> sprites;
	private HashMap<String, AnimationSequence> animations;
	
	public SpriteManagerImpl() {
		sprites = new HashMap<String, ISprite>();
		animations = new HashMap<String, AnimationSequence>();
	}
	
	public boolean isSpriteLoaded(String spritename) {
		return sprites.get(spritename) != null;
	}
	
	public boolean isAnimationLoaded(String animationName) {
		return animations.get(animationName) != null;
	}
	
	public boolean loadSprite(String filename, String spritename) {
		try {
			Texture tex = TextureLoader.getInstance().getTexture(IMAGES_DIRECTORY+filename);
			addSprite(spritename, new SpriteImpl(tex));
			return true;
		} catch (IOException e) {
			System.err.println("error when loading "+filename);
		}
		return false;
	}
	
	public boolean loadSprite(String filename) {
		try {
			Texture tex = TextureLoader.getInstance().getTexture(IMAGES_DIRECTORY+filename);
			addSprite(filename, new SpriteImpl(tex));
			return true;
		} catch (IOException e) {
			System.err.println("error when loading "+filename);
		}
		return false;
	}
	
	
	protected void addSprite(String name, ISprite sprite) {
		sprites.put(name.replaceAll("\\.png", ""), sprite);//in case
		System.out.println("new sprite loaded and added : "+name);
	}
	
	protected void addAnimation(String name, AnimationSequence as) {
		animations.put(name, as);//in case
		System.out.println("new animation loaded and added : ["+name+"] [nb frames ="+as.getFrames().size()+"]");
	}
	
	public boolean loadImagesFromXml(String filename) {
		return loadImagesFromXml(new XMLParser(filename));
	}
	
	public boolean loadImagesFromXml(XMLParser parser) {
		Element imagesNode = parser.getElement("assets/images");
		Element animationsNode = parser.getElement("assets/animations");
		boolean success = true;
		
		if (imagesNode != null && imagesNode.hasChildNodes()) {
			for (int i = 0; i < imagesNode.getChildNodes().getLength(); i++) {
				if (imagesNode.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE) continue;
				success &= getImageFromNode(imagesNode.getChildNodes().item(i));
			}
		}
		
		if (animationsNode != null && animationsNode.hasChildNodes()) {
			for (int i = 0; i < animationsNode.getChildNodes().getLength(); i++) {
				if (animationsNode.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE) continue;
				success &= getAnimationFromNode(animationsNode.getChildNodes().item(i));
			}
		}
		
		System.out.println("XML image loading : "+(success ? "success":"failure"));
		return success;
	}
	
	
	private boolean getAnimationFromNode(Node node) {
		if (node == null || !node.hasAttributes()) return false;
		NamedNodeMap attributes = node.getAttributes();
		
		String [] attr = {"name","filename","frameduration","nbframes", "loop","startx","starty","framewidth","frameheight"};
		String []vals = new String[attr.length];
		Node attrnode;
		
		for (int i = 0; i < attr.length; i++) {
			attrnode = attributes.getNamedItem(attr[i]);
			if (attrnode == null) {
				System.out.println("attribute not found : "+attr[i]);
				return false;
			}
			vals[i] = attrnode.getNodeValue();
		}
		
		String name = vals[0], filename = vals[1];
		int frameduration = Integer.parseInt(vals[2]), nbframes = Integer.parseInt(vals[3]),
			startx = Integer.parseInt(vals[5]), starty = Integer.parseInt(vals[6]), 
			framewidth = Integer.parseInt(vals[7]), frameheight = Integer.parseInt(vals[8]);
		
		boolean loop = vals[4].equals("1");
		Vector2f []offSets = new Vector2f[nbframes];
		Vector2f []frameDims = new Vector2f[nbframes];
		
		/* if image needs replacing */
		int offx=0, offy=0;
		attrnode = attributes.getNamedItem("offx");
		if (attrnode != null) {
			offx = Integer.parseInt(attrnode.getNodeValue());
		}
		attrnode = attributes.getNamedItem("offy");
		if (attrnode != null) {
			offy = Integer.parseInt(attrnode.getNodeValue());
		}
		/*----------------*/
		
		for (int i = 0; i < nbframes; i++) {
			offSets[i] = new Vector2f(offx, offy);
			frameDims[i] = new Vector2f(framewidth, frameheight);
		}
		System.out.println("framedim[0] = "+frameDims[0]);
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.TEXT_NODE) continue;
				if (!children.item(i).hasAttributes()) return false;
				attributes = children.item(i).getAttributes();
				attrnode = attributes.getNamedItem("index");
				if (attrnode == null)  {
					return false;
				}
				int index = Integer.parseInt(attrnode.getNodeValue());
				if (index < 0 || index >= nbframes) {
					return false;
				}
				
				attrnode = attributes.getNamedItem("offx");
				if (attrnode != null) {
					offx = Integer.parseInt(attrnode.getNodeValue());
					offSets[index].setX(offx);
				}
				
				attrnode = attributes.getNamedItem("offy");
				if (attrnode != null) {
					offy = Integer.parseInt(attrnode.getNodeValue());
					offSets[index].setY(offy);
				}
				
				
				
				attrnode = attributes.getNamedItem("width");
				if (attrnode != null) {
					frameDims[index].setX(Integer.parseInt(attrnode.getNodeValue()));
				}
				attrnode = attributes.getNamedItem("height");
				if (attrnode != null) {
					frameDims[index].setY(Integer.parseInt(attrnode.getNodeValue()));
				}
				
				//TODO : handle different frame durations
				/*attrnode = attributes.getNamedItem("frameduration");
				if (attrnode != null) {
					int duration = Integer.parseInt(attrnode.getNodeValue());
				}*/
			}
		}
		
		AnimationSequence as = new AnimationSequence(name, loop);
		as.createFramesFromSpriteSheet(filename,
									   nbframes,
									   new Vector2f(startx, starty),
									   frameDims, 
									   offSets, frameduration);
		
		addAnimation(name, as);
		return true;
	}
	
	private Texture getTexture(String filename) {
		Texture tex = null;
		try {
			tex = TextureLoader.getInstance().getTexture(filename);
		} catch (IOException e) {
			System.err.println("error when loading texture "+filename);
		}
		return tex;
	}
	
	private boolean getImageFromNode(Node node) {
		
		if (node == null || !node.hasAttributes()) {
			return false;
		}
		
		NamedNodeMap attributes = node.getAttributes();
		
		String [] attr = {"name","filename","subimage"};
		String []vals = new String[attr.length];
		int offx = 0, offy = 0;
		
		Node attrnode;
		Texture tex;
		ISprite sprite;
		
		for (int i = 0; i < attr.length; i++) {
			attrnode = attributes.getNamedItem(attr[i]);
			if (attrnode == null) {
				System.out.println("attribute not found : "+attr[i]);
				return false;
			}
			vals[i] = attrnode.getNodeValue();
		}
		
		
		/* if image needs replacing */
		attrnode = attributes.getNamedItem("offx");
		if (attrnode != null) {
			offx = Integer.parseInt(attrnode.getNodeValue());
		}
		attrnode = attributes.getNamedItem("offy");
		if (attrnode != null) {
			offy = Integer.parseInt(attrnode.getNodeValue());
		}
		/*----------------*/
		
		if (vals[2].equals("0")) {
			tex = getTexture(IMAGES_DIRECTORY+vals[1]);
			sprite = new SpriteImpl(tex, new Vector2f(tex.getImageWidth(), tex.getImageHeight()), new Vector2f(offx, offy));
		} else {
			
			String [] attrSub = {"x","y","width","height"};
			String [] valsSub = new String[attrSub.length];
			Node attrnodeSub;
			
			
			
			for (int i = 0; i < attrSub.length; i++) {
				attrnodeSub = attributes.getNamedItem(attrSub[i]);
				if (attrnodeSub == null) return false;
				valsSub[i] = attrnodeSub.getNodeValue();
			}
			
			
			sprite = getSubSprite(vals[1], 
						new Vector2f(Integer.parseInt(valsSub[0]), Integer.parseInt(valsSub[1])),
						new Vector2f(Integer.parseInt(valsSub[2]), Integer.parseInt(valsSub[3])),
						new Vector2f(offx, offy));
			
		}

		if (sprite == null) return false;
		
		addSprite(vals[0], sprite);
		return true;
	}
	
	@Override
	public AnimationSequence getAnimationSequence(String name) {
		AnimationSequence sequence = animations.get(name);
		if (sequence == null) return null;
		return new AnimationSequence(sequence);
	}
	
	@Override
	public ISprite getSprite(String name) {
		SpriteImpl sprite = (SpriteImpl) sprites.get(name);
		if (sprite == null) sprite = (SpriteImpl) sprites.get(name.replaceAll("\\.png", ""));
		
		if (sprite == null) {
			if (!loadSprite(name.replaceAll("\\.png", "")+".png")) {
				//System.err.println("cant load "+name.replaceAll("\\.png", "")+".png");
				return null;
			}
			sprite = (SpriteImpl) sprites.get(name.replaceAll("\\.png", ""));
		}
		return new SpriteImpl(sprite);
	}
	
	@Override
	public ISprite getSubSprite(String sheetName, Vector2f pos, Vector2f dim, Vector2f offSet) {
		return getSubSprite(getSprite(sheetName),pos,dim, offSet);
	}
	
	@Override
	public ISprite getSubSprite(ISprite sheet, Vector2f pos, Vector2f dim, Vector2f offSet) {
		if (sheet == null) {
			return null;//throw exception maybe
		}
		Texture refTexture = sheet.getTexture();
		Texture frameTex;
		try {
			frameTex = (Texture)refTexture.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;//throw exception ? 
		}
		
		frameTex.setWidth((int)dim.x);
		frameTex.setHeight((int)dim.y);
		
		Vector2f texPos = new Vector2f(pos.x / refTexture.getTextureWidth(), pos.y / refTexture.getTextureHeight());
		return new SpriteImpl(frameTex, texPos, offSet);
	}
}
