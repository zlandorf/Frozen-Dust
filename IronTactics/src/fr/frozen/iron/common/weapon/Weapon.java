package fr.frozen.iron.common.weapon;

public class Weapon {

	protected String name;
	protected int id;
	protected int damage;
	protected int maxRange;
	protected int minRange;
	protected boolean cutWood;
	protected boolean displayIdle;
	
	public Weapon(String name, int id, int damage, int maxRange, int minRange, boolean cutWood, boolean displayIdle) {
		this.name = name;
		this.damage = damage;
		this.id = id;
		this.cutWood = cutWood;
		this.displayIdle = displayIdle;
		this.maxRange = maxRange;
		this.minRange = minRange;
	}
	
	
	public String toString() {
		return "["+id+"] "+name+"  damage ="+damage+"  range=["+minRange+ ","+maxRange+"] displayIde ? "+displayIdle;
	}
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}

	
	public int getMaxRange() {
		return maxRange;
	}


	public void setMaxRange(int range) {
		this.maxRange = range;
	}
	
	public int getMinRange() {
		return minRange;
	}


	public void setMinRange(int range) {
		this.minRange = range;
	}

	public int getDamage() {
		return damage;
	}


	public void setDamage(int damage) {
		this.damage = damage;
	}


	public boolean isCutWood() {
		return cutWood;
	}


	public void setCutWood(boolean cutWood) {
		this.cutWood = cutWood;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}


	public boolean isDisplayIdle() {
		return displayIdle;
	}


	public void setDisplayIdle(boolean displayIdle) {
		this.displayIdle = displayIdle;
	}
}
