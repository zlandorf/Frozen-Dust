package fr.frozen.iron.common.weapon;

import java.util.Hashtable;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.frozen.iron.util.IronConfig;
import fr.frozen.util.XMLParser;

public class WeaponManager {
	private static WeaponManager instance = new WeaponManager();

	public static WeaponManager getInstance() {
		return instance;
	}
	
	private Hashtable<String, Weapon> weaponsByName;
	private Hashtable<Integer, Weapon> weaponsById;
	
	private WeaponManager() {
		weaponsById = new Hashtable<Integer, Weapon>();
		weaponsByName = new Hashtable<String, Weapon>();
		findWeaponsInXml(IronConfig.getIronXMLParser());
	}
	
	
	public Weapon getWeapon(int id) {
		if (id == -1) return null;
		return weaponsById.get(id);
	}
	
	public Weapon getWeapon(String name) {
		if (name.equals("none")) return null;
		return weaponsByName.get(name);
	}
	
	private void findWeaponsInXml(XMLParser parser) {
		Node weaponsNode = parser.getElement("weapons");
		NamedNodeMap attr;
		
		if (weaponsNode.hasChildNodes()) {
			for (int i = 0 ; i < weaponsNode.getChildNodes().getLength(); i++) {
				Node node = weaponsNode.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE || !node.hasAttributes()) continue;
				attr = node.getAttributes();
				node = attr.getNamedItem("name");
				if (node == null) continue;
				String name = node.getNodeValue();
				
				node = attr.getNamedItem("id");
				if (node == null) continue;
				int id = Integer.parseInt(node.getNodeValue());
				
				node = attr.getNamedItem("damage");
				if (node == null) continue;
				int damage = Integer.parseInt(node.getNodeValue());
				
				node = attr.getNamedItem("maxrange");
				if (node == null) continue;
				int maxrange = Integer.parseInt(node.getNodeValue());
				
				node = attr.getNamedItem("minrange");
				if (node == null) continue;
				int minrange = Integer.parseInt(node.getNodeValue());
				
				node = attr.getNamedItem("cutwood");
				if (node == null) continue;
				boolean cutTrees = Integer.parseInt(node.getNodeValue()) == 1;
				
				node = attr.getNamedItem("displayidle");
				if (node == null) continue;
				boolean displayidle = Integer.parseInt(node.getNodeValue()) == 1;
				
				node = attr.getNamedItem("ranged");
				boolean ranged = false;
				if (node != null) {
					ranged = Integer.parseInt(node.getNodeValue()) == 1;
				}

				String projectileName = null;
				if (ranged) {
					node = attr.getNamedItem("projectile");
					if (node == null) continue;
					projectileName = node.getNodeValue();
					if (projectileName == null || projectileName.equals("")) continue;
				}
				
				
				Weapon weapon;
				
				if (ranged) {
					weapon = new RangedWeapon(name, id, damage, maxrange, minrange, cutTrees, displayidle, projectileName);
				} else {
					weapon = new Weapon(name, id, damage, maxrange, minrange, cutTrees, displayidle);
				}
				
				weaponsById.put(id, weapon);
				weaponsByName.put(name, weapon);
				
				System.out.println("adding weapon "+name);
			}
		}
	}
}
