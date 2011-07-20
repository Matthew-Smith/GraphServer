/*
 * File:         UDPReceiver.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * A UDPReceiver acts as a server where packets can be received and passed to 
 * objects which are interested in handling the packets the server receives.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 * @see UDPListener
 */
public class UDPReceiver {

	public static final int theport = 8888;
	public static final int timeBetween = 100;
	public static final int maxDataPackets = 100;
	
	private int port;
	private DatagramSocket serverSocket;
	private boolean listening;
	private LinkedList<UDPListener> listeners;
	private Thread packetDispatch;
	private Thread socketListener;
	private LinkedList<DatagramPacket> receivedData;

	//[start] Constructors
	/**
	 * Construct the UDPReceiver
	 * sets up the port, UDP socket and threads for handling incoming packets.
	 * @throws SocketException If there was an error in creating the UDP socket.
	 */
	public UDPReceiver() throws SocketException{
		this(theport);
	}
	
	/**
	 * Construct the UDPReceiver
	 * sets up the port, UDP socket and threads for handling incoming packets.
	 * @param port The Port for the socket to use.
	 * @throws SocketException If there was an error in creating the UDP socket.
	 */
	public UDPReceiver(int port) throws SocketException{
		this.port = port;
		serverSocket = new DatagramSocket(port);
		listening = true;
		listeners = new LinkedList<UDPListener>();
		
		/**
		 * packet Dispatch Thread, notifies listeners about received packets.
		 */
		packetDispatch = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Event Dispatch Thread Started");
				while(listening) {
					consumeData();
				}
			}
			
		});
		packetDispatch.setDaemon(true);
		
		/**
		 * Socket Listener Thread, receives messages through the UDP Connection.
		 */
		socketListener = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Socket Listener Thread Started");
				while(listening) {
					try {
						byte[] arrayOfByte = new byte[1024];
						DatagramPacket packet = new DatagramPacket(arrayOfByte, arrayOfByte.length);
						serverSocket.receive(packet);
						putData(packet);
					} catch(IOException unused) {}
				}
				
			}
			
		});
		socketListener.setDaemon(true);
		
		receivedData = new LinkedList<DatagramPacket>();
	}
	//[end] Constructors
	
	//[start] Listener Methods

	/**
	 * Adds a UDP Listener to the list of listeners.
	 * @param listener The UDP Listener which will handle any incoming messages.
	 * @see UDPListener
	 */
	public void addListener(UDPListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notifies all listeners on a new thread that a message has been received.
	 * @param packet the packet to notify listeners about.
	 * @see UDPListener
	 */
	protected void notifyReceiveMessage(final DatagramPacket packet) {
		Thread packetDispatch = new Thread(new Runnable() {

			@Override
			public void run() {
				for(UDPListener l : listeners) {
					l.receiveMessage(packet);
				}
			}
		});
		packetDispatch.start();
	}
	//[end] Listener Methods

	//[start] Producer Consumer Methods
	/**
	 * Starts The thread listening to the UDP socket, and 
	 * the thread which dispatches packets to any listeners.
	 */
	public void startReceiving() {
		socketListener.start();
		packetDispatch.start();
	}
	
	
	/**
	 * Puts the most recently received packet at the end of the receivedData list.
	 * @param packet Most recently received packet from the UDP socket.
	 * @see <A HREF="http://en.wikipedia.org/wiki/Producer-consumer_problem">Producer-Consumer</A>
	 */
	protected void putData(DatagramPacket packet) {
		
		synchronized(receivedData) {
			while(receivedData.size() == maxDataPackets) {
				try {
					receivedData.wait();
				} catch(InterruptedException ignore) {}
			}
			receivedData.add(packet);
			receivedData.notifyAll();
		}
	}

	/**
	 * Removes the first packet (earliest) from the list of received data and notifies any listeners of the packet.
	 * @see <A HREF="http://en.wikipedia.org/wiki/Producer-consumer_problem">Producer-Consumer</A>
	 */
	protected void consumeData() {
		
		synchronized(receivedData) {
			while(receivedData.size() == 0) {
				try {
					receivedData.wait();
				} catch(InterruptedException ignore) {}
			}
			DatagramPacket packet = receivedData.removeFirst();
			notifyReceiveMessage(packet);
			receivedData.notifyAll();
		}
	}
	//[end] Producer Consumer Methods

	//[start] Debug
	/**
	 * Returns a String with debug information about the UDP Receiver.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with debug information.
	 */
	public String getDebugInfo() {
		StringBuffer b = new StringBuffer();
		
		b.append("\t<H3>UDP Info</H3>\n");
		b.append("<p>\n<dd>UDP Port: "+port+"<br />\n");
		b.append("<dd>Listening: "+listening+"<br />\n");
		b.append("<dd>Number of Listeners: "+listeners.size()+"<br />\n");
		b.append("<dd>Number of packets in the buffer: "+receivedData.size()+"\n</p>\n");
		
		return b.toString();
	}
	//[end] Debug
}