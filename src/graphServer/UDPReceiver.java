package graphServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.LinkedList;

import javax.swing.Timer;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class UDPReceiver implements ActionListener {

	public static final int theport = 8888;
	public static final int timeBetween = 100;
	private int port;
	private DatagramSocket serverSocket;
	private boolean listening;
	private LinkedList<UDPListener> listeners;
	private Timer scheduler;

	public UDPReceiver() throws SocketException{
		port = theport;
		serverSocket = new DatagramSocket(port);
		listening = true;
		listeners = new LinkedList<UDPListener>();
		scheduler = new Timer(timeBetween,this);
	}
	
	//[start] Listener Methods
	
	/**
	 * Adds a UDP Listener to the list of listeners.
	 * @param listener	The UDP Listener which will handle any incoming messages.
	 */
	public void addListener(UDPListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Notifies all listeners on a new thread that a message has been received.
	 * @param message	The received message.
	 * @param IPAddress	The IP-Address of the sender of the message.
	 * @param port		The port on which the message was received.
	 */
	private void notifyReceiveMessage(final String message, final InetAddress IPAddress, final int port) {
		Thread eventDispatch = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(UDPListener l : listeners) {
					l.receiveMessage(message, IPAddress, port);
				}
			}
		});
		eventDispatch.start();
	}
	//[end] Listener Methods
	
	public void startReceiving() {
		scheduler.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(listening)
		{
			try {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String message = new String( receivePacket.getData());
				message = message.substring(0,message.indexOf("\n"));//cut off string at new line
				System.out.println("RECEIVED: " + message + " from :");
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
				System.out.println(IPAddress + " / "+ port);
				
				notifyReceiveMessage(message, IPAddress, port);
			} catch(IOException e) {

			}
		}
	}

	public static void main(String args[]) throws Exception
	{
		UDPReceiver bip = new UDPReceiver();
		bip.startReceiving();
		// bip.testhex();
		//bip.listenForever();
	}

	public void testhex(){
		SecureRandom saltGenerator = new SecureRandom();
		HexBinaryAdapter hexConverter = new HexBinaryAdapter();
		byte[] salt = new byte[8];

		saltGenerator.nextBytes(salt);
		//if (salt==null)
		System.out.println("salt is \n");
		for(byte b :salt)
			System.out.println(b);
		String saltHex = hexConverter.marshal(salt);
		//	config.addProperty("up2p.password.salt", saltHex,
		//"Hex string of the salt bytes used for user authentication.");
		System.out.println("Saved salt:"+ saltHex);
	}


	public void listenForever() throws IOException{
		byte[] receiveData = new byte[1024];
		//byte[] sendData = new byte[1024];
		System.out.println("Listening...");
		while(listening)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			String message = new String( receivePacket.getData());
			message = message.substring(0,message.indexOf("\n"));//cut off string at new line
			System.out.println("RECEIVED: " + message + " from :");
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			System.out.println(IPAddress + " / "+ port);
			
			notifyReceiveMessage(message, IPAddress, port);
			
			/*String capitalizedSentence = sentence.toUpperCase();
              sendData = capitalizedSentence.getBytes();
              DatagramPacket sendPacket =
              new DatagramPacket(sendData, sendData.length, IPAddress, port);
              serverSocket.send(sendPacket);*/
		}
	}

}