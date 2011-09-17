package fr.frozen.iron.common.equipment;


public class UnitEquipment {
	protected Weapon meleeWeapon = null;
	protected Weapon rangedWeapon = null;
	protected Armor shield = null;
	protected Armor armor = null;
	
	public UnitEquipment() {
	}
	
	public UnitEquipment(Weapon meleeWeapon, Weapon rangedWeapon, Armor shield,
			Armor armor) {
		this.meleeWeapon = meleeWeapon;
		this.rangedWeapon = rangedWeapon;
		this.shield = shield;
		this.armor = armor;
	}

	public void reInit() {
		if (meleeWeapon != null) meleeWeapon.reInit();
		if (rangedWeapon != null) rangedWeapon.reInit();
		if (shield != null) shield.reInit();
		if (armor != null) armor.reInit();
	}
	
	public Weapon getMeleeWeapon() {
		return meleeWeapon;
	}

	public void setMeleeWeapon(Weapon meleeWeapon) {
		this.meleeWeapon = meleeWeapon;
	}

	public Weapon getRangedWeapon() {
		return rangedWeapon;
	}

	public void setRangedWeapon(Weapon rangedWeapon) {
		this.rangedWeapon = rangedWeapon;
	}

	public Armor getShield() {
		return shield;
	}

	public void setShield(Armor shield) {
		this.shield = shield;
	}

	public Armor getArmor() {
		return armor;
	}

	public void setArmor(Armor armor) {
		this.armor = armor;
	}
}
