package fr.frozen.iron.client.components;

public interface MouseListener {
	public void onLeftClick(int x, int y);
	public void onRightClick(int x, int y);
	
	public void onHover(int x, int y); //hover
	public void onExit();
}
