package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.newdawn.slick.Color;

import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;

public class Dialog extends Component implements ActionListener {

	protected static int PADDING = 16;
	protected static int MAX_WIDTH = 400;
	protected static int INTERLINE = 5;

	protected static int BUTTON_WIDTH = 50;
	protected static int BUTTON_HEIGHT = 30;

	protected ISprite tex;
	protected ISprite corner;
	protected ISprite top;
	protected ISprite left;

	protected List<String> text;

	protected GUI gui;
	protected Button okButton;
	protected Color fontColor = new Color(0x29, 0x16,0x2);

	public Dialog(GUI gui, String text, int x, int y) {
		super(x, y, 0, 0);
		this.gui = gui;

		int width, height;

		int textWidth = font.getWidth(text);
		if (textWidth + 2 * PADDING >= MAX_WIDTH) {
			this.text = splitText(text);
			width = MAX_WIDTH;
		} else {
			this.text = new ArrayList<String>();
			this.text.add(text);
			width = textWidth + 2 * PADDING;
		}

		if (this.text == null || this.text.size() == 0) {
			Logger.getLogger(getClass()).error("could not find dialog text");
			return;
		}

		height = (this.text.size() - 1) * INTERLINE;
		for (String str : this.text) {
			height += font.getHeight(str);
		}

		width = Math.max(width, BUTTON_WIDTH + 2 * PADDING);
		height += PADDING * 3; // padding between top - text, text button and
								// button bottom;
		height += BUTTON_HEIGHT;

		setDim(width, height);

		tex = SpriteManager.getInstance().getSprite("popupTex");
		corner = SpriteManager.getInstance().getSprite("popup_corner");
		top = SpriteManager.getInstance().getSprite("popup_top");
		left = SpriteManager.getInstance().getSprite("popup_left");

		okButton = new Button("OK", x + width / 2 - BUTTON_WIDTH / 2, y
				+ height - BUTTON_HEIGHT - PADDING, 0, 0);
		okButton.setDim(BUTTON_WIDTH, BUTTON_HEIGHT);
		okButton.addActionListener(this);
	}

	private List<String> splitText(String fullText) {
		int nbLines = 1 + font.getWidth(fullText) / (MAX_WIDTH - 2 * PADDING);
		List<String> splitTexts = new ArrayList<String>();
		int[] charPerLine = new int[nbLines];

		int startIndex = 0, currentIndex = 0;
		for (int i = 0; i < nbLines; i++) {
			charPerLine[i] = 0;
			while (currentIndex < fullText.length()
					&& font.getWidth(fullText.substring(startIndex,
							currentIndex)) < (getWidth() - 2 * PADDING)) {
				currentIndex++;
				charPerLine[i]++;
			}

			if (currentIndex < fullText.length()) {
				currentIndex--;
				charPerLine[i]--;
			}
			splitTexts.add(fullText.substring(startIndex, currentIndex));
			startIndex = currentIndex;
		}

		return splitTexts;
	}

	@Override
	public void render(float deltaTime) {
		if (!visible)
			return;

		int width = getWidth();
		int height = getHeight();

		corner.draw(pos.getX(), pos.getY());
		corner.draw(pos.getX() - PADDING + width, pos.getY(), true, false);
		corner.draw(pos.getX(), pos.getY() + height - PADDING, false, true);
		corner.draw(pos.getX() - PADDING + width,
				pos.getY() + height - PADDING, true, true);

		top.fillIn(pos.getX() + PADDING, pos.getY(), pos.getX() - PADDING
				+ width, pos.getY() + PADDING);
		top.fillIn(pos.getX() + PADDING, pos.getY() + height - PADDING, pos
				.getX()
				- PADDING + width, pos.getY() + height, false, true);

		left.fillIn(pos.getX(), pos.getY() + PADDING, pos.getX() + PADDING, pos
				.getY()
				+ height - PADDING);
		left.fillIn(pos.getX() - PADDING + width, pos.getY() + PADDING, pos
				.getX()
				+ width, pos.getY() + height - PADDING, true, false);

		tex.fillIn(pos.getX() + PADDING, pos.getY() + PADDING, pos.getX()
				+ width - PADDING, pos.getY() + height - PADDING);

		int x = (int) getLocation().getX() + PADDING;
		int y = (int) getLocation().getY() + PADDING;

		for (String str : text) {
			font.drawString(x, y, str, fontColor);
			y += font.getHeight(str) + INTERLINE;
		}

		okButton.render(deltaTime);
	}

	@Override
	public boolean update(float deltaTime) {
		return false;
	}

	@Override
	public void onExit() {
		okButton.onExit();
	}

	@Override
	public void onHover(int x, int y) {
		if (okButton.contains(x, y)) {
			okButton.onHover(x, y);
		} else if (okButton.hover) {
			okButton.onExit();
		}
	}

	@Override
	public void onLeftClick(int x, int y) {
		if (okButton.contains(x, y))
			okButton.onLeftClick(x, y);
	}

	@Override
	public void onRelease() {
		okButton.onRelease();
	}

	@Override
	public void onRightClick(int x, int y) {
		if (okButton.contains(x, y))
			okButton.onRightClick(x, y);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.removeComponent(this);
	}
}
