package fr.frozen.iron.common.equipment;

public class RangedWeapon extends Weapon {

	protected String projectileName="";
	
	public RangedWeapon(String name, int id, int damage, int maxRange,
			int minRange, boolean cutWood, String projectileName, boolean magical, int manaCost) {
		super(name, id, damage, maxRange, minRange, cutWood, magical, manaCost);
		this.projectileName=projectileName;
	}

	public boolean sendsProjectile() {
		return projectileName != null && !projectileName.equals("");
	}
	
	public String getProjectileName() {
		return projectileName;
	}
	
	public void setProjectileName(String name) {
		projectileName = name;
	}
}
