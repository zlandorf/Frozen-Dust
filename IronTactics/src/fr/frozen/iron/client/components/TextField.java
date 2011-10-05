package fr.frozen.iron.client.components;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;

import fr.frozen.game.FontManager;
import fr.frozen.iron.util.IronGL;

public class TextField extends Component implements KeyboardListener {

	protected static int CARRET_DISPLAY_TIME = 1;//s
	protected static int PADDING = 5;

	protected boolean active = true;
	protected StringBuffer buffer;

	protected float timeToNext = CARRET_DISPLAY_TIME;
	protected boolean showCarret = false;
	protected Font chatFont;
	
	public TextField(int x, int y, int w, int h) {
		super(x,y, w, h);
		buffer = new StringBuffer();
		chatFont = FontManager.getFont("chatFont");
	}

	public String getText() {
		return buffer.toString();
	}

	public void setText(String text) {
		buffer.delete(0, buffer.length());
		buffer.append(text);
	}

	@Override
	public void render(float deltaTime) {
		if (!visible) return;

		float r = 0.1607843f;
		float g = 0.06274509f;
		float b = 0.0078431f;

		IronGL.drawRect((int)pos.getX(),(int) pos.getY(), getWidth(), getHeight(),
				r, g, b, 0.7f);

		String tmpText = buffer.toString();
		String text = "";
		if (tmpText != null && tmpText.length() > 0) {
			int startIndex = tmpText.length() - 1;
			
			while (startIndex >= 0 && chatFont.getWidth(tmpText.substring(startIndex)) <= getWidth() - 2 * PADDING) {
				startIndex --;
			}
			//text = tmpText.substring(Math.max(0, Math.min(startIndex + 1,tmpText.length() - 1)));
			text = tmpText.substring(Math.min(startIndex + 1, tmpText.length()));
			chatFont.drawString(pos.getX() + PADDING , pos.getY() + PADDING, text, Color.white);
		}
		if (showCarret) {
			int x = (int)(pos.getX() + PADDING + chatFont.getWidth(text) + PADDING);
			int y = (int)pos.getY() + 6;

			IronGL.drawRect(x, y, 6, 15,
					1, 1, 1, 1);
		}
	}

	@Override
	public boolean update(float deltaTime) {
		if (active) {
			timeToNext -= deltaTime;
			if (timeToNext <= 0) {
				showCarret = !showCarret;
				timeToNext = CARRET_DISPLAY_TIME;
			}
		} else {
			showCarret = false;
		}


		return false;
	}

	@Override
	public void onKeyPressed(int eventKey, char eventChar) {
		// first encode the utf-16 string as a ByteBuffer
		if (eventKey == Keyboard.KEY_BACK && buffer.length() >= 1) {
			buffer.deleteCharAt(buffer.length() - 1);
		} else if (eventKey == Keyboard.KEY_RETURN) {
			if (buffer.length() >= 1)
				notifyActionListeners();
		} else {
			if (eventChar >= 32 && eventChar <= 256 && eventKey != Keyboard.KEY_DELETE) {
				buffer.append(Keyboard.getEventCharacter());
			}
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean val) {
		active = val;
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
