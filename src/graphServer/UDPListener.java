package graphServer;

import java.net.DatagramPacket;

public interface UDPListener {

	public void receiveMessage(DatagramPacket receivePacket);
	
}