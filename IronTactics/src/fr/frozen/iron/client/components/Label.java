package fr.frozen.iron.client.components;

import fr.frozen.game.FontManager;
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
		
		int y = (int) (pos.getY() + getHeight()) - 16 - 5;
		FontManager.getFont("Font").setColor(1, 1, 1);
		FontManager.getFont("Font").glPrint(label, pos.getX() + 5, y, 0);
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
}
