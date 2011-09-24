package fr.frozen.iron.common.skills;

import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.game.Sound;
import fr.frozen.game.SoundManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.DamageParticle;
import fr.frozen.iron.common.entities.particles.IntParticle;
import fr.frozen.iron.common.entities.particles.ManaParticle;
import fr.frozen.iron.util.IronConst;

public abstract class Skill {
	
	public static final int MELEE_ATTACK = 0;
	public static final int RANGED_ATTACK = 1;
	public static final int HEAL = 2;
	public static final int BLIND_SHOT = 3;
	public static final int SHIELD_BLOCK = 4;
	public static final int CHARGE = 5;
	
	protected SoundManager soundManager;
	
	public static Skill getSkill(int type) {
		switch (type) {
		case MELEE_ATTACK : 
			return MeleeAttack.getInstance();
		case RANGED_ATTACK :
			return RangedAttack.getInstance();
		case HEAL :
			return Heal.getInstance();
		case BLIND_SHOT :
			return BlindShot.getInstance();
		case SHIELD_BLOCK :
			return ShieldBlock.getInstance();
		case CHARGE :
			return Charge.getInstance();
		default :
			Logger.getLogger(Skill.class).error("Skill not found");
		}
		
		return null;
	}
	
	protected String name;
	protected int type;
	
	protected Skill(String name, int type) {
		this.name = name;
		this.type = type;
		soundManager = SoundManager.getInstance();
	}
	
	public abstract boolean canDo(IronWorld world, int srcId, int x, int y);
	public abstract List<int[]> executeSkill(IronWorld world, int srcId, int x, int y);//returns list of couple [unitId, value]
	//value being damage or heal or whatever

	public String getSkillName() {
		return name;
	}

	public int getSkillType() {
		return type;
	}
	
	protected void executeCommon(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {

		IronUnit src = world.getUnitFromId(srcId);
		IronUnit target; 
		
		if (src == null) {
			return;
		}
		
		src.setPlayed(true);

		for (int [] couple : values) {
			target = world.getUnitFromId(couple[0]);
			if (target == null || target.isDead()) continue;
			target.setHp(target.getHp() + couple[1]);
		}
	}
	
	public void executeClientSide(IronWorld world, int srcId, int x, int y,
			List<int[]> values) {
		if (values == null) return;
		IronUnit src = world.getUnitFromId(srcId);
		if (src == null) return;
		int manaBefore = src.getStats().getMana();
		
		executeCommon(world, srcId, x, y, values);
		
		IronUnit target;
		IntParticle damage;
		
		for (int [] couple : values) {
			target = world.getUnitFromId(couple[0]);
			if (target == null) {
				continue;
			}
			if (target.isDead()) {
				target.setCorpseSprite();
				Sound deathSound = soundManager.getSound(target.getRaceStr()+"_death");
				if (deathSound != null) {
					deathSound.playAsSoundEffect(false);
				}
				
			}
			damage = new DamageParticle(world, target.getX() * IronConst.TILE_WIDTH,
											   target.getY() * IronConst.TILE_WIDTH,
											   couple[1]);
			
			world.addGameObject(damage, "gfx");
			
			if (couple[1] < 0) {
				if (couple[1] > 100) {
					SoundManager.getInstance().getSound("strong_hit").playAsSoundEffect(false);
				} else {
					float armorValue = 0;
					if (target.getArmor() != null) {
						armorValue += target.getArmor().getPhysicalArmor();
					} 
					if (target.getShield() != null) {
						armorValue += target.getShield().getPhysicalArmor();
					}

					if (armorValue >= 50) {
						SoundManager.getInstance().getSound("armor_hit").playAsSoundEffect(false);
					} else {
						SoundManager.getInstance().getSound("medium_hit").playAsSoundEffect(false);
					}
				}
			}
		}
		
		int manaAfter = src.getStats().getMana();
		if (src.getStats().getMaxMana() > 0) {
			int manaCost = manaAfter - manaBefore;
			
			if (manaCost != 0) {
				ManaParticle mp = new ManaParticle(world, src.getX() * IronConst.TILE_WIDTH,
						src.getY() * IronConst.TILE_WIDTH,
						manaCost);
				world.addGameObject(mp, "gfx");
			}
		}
	}
}
