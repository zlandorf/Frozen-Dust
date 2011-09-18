package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.equipment.Weapon;
import fr.frozen.iron.util.IronUtil;

public class MeleeAttack extends Skill {

	private static MeleeAttack instance = new MeleeAttack();
	
	public static MeleeAttack getInstance() {
		return instance;
	}
	
	private MeleeAttack() {
		super("Melee Attack",Skill.MELEE_ATTACK);
	}
	
	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		Weapon meleeWeapon = src.getMeleeWeapon();
		if (meleeWeapon == null) return false;//cant perform melee attack because, doesn't have a melee weapon
		
		float maxRange = meleeWeapon.getMaxRange();
		float minRange = meleeWeapon.getMinRange();
		
		IronUnit dst = world.getUnitAtXY(x, y);
		if (dst == null || dst.getId() == src.getId()) return false;
		if (dst.getOwnerId() == src.getOwnerId()) return false;
		if (dst.isDead()) return false;
		/*if (!((x >= 0 && x < IronConfig.MAP_WIDTH && y == (int)src.getPos().getY() && Math.abs(x - src.getPos().getX()) <= range) 
		  || (y >= 0 && y < IronConfig.MAP_HEIGHT && x == (int)src.getPos().getX() && Math.abs(y - src.getPos().getY()) <= range))) {
			return false;
		}*/
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		if (distance > maxRange || distance < minRange) return false;
		
		return true;
	}

	@Override
	public void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		super.executeCommon(world, srcId, x, y, values);
		IronUnit src = world.getUnitFromId(srcId);
		src.getStats().setMana(src.getStats().getMana() - src.getMeleeWeapon().getManaCost());
	}
	
	@Override
	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		List <int[]> res = new ArrayList<int[]>();
		IronUnit dst = world.getUnitAtXY(x, y);
		IronUnit src = world.getUnitFromId(srcId);
		
		if (dst == null || src == null) return null;
		
		int damage = IronUtil.getDamage(src, dst, true);
		res.add(new int[]{dst.getId(), - damage});
		
		executeCommon(world, srcId, x, y, res);
		
		return res;
	}
}
