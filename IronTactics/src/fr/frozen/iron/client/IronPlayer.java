package fr.frozen.iron.client;

public class IronPlayer {
	private int Id;
	private String name;
	
	public IronPlayer(int Id, String name) {
		this.Id = Id;
		this.name = name;
	}
	
	public int getId() {
		return Id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setId(int val) {
		Id = val;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "["+Id+ "] "+name;
	}
}
