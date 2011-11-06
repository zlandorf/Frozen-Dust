package fr.frozen.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.network.common.Attachment;
import fr.frozen.network.common.IMessageProcessor;
import fr.frozen.network.common.Message;
import fr.frozen.network.common.MessageWriter;

public class BaseServer extends Thread implements IMessageProcessor {
	
	protected static int MAX_WAIT_TIME = 100;// = 20;
	
	protected HashMap<SocketChannel, Client> clientsByChannel;
	protected HashMap<Integer, Client> clientsById;
	//private GameController game;
	
	protected int port;
	protected ServerSocketChannel socketChannel;
	protected Selector selector;
	
	protected boolean running = true;
	
	protected IDGiver IdGiver; 
	protected MessageWriter msgWriter;
	
	protected List<IServerSession> gameSessions;
	protected List<IServerSession> gameSessionsToAdd;
	protected List<IServerSession> gameSessionsToRemove;
	
	protected Logger logger = Logger.getLogger(getClass());
	
	protected double lastTime = 0;
	protected double deltaTime = 0;
	
	public BaseServer(int port) {
		//TODO add gamecontroller here
		this.port = port;
		clientsByChannel = new HashMap<SocketChannel, Client>();
		clientsById = new HashMap<Integer, Client>();
		IdGiver = new IDGiver();
		
		gameSessions = new ArrayList<IServerSession>();
		gameSessionsToAdd = new ArrayList<IServerSession>();
		gameSessionsToRemove = new ArrayList<IServerSession>();
	}
	
	public void init() {
		Logger.getLogger(getClass()).info("initialising server...");
		initServerSocket();
		buildInitialGameSessions();
		msgWriter = new MessageWriter();
		msgWriter.start();
	}
	
	public void buildInitialGameSessions() {
		//TODO implement in subclass
		//TODO make method abstract
	}
	
	public synchronized void addGameSession(IServerSession session) {
		gameSessionsToAdd.add(session);
	}
	
	public synchronized void removeGameSession(IServerSession session) {
		gameSessionsToRemove.add(session);
	}
	
	protected synchronized void updateGameSessionsList() {
		for (IServerSession session : gameSessionsToAdd) {
			gameSessions.add(session);
		}
		gameSessionsToAdd.clear();
		
		
		for (IServerSession session : gameSessionsToRemove) {
			gameSessions.remove(session);
		}
		gameSessionsToAdd.clear();
	}
	
	public MessageWriter getWriter() {
		return msgWriter;
	}
	
	public List<Client> getPlayers() {
		return new ArrayList<Client>(clientsById.values());
	}
	
	public void initServerSocket() {
		try {
			socketChannel = ServerSocketChannel.open();
			socketChannel.configureBlocking(false);
		    InetSocketAddress addr = new InetSocketAddress(port);
		    socketChannel.socket().bind(addr);
		    
		    selector = Selector.open();
		    socketChannel.register(selector, SelectionKey.OP_ACCEPT);
		    logger.info("server initialised "+addr);
		}
		catch (Exception e) {
			logger.fatal("Server failed to launch");
			e.printStackTrace();
		    System.exit(1);
		}
	}
	
	@Override
	public void run() {
		init();
		heartBeat();//first one to init
		running = true;
		while (running) {
			// note, since we only have one ServerSocket to listen to,
			// we don't need a Selector here, but we set it up for 
			// later additions such as listening on another port 
			// for administrative uses.
			try {
				// blocking select, will return when we get a new connection
				//TODO : maybe use a timeout here
				selector.select(MAX_WAIT_TIME);
				heartBeat();
				updateGameSessionsList();
				for (IServerSession session : gameSessions) {
					session.update((float)deltaTime);
				}
				
				for (SelectionKey key : selector.selectedKeys()) {
					if (!key.isValid()) {
						continue;
					}
					if (key.isConnectable()) { 
						((SocketChannel)key.channel()).finishConnect();
					} 
					if (key.isAcceptable()) {
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
						SocketChannel channel = serverSocketChannel.accept();
						if (channel != null) {
							addNewClient(channel);
						}
					} else if (key.isReadable()) {
						read(key);
					}
				}
			}
			catch (IOException ioe) {
				logger.error("error during serverSocket select(): " + ioe.getMessage());
				ioe.printStackTrace();
			}
			catch (Exception e) {
				logger.error("exception in run()");
				e.printStackTrace();
			}
		}
		logger.fatal("Server no longer running");
	}
	
	protected Client addNewClient(SocketChannel channel) throws IOException {
		channel.configureBlocking(false);
		channel.socket().setTcpNoDelay(true);
		
		Client player = new Client(IdGiver.getId(),channel);
		clientsByChannel.put(channel, player);
		clientsById.put(player.getId(), player);
		//player.setCurrentGameSession(lobby);
		
		channel.register(selector, SelectionKey.OP_READ, new Attachment(player.getId(), this));
		logger.info("new client connected : "+channel.socket().getInetAddress()+" "+player);
		return player;
	}
	
	//todo factor this ?
	private void read(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		Attachment attachment = (Attachment) key.attachment();
		int numRead;
		try {
			numRead = channel.read(attachment.getBuff());
		} catch (IOException e) {
			drop(key);
			return;
		}

		if (numRead == -1) {
			drop(key);
			return;
		}
		attachment.checkForMessages();
	}
	
	
	public void drop(SelectionKey key) {
		Client player = clientsByChannel.get((SocketChannel)key.channel());
		IServerSession currentGameSession = player.getCurrentGameSession();
		if (currentGameSession != null) {
			currentGameSession.removeClient(player, "logged out");
		}
		clientsByChannel.remove(key.channel());
		clientsById.remove(player.getId());
		IdGiver.freeId(player.getId());
		key.cancel();
		try {
			key.channel().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("player dropped : "+player+" logged out");
	}
	
	public void processMessage(Message msg) {
		IServerSession currentGameSession = clientsById.get(msg.getClientId()).getCurrentGameSession();
		if (currentGameSession == null) {
			logger.error("player does not have any game session");
			return;
		}
		else {
			currentGameSession.enQueueMessage(msg);
		}
		/*System.out.println("new message : "+msg);
		MessageToSend msgToSend = new MessageToSend(clientsById.get(msg.getClientId()).getChannel(), msg);
		msgWriter.addMsg(msgToSend);*/
	}
	
	public void heartBeat() {
		long currentTime = System.currentTimeMillis() * 1000;
		deltaTime = currentTime - lastTime;
		deltaTime /= 1000000.0;
		lastTime = currentTime;
	}
}
