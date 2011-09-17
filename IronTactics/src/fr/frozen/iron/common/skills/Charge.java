package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.equipment.Weapon;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class Charge extends Skill {
	private static Charge instance = new Charge();
	
	public static Charge getInstance() {
		return instance;
	}
	
	protected Charge() {
		super("Charge", Skill.CHARGE);
	}

	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitAtXY(x, y);
		
		if (src == null || src.isDead() || src.hasPlayed()) return false;
		if (dst == null || dst.isDead()) return false;
		if (src.getId() == dst.getId()) return false;
		if (src.getOwnerId() == dst.getOwnerId()) return false;
		
		Weapon weapon = src.getMeleeWeapon();
		if (weapon == null) return false;
		
		if (dst.getX() != src.getX() && dst.getY() != src.getY()) return false;
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		if (distance <= 2 || distance  > 1 + src.getMovement() / IronConst.MOVE_COST_DEFAULT) return false;
		
		
		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		return IronUtil.checkGrid(world, x1, y1, x2, y2, false);
	}

	@Override
	public void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		super.executeCommon(world, srcId, x, y, values);
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitAtXY(x, y);
		if (src == null) return;
		
		src.getStats().setMana(src.getStats().getMana() - src.getMeleeWeapon().getManaCost());
		
		if (dst == null) return;
		
		int xmove, ymove;
		if (src.getX() == dst.getX()) {
			xmove = (int) src.getX();
			if (src.getY() < dst.getY()) {
				ymove = (int) (dst.getY() - 1);
			} else {
				ymove = (int) (dst.getY() + 1);
			}
		} else {
			ymove = (int) src.getY();
			if (src.getX() < dst.getX()) {
				xmove = (int) (dst.getX() - 1);
			} else {
				xmove = (int) (dst.getX() + 1);
			}
		}
		src.move(xmove, ymove, 0);
	}
	
	@Override
	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		List <int[]> res = new ArrayList<int[]>();
		IronUnit dst = world.getUnitAtXY(x, y);
		IronUnit src = world.getUnitFromId(srcId);
		
		if (dst == null || src == null) return null;
		
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		float damage = IronUtil.getDamage(src, dst, true);
		damage += damage * (0.025 + distance / 15.0);
		res.add(new int[]{dst.getId(), - (int)damage});
		
		executeCommon(world, srcId, x, y, res);
		
		return res;
	}
}
