/*
 * File:         UDPReceiver.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
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
 * @version $Date: 18/07/2011 $
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
	private Thread eventDispatch;
	private Thread socketListener;
	private LinkedList<DatagramPacket> receivedData;

	public UDPReceiver() throws SocketException{
		port = theport;
		serverSocket = new DatagramSocket(port);
		listening = true;
		listeners = new LinkedList<UDPListener>();
		
		eventDispatch = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Event Dispatch Thread Started");
				while(listening) {
					consumeData();
				}
			}
			
		});
		eventDispatch.setDaemon(true);
		
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
	
	public String getDebugInfo() {
		StringBuffer b = new StringBuffer();
		
		
		
		
	
		return b.toString();
	}

	//[start] Listener Methods

	/**
	 * Adds a UDP Listener to the list of listeners.
	 * @param listener The UDP Listener which will handle any incoming messages.
	 */
	public void addListener(UDPListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notifies all listeners on a new thread that a message has been received.
	 * @param packet	
	 */
	private void notifyReceiveMessage(final DatagramPacket packet) {
		Thread eventDispatch = new Thread(new Runnable() {

			@Override
			public void run() {
				for(UDPListener l : listeners) {
					l.receiveMessage(packet);
				}
			}
		});
		eventDispatch.start();
	}
	//[end] Listener Methods

	public void startReceiving() {
		socketListener.start();
		eventDispatch.start();
		System.out.println("Start Receiving UDP");
	}
	
	private void putData(DatagramPacket packet) {
		
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
	
	private void consumeData() {
		
		synchronized(receivedData) {
			while(receivedData.size() == 0) {
				try {
					receivedData.wait();
				} catch(InterruptedException ignore) {}
			}
			DatagramPacket packet = receivedData.removeLast();
			notifyReceiveMessage(packet);
			receivedData.notifyAll();
		}
	}
}