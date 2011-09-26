package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.game.FontManager;
import fr.frozen.iron.util.IronGL;

public class ChatWindow extends Component {
	
	protected int PADDING = 5;
	protected List<ChatWindowMessage> messages;
	protected int maxMessages = 31;
	protected int charPerLine;
	
	public ChatWindow (int x, int y, int w, int h) {
		super(x,y, w, h);
		messages = new ArrayList<ChatWindowMessage>();
		charPerLine = (w - 2 * PADDING) / 11;
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
				FontManager.getFont("Font").setColor(.3f, 0.8f, .3f);
				FontManager.getFont("Font").glPrint(messages.get(i).getPrefix(), x, y, 0);
				x += 11 * messages.get(i).getPrefix().length();
			}
			
			
			
			if (messages.get(i).getType() == ChatWindowMessage.CHAT_MESSAGE) {
				FontManager.getFont("Font").setColor(0.75f, 0.75f, 0.75f);
			} else {
				FontManager.getFont("Font").setColor(1, 0, 0);
			}
			if (messages.get(i).getMessage() != null && !messages.get(i).getMessage().equals("")) {
				FontManager.getFont("Font").glPrint(messages.get(i).getMessage(), x, y, 0);
			}
			y -= 16;
		}
	}

	public synchronized void addMessage(ChatWindowMessage message) {
		String []splitMessages = message.getFullMessage().split("(?<=\\G.{"+charPerLine+"})");
		int prefixLeft = 0;
		
		if (message.getPrefix() != null) {
			prefixLeft = message.getPrefix().length();
		}
		int prefixDone = 0;
		ChatWindowMessage cwm;
		for (String msg : splitMessages) {
			if (prefixLeft > 0) {
				int nbPrefix = Math.min(charPerLine, prefixLeft);
				String prefix = msg.substring(0, nbPrefix);
				String strmsg;
				if (nbPrefix >= charPerLine)
					strmsg = "";
				else
					strmsg = msg.substring(nbPrefix , Math.min(charPerLine, msg.length()));
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
