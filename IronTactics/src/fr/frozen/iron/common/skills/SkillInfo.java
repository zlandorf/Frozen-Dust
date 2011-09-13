package fr.frozen.iron.common.skills;

public class SkillInfo {
	
	protected int x, y;
	protected int unitId;
	protected boolean canDo;
	protected Skill skill;
	
	public SkillInfo(Skill skill, int unitId, int x, int y, boolean canDo) {
		this.x = x;
		this.y = y;
		this.unitId = unitId;
		this.canDo = canDo;
		this.skill = skill;
	}
	
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getUnitId() {
		return unitId;
	}
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	public boolean canDo() {
		return canDo;
	}
	public void setCanDo(boolean canDo) {
		this.canDo = canDo;
	}
	public Skill getSkill() {
		return skill;
	}
	public void setSkill(Skill skill) {
		this.skill = skill;
	}
}
