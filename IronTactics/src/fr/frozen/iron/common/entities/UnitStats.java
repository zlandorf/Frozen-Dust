package fr.frozen.iron.common.entities;

public class UnitStats {
	protected int maxHp = 0;
	protected int hp = 0;
	
	protected int maxMana = -1;
	protected int mana = -1;
	
	protected int strength = 0;
	protected int agility = 0;
	protected int intelligence = 0;
	
	public UnitStats() {
	}
	
	public UnitStats(int maxHp, int maxMana, int strength, int agility, int intelligence) {
		this.maxHp = this.hp = maxHp;
		this.strength = strength;
		this.agility = agility;
		this.intelligence = intelligence;
		this.maxMana = this.mana = maxMana;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int val) {
		this.hp = Math.max(0, Math.min(val, this.maxHp));
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getAgility() {
		return agility;
	}

	public void setAgility(int agility) {
		this.agility = agility;
	}

	public int getIntelligence() {
		return intelligence;
	}

	public void setIntelligence(int intelligence) {
		this.intelligence = intelligence;
	}

	public int getMaxMana() {
		return maxMana;
	}

	public void setMaxMana(int maxMana) {
		this.maxMana = maxMana;
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int val) {
		if (maxMana >= 0)
			this.mana = Math.max(0, Math.min(val, this.maxMana));
	}
}
