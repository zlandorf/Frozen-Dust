package fr.frozen.iron.common.equipment;

public class Armor implements Cloneable {
	
	protected int id;
	protected String name;
	
	protected float physicalArmor = 0;
	protected float magicalArmor = 0;
	
	protected float basePhysicalArmor = 0;
	protected float baseMagicalArmor = 0;

	public Armor(int id, String name, float physicalArmor, float magicalArmor) {
		this.id = id;
		this.name = name;
		this.basePhysicalArmor = this.physicalArmor = physicalArmor;
		this.baseMagicalArmor = this.magicalArmor = magicalArmor;
	}
	
	public int getId() {
		return id;
	}
	
	public void reInit() {
		physicalArmor = basePhysicalArmor;
		magicalArmor = baseMagicalArmor;
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getPhysicalArmor() {
		return physicalArmor;
	}
	
	public void setPhysicalArmor(float physicalArmor) {
		this.physicalArmor = physicalArmor;
	}
	
	public float getMagicalArmor() {
		return magicalArmor;
	}
	
	public void setMagicalArmor(float magicalArmor) {
		this.magicalArmor = magicalArmor;
	}
	
	public String toString() {
		return "["+id+"]"+name;
	}
}
