package fr.frozen.network.server;

import java.util.List;

import fr.frozen.network.common.*;

public interface IGameController {

	public void enQueueMessage(Message msg);
	public List<MessageToSend> getOutgoingMessages();
	
	public void update(float delta);
	public void addClient(Client player);
	public void removeClient(Client player, String reason);
	public void notifyNameChange(Client player);
	public int getSessionType();
}
