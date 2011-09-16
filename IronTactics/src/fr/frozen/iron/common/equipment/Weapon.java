package fr.frozen.iron.common.equipment;

public class Weapon {

	protected String name;
	protected int id;
	protected int damage;
	protected int maxRange;
	protected int minRange;
	protected boolean cutWood;
	protected boolean magical;
	protected int manaCost;
	
	public Weapon(String name, int id, int damage, int maxRange, int minRange, boolean cutWood, boolean magical, int manaCost) {
		this.name = name;
		this.damage = damage;
		this.id = id;
		this.cutWood = cutWood;
		this.maxRange = maxRange;
		this.minRange = minRange;
		this.magical = magical;
		this.manaCost = manaCost;
	}
	
	
	public String toString() {
		return "["+id+"] "+name+"  damage ="+damage+"  range=["+minRange+ ","+maxRange+"]";
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

	public boolean isPhysical() {
		return !isMagical();
	}
	
	
	public boolean isMagical() {
		return magical;
	}


	public void setMagical(boolean magical) {
		this.magical = magical;
	}


	public int getManaCost() {
		return manaCost;
	}


	public void setManaCost(int manaCost) {
		this.manaCost = manaCost;
	}
}
