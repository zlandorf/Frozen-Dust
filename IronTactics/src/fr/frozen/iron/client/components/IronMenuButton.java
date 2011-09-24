package fr.frozen.iron.client.components;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;

public class IronMenuButton extends Button {
	
	public IronMenuButton(String label, int y) {
		super(label, 0, y, 0, 0);
		ISprite spriteNormal = SpriteManager.getInstance().getSprite("buttonNormal");
		ISprite spriteHover = SpriteManager.getInstance().getSprite("buttonHover");

		setDim((int)spriteNormal.getWidth(),(int)spriteNormal.getHeight());
		
		setHoverSprite(spriteHover);
		setNormalSprite(spriteNormal);

		
		//centered button
		pos = new Vector2f((float) (Display.getDisplayMode().getWidth() / 2 - getWidth()/ 2), y); 
	}

}
