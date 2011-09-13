package fr.frozen.network.client;

public class ConnectEvent implements NetEvent {
	protected boolean status;

	public ConnectEvent(boolean status) {
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
