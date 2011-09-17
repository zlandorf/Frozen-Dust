package fr.frozen.iron.common.equipment;

public class Weapon implements Cloneable {

	protected String name;
	protected int id;
	protected int damage;
	protected int maxRange;
	protected int minRange;
	protected boolean cutWood;
	protected boolean magical;
	protected int manaCost;
	
	protected int baseDamage;
	protected int baseMaxRange;
	protected int baseMinRange;
	protected boolean baseCutWood;
	protected boolean baseMagical;
	protected int baseManaCost;
	
	public Weapon(String name, int id, int damage, int maxRange, int minRange, boolean cutWood, boolean magical, int manaCost) {
		this.name = name;
		this.id = id;

		this.baseDamage = this.damage = damage;
		this.baseCutWood = this.cutWood = cutWood;
		this.baseMaxRange = this.maxRange = maxRange;
		this.baseMinRange = this.minRange = minRange;
		this.baseMagical = this.magical = magical;
		this.baseManaCost = this.manaCost = manaCost;
	}
	
	public String toString() {
		return "["+id+"] "+name+"  damage ="+damage+"  range=["+minRange+ ","+maxRange+"]";
	}
	
	public int getId() {
		return id;
	}

	public void reInit() {
		damage = baseDamage; 
		cutWood = baseCutWood;
		maxRange = baseMaxRange;
		minRange = baseMinRange;
		magical = baseMagical;
		manaCost = baseManaCost;
	}
	
	@Override
	public Object clone() {
		try {
			Object o = super.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			return null;
		}
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
