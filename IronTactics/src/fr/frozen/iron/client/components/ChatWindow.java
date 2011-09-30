package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;

import fr.frozen.iron.util.IronGL;

public class ChatWindow extends Component {
	
	protected int PADDING = 5;
	protected List<ChatWindowMessage> messages;
	protected int maxMessages = 31;
	
	protected Color prefixColor = new Color(.3f, 0.8f, .3f);
	protected Color normalColor = new Color(0.85f, 0.85f, 0.85f);
	protected Color servColor = new Color(1f, 0f, 0f);
	
	public ChatWindow (int x, int y, int w, int h) {
		super(x,y, w, h);
		messages = new ArrayList<ChatWindowMessage>();
	}

	public synchronized void clearMessages() {
		messages.clear();
	}
	
	@Override
	public synchronized void render(float deltaTime) {
		if (!visible) return;
		
		IronGL.drawRect((int)pos.getX(),(int) pos.getY(), getWidth(), getHeight(),
				0.1607843f, 0.06274509f, 0.0078431f,0.8f);//background color
		
		int y = (int) (pos.getY() + getHeight()) - 16 - PADDING;
		
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (y < pos.getY()) break;
			int x = (int)pos.getX() + PADDING;
			
			if (messages.get(i).getPrefix() != null && !messages.get(i).getPrefix().equals("")) {
				font.drawString(x, y, messages.get(i).getPrefix(), prefixColor);
				x += font.getWidth(messages.get(i).getPrefix());
			}
			
			
			Color color = normalColor;
			if (messages.get(i).getType() == ChatWindowMessage.SERVER_MESSAGE) {
				color = servColor;
			}
			
			if (messages.get(i).getMessage() != null && !messages.get(i).getMessage().equals("")) {
				font.drawString(x, y, messages.get(i).getMessage(), color);
			}
			y -= font.getLineHeight();
		}
	}

	public synchronized void addMessage(ChatWindowMessage message) {
		
		String fullMessage = message.getFullMessage();
		int nbLines = 1 + font.getWidth(fullMessage) / getWidth();
		String []splitMessages = new String[nbLines];
		int []charPerLine = new int[nbLines];
		
		int startIndex = 0, currentIndex = 0;
		for (int i = 0; i < nbLines; i++) {
			charPerLine[i] = 0;
			while (currentIndex < fullMessage.length()
				   && font.getWidth(fullMessage.substring(startIndex, currentIndex)) < getWidth() - PADDING) {
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
