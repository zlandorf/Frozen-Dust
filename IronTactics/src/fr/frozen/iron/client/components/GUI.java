package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class GUI {
	protected List<Component> components;
	protected List<Component> componentsToAdd;
	protected List<Component> componentsToRemove;

	protected List<KeyboardListener> keyboardListeners;
	protected List<KeyboardListener> keyboardListenersToAdd;
	protected List<KeyboardListener> keyboardListenersToRemove;

	//protected List<MouseListener> listeners;
	protected MouseListener defaultListener;
	int oldX, oldY;

	public GUI() {
		this(null);
	}

	public GUI(MouseListener defaultListener) {
		components = new ArrayList<Component>();
		componentsToAdd = new ArrayList<Component>();
		componentsToRemove = new ArrayList<Component>();

		keyboardListeners = new ArrayList<KeyboardListener>();
		keyboardListenersToAdd = new ArrayList<KeyboardListener>();
		keyboardListenersToRemove = new ArrayList<KeyboardListener>();

		this.defaultListener = defaultListener;
		oldX = Mouse.getX();
		oldY = Mouse.getY();
	}


	private synchronized void updateListeners() {
		for (KeyboardListener listener : keyboardListenersToAdd)
			keyboardListeners.add(listener);
		keyboardListenersToAdd.clear();
		for (KeyboardListener listener : keyboardListenersToRemove)
			keyboardListeners.remove(listener);
		keyboardListenersToRemove.clear();
	}

	private synchronized void updateComponents() {
		for (Component c : componentsToAdd) {
			components.add(c);
			if (c instanceof TextField)
				addKeyboardListener((TextField)c);
		}
		componentsToAdd.clear();
		for (Component c : componentsToRemove)
			components.remove(c);
		componentsToRemove.clear();
	}

	public synchronized void addComponent(Component c) {
		componentsToAdd.add(c);
	}

	public synchronized void removeComponent(Component c) {
		components.remove(c);
	}

	public synchronized void addKeyboardListener(KeyboardListener listener) {
		keyboardListenersToAdd.add(listener);
	}

	public synchronized void removeKeyboardListener(KeyboardListener listener) {
		keyboardListenersToRemove.remove(listener);
	}

	public synchronized void updateMouse(float deltaTime) {
		Mouse.poll();
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
			int x = Mouse.getX();
			int y =(int) Display.getDisplayMode().getHeight() - Mouse.getY();

			/*int oldX = x - Mouse.getEventDX();
			int oldY = y + Mouse.getEventDY();

			System.out.println("x,y="+x+","+y+"   dx,dy="+Mouse.getEventDX()+","+Mouse.getEventDY());*/

			for (Component c : components) {
				if (c.contains(oldX, oldY) && !c.contains(x, y)) {
					c.onExit();
				}
			}


			boolean hovered = false;

			for (Component c : components) {
				if (c.contains(x,y)) {
					hovered = true;
					c.onHover(x, y);
					break;
				}
			}

			if (!hovered && defaultListener != null) {
				defaultListener.onHover(x, y);
			}
			
			if (eventButton >= 0) {
				if (Mouse.getEventButtonState()) {
					for (Component c : components) {
						if (!c.contains(x,y)) {
							c.setSelected(false);//to close drop lists and shit
						}
					}

					boolean clicked = false;
					for (Component c : components) {
						if (c.contains(x,y)) {
							if (eventButton == 0) {
								c.onLeftClick(x,y);
							} else if (eventButton == 1) {
								c.onRightClick(x,y);
							}
							clicked = true;
							break;
						}
					}

					if (!clicked && defaultListener != null) {
						if (eventButton == 0) {
							defaultListener.onLeftClick(x,y);
						} else if (eventButton == 1) {
							defaultListener.onRightClick(x,y);
						}
					}
				} else { // mouse released
					boolean released = false;
					for (Component c : components) {
						if (c.contains(x,y)) {
							c.onRelease();
							released = true;
							break;
						}
					}
					if (!released && defaultListener != null) {
						defaultListener.onRelease();
					}
				}
			}
			oldX = x;
			oldY = y;
		}
	}

	public synchronized void updateKeyboard(float deltaTime) {
		Keyboard.poll();
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				for (KeyboardListener listener : keyboardListeners) {
					listener.onKeyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter());
				}
			}
		}
	}


	public synchronized void update(float deltaTime) {
		updateComponents();
		updateListeners();

		updateMouse(deltaTime);
		updateKeyboard(deltaTime);

		for (Component c : components) {
			c.update(deltaTime);
		}
	}

	public synchronized void render(float deltaTime) {
		for (Component c : components) 
			c.render(deltaTime);
	}
}
