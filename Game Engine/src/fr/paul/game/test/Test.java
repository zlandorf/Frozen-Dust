package fr.paul.game.test;


import java.awt.BasicStroke;
import java.awt.Color;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Font;
import org.newdawn.slick.font.effects.OutlineEffect;

import fr.frozen.game.FontManager;
import fr.frozen.game.GameEngine;
import fr.frozen.game.GameState;
import fr.frozen.game.IGameEngine;
import fr.frozen.game.SoundManager;
import fr.frozen.game.SpriteManager;

class Test extends GameEngine {

	int viewX = 0;
	int viewY = 0;
	protected Logger logger = Logger.getLogger("engine");
	Font font;
	Font font2;
	
	public Test() {
		BasicConfigurator.configure();
		logger.setLevel(Level.ALL);
	}
	
	@Override
	protected void buildAssets() {
		/*ISpriteManager.getInstance().loadSprite("image.png");
		ISpriteManager.getInstance().loadSprite("rebel.png");
		ISpriteManager.getInstance().loadSprite("sheet.png");*/
		SpriteManager.getInstance().loadImagesFromXml("Data/iron.cfg");
		SoundManager.getInstance().loadSoundsFromXml("Data/iron.cfg");
		OutlineEffect outLineEffect = new OutlineEffect(20, new Color(.5f,.5f,.5f));
		outLineEffect.setJoin(BasicStroke.JOIN_ROUND);
		OutlineEffect outLineEffect2 = new OutlineEffect(20, new Color(1f,0,0, 0.5f));
		outLineEffect2.setJoin(BasicStroke.JOIN_BEVEL);
		font = FontManager.loadFont("visitor.ttf", 50, false, true, outLineEffect, outLineEffect2);
		
		/*try {
			font2 = new AngelCodeFont("Data/augusta.fnt", "Data/augusta.png");
		} catch (SlickException e) {
			e.printStackTrace();
		}*/
//		UnicodeFont ufont = new UnicodeFont();
//		try {
//			ufont.addAsciiGlyphs();
//			ufont.addGlyphs(400, 600);
//			ufont.getEffects().add(new OutlineEffect(1,Color.red));
//			ufont.getEffects().add(new ColorEffect(Color.white));
//			ufont.loadGlyphs();
//		} catch (SlickException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		font2 = FontManager.loadFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20), new OutlineEffect(1,Color.red));
		System.out.println("\n"+font2.getLineHeight()+"\n");
		
	}
	
	protected void buildInitialGameStates() {
		GameState gs = new GameState(this,"state", true, true); 
		addGameState(gs);
		gs.addGameObject(new TestObject(gs,50,50,1),"archer");
		gs.addGameObject(new TestObject(gs,50,70,1),"archer");
		gs.addGameObject(new TestObject(gs,50,90,1),"archer");
		gs.addGameObject(new TestObject(gs,50,110,1),"archer");
		
		gs.addGameObject(new TestObject(gs,50,50,5),"archer2");
		
		gs.addGameObject(new TestObject2(gs,100,50),"archer2");
		gs.addGameObject(new TestShooter(gs,200,50),"shooter");
		setCurrentGameState(gs);
	}
	
	@Override
	protected void render() {
		super.render();
		String str = "Ceci est un test^éèàù£$¤";
		font.drawString(300, 300, str, org.newdawn.slick.Color.yellow);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		font2.drawString(300, 400, "abcdefghijklmiwinop+-~");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//font.glPrint(str, 300, 300);
		//font.glPrint("test 2", 300, 400);
		//ISprite sprite = ISpriteManager.getInstance().getSprite("tex");
		//sprite.fillIn(0, 0, 800, 600);
	}
	
	@Override
	protected void update() {
		super.update();
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			viewX += 1;
			SoundManager.getInstance().getSound("sword").playAsSoundEffect(false);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			viewX -= 1;
		}
		
		
		//System.out.println("fps = "+(1.f/GameEngine._tick));
	}
	
	public static void main(String []args) {
		IGameEngine gw = new Test();
		gw.setTitle("");
		gw.setVSync(true);
		//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		//gw.setResolution(300,200);//(int)screen.getWidth(),(int)screen.getHeight)());
		gw.setFullScreen(false);
		gw.setSize(800, 600);
		gw.start();
	}
}
