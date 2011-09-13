package fr.frozen.iron.util;

import fr.frozen.util.XMLParser;

public class IronConfig {
	
	protected static XMLParser xmlinstance = new XMLParser("Data/iron.cfg");
	protected static IronConfig instance = new IronConfig();
	
	protected String username;
	protected boolean showGrid;
	
	protected IronConfig() {
		username = IronUtil.findName();
		
		String showGridOption = IronUtil.findOptionValue("showgrid");
		showGrid = IronConst.showGridDefault;
		if (showGridOption != null) {
			if (showGridOption.equals("true")) {
				showGrid = true;
			} else if (showGridOption.equals("false")) {
				showGrid = false;
			}
		}
	}
	
	public static String getUserName() {
		return instance.username;
	}
	
	public static void setUserName(String name) {
		instance.username = name;
		IronUtil.saveName(name);
	}
	
	public static boolean isShowGrid() {
		return instance.showGrid;
	}
	
	public static void setShowGrid(boolean value) {
		instance.showGrid = value;
		IronUtil.saveOptionValue("showgrid", value ? "true":"false");
	}
	
	public static XMLParser getIronXMLParser() {
		return xmlinstance;
	}
	
	/*public static void main(String []args) {
		IronConfig ic = new IronConfig();
		System.out.println("---------------");
		String orcMaxHp = ic.getAttributeValue("unitstats/footsoldier/orc", "maxhp");
		String elfweaponid = ic.getAttributeValue("unitstats/footsoldier/elf", "weaponid");
		System.out.println("orc max hp = "+orcMaxHp);
		System.out.println("elf weapon id = "+elfweaponid);
	}*/
}
