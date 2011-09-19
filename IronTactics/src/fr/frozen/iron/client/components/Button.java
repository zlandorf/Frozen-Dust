package fr.frozen.iron.client.components;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.FontManager;
import fr.frozen.game.ISprite;

public class Button extends Component {

	protected String label;
	
	protected ISprite normalSprite = null;
	protected ISprite hoverSprite = null;
	protected boolean enabled;
	//protected ISprite selectedSprite = null;
	
	boolean hover;
	
	boolean clickedLastTick = false;
	
	
	public Button(String label, int x, int y, int w, int h) {
		super (x,y, w, h);
		this.label = label;
		enabled = true;
	}
	
	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public void setNormalSprite(ISprite sprite) {
		sprite.setWidth(getWidth());
		sprite.setHeight(getHeight());
		normalSprite = sprite;
	}
	
	public void setHoverSprite(ISprite sprite) {
		sprite.setWidth(getWidth());
		sprite.setHeight(getHeight());
		hoverSprite = sprite;
	}
	
	@Override
	public void setDim(Vector2f val) {
		super.setDim(val);
		
		if (hoverSprite != null) {
			hoverSprite.setWidth(getWidth());
			hoverSprite.setHeight(getHeight());
		}

		if (normalSprite != null) {
			normalSprite.setWidth(getWidth());
			normalSprite.setHeight(getHeight());
		}
	}
	
	/*public void setSelectedSprite(ISprite sprite) {
		sprite.setWidth((int) size.getWidth());
		sprite.setHeight((int) size.getHeight());
		selectedSprite = sprite;
	}*/
	
	
	
	@Override
	public void render(float deltaTime) {
		
		if (hoverSprite == null && normalSprite == null /*&& selectedSprite == null*/) {
			System.out.println("button needs sprites to be displayed");
			return;
		}
		ISprite sprite = normalSprite;

		if (hover && enabled) {
			sprite = hoverSprite;
		}
		/*if (selectedSprite) = selectedSprite;*/
		if (enabled) {
			sprite.setColor(0xffffff);
		} else {
			sprite.setColor(0xbbbbbb);
		}
		sprite.draw(pos.getX(), pos.getY());
		
		
		float red = (float)0x5e / (float)0xff; 
		float green = (float)0x32 / (float)0xff;
		float blue = (float)0x03 / (float)0xff;
		
		if (hover && enabled) {
			red = (float)0x29 / (float) 0xff; 
			green = (float)0x16 / (float) 0xff;
			blue = (float)0x2 / (float) 0xff;
		}
		
		//TODO : dans textrenderer mettre method qui retourne la distance entre deux lettres
		float textWidth = label.length() * 10;
		
		
		/*if (!enabled) {
			red = green = blue = 0.5f;
		}*/
		
		float x = (float) (pos.getX() + getWidth() / 2 - textWidth / 2);
		float y = (float)(pos.getY() + getHeight() / 2 - 8);
		//GL11.glColor3f(1,1,1);
		FontManager.getFont("Font").setColor(red, green, blue, 1);
		FontManager.getFont("Font").glPrint(label, x, y, 0);
	}

	@Override
	public void onHover(int x, int y) {
		hover = true;
	}
	
	@Override
	public void onExit() {
		hover = false;
	}

	@Override
	public void onLeftClick(int x, int y) {
		if (enabled) {
			notifyActionListeners();
		}
	}

	@Override
	public void onRightClick(int x, int y) {
		if (enabled) {
			notifyActionListeners();
		}
	}

	@Override
	public boolean update(float deltaTime) {
		return false;
	}
	
	
	/*@Override
	public boolean update(float deltaTime) {
		int x = (int) (Mouse.getX() - pos.getX());
		int y = (int) (Display.getDisplayMode().getHeight() - Mouse.getY() - pos.getY());
		
		if (x < 0 || x > size.getWidth() || y < 0 || y > size.getHeight()) { //outside
			hover = false;
			return false;
		}
		
		if (Mouse.isButtonDown(0)) {
			selected = true;
			if (!clickedLastTick) {
				notifyActionListeners();
			} 
			clickedLastTick = true;
		} else {
			hover = true;
			clickedLastTick = false;
		}
		
		return clickedLastTick;
	}*/
}
