package fr.frozen.iron.common;

import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;

public class PlayerGameInfo {
	//TODO find a way to not duplicate all the crap :/
	
	protected Protocol race;
	protected boolean turnToPlay = false;
	protected int color = 0x0;
	
	public PlayerGameInfo(Protocol race, int color) {
		this.race = race;
		this.color = color;
	}

	public boolean isTurnToPlay() {
		return turnToPlay;
	}
	
	public void setTurnToPlay(boolean val) {
		turnToPlay = val;
	}
	
	public Protocol getRace() {
		return race;
	}
	
	public void setRace(Protocol race) {
		//TODO : check its a good value
		this.race = race;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
	
	public String toString() {
		return "playing race : "+race;
	}
	
	public byte[] serialize() {
		/*ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		
		byteArray.reset();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				byteArray.write(tiles[i][j].serialize());
			}
		}
		return byteArray.toByteArray();*/
		
		byte [] data = new byte[9];
		System.arraycopy(IronUtil.intToByteArray(race.getValue()), 0, data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(color), 0, data, 4, 4);
		
		return data;
	}
	
	public void unserialize(byte [] data) {
		//not used for the moment :(
		byte [] raceData = new byte[4];
		byte [] colorData = new byte[4];
		
		System.arraycopy(data, 0, raceData, 0, 4);
		System.arraycopy(data, 4, colorData, 0, 4);
		
		race = Protocol.get(IronUtil.byteArrayToInt(raceData));
		color = IronUtil.byteArrayToInt(colorData);
		turnToPlay = data[8] == 1;
	}
}
