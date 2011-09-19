package fr.frozen.iron.common.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.AnimationSequence;
import fr.frozen.game.Font;
import fr.frozen.game.FontManager;
import fr.frozen.game.GameObject;
import fr.frozen.game.ISprite;
import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.particles.ManaParticle;
import fr.frozen.iron.common.equipment.Armor;
import fr.frozen.iron.common.equipment.EquipmentManager;
import fr.frozen.iron.common.equipment.RangedWeapon;
import fr.frozen.iron.common.equipment.UnitEquipment;
import fr.frozen.iron.common.equipment.Weapon;
import fr.frozen.iron.common.skills.MeleeAttack;
import fr.frozen.iron.common.skills.RangedAttack;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronGL;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.util.XMLParser;
import fr.frozen.util.pathfinding.Mover;

public class IronUnit extends GameObject implements Mover {
	/*serializes as :
	 * id - type - ownerId - float x - float y
	 */
	public static final int MANA_COLOR = 0x2ea4ff;
	
	public static final int ACTION_MOVE = 0;
	public static final int ACTION_SKILL = 1;
	
	public static final int TYPE_FOOTSOLDIER = 0;
	public static final int TYPE_ARCHER = 1;
	public static final int TYPE_CAVALRY = 2;
	public static final int TYPE_HEALER = 3;
	public static final int TYPE_MAGE = 4;
	public static final int TYPE_SPECIAL = 5;
	
	public static String[] typeNames = {"footsoldier","archer","cavalry","healer","mage", "special"};
	
	protected List<int []> movementCache;
	protected int [] movementCachePos;
	
	protected IronWorld world;
	
	protected int id;
	protected int type;
	protected int ownerId;
	protected String name;
	
	protected UnitStats stats;
	protected UnitEquipment equipment;
	
	protected boolean selected = false;
	protected boolean played = true;
	protected boolean playedLastTurn = false;
	
	protected List<Skill> skills;

	protected AnimationSequence animation = null;
	protected ISprite teamColorSprite = null;
	protected ISprite weaponSprite = null;
	protected AnimationSequence bloodFx;
	
	protected boolean canUndo = false;
	protected Vector2f oldPos;
	
	public IronUnit(IronWorld world, int id, int type, int ownerId, float x, float y) {
		super(null, x, y);
		this.id = id;
		this.type = type;
		this.ownerId = ownerId;
		this.world = world;
		
		stats = new UnitStats();
		equipment = new UnitEquipment();
		
		skills = new ArrayList<Skill>();
		addInitialSkills();
		
		movementCachePos = new int[2];
		movementCachePos[0] = movementCachePos[1] = -1;
		movementCache = new ArrayList<int[]>();
		
		if (world != null) findStatsFromXml();
	}
	
	public static IronUnit getUnit(char letter, IronWorld world, int entId, int clientId, int x, int y) {
		switch (letter) {
		case 'F' :
			return getUnit(TYPE_FOOTSOLDIER, world, entId, clientId, x, y);
		case 'A' :
			return getUnit(TYPE_ARCHER, world, entId, clientId, x, y);
		case 'H' :
			return getUnit(TYPE_HEALER, world, entId, clientId, x, y);
		case 'C' :
			return getUnit(TYPE_CAVALRY, world, entId, clientId, x, y);
		case 'M' :
			return getUnit(TYPE_MAGE, world, entId, clientId, x, y);
		case 'S' :
			return getUnit(TYPE_SPECIAL, world, entId, clientId, x, y);
		}
		return null;
	}
	
	public static IronUnit getUnit(int type, IronWorld world, int entId, int clientId, int x, int y) {
		switch (type) {
		case TYPE_FOOTSOLDIER :
			return new FootSoldier(world, entId, clientId, x, y);
			
		case TYPE_ARCHER :
			return new Archer(world, entId, clientId, x, y);
			
		case TYPE_HEALER :
			return new Healer(world, entId, clientId, x, y);
			
		case TYPE_CAVALRY :
			return new Cavalry(world, entId, clientId, x, y);
			
		case TYPE_MAGE :
			return new Mage(world, entId, clientId, x, y);
			
		case TYPE_SPECIAL :
			return new Special(world, entId, clientId, x, y);
		}
		return null;
	}

	//TODO : implement in subclasses
	protected void addInitialSkills() {
	}
	
	public void onEndTurn() {
		if (played || hasMoved())
			playedLastTurn = true;
		played = true;
		canUndo = false;
	}
	
	public boolean canUndo() {
		return canUndo;
	}
	
	public UnitStats getStats() {
		return stats;
	}
	
	public UnitEquipment getEquipment() {
		return equipment;
	}
	
	public int getHp() {
		return stats.getHp();
	}
	
	public int getMaxHp() {
		return stats.getMaxHp();
	}

	public void setMaxHp(int val) {
		stats.setMaxHp(val);
	}
	
	public void setHp(int val) {
		stats.setHp(val);
		if (stats.getHp() <= 0) {
			onDeath();
		}
	}
	
	public boolean hasMoved() {
		return getStats().getMovement() < getStats().getMaxMovement();
	}
	
	public int getMaxMovement() {
		return getStats().getMaxMovement();
	}
	
	public void onDeath() {
		world.getMap().getTile((int)getX(), (int)getY()).setUnitOnTile(null);
		world.addGameObject(this, "corpse");
	}
	
	public void setCorpseSprite() {
		setSprite(_spriteManager.getSprite("corpse_"+getRaceStr()));
		_sprite.setAlpha(.8f);
	}
	
	public void onStartTurn(boolean addParticle) {

		int manaBefore = getStats().getMana();
		if (getStats().getMaxMana() >= 0 && !isDead()) {
			getStats().setMana((int)(manaBefore + getStats().intelligence * IronConst.INTELLIGENCE_REGEN_FACTOR));
			
			if (!playedLastTurn)
				getStats().setMana((int)(getStats().getMana() + getStats().intelligence * IronConst.INTELLIGENCE_IDLE_REGEN_FACTOR));
			
			int manaAfter = getStats().getMana();
			if (manaAfter - manaBefore != 0 && addParticle) {
				GameObject manaParticle = new ManaParticle(getWorld(), 
														   getX() * IronConst.TILE_WIDTH, 
														   getY() * IronConst.TILE_HEIGHT, manaAfter - manaBefore);
				world.addGameObject(manaParticle, "gfx");
			}
		}
		played = isDead();
		playedLastTurn = false;
		canUndo = false;
		getStats().reInit();
		getEquipment().reInit();
	}
	
	public List<Skill> getSkills() {
		synchronized (skills) {
			return skills;
		}
	}
	
	public void addSkill(Skill s) {
		synchronized (skills) {
			skills.add(0, s);
		}
	}
	
	public void removeSkill(Skill skill) {
		synchronized (skills) {
			skills.remove(skill);
		}
	}
	
	public void move(int x, int y, int cost) {
		oldPos = new Vector2f(_pos.getX(), _pos.getY());
		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(null);
		_pos.set(x, y);
		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(this);
		
		getStats().setMovement(getStats().getMovement() - cost);
		
		if (getStats().getMovement() == 0) {
			setPlayed(true);
		}
		canUndo = true;
	}
	
	public void undoMove() {
		setPlayed(false);
		getStats().setMovement(getStats().getOldMovement());
		canUndo = false;
		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(null);
		_pos.set(oldPos.getX(), oldPos.getY());
		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(this);
	}
	
	@Override
	public void update(float deltaTime) {
		if (animation != null && !isDead()) {
			animation.update(deltaTime);
			_sprite = animation.getCurrentSprite();
		}
	}
	
	public void findSprite() {
		String spriteName = typeNames[type]+"_"+getRaceStr();
		if (_spriteManager.isSpriteLoaded(spriteName)) {
			setSprite(ISpriteManager.getInstance().getSprite(spriteName));
		} else {
			animation = _spriteManager.getAnimationSequence(spriteName);
			if (animation != null) {
				animation.start();
				setSprite(animation.getCurrentSprite());
			}
		}
		
		String teamColorSpriteName = typeNames[type]+"_"+getRaceStr()+"_color";
		if (_spriteManager.isSpriteLoaded(teamColorSpriteName)) {
			teamColorSprite = _spriteManager.getSprite(teamColorSpriteName);
		} else {
			teamColorSprite = _spriteManager.getSprite(typeNames[type]+"_color");
		}
		
		if (teamColorSprite != null) {
			teamColorSprite.setColor(world.getContext().getPlayerInfo(ownerId).getColor());
		} 
		
		String bloodFxName = "bloodfx_"+getRaceStr();
		if (_spriteManager.isAnimationLoaded(bloodFxName)) {
			bloodFx = _spriteManager.getAnimationSequence(bloodFxName);
		} else {
			bloodFx = null;
		}
		
		/*if (weapon != null) {
			//weaponSprite = ISpriteManager.getInstance().getSprite("weapon_"+weapon.getName());
		} else {
			System.out.println("WEAPON NULL, NOT GETTING ANY SPRITE");
		}*/
	}
	
	public int getMovement() {
		return getStats().getMovement();
	}
	
	protected String getRaceStr() {
		return IronUtil.getRaceStr(world.getContext().getPlayerInfo(ownerId).getRace());
	}
	
	public void findStatsFromXml() {
		//TODO throw exception if problem ?!
		XMLParser ic = IronConfig.getIronXMLParser();
		String racestr = getRaceStr();
		String base = "unitstats/"+typeNames[type]+"/"+racestr;
		
		if (racestr == null) {
			System.err.println("PROBLEM WHILE GETTING UNIT STATS");
			return;
		}
		
		name = ic.getAttributeValue(base, "name");
		
		int maxHp = Integer.parseInt(ic.getAttributeValue(base, "maxhp"));
		int strength = Integer.parseInt(ic.getAttributeValue(base, "strength"));
		int agility = Integer.parseInt(ic.getAttributeValue(base, "agility"));
		int intelligence = Integer.parseInt(ic.getAttributeValue(base, "intelligence"));
		int maxMana = Integer.parseInt(ic.getAttributeValue(base, "maxmana"));
		int maxMovement = IronConst.MOVE_COST_DEFAULT * Integer.parseInt(ic.getAttributeValue(base, "movement"));
		stats = new UnitStats(maxHp, maxMana, strength, agility, intelligence, maxMovement);

		if (equipment.getMeleeWeapon() == null) {
			String weaponName = ic.getAttributeValue(base, "meleeweapon");
			if (weaponName != null && !weaponName.equals(""))
				setMeleeWeapon(EquipmentManager.getInstance().getWeapon(weaponName));
		}
		
		if (equipment.getRangedWeapon() == null) {
			String weaponName = ic.getAttributeValue(base, "rangedweapon");
			if (weaponName != null && !weaponName.equals("")) {
				setRangedWeapon(EquipmentManager.getInstance().getWeapon(weaponName));
			}
		}
		
		if (equipment.getShield() == null) {
			String shieldName = ic.getAttributeValue(base, "shield");
			if (shieldName != null && !shieldName.equals("")) {
				setShield(EquipmentManager.getInstance().getShield(shieldName));
			}
		}
		
		if (equipment.getArmor() == null) {
			String armorName = ic.getAttributeValue(base, "armor");
			if (armorName != null && !armorName.equals("")) {
				setArmor(EquipmentManager.getInstance().getArmor(armorName));
			}
		}
	}

	public boolean hasPlayed() {
		return played;
	}
	
	public boolean isDead() {
		return stats.getHp() <= 0;
	}
	
	public void setPlayed(boolean val) {
		played = val;
		canUndo = false;
	}
	
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean val) {
		selected = val;
	}
	
	public IronWorld getWorld() {
		return world;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int val) {
		ownerId = val;
	}
	
	public void setWorld(IronWorld world) {
		this.world = world;
		if (world != null) findStatsFromXml();
	}

	public int getId() {
		return id;
	}
	
	public Armor getShield() {
		return getEquipment().getShield();
	}
	
	public Armor getArmor() {
		return getEquipment().getArmor();
	}
	
	public void setShield(Armor shield) {
		getEquipment().setShield(shield);
	}
	
	public void setArmor(Armor armor) {
		getEquipment().setArmor(armor);
	}
	
	public Weapon getMeleeWeapon() {
		return getEquipment().getMeleeWeapon();
	}
	
	public Weapon getRangedWeapon() {
		return getEquipment().getRangedWeapon();
	}

	public void setMeleeWeapon(Weapon w) {
		getEquipment().setMeleeWeapon(w);
		setWeaponAux(w, MeleeAttack.getInstance());
	}
	
	protected void setWeaponAux(Weapon w, Skill skill) {
		if (w != null){
			if (!skills.contains(skill)) {
				addSkill(skill);
			} else {
				System.out.println("already contains skill "+skill);
			}
		} else {
			if (skills.contains(skill)) {
				removeSkill(skill);
			}
		}
	}
	
	public void setRangedWeapon(Weapon w) {
		getEquipment().setRangedWeapon(w);
		if (w instanceof RangedWeapon) {
			setWeaponAux(w, RangedAttack.getInstance());
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public byte [] serialize() throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		byteArray.reset();
		
		byteArray.write(IronUtil.intToByteArray(id));
		byteArray.write(IronUtil.intToByteArray(type));
		byteArray.write(IronUtil.intToByteArray(ownerId));
		
		byteArray.write(IronUtil.intToByteArray((int)_pos.x));//supposed to be float, but we will use as int
		byteArray.write(IronUtil.intToByteArray((int)_pos.y));
		
		int meleeWeaponId = -1;
		if (getMeleeWeapon() != null) {
			meleeWeaponId = getMeleeWeapon().getId();
		}
		byteArray.write(IronUtil.intToByteArray(meleeWeaponId));
		
		int rangedWeaponId = -1;
		if (getRangedWeapon() != null) {
			rangedWeaponId = getRangedWeapon().getId();
		}
		byteArray.write(IronUtil.intToByteArray(rangedWeaponId));
		
		int shieldId = -1;
		if (getShield() != null) {
			shieldId = getShield().getId();
		}
		byteArray.write(IronUtil.intToByteArray(shieldId));
		
		int armorId = -1;
		if (getArmor() != null) {
			armorId = getArmor().getId();
		}
		byteArray.write(IronUtil.intToByteArray(armorId));
		
		return byteArray.toByteArray();
	}
	
	public int getBloodState() {
		if (isDead() || bloodFx == null) return 0;
		
		float hp = getHp();
		float maxHp = getMaxHp();
		
		float perc = hp / maxHp;
		int nbStates = bloodFx.getFrames().size() + 1;// i consider 0 is the state where there is no blood
		float factor = 1.0f / nbStates;
		
		
		for (float i = 1 - factor, j = 0; i >= 0; i -= factor, j++) {
			if (perc >= i) {
				return (int)j;
			}
		}
		return nbStates - 1;
	}

	
	
	@Override
	public void render(float deltaTime) {
		if (_sprite != null) {
			float x = _pos.x * IronConst.TILE_WIDTH;
			float y = _pos.y * IronConst.TILE_HEIGHT;
			
			_sprite.draw(x, y);
			
			if (teamColorSprite != null && !isDead()) {
				teamColorSprite.draw(x, y);
			}
			
			if (!isDead() && bloodFx != null) {
				int bloodState = getBloodState();
				if (bloodState > 0) {
					bloodState--;
					ISprite fx = bloodFx.getSprite(bloodState);
					if (fx != null) {
						fx.draw(x, y);
					}
				}
			}
			
			/*if (weapon != null && weaponSprite != null) {
				if (weapon.isDisplayIdle()) {
					weaponSprite.draw(_pos.x * IronConfig.TILE_WIDTH, _pos.y * IronConfig.TILE_HEIGHT);
				}
			}*/
		} else {
			findSprite();
		}
	}
	
	public void renderStatusBars(float deltaTime, float x, float y) {
		if (y < 0) y = 0;
		
		int height = 8 + (getStats().getMaxMana() > 0 ? 3 : 0);
		
		IronGL.drawRect(x, y,IronConst.TILE_WIDTH, height,
				0.1f, 0.1f, 0.1f, 1f);
		
		IronGL.drawRect(x+1, y+1,IronConst.TILE_WIDTH - 2, height - 2,
				0.5f, 0.5f, 0.5f, 1f);
		
		IronGL.drawRect(x+2, y+2,IronConst.TILE_WIDTH - 4, height - 4,
				0.1f, 0.1f, 0.1f, 1f);
		
		
		IronGL.drawRect(x+3, y+3, IronConst.TILE_WIDTH - 6, height - 6,
				0f, 0f, 0f, 1f);
		
		float percentHealthLeft = (float)stats.getHp() / stats.getMaxHp();
		
		float healthWidth = percentHealthLeft * (IronConst.TILE_WIDTH - 6);
		IronGL.drawRect(x+3, y+3, healthWidth, 2,
				1 - percentHealthLeft, percentHealthLeft, 0f, 1f);
		
		if (getStats().getMaxMana() > 0) {
			float percentManaLeft = (float)stats.getMana() / stats.getMaxMana();
			float manaWidth = percentManaLeft * (IronConst.TILE_WIDTH - 6);
			IronGL.drawRect(x+3, y+6, manaWidth, 2,	MANA_COLOR);
		}
	}
	
	public void renderStatusBars(float deltaTime) {
		if (isDead() || _sprite == null) return;
		
		int y = (int)(getY() * IronConst.TILE_HEIGHT - (_sprite.getHeight() - IronConst.TILE_HEIGHT));
		int x = (int)(getX() * IronConst.TILE_WIDTH);
		y -= 11;
		
		renderStatusBars(deltaTime, x, y);
	}
	
	public void renderTileGfx(float deltaTime) {
		if (!hasPlayed() 
			&& world.getContext().getClientId() >= 0
			&& world.getContext().getPlayerInfo(world.getContext().getClientId()).isTurnToPlay()) {
			
			IronGL.drawRect((int)getX() * IronConst.TILE_WIDTH,
					(int)getY() * IronConst.TILE_HEIGHT,
					IronConst.TILE_WIDTH,
					IronConst.TILE_HEIGHT,
					1f, 0.8f, 0f, 0.3f);
			
			if (isSelected()) {
				IronGL.drawRect((int)_pos.x * IronConst.TILE_WIDTH,
								(int)_pos.y * IronConst.TILE_HEIGHT,
								IronConst.TILE_WIDTH,
								IronConst.TILE_HEIGHT,
								1f, 0f, 0f, 0.5f);
			}//end if isSelected()
		}
	}
	

	public void renderMoveableTiles() {
		if (!hasPlayed() && isSelected()
				&& world.getContext().getClientId() >= 0
				&& world.getContext().getPlayerInfo(world.getContext().getClientId()).isTurnToPlay()) {
		
			//if (_pos.getX() != movementCachePos[0] || _pos.getY() != movementCachePos[1]) {
				//cache needs to be computed
				int xstart, ystart, xend, yend;
				movementCache.clear();
				
				xstart = Math.max(0, (int)_pos.getX() - getMovement() / IronConst.MOVE_COST_DEFAULT);
				ystart = Math.max(0, (int)_pos.getY() - getMovement() / IronConst.MOVE_COST_DEFAULT);
				
				xend = Math.min(IronConst.MAP_WIDTH - 1, (int)_pos.getX() + getMovement() / IronConst.MOVE_COST_DEFAULT);
				yend = Math.min(IronConst.MAP_HEIGHT - 1, (int)_pos.getY() + getMovement() / IronConst.MOVE_COST_DEFAULT);
				
				for (int i = xstart; i <= xend; i++) {
					for (int j = ystart; j <= yend; j++) {
						if (i == _pos.getX() && j == _pos.getY()) continue;
						
						if (world.getPath(getId(), i, j) != null) {
							int []pos = new int[2];
							pos[0] = i;
							pos[1] = j;
							movementCache.add(pos);
						}
					}
				}//end for i
				
				movementCachePos[0] = (int)_pos.getX();
				movementCachePos[1] = (int)_pos.getY();
			//}//cache computed
			
			for (int [] p : movementCache) {
				IronGL.drawRect(p[0] * IronConst.TILE_WIDTH,
						p[1] * IronConst.TILE_HEIGHT,
						IronConst.TILE_WIDTH,
						IronConst.TILE_HEIGHT,
						0.3f, 1f, 0f, 0.2f);
			}
		}
	}
	
	public void renderStatsInGui(float deltaTime, float x1, float y1, float w, float h) {

		if (_sprite == null) return;
		
		float x = 707;
		float y = 120;
		
		_sprite.draw(x, y);

		if (teamColorSprite != null && !isDead()) {
			teamColorSprite.draw(x, y);
		}

		if (!isDead() && bloodFx != null) {
			int bloodState = getBloodState();
			if (bloodState > 0) {
				bloodState--;
				ISprite fx = bloodFx.getSprite(bloodState);
				if (fx != null) {
					fx.draw(x, y);
				}
			}
		}
		
		y += 32;
		renderStatusBars(deltaTime, x, y);
		
		x = 655;
		y = 170;
		Font font = FontManager.getFont("StatsFont");
		font.setColor(1,1,1);
		List<String> stats = new ArrayList<String>();
		
		if (name != null && !name.equals("")) {
			Font font2 = FontManager.getFont("Font");
			font2.setColor(1,1,1);
			float offx = (w - 4) / 2 - name.length() * font2.getCharWidth() / 2;
			font2.glPrint(name, x1 + offx, y);
			
			y += 25;
		}
		stats.add("HP   : "+getStats().getHp()+"/"+getStats().getMaxHp());
		if (getStats().getMaxMana() > 0) {
			stats.add("MP   : "+getStats().getMana()+"/"+getStats().getMaxMana());
		} else {
			stats.add("MP   : -/-");
		}
		stats.add("");
		stats.add("Strength : "+getStats().getStrength());
		stats.add("Agility  : "+getStats().getAgility());
		stats.add("Intell   : "+getStats().getIntelligence());
		stats.add("");
		
		int meleeDamage = 0;
		if (getMeleeWeapon() != null) {
			meleeDamage = getMeleeWeapon().getDamage();
		}
		
		int rangedDamage = 0;
		if (getRangedWeapon() != null) {
			rangedDamage = getRangedWeapon().getDamage();
		}
		
		int physArmorValue = 0;
		int magicalArmorValue = 0;
		
		if (getShield() != null) {
			physArmorValue += getShield().getPhysicalArmor();
			magicalArmorValue += getShield().getMagicalArmor();
		}
		
		if (getArmor() != null) {
			physArmorValue += getArmor().getPhysicalArmor();
			magicalArmorValue += getArmor().getMagicalArmor();
		}
		
		stats.add("Melee  dmg : "+meleeDamage);
		stats.add("Range  dmg : "+rangedDamage);
		stats.add("Phys armor : "+physArmorValue);
		stats.add("Magi armor : "+magicalArmorValue);
		
		for (String str : stats) {
			if (str.equals("")) {
				y += 5;
				continue;
			}
			font.glPrint(str, x, y);
			y += 16;
		}
	}
	
	
	public String toString() {
		String str = super.toString();
		str += " id = "+id+" name="+name+" type = "+type+" ownerId = "+ownerId+"\n";
		str += "can perform skills : ";
		for (Skill skill : skills) {
			str += skill.getSkillName()+" ";
		}
		return str;
	}
}
