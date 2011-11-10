package fr.frozen.network.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import fr.frozen.network.common.Attachment;
import fr.frozen.network.common.IMessageProcessor;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageWriter;

public class BaseClient extends Thread implements IMessageProcessor {
	/** address of server */
	protected String host;
	protected int port;

	protected InetAddress serverAddress;
	protected int SERVER_ID = -1;

	/** connection to server */
	protected SocketChannel serverChannel;
	protected MessageWriter msgWriter;
	private Selector selector;

	protected int Id;

	protected boolean running = false;
	protected boolean connected = false;

	protected List<NetEventListener> listeners;
	protected List<NetEventListener> listenersToAdd;
	protected List<NetEventListener> listenersToRemove;

	public BaseClient(String host, int port) {
		this.host = host;
		this.port = port;
		listeners = new Vector<NetEventListener>();
		listenersToAdd = new Vector<NetEventListener>();
		listenersToRemove = new Vector<NetEventListener>();
		msgWriter = new MessageWriter();
	}

	public boolean isConnected() {
		return connected;
	}

	public void init() {
		if (!isConnected()) {
			connect();
		}
		msgWriter.start();
	}

	public synchronized void addNetEventListener(NetEventListener listener) {
		if (!running) {
			listeners.add(listener);
		} else {
			listenersToAdd.add(listener);
			selector.wakeup();
		}
	}

	public synchronized void removeNetEventListener(NetEventListener listener) {
		if (!running) {
			listeners.remove(listener);
		} else {
			listenersToRemove.add(listener);
			selector.wakeup();
		}
	}

	public synchronized void dispatchEvent(NetEvent ne) {
		for (NetEventListener listener : listeners) {
			listener.onNetEvent(ne);
		}
	}

	public synchronized void connect() {
		try {
			Logger.getLogger(getClass()).info(
					"Trying to connect to " + host + ":" + port);
			serverAddress = InetAddress.getByName(host);
			System.out.println("serverAddress = " + serverAddress);
			serverChannel = SocketChannel.open(new InetSocketAddress(
					serverAddress, port));
			serverChannel.configureBlocking(false);
			serverChannel.socket().setTcpNoDelay(true);

			while (!serverChannel.finishConnect())
				;

			selector = Selector.open();
			// socketChannel.register(selector,SelectionKey.OP_CONNECT);
			serverChannel.register(selector, SelectionKey.OP_READ,
					new Attachment(SERVER_ID, this));
			Logger.getLogger(getClass()).info("connected");
			connected = true;
			dispatchEvent(new ConnectEvent(true));
		} catch (Exception e) {
			Logger.getLogger(getClass()).error(
					"problem when connecting : " + e.getMessage());
			connected = false;
			running = false;
			dispatchEvent(new ConnectEvent(false));
			// shutdown();
			// System.exit(1);
		}
	}

	// this doesnt seem to be called
	/*
	 * public void finishConnection(SelectionKey key) { SocketChannel channel =
	 * (SocketChannel) key.channel();
	 * 
	 * // Finish the connection. If the connection operation failed // this will
	 * raise an IOException. try { channel.finishConnect();
	 * channel.register(selector, SelectionKey.OP_READ, new Attachment(this));
	 * System.out.println("connection finished");
	 * 
	 * } catch (IOException e) { System.out.println("connection failed");
	 * key.cancel(); e.printStackTrace(); return; } }
	 */

	private synchronized void read(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		Attachment attachment = (Attachment) key.attachment();
		int numRead;
		try {
			numRead = channel.read(attachment.getBuff());
		} catch (IOException e) {
			if (running)
				shutdown();
			return;
		}

		if (numRead == -1) {
			if (running)
				shutdown();
			return;
		}
		attachment.checkForMessages();
	}

	protected synchronized void shutdown() {
		if (!running)
			return;

		Logger.getLogger(getClass()).info("shutdown");
		running = false;
		if (msgWriter != null)
			msgWriter.end();
		if (selector != null)
			selector.wakeup();
		try {
			serverChannel.close();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		running = true;
		init();

		if (!isConnected()) {
			Logger.getLogger(getClass()).error(
					"connection failed, not entering loop");
			running = false;
		} else {
			Logger.getLogger(getClass()).debug("entering loop");
		}
		while (running) {
			try {
				selector.select();
				update();

				for (SelectionKey key : selector.selectedKeys()) {
					if (!key.isValid())
						continue;

					if (key.isReadable()) {
						read(key);
					}
				}
			} catch (IOException ioe) {
				Logger.getLogger(getClass()).error(
						"error during serverSocket select(): "
								+ ioe.getMessage());
				ioe.printStackTrace();
			} catch (Exception e) {
				Logger.getLogger(getClass()).error("exception in run()");
				e.printStackTrace();
			}
		}
	}

	protected synchronized void update() {
		for (NetEventListener listener : listenersToAdd) {
			System.out.println("adding : "+listener);
			listeners.add(listener);
		}
		for (NetEventListener listener : listenersToRemove) {
			listeners.remove(listener);
		}
		listenersToAdd.clear();
		listenersToRemove.clear();
	}

	@Override
	public void processMessage(Message msg) {
		// TODO implement in subclass
		// System.out.println("[CLIENT] Message received : "+msg);
		/*
		 * try { Thread.sleep(1000); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */
	}

	public static void main(String[] args) {
		BaseClient client;
		try {
			client = new BaseClient(InetAddress.getLocalHost().getHostName(),
					1234);

			// client = new BaseClient("92.102.7.170", 1234);
			client.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
