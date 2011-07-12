package graphServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

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
				while(listening) {
					consumeData();
				}
			}
			
		});
		
		socketListener = new Thread(new Runnable() {

			@Override
			public void run() {
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
		
		receivedData = new LinkedList<DatagramPacket>();
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
	 * @param message The received message.
	 * @param IPAddress The IP-Address of the sender of the message.
	 * @param port The port on which the message was received.
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
			notifyReceiveMessage(receivedData.removeLast());
			receivedData.notifyAll();
		}
	}
}