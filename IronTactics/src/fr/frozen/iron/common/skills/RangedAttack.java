package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.Sound;
import fr.frozen.game.SoundManager;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.DamageParticle;
import fr.frozen.iron.common.entities.particles.IntParticle;
import fr.frozen.iron.common.entities.particles.ManaParticle;
import fr.frozen.iron.common.entities.particles.Projectile;
import fr.frozen.iron.common.equipment.RangedWeapon;
import fr.frozen.iron.common.equipment.Weapon;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class RangedAttack extends Skill {

	private static RangedAttack instance = new RangedAttack();
	
	public static RangedAttack getInstance() {
		return instance;
	}
	
	protected RangedAttack() {
		super("Ranged Attack", Skill.RANGED_ATTACK);
	}
	
	protected boolean canDoAux(IronUnit src, IronUnit dst) {
		if (src == null || dst == null) return false;
		
		Weapon rangedWeapon = src.getRangedWeapon();
		if (rangedWeapon == null || !(rangedWeapon instanceof RangedWeapon)) return false;
		if (rangedWeapon.getManaCost() > 0 && src.getStats().getMana() < rangedWeapon.getManaCost()) return false;
		if (src.getId() == dst.getId()) return false;
		if (src.getOwnerId() == dst.getOwnerId()) return false;
		if (dst.isDead()) return false;

		//range check
		float minRange = rangedWeapon.getMinRange();
		float maxRange = rangedWeapon.getMaxRange();
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		
		if (distance < minRange || distance > maxRange) return false;
		
		return true;
	}
	
	@Override
	public void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		super.executeCommon(world, srcId, x, y, values);
		IronUnit src = world.getUnitFromId(srcId);
		src.getStats().setMana(src.getStats().getMana() - src.getRangedWeapon().getManaCost());
	}
	
	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitAtXY(x, y);
		
		if (!canDoAux(src, dst)) return false;
		if (world.isInMelee(srcId)) return false;
		
		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		return IronUtil.checkGrid(world, x1, y1, x2, y2, true);
	}

	

	@Override
	public List<int[]> computeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		IronUnit dst = world.getUnitAtXY(x, y);
		IronUnit src = world.getUnitFromId(srcId);
		
		if (dst == null || src == null || src.getRangedWeapon() == null) return null;
		
		List<int[]> res = new ArrayList<int[]>();
		int damage = IronUtil.getDamage(src, dst, false);
		
		if (!dst.isDead())
			res.add(new int[]{dst.getId(), - (int)damage});
		
		return res;
	}

	@Override
	public void executeClientSide(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		
		if (values == null) return;
		IronUnit src = world.getUnitFromId(srcId);
		if (src == null) return;
		int manaBefore = src.getStats().getMana();
		executeCommon(world, srcId, x, y, values);
		
		IronUnit target;
		IntParticle damage = null;
		
		List<Sound> impactSounds = new ArrayList<Sound>();
		
		for (int [] couple : values) {
			target = world.getUnitFromId(couple[0]);
			if (target == null) {
				continue;
			}
			if (target.isDead()) {
				target.setCorpseSprite();
				Sound deathSound = SoundManager.getInstance().getSound(target.getRaceStr()+"_death");
				if (deathSound != null) {
					impactSounds.add(deathSound);
				}
			}
			damage = new DamageParticle(world, target.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2,
											   target.getY() * IronConst.TILE_WIDTH,
											   couple[1]);
			
			if (couple[1] < 0) {
				Sound impactSound = null;
				if (couple[1] <= -100) {
					impactSound = SoundManager.getInstance().getSound("strong_hit");
				} else {
					float armorValue = 0;
					if (target.getArmor() != null) {
						armorValue += target.getArmor().getPhysicalArmor();
					} 
					if (target.getShield() != null) {
						armorValue += target.getShield().getPhysicalArmor();
					}

					if (armorValue >= 50) {
						impactSound = SoundManager.getInstance().getSound("armor_hit");
					} else {
						impactSound = SoundManager.getInstance().getSound("medium_hit");
					}
				}
				if (impactSound != null) {
					impactSounds.add(impactSound);
				}
			}
		}
		
		int manaAfter = src.getStats().getMana();
		if (src.getStats().getMaxMana() > 0) {
			int manaCost = manaAfter - manaBefore;
			
			if (manaCost != 0) {
				ManaParticle mp = new ManaParticle(world, src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2,
						src.getY() * IronConst.TILE_WIDTH,
						manaCost);
				world.addGameObject(mp, "gfx");
			}
		}
		
		IronUnit dst = world.getUnitFromId(values.get(0)[0]);
		if (!(src.getRangedWeapon() instanceof RangedWeapon)) {
			return;
		}
		RangedWeapon weapon = (RangedWeapon)src.getRangedWeapon();
		if (weapon == null || !weapon.sendsProjectile()) return;
		if (!SpriteManager.getInstance().isSpriteLoaded(weapon.getProjectileName())) return;


		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;

		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;

		Projectile arrow = new Projectile(world, x1 - IronConst.TILE_WIDTH / 2,
				y1 - IronConst.TILE_HEIGHT / 2,
				new Vector2f(x2 - x1, y2 - y1), weapon.getProjectileName(), damage, impactSounds);
		world.addGameObject(arrow, "gfx");
		
		if (weapon.getProjectileName().equals("arrow")) {
			SoundManager.getInstance().getSound("arrow").playAsSoundEffect(false);
		}
	}
}
