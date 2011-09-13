import fr.frozen.network.server.BaseServer;


public class Test {
	
	public static void main(String []args) {
		BaseServer server = new BaseServer(1234);
		server.start();
	}
}
