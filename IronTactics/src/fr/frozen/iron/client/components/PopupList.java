package fr.frozen.iron.client.components;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;

import fr.frozen.game.ISprite;
import fr.frozen.game.SpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.common.skills.SkillInfo;
import fr.frozen.iron.util.IronConst;

public class PopupList extends Component {

	protected static int SKILL_HEIGHT = 30;
	protected static int PADDING = 16;
	
	
	protected ISprite tex;
	protected ISprite corner;
	protected ISprite top;
	protected ISprite left;
	
	protected int unitId = -1;

	protected int itemHovered = -1;
	protected int itemSelected = -1;
	
	protected boolean clickedLastTick = false;
	
	protected Color canDoColor = new Color(0,0,0);
	protected Color HoverColor = new Color(.6f,0,0);
	protected Color cantDoColor = new Color(.5f,.5f,.5f);
	
	List<SkillInfo> skills;
	
	public PopupList(int x, int y) {
		super(x, y, 0, 0);
		visible = false;
		tex = SpriteManager.getInstance().getSprite("popupTex");
		corner = SpriteManager.getInstance().getSprite("popup_corner");
		top = SpriteManager.getInstance().getSprite("popup_top");
		left = SpriteManager.getInstance().getSprite("popup_left");
		
		skills = new ArrayList<SkillInfo>();
	}
	
	public SkillInfo getSelectedSkillInfo() {
		return skills.get(itemSelected);
	}
	
	public void setWorld() {
		
	}
	
	@Override
	public boolean contains(int x, int y) {
		if (!visible) return false;
		else return super.contains(x, y);
	}
	
	public void setUnit(IronWorld world, IronUnit unit, int x, int y) {
		skills.clear();
		int skillx = x / IronConst.TILE_WIDTH;
		int skilly = y / IronConst.TILE_HEIGHT;
		
		unitId = unit.getId();
		int maxSkillWidth = 0;
		for (Skill skill : unit.getSkills()) {
			boolean canDo = skill.canDo(world, unitId, skillx, skilly);
			skills.add(skills.size(), new SkillInfo(skill, unitId, skillx, skilly, canDo));
			if (font.getWidth(skill.getSkillName()) > maxSkillWidth) {
				maxSkillWidth = font.getWidth(skill.getSkillName());
			}
		}
		
		int height = PADDING * 2 + skills.size() * SKILL_HEIGHT;
		int width = maxSkillWidth + 5 + PADDING * 2;// 5 just to widen a bit more

		int maxWidth = world.getMap().getWidth() * IronConst.TILE_WIDTH;
		int maxHeight= world.getMap().getHeight() * IronConst.TILE_HEIGHT;
		
		setLocation(Math.max(Math.min(maxWidth - width, x), 0),
				    Math.max(Math.min(maxHeight - height, y), 0));
		setDim(width, height);
	}
	
	@Override
	public void render(float deltaTime) {
		if (!visible || unitId < 0 || skills.size() <= 0) return;
		
		int width = getWidth();
		int height = getHeight();
		
		corner.draw(pos.getX(), pos.getY());
		corner.draw(pos.getX() - PADDING + width, pos.getY(), true, false);
		corner.draw(pos.getX(), pos.getY() + height - PADDING, false, true);
		corner.draw(pos.getX() - PADDING + width, pos.getY() + height - PADDING, true, true);
		
		
		top.fillIn(pos.getX() + PADDING, pos.getY(),	pos.getX() - PADDING + width, pos.getY() + PADDING);
		top.fillIn(pos.getX() + PADDING, pos.getY() + height - PADDING,	pos.getX() - PADDING + width, pos.getY() + height, false, true);
		
		
		left.fillIn(pos.getX(), pos.getY() + PADDING,	pos.getX() + PADDING, pos.getY() + height - PADDING);
		left.fillIn(pos.getX()- PADDING + width, pos.getY() + PADDING,	pos.getX() + width, pos.getY() + height - PADDING, true, false);
		
		tex.fillIn(pos.getX() + PADDING , pos.getY() + PADDING, pos.getX() + width - PADDING, pos.getY() + height - PADDING);
		
		
		/*if (itemHovered >= 0 && itemHovered < skills.size() && canDo.get(itemHovered)) {
			IronGL.drawRect((int)pos.getX() + PADDING, (int)pos.getY() + PADDING + itemHovered * SKILL_HEIGHT,
							width - PADDING * 2, SKILL_HEIGHT, 0f, 1f, 0, 0.3f);
		}*/
		
		int y = (int)pos.getY() + PADDING;

		Color color;
		for (int i = 0; i < skills.size(); i++) {
			color = cantDoColor;
			if (skills.get(i).canDo()) {
				color = canDoColor;
			}
			
			if (i == itemHovered && skills.get(i).canDo()) {
				color = HoverColor;
			}
			
			font.drawString(pos.getX() + PADDING, y, skills.get(i).getSkill().getSkillName(), color);
			y += SKILL_HEIGHT;
		}
	}

	@Override
	public boolean update(float deltaTime) {
		return false;
	}

	@Override
	public void onExit() {
		itemHovered = -1;
	}

	@Override
	public void onHover(int x, int y) {
		if (!visible) {
			itemHovered = -1;
			return;
		}
		
		if (x - pos.getX() >= PADDING && x - pos.getX() < getWidth() - PADDING 
		    && y - pos.getY() >= PADDING && y - pos.getY() < getHeight() - PADDING) {
			
			itemHovered = (int)(y - PADDING - pos.getY()) / SKILL_HEIGHT;
		} else {
			itemHovered = -1;
		}
	}

	@Override
	public void onLeftClick(int x, int y) {
		if (!visible) {
			return;
		}
		
		if (x - pos.getX() >= PADDING && x - pos.getX() < getWidth() - PADDING 
			    && y - pos.getY() >= PADDING && y - pos.getY() < getHeight() - PADDING) {
			
			itemSelected = (int)(y - PADDING - pos.getY()) / SKILL_HEIGHT;
			if (itemSelected >= 0 && itemSelected < skills.size() && skills.get(itemSelected).canDo()) {
				notifyActionListeners();
				visible = false;
			}
		}
	}

	@Override
	public void onRightClick(int x, int y) {
	}
	
	@Override
	public void onRelease() {
	}
}
