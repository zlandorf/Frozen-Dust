package fr.frozen.iron.common.skills;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fr.frozen.game.ISpriteManager;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.entities.particles.Projectile;
import fr.frozen.iron.common.weapon.RangedWeapon;
import fr.frozen.iron.common.weapon.Weapon;
import fr.frozen.iron.util.IronConst;
import fr.frozen.iron.util.IronUtil;

public class RangedAttack extends Skill {

	private static RangedAttack instance = new RangedAttack();
	
	public static RangedAttack getInstance() {
		return instance;
	}
	
	public List<Vector2f> points;
	
	private RangedAttack() {
		super("Ranged Attack", Skill.RANGED_ATTACK);
		points = new ArrayList<Vector2f>();
	}
	
	
	@Override
	public boolean canDo(IronWorld world, int srcId, int x, int y) {
		IronUnit src = world.getUnitFromId(srcId);
		IronUnit dst = world.getUnitAtXY(x, y);
		Weapon rangedWeapon = src.getRangedWeapon();
		points.clear();
		
		if (rangedWeapon == null || !(rangedWeapon instanceof RangedWeapon)) return false;
		if (src == null || dst == null) return false;
		if (src.getId() == dst.getId()) return false;
		if (src.getOwnerId() == dst.getOwnerId()) return false;
		if (dst.isDead()) return false;

		//range check
		int minRange = rangedWeapon.getMinRange();
		int maxRange = rangedWeapon.getMaxRange();
		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
		
		
		if (distance < minRange || distance > maxRange) return false;
		
		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		Vector2f vec = new Vector2f(x2 - x1, y2 - y1);
		double angle = IronUtil.getAngle(vec, new Vector2f(1,0));
		
		getHorizontalIntersections(world, x1, y1, x2, y2, angle);
		return checkGrid(world, x1, y1, x2, y2);
	}

	public void getHorizontalIntersections(IronWorld world, int x1, int y1, int x2, int y2, double angle) {
		int Py = (int)(y1 / IronConst.TILE_HEIGHT);
		if (y2 - y1 >= 0) Py ++;
		Py *= IronConst.TILE_HEIGHT;

		int nbIntersections = (int)(y2/ IronConst.TILE_HEIGHT) - (int)(y1 / IronConst.TILE_HEIGHT);
		nbIntersections = Math.abs(nbIntersections);
		
		int side = 1;
		if (y2 - y1 < 0) {
			side = - side;
		}
		
		points.add(new Vector2f(x1, y1));
		
		for (int i = 0; i <  nbIntersections; i++) {
			Vector2f ref1 = new Vector2f(0, Py);
			Vector2f ref2 =  new Vector2f(IronConst.MAP_WIDTH * IronConst.TILE_WIDTH - 1, Py);
			
			Vector2f P = IronUtil.getIntersectionPoint(new Vector2f(x1, y1), new Vector2f(x2, y2), ref1,ref2);
			//P.setY(Py);//theres a bug where i get 255.99998 and it screws up the checkGrid part
			Py += IronConst.TILE_HEIGHT * side;
			
			if (P == null) break;
			points.add(P);
		}
		
		points.add(new Vector2f(x2, y2));
	}
	
	public boolean checkGrid(IronWorld world, int x1, int y1, int x2, int y2) {
		
		Vector2f A,B;
		int Px, nbIntersections, squareX, squareY;
		float tmpX, tmpY;
		
		main : for (int i = 1; i < points.size(); i++) {
			A = points.get(i - 1);
			B = points.get(i);
			
			Px = (int)(A.getX() / IronConst.TILE_WIDTH);
			if (B.getX() - A.getX() >= 0) Px ++;
			Px *= IronConst.TILE_HEIGHT;
			
			nbIntersections = (int)(B.getX() / IronConst.TILE_WIDTH) - (int)(A.getX() / IronConst.TILE_WIDTH);
			nbIntersections = Math.abs(nbIntersections);
			
			int side = 1;
			if (B.getX() - A.getX() < 0) {
				side = - side;
			}
			
			tmpX = A.getX();
			tmpY = A.getY();
			
			for (int j = 0; j < nbIntersections; j++) {
				Vector2f P = null, ref1, ref2;
				
				while (P == null &&  Px >= 0 && Px < IronConst.MAP_WIDTH * IronConst.TILE_WIDTH) {
					ref1 = new Vector2f(Px, 0);
					ref2 =  new Vector2f(Px, IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT - 1);
					P = IronUtil.getIntersectionPoint(A, B, ref1,ref2);
					
					//if (P != null)
						//P.setX(Px);//theres a bug where i get 255.99998 and it screws up the checkGrid part
					Px += IronConst.TILE_WIDTH * side;
				}
				
				if (P == null) break;
				
				squareX =(int) Math.min(P.getX() / IronConst.TILE_WIDTH, tmpX / IronConst.TILE_WIDTH);
				squareY =(int) Math.min(P.getY() / IronConst.TILE_HEIGHT, tmpY / IronConst.TILE_HEIGHT);
				

				if (P.getX() == tmpX && P.getY() == tmpY) {
					tmpX = P.getX();
					tmpY = P.getY();
					continue;
				}

				tmpX = P.getX();
				tmpY = P.getY();
				
				
				if (squareX < 0 || squareX >= IronConst.MAP_WIDTH || squareY < 0 || squareY >= IronConst.MAP_HEIGHT)
					break main;
				
				if (squareX == x1 / IronConst.TILE_WIDTH && squareY == y1 / IronConst.TILE_HEIGHT) continue;
				if (squareX == x2 / IronConst.TILE_WIDTH && squareY == y2 / IronConst.TILE_HEIGHT) break main;
				
				if (!world.getMap().getTile(squareX, squareY).canShootOver()) {
					return false;
				}
				
			}
			
			squareX =(int) Math.min(B.getX() / IronConst.TILE_WIDTH, tmpX / IronConst.TILE_WIDTH);
			squareY =(int) Math.min(B.getY() / IronConst.TILE_HEIGHT, tmpY / IronConst.TILE_HEIGHT);
			
			if (squareX < 0 || squareX >= IronConst.MAP_WIDTH || squareY < 0 || squareY >= IronConst.MAP_HEIGHT)
				break main;
			
			if (squareX == x1 / IronConst.TILE_WIDTH && squareY == y1 / IronConst.TILE_HEIGHT) continue;
			if (squareX == x2 / IronConst.TILE_WIDTH && squareY == y2 / IronConst.TILE_HEIGHT) break main;
			
			if (!world.getMap().getTile(squareX, squareY).canShootOver()) {
				return false;
			}
		}//end for main
		
		return true;
	}

	@Override
	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
		if (!canDo(world, srcId, x, y)) return null;
		IronUnit dst = world.getUnitAtXY(x, y);
		IronUnit src = world.getUnitFromId(srcId);
		
		if (dst == null || src == null || src.getRangedWeapon() == null) return null;
		
		List<int[]> res = new ArrayList<int[]>();
		/*for (int i = (int)dst.getX() - 3; i < dst.getX() + 3; i++) {
			for (int j = (int)dst.getY() - 3; j < dst.getY() + 3; j++) {
				System.out.println("checking ["+x+","+y+"]");
				Tile tile = world.getMap().getTile(i, j);
				if (tile == null) continue;
				IronUnit unit = tile.getUnitOnTile();
				if (unit !=null) {
					res.add(new int[]{unit.getId(), -5});
					System.out.println("ADDING ");
				}
			}
		}
		System.out.println("out");*/
		res.add(new int[]{dst.getId(), - src.getRangedWeapon().getDamage()});
		
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
		if (!(src.getRangedWeapon() instanceof RangedWeapon)) {
			System.out.println("NOT INSTANCE OF");
			return;
		}
		RangedWeapon weapon = (RangedWeapon)src.getRangedWeapon();
		if (!ISpriteManager.getInstance().isSpriteLoaded(weapon.getProjectileName())) return;
		

		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
		
		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;

		Projectile arrow = new Projectile(world, x1 - IronConst.TILE_WIDTH / 2,
									   y1 - IronConst.TILE_HEIGHT / 2,
									   new Vector2f(x2 - x1, y2 - y1), weapon.getProjectileName());
		world.addGameObject(arrow, "gfx");
	}
}
