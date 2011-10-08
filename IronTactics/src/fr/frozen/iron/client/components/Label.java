package fr.frozen.iron.client.components;

import org.newdawn.slick.Color;

import fr.frozen.iron.util.IronGL;

public class Label extends Component {
	
	protected String label;
	
	public Label(String label, int x, int y, int w, int h) {
		super (x,y, w, h);
		this.label = label;
	}

	public void setLabel(String val) {
		label = val;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public void render(float deltaTime) {
		if (!visible) return;
		
		
		float r = 0.1607843f;
		float g = 0.06274509f;
		float b = 0.0078431f;
		
		IronGL.drawRect((int)pos.getX(),(int) pos.getY(), getWidth(), getHeight(),
				r, g, b, 0.7f);
		
		float y = pos.getY() + getHeight() - font.getHeight(label) - 5;
		font.drawString(pos.getX() + 5, y, label, Color.white);
	}

	@Override
	public boolean update(float deltaTime) {
		return false;
	}

	@Override
	public void onExit() {
	}

	@Override
	public void onHover(int x, int y) {
	}

	@Override
	public void onLeftClick(int x, int y) {
	}

	@Override
	public void onRightClick(int x, int y) {
	}
	
	@Override
	public void onRelease() {
	}
}
