package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.HealAnimation;
import fr.frozen.iron.common.equipment.Weapon;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class Heal extends Skill {

	private static Heal instance = new Heal();
	
	public static Heal getInstance() {
		return instance;
	}
	
	protected Heal() {
		super("Heal", Skill.HEAL);
	}
	
	protected boolean canDoAux(IronUnit src, IronUnit dst) {
		if (src == null || dst == null) return false;
		
		Weapon rangedWeapon = src.getRangedWeapon();
		
		if (src.getId() == dst.getId()) return false;
		if (src.getOwnerId() != dst.getOwnerId()) return false;//done on friend unit
		if (dst.isDead()) return false;
		if (rangedWeapon == null) return false;
		if (!rangedWeapon.isMagical()) return false;
		if (rangedWeapon.getManaCost() > 0 && src.getStats().getMana() < rangedWeapon.getManaCost()) return false;
		//range check
		float minRange = rangedWeapon.getMinRange();
		float maxRange = rangedWeapon.getMaxRange();
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		
		if (distance < minRange || distance > maxRange) return false;
		return true;
	}
	
	
	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitAtXY(x, y);
		
		if (!canDoAux(src, dst)) return false;
		
		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		return IronUtil.checkGrid(world, x1, y1, x2, y2, true);
	}

	@Override
	public void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		super.executeCommon(world, srcId, x, y, values);
		IronUnit src = world.getUnitFromId(srcId);
		src.getStats().setMana(src.getStats().getMana() - src.getRangedWeapon().getManaCost());
	}

	@Override
	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		IronUnit dst = world.getUnitAtXY(x, y);
		IronUnit src = world.getUnitFromId(srcId);
		
		if (dst == null || src == null || src.getRangedWeapon() == null) return null;
		
		List<int[]> res = new ArrayList<int[]>();
		float valToHeal = src.getRangedWeapon().getDamage();
		
		valToHeal += valToHeal * (float)(src.getStats().getIntelligence() / 10.0);
		float movePenalty = (float)src.getMovement() / src.getMaxMovement();
		valToHeal *= movePenalty;
		
		res.add(new int[]{dst.getId(), (int)valToHeal});
		
		executeCommon(world, srcId, x, y, res);
		return res;
	}

	@Override
	public void executeClientSide(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		
		super.executeClientSide(world, srcId, x, y, values);
		if (values.size() <= 0) return;
		
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitFromId(values.get(0)[0]);
		if (dst == null || src == null) return;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT;
		
		HealAnimation heal = new HealAnimation(world, x2, y2);
		world.addGameObject(heal, "gfx");
	}
}
