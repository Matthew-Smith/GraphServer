/*
 * File:         UDPListener.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer;

import java.net.DatagramPacket;

/**
 * Represents an interface for a class which will be receiving messages over a UDP Connection.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version $Date: 18/07/2011 $
 */
public interface UDPListener {

	/**
	 * Receive and handle a packet from the UDP Connection.
	 * @param receivePacket The Packet received through the UDP connection.
	 */
	public void receiveMessage(DatagramPacket receivePacket);
	
}