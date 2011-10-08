package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;

import fr.frozen.game.FontManager;
import fr.frozen.iron.util.IronGL;

public class ChatWindow extends Component {
	
	protected int PADDING = 5;
	protected List<ChatWindowMessage> messages;
	protected int maxMessages = 100;
	
	protected Color prefixColor = new Color(.3f, 0.8f, .3f);
	protected Color normalColor = new Color(0.85f, 0.85f, 0.85f);
	protected Color servColor = new Color(1f, 0f, 0f);
	
	protected Font chatFont;
	
	protected Button buttonUp;
	protected Button buttonDown;
	
	protected boolean buttonUpClicked = false;
	protected boolean buttonDownClicked = false;
	
	protected float scrollInitTime = 0.5f;
	protected float scrollInitTimeLeft = 0.5f;
	protected float timeBetweenTwoScrolls = 0.1f;
	protected float timeLeftBeforeNextScroll = 0;
	
	protected int scrollIndex = 0;
	protected int nbLinesDisplayed;
	
	public ChatWindow (int x, int y, int w, int h) {
		super(x,y, w, h);
		messages = new ArrayList<ChatWindowMessage>();
		chatFont = FontManager.getFont("chatFont");
		nbLinesDisplayed = (h - 2 * PADDING) / chatFont.getLineHeight();
		
		buttonUp = new Button("", (int)getLocation().getX() + getWidth() - 25,
								  (int)getLocation().getY(), 25,25,
								  "buttonUpNormal", "buttonUpHover");
		
		buttonDown = new Button("", (int)getLocation().getX() + getWidth() - 25,
									(int)getLocation().getY() + getHeight() - 25, 25,25,
									"buttonDownNormal", "buttonDownHover");
		
		buttonUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scrollUp();
				buttonUpClicked = true;
				timeLeftBeforeNextScroll = 0;
				scrollInitTimeLeft = scrollInitTime;
			}
		});
		
		buttonDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scrollDown();
				buttonDownClicked = true;
				timeLeftBeforeNextScroll = 0;
				scrollInitTimeLeft = scrollInitTime;
			}
		});
	}

	public void scrollUp() {
		if (messages.size() > nbLinesDisplayed) {
			scrollIndex = Math.min(scrollIndex + 1, messages.size() - nbLinesDisplayed);
		}
	}
	
	
	public void scrollDown() {
		scrollIndex = Math.max(scrollIndex - 1, 0);
	}
	
	public synchronized void clearMessages() {
		messages.clear();
		scrollIndex = 0;
	}
	
	@Override
	public synchronized void render(float deltaTime) {
		if (!visible) return;
		
		IronGL.drawRect((int)pos.getX(),(int) pos.getY(), getWidth() - buttonUp.getWidth(), getHeight(),
				0.1607843f, 0.06274509f, 0.0078431f,0.8f);//background color
		
		int y = (int) (pos.getY() + getHeight()) - 16 - PADDING;
		
		for (int i = messages.size() - 1 - scrollIndex; i >= 0; i--) {
			if (y < pos.getY()) break;
			int x = (int)pos.getX() + PADDING;
			
			if (messages.get(i).getPrefix() != null && !messages.get(i).getPrefix().equals("")) {
				chatFont.drawString(x, y, messages.get(i).getPrefix(), prefixColor);
				x += chatFont.getWidth(messages.get(i).getPrefix());
			}
			
			
			Color color = normalColor;
			if (messages.get(i).getType() == ChatWindowMessage.SERVER_MESSAGE) {
				color = servColor;
			}
			
			if (messages.get(i).getMessage() != null && !messages.get(i).getMessage().equals("")) {
				chatFont.drawString(x, y, messages.get(i).getMessage(), color);
			}
			y -= chatFont.getLineHeight();
		}
		
		buttonUp.render(deltaTime);
		buttonDown.render(deltaTime);
	}

	public synchronized void addMessage(ChatWindowMessage message) {
		
		String fullMessage = message.getFullMessage();
		int nbLines = 1 + chatFont.getWidth(fullMessage) / (getWidth() - 2 * PADDING - buttonUp.getWidth());
		String []splitMessages = new String[nbLines];
		int []charPerLine = new int[nbLines];
		
		int startIndex = 0, currentIndex = 0;
		for (int i = 0; i < nbLines; i++) {
			charPerLine[i] = 0;
			while (currentIndex < fullMessage.length()
				   && chatFont.getWidth(fullMessage.substring(startIndex, currentIndex)) < (getWidth() - 2 * PADDING - buttonUp.getWidth())) {
				currentIndex ++;
				charPerLine[i]++;
			}
			
			if (currentIndex < fullMessage.length()) {
				currentIndex--;
				charPerLine[i]--;
			}
			splitMessages[i] = fullMessage.substring(startIndex, currentIndex);
			startIndex = currentIndex;
		}
		
		//String []splitMessages = message.getFullMessage().split("(?<=\\G.{"+charPerLine+"})");
		int prefixLeft = 0;
		
		if (message.getPrefix() != null) {
			prefixLeft = message.getPrefix().length();
		}
		int prefixDone = 0;
		ChatWindowMessage cwm;
		for (int i = 0; i < splitMessages.length; i++) {
			String msg = splitMessages[i];
			if (prefixLeft > 0) {
				int nbPrefix = Math.min(charPerLine[i], prefixLeft);
				String prefix = msg.substring(0, nbPrefix);
				String strmsg;
				if (nbPrefix >= charPerLine[i])
					strmsg = "";
				else
					strmsg = msg.substring(nbPrefix , Math.min(charPerLine[i], msg.length()));
				cwm = new ChatWindowMessage(message.type, prefix, strmsg);
				
				prefixDone += nbPrefix;
				prefixLeft -= nbPrefix;
			} else {
				cwm = new ChatWindowMessage(message.getType(), msg);
			}
			
			if (messages.size() >= maxMessages) {
				messages.remove(0);
			}
			
			messages.add(messages.size(), cwm);
		}
		
	}
	
	@Override
	public boolean update(float deltaTime) {
		
		if (buttonDownClicked || buttonUpClicked) {
			scrollInitTime -= deltaTime;
			if (scrollInitTime > 0) return false;
		}
		
		if (buttonDownClicked || buttonUpClicked) {
			timeLeftBeforeNextScroll -= deltaTime;
		}
		
		if (buttonUpClicked) {
			if (timeLeftBeforeNextScroll <= 0) {
				scrollUp();
				timeLeftBeforeNextScroll = timeBetweenTwoScrolls;
			}
		}

		if (buttonDownClicked) {
			if (timeLeftBeforeNextScroll <= 0) {
				scrollDown();
				timeLeftBeforeNextScroll = timeBetweenTwoScrolls;
			}
		}
		
		return false;
	}

	@Override
	public void onExit() {
		buttonUpClicked = false;
		buttonDownClicked = false;
		
		if (buttonUp.hover) {
			buttonUp.onExit();
		}
		
		if (buttonDown.hover) {
			buttonDown.onExit();
		}
	}

	@Override
	public void onHover(int x, int y) {
		if (buttonUp.contains(x, y)) {
			buttonUp.onHover(x, y);
		} else {
			if (buttonUp.hover && !buttonUp.contains(x, y)) {
				buttonUpClicked = false;
				buttonUp.onExit();
			}
		}
		
		if (buttonDown.contains(x, y)) {
			buttonDown.onHover(x, y);
		} else {
			if (buttonDown.hover && !buttonDown.contains(x, y)) {
				buttonDownClicked = false;
				buttonDown.onExit();
			}
		}
	}

	@Override
	public void onLeftClick(int x, int y) {
		if (buttonUp.contains(x, y)) {
			buttonUp.onLeftClick(x, y);
		}
		
		if (buttonDown.contains(x, y)) {
			buttonDown.onLeftClick(x, y);
		}
	}

	@Override
	public void onRightClick(int x, int y) {
		if (buttonUp.contains(x, y)) {
			buttonUp.onRightClick(x, y);
		}
		
		if (buttonDown.contains(x, y)) {
			buttonDown.onRightClick(x, y);
		}
	}
	
	@Override
	public void onRelease() {
		buttonUpClicked = false;
		buttonDownClicked = false;
	}
}
