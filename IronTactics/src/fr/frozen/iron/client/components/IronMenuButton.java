package fr.frozen.iron.client.components;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

public class IronMenuButton extends Button {
	
	public IronMenuButton(String label, int y) {
		super(label, 0, y, 0, 0);
		//centered button
		pos = new Vector2f((float) (Display.getWidth() / 2 - getWidth()/ 2), y); 
	}

}
