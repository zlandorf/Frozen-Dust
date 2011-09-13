package fr.frozen.iron.client.components;


public class ActionEvent {
	protected Component source;
	
	public ActionEvent(Component source) {
		this.source = source;
	}
	
	public Component getSource() {
		return source;
	}
}
