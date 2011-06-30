package graphServer;

import java.net.InetAddress;

public interface UDPListener {
	
	public void receiveMessage(String message, InetAddress IPAddress, int port);
	
}
