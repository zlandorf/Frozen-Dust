package fr.frozen.iron.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.iron.client.messageEvents.ChatMessageEvent;
import fr.frozen.iron.client.messageEvents.GameActionEvent;
import fr.frozen.iron.client.messageEvents.GameInfoReceivedEvent;
import fr.frozen.iron.client.messageEvents.GameListReceivedEvent;
import fr.frozen.iron.client.messageEvents.GameOverEvent;
import fr.frozen.iron.client.messageEvents.GameTurnEvent;
import fr.frozen.iron.client.messageEvents.MapRecievedEvent;
import fr.frozen.iron.client.messageEvents.NameChangeEvent;
import fr.frozen.iron.client.messageEvents.NewPlayerEvent;
import fr.frozen.iron.client.messageEvents.NewSessionEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
import fr.frozen.iron.client.messageEvents.RaceChoiceEvent;
import fr.frozen.iron.client.messageEvents.UndoMoveEvent;
import fr.frozen.iron.client.messageEvents.UnitsListReceivedEvent;
import fr.frozen.iron.common.IronMap;
import fr.frozen.iron.common.PlayerGameInfo;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.equipment.EquipmentManager;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.BaseClient;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageToSend;

public class IronClient extends BaseClient {
	
	private int myId = -1;
	
	public IronClient(String host, int port) {
		super(host, port);
		//this.gui.addWindowListener(this);
	}
	
	public void sendEmptyMessage(Protocol type) {
		byte [] data = {0};
		sendMessage(type,data);
	}
	
	public void sendMessage(Protocol type, byte[] data) {
		MessageToSend msgToSend = new MessageToSend(serverChannel, type.getValue(), data);
		msgWriter.addMsg(msgToSend);
	}
	
	public void sendMessage(Protocol type, String text) {
		MessageToSend msgToSend = new MessageToSend(serverChannel, type.getValue(), text.getBytes());
		msgWriter.addMsg(msgToSend);
	}
	
	public int getClientId() {
		return myId;
	}

	@Override
	public void processMessage(Message msg) {
		DataInputStream is;
		switch (Protocol.get(msg.getType())) {
		case GAME_TURN :
			dispatchEvent(new GameTurnEvent(IronUtil.byteArrayToInt(msg.getData())));
			break;
		case GAME_OVER :
			dispatchEvent(new GameOverEvent(IronUtil.byteArrayToInt(msg.getData())));
			break;
		case GAME_UNDO:
			dispatchEvent(new UndoMoveEvent(IronUtil.byteArrayToInt(msg.getData())));
			break;
		case GAME_ACTION : 
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg.getData()));
				int unitId = is.readInt();
				int actionType = is.readInt();
				byte [] data = new byte[is.available()];
				is.read(data, 0, is.available());
				
				dispatchEvent(new GameActionEvent(unitId, actionType, data));
			} catch (IOException e6) {
				e6.printStackTrace();
			}
			break;
		case GAME_MAP_SEND :
			IronMap map = new IronMap();
			map.unserialize(msg.getData());
			for (int i = 0; i < map.getTiles().length; i++) {
				for (int j = 0; j < map.getTiles()[i].length; j++) {
					map.getTiles()[i][j].findSprites();
				}
			}
			dispatchEvent(new MapRecievedEvent(map));
			break;
			
		case GAME_UNIT_LIST_SEND :
			try {
				handleUnitList(msg.getData());
			} catch (IOException e5) {
				e5.printStackTrace();
			}
			break;
			
		case GAME_PLAYER_INFO_SEND :
			try {
				handleGameInfo(msg.getData());
			} catch (IOException e5) {
				e5.printStackTrace();
			}
		break;
		case GAME_CREATION_RACE :
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg.getData()));
				int clientId = is.readInt();
				int race;
				race = is.readInt();
				dispatchEvent(new RaceChoiceEvent(clientId, race));
			} catch (IOException e4) {
				e4.printStackTrace();
			}
			break;
		case SERVER_S_NEW_SESSION :
			try {
				handleNewSession(msg.getData());
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			break;
			
		case CONNECTION_S_SEND_PLAYER_ID :
			myId = IronUtil.byteArrayToInt(msg.getData());
			Logger.getLogger(getClass()).info("ID received : "+myId);
			break;
			
		case SESSION_S_SERVER_MESSAGE : 
			try {
				handleSessionServerMessage(msg.getData());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			break;
			
		case SESSION_PLAYER_LIST :
			try {
				retrievePlayerList(msg.getData());
			}catch (IOException e2) {
				e2.printStackTrace();
			}
			break;
			
		case SESSION_GAME_LIST : 
			try {
				retrieveGameCreationList(msg.getData());
			}catch (IOException e2) {
				e2.printStackTrace();
			}
			break;
		case SESSION_CHAT_MESSAGE : 
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg.getData()));
				int senderId = is.readInt();
				byte [] textBytes = IronUtil.readToArray(is,msg.getData().length - 4);
				String text = new String(textBytes);

				dispatchEvent(new ChatMessageEvent(senderId, text));
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			break;
			
		default : 
			Logger.getLogger(getClass()).error("not handled : "+Protocol.get(msg.getType()));
		}
	}
	
	public void handleGameInfo(byte [] data) throws IOException {
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
		Hashtable<Integer, PlayerGameInfo> playerInfo = new Hashtable<Integer, PlayerGameInfo>();
		PlayerGameInfo pgi;
		
		for (int i = 0; i < 2; i++) {
			int id = is.readInt();
			
			Protocol race = Protocol.get(is.readInt());
			int color = is.readInt();
			boolean turnToPlay = is.readByte() == 1;
			
			pgi = new PlayerGameInfo(race, color);
			pgi.setTurnToPlay(turnToPlay);
			
			playerInfo.put(id, pgi);
		}
		
		dispatchEvent(new GameInfoReceivedEvent(playerInfo));
	}
	
	public void handleUnitList(byte [] data) throws IOException {
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
		List<IronUnit> list = new ArrayList<IronUnit>();
		
		int entityId = is.readInt();
		int type, ownerId, meleeWeaponId, rangedWeaponId, shieldId, armorId;
		int x, y;
		
		while (entityId != -1) { //-1 is end of list
			type = is.readInt();
			ownerId = is.readInt();
			
			x = is.readInt();
			y = is.readInt();
			
			IronUnit unit = IronUnit.getUnit(type, null, entityId, ownerId, x, y);
			meleeWeaponId = is.readInt();
			rangedWeaponId = is.readInt();
			shieldId = is.readInt();
			armorId = is.readInt();
			
			if (unit != null) {
				unit.setMeleeWeapon(EquipmentManager.getInstance().getWeapon(meleeWeaponId));
				unit.setRangedWeapon(EquipmentManager.getInstance().getWeapon(rangedWeaponId));
				unit.setShield(EquipmentManager.getInstance().getShield(shieldId));
				unit.setArmor(EquipmentManager.getInstance().getArmor(armorId));
				list.add(unit);
			}

			entityId = is.readInt();
		}
		
		dispatchEvent(new UnitsListReceivedEvent(list));
	}
	
	public void handleNewSession(byte [] data) throws IOException {
		dispatchEvent(new NewSessionEvent(Protocol.get(IronUtil.byteArrayToInt(data))));
	}

	public void handleSessionServerMessage(byte [] data) throws IOException {
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
		int type = is.readInt();
		
		Integer playerId = new Integer(is.readInt());;
		String textData = new String(IronUtil.readToArray(is,data.length - 8));
		
		switch (Protocol.get(type)) {
		case SESSION_NAME_CHANGE : 
			dispatchEvent(new NameChangeEvent(playerId, textData));
			break;
			
		case SESSION_NEW_PLAYER :
			dispatchEvent(new NewPlayerEvent(new IronPlayer(playerId, textData)));
			break;
		
		case SESSION_PLAYER_LOGOUT :
			dispatchEvent(new PlayerLogoutEvent(playerId, textData));
			break;
		default : 
			System.out.println("not handled : "+Protocol.get(type));
		}
	}
	
	public void retrievePlayerList(byte []data) throws IOException {
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
		Integer id = is.readInt();
		List<IronPlayer> list = new ArrayList<IronPlayer>();

		while (id >= 0) {
			int nameLength = is.readInt();
			String name = new String(IronUtil.readToArray(is,nameLength));
			
			if (name == null || name.equals("")) {
				throw new IOException("problem while retrieving list : name not retrieved");
			}
			
			list.add(new IronPlayer(id,name));
			id = is.readInt();
		}
		dispatchEvent(new PlayerListReceivedEvent(list));
	}
	
	public void retrieveGameCreationList(byte []data) throws IOException {
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
		Integer id = is.readInt();
		List<GameInfo> list = new ArrayList<GameInfo>();

		while (id >= 0) {
			int nameLength = is.readInt();
			String host = new String(IronUtil.readToArray(is,nameLength));
			
			if (host == null || host.equals("")) {
				throw new IOException("problem while retrieving game list : host name not retrieved");
			}
			
			list.add(new GameInfo(id, host));
			id = is.readInt();
		}
		dispatchEvent(new GameListReceivedEvent(list));
	}
	
	/*public static void main(String []args) {
		IronClient ic = new IronClient();
		ic.start();
	}*/
	
	@Override
	public void shutdown() {
		super.shutdown();
	}
	
}
