package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.ShieldBlockAnimation;
import fr.frozen.iron.common.equipment.Armor;
import fr.frozen.iron.util.IronConst;

public class ShieldBlock extends Skill {

	private static ShieldBlock instance = new ShieldBlock();
	
	public static ShieldBlock getInstance() {
		return instance;
	}
	
	protected ShieldBlock() {
		super("Shield Block", Skill.SHIELD_BLOCK);
	}

	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		
		if (src == null || src.getShield() == null || src.isDead()) return false;
		if (src.getMovement() < 2 * IronConst.MOVE_COST_DEFAULT || src.hasPlayed()) return false;
		return true;
	}

	@Override
	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		executeCommon(world, srcId, x, y, null);
		List<int[]> res = new ArrayList<int[]>();
		res.add(new int[]{-2,0});
		return res;
	}

	@Override
	public void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		IronUnit src = world.getUnitFromId(srcId);
		if (src == null) return;
		Armor shield = src.getShield();
		if (shield == null) return;
		
		shield.setPhysicalArmor(shield.getPhysicalArmor() * 2);
		shield.setMagicalArmor(shield.getMagicalArmor() * 2);
		src.setPlayed(true);
	}
	
	@Override
	public void executeClientSide(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		executeCommon(world, srcId, x, y, null);
		IronUnit src = world.getUnitFromId(srcId);
		if (src == null) return;
		
		float xsrc = src.getX() * IronConst.TILE_WIDTH;
		float ysrc = src.getY() * IronConst.TILE_HEIGHT;
		
		ShieldBlockAnimation sba = new ShieldBlockAnimation(world, xsrc, ysrc);
		world.addGameObject(sba, "gfx");
	}
}
