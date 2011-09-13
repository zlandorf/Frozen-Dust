package fr.frozen.iron.common.weapon;

public class RangedWeapon extends Weapon {

	protected String projectileName="";
	
	public RangedWeapon(String name, int id, int damage, int maxRange,
			int minRange, boolean cutWood, boolean displayIdle, String projectileName) {
		super(name, id, damage, maxRange, minRange, cutWood, displayIdle);
		this.projectileName=projectileName;
	}

	public String getProjectileName() {
		return projectileName;
	}
	
	public void setProjectileName(String name) {
		projectileName = name;
	}
}
