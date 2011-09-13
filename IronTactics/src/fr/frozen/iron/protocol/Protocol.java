package fr.frozen.iron.protocol;

public enum Protocol {
	//C_* is for the client
	//S_* is for the server
	//nothing can be used by both
	CONNECTION_S_SEND_PLAYER_ID,
	CONNECTION_S_CONNECTION_STATUS,

	CONNECTION_STATUS_OK,
	CONNECTION_STATUS_KO,
	
	SERVER_C_SEND_PLAYER_NAME,
	SERVER_C_LOGOUT,
	SERVER_S_KICK,
	SERVER_C_REQUEST_SESSION,
	SERVER_S_NEW_SESSION,
	
	SESSION_PLAYER_LIST_REQUEST,
	SESSION_PLAYER_LIST,/* [int id, int nameLength, string name] ... -1 for end */
	SESSION_S_SERVER_MESSAGE,/* [int type + extra info]*/
	SESSION_CHAT_MESSAGE, /* int sender + string text */
	
	SESSION_NEW_PLAYER,/*server messages*/
	SESSION_PLAYER_LOGOUT,/* int id */
	SESSION_NAME_CHANGE,/*int id + string new name */
	
	SESSION_CREATION,
	SESSION_GAME_LIST_REQUEST,
	SESSION_GAME_LIST,/*int id + String host */
	SESSION_JOIN_GAME_REQUEST,
	SESSION_CREATE_GAME_REQUEST,
	
	SESSION_LOBBY,
	SESSION_GAME_CREATION,
	SESSION_GAME,
	
	GAME_CREATION_RACE_REQUEST,
	GAME_CREATION_RACE,
	GAME_CREATION_READY,
	
	ORC_RACE,
	ELF_RACE,
	HUMAN_RACE,
	DWARF_RACE,
	UNDEAD_RACE,
	
	GAME_UNIT_LIST_REQUEST,
	GAME_UNIT_LIST_SEND,
	GAME_PLAYER_INFO_REQUEST,
	GAME_PLAYER_INFO_SEND,
	GAME_MAP_REQUEST,
	GAME_MAP_SEND,
	GAME_READY,
	GAME_TURN,//tells whos turn it is
	GAME_END_TURN_REQUEST,//indicates that the player has ended his turn
	
	GAME_ACTION_REQUEST,// unit id - action type - further data
	GAME_ACTION;// unit id - action type - further data
	
	public int getValue() {
		return ordinal();
	}
	
	public static Protocol get(int val) {
		return Protocol.class.getEnumConstants()[val];
	}
}