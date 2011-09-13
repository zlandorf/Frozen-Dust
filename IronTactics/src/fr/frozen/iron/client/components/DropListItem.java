package fr.frozen.iron.client.components;

public class DropListItem {
	protected String label;
	protected int value;
	protected boolean selected = false;

	public DropListItem(String label, int value) {
		super();
		this.label = label;
		this.value = value;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void setSelected(boolean value) {
		this.selected = value;
	}
	
	public boolean isSelected() {
		return selected;
	}
}
