package fr.frozen.iron.common.equipment;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.frozen.iron.util.IronConfig;
import fr.frozen.util.XMLParser;

public class EquipmentManager {
	private static EquipmentManager instance = new EquipmentManager();

	public static EquipmentManager getInstance() {
		return instance;
	}
	
	private Hashtable<String, Weapon> weaponsByName;
	private Hashtable<Integer, Weapon> weaponsById;

	private Hashtable<String, Armor> shieldsByName;
	private Hashtable<Integer, Armor> shieldsById;

	private Hashtable<String, Armor> armorsByName;
	private Hashtable<Integer, Armor> armorsById;

	
	private EquipmentManager() {
		weaponsById = new Hashtable<Integer, Weapon>();
		weaponsByName = new Hashtable<String, Weapon>();
		
		shieldsById = new Hashtable<Integer, Armor>();
		shieldsByName = new Hashtable<String, Armor>();

		armorsById = new Hashtable<Integer, Armor>();
		armorsByName = new Hashtable<String, Armor>();
		
		findWeaponsInXml(IronConfig.getIronXMLParser());
		findShieldsAndArmorsInXml(IronConfig.getIronXMLParser());
		
		String weaponsStr = "Weapons added = [";
		weaponsStr += getArrayStr(weaponsByName.values().toArray());
		weaponsStr += "]";
		Logger.getLogger(getClass()).info(weaponsStr);
		
		String armorStr = "Armors added = [";
		armorStr += getArrayStr(armorsByName.values().toArray());
		armorStr += "]";
		Logger.getLogger(getClass()).info(armorStr);
		
		String shieldStr = "Shields added = [";
		shieldStr += getArrayStr(shieldsByName.values().toArray());
		shieldStr += "]";
		Logger.getLogger(getClass()).info(shieldStr);
	}
	
	protected String getArrayStr(Object[] array) {
		String str = "";
		for (int i = 0; i < array.length; i++) {
			str += array[i];
			if (i < array.length - 1) {
				str += ",";
			}
		}
		return str;
	}
	
	public Weapon getWeapon(int id) {
		if (id == -1) return null;
		Weapon w = weaponsById.get(id);
		if (w != null) {
			return (Weapon)w.clone();
		}
		return null;
	}
	
	public Weapon getWeapon(String name) {
		if (name.equals("none")) return null;
		Weapon w = weaponsByName.get(name);
		if (w != null) {
			return (Weapon)w.clone();
		}
		return null;
	}
	
	public Armor getShield(int id) {
		if (id == -1) return null;
		Armor shield = shieldsById.get(id);
		if (shield != null) {
			return (Armor)shield.clone();
		}
		return null;
	}
	
	public Armor getShield(String name) {
		if (name.equals("none")) return null;
		
		Armor shield = shieldsByName.get(name);
		if (shield != null) {
			return (Armor)shield.clone();
		}
		return null;
	}
	
	public Armor getArmor(int id) {
		if (id == -1) return null;
		Armor armor = armorsById.get(id);
		if (armor != null) {
			return (Armor)armor.clone();
		}
		return null;
	}
	
	public Armor getArmor(String name) {
		if (name.equals("none")) return null;
		Armor armor = armorsByName.get(name);
		if (armor != null) {
			return (Armor)armor.clone();
		}
		return null;
	}
	
	private void findShieldsAndArmorsInXml(XMLParser parser) {
		Node weaponsNode = parser.getElement("armors");
		NamedNodeMap attr;
		Node tmp;
		
		String []attrNames = {"name","id","magicres","physres"};
		String []values = new String[attrNames.length];
		
		if (weaponsNode.hasChildNodes()) {
			main : for (int i = 0 ; i < weaponsNode.getChildNodes().getLength(); i++) {
				Node node = weaponsNode.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE || !node.hasAttributes()) continue;
				
				attr = node.getAttributes();
				for (int j = 0; j < attrNames.length; j++) {
					tmp = attr.getNamedItem(attrNames[j]);
					if (tmp == null) continue main;
					values[j] = tmp.getNodeValue();
				}
				
				String name = values[0];
				int id = Integer.parseInt(values[1]);
				float magicres = Float.parseFloat(values[2]);
				float physres = Float.parseFloat(values[3]);

				Armor armor = new Armor(id, name, physres, magicres);
				
				if (node.getNodeName().equals("shield")) {
					shieldsById.put(id, armor);
					shieldsByName.put(name,armor);
				} else {
					armorsById.put(id, armor);
					armorsByName.put(name,armor);
				}
			}
		}
	}
	
	
	private void findWeaponsInXml(XMLParser parser) {
		Node weaponsNode = parser.getElement("weapons");
		NamedNodeMap attr;
		
		String []attrNames = {"name","id","damage","maxrange","minrange","cutwood"};
		String []values = new String[attrNames.length];
		
		if (weaponsNode.hasChildNodes()) {
			main : for (int i = 0 ; i < weaponsNode.getChildNodes().getLength(); i++) {
				Node node = weaponsNode.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE || !node.hasAttributes()) continue;
				
				attr = node.getAttributes();
				for (int j = 0; j < attrNames.length; j++) {
					node = attr.getNamedItem(attrNames[j]);
					if (node == null) continue main;
					values[j] = node.getNodeValue();
				}
				
				String name = values[0];
				int id = Integer.parseInt(values[1]);
				int damage = Integer.parseInt(values[2]);
				float maxrange = Float.parseFloat(values[3]);
				float minrange = Float.parseFloat(values[4]);
				boolean cutTrees = Integer.parseInt(values[5]) == 1;
				
				node = attr.getNamedItem("ranged");
				boolean ranged = false;
				if (node != null) {
					ranged = Integer.parseInt(node.getNodeValue()) == 1;
				}

				String projectileName = null;
				if (ranged) {
					node = attr.getNamedItem("projectile");
					if (node != null) {
						projectileName = node.getNodeValue();
					}
				}
				
				node = attr.getNamedItem("magical");
				boolean magical = false;
				if (node != null) {
					magical = Integer.parseInt(node.getNodeValue()) == 1;
				}

				int manaCost = 0;
				if (magical) {
					node = attr.getNamedItem("manacost");
					if (node != null) {
						manaCost = Integer.parseInt(node.getNodeValue());
					}
				}
				
				Weapon weapon;
				if (ranged) {
					weapon = new RangedWeapon(name, id, damage, maxrange, minrange, cutTrees, projectileName, magical, manaCost);
				} else {
					weapon = new Weapon(name, id, damage, maxrange, minrange, cutTrees, magical, manaCost);
				}
				
				weaponsById.put(id, weapon);
				weaponsByName.put(name, weapon);
			}
		}
	}
}
