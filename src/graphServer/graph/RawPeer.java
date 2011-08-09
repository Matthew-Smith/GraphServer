/*
 * File:         RawPeer.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

/**
 * RawPeer is created when a new peer comes online, 
 * the peer is given a key value as a way of indexing all peers that ever come online.
 * 
 * The Peer's unique Identifier is their P2P traffic information ("IP:Gnutella Port")
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class RawPeer {
	private String identifier;
	private int key;
	private static int keyCounter=0;
	
	/**
	 * Keys the peer.
	 * @param identifier the peer's unique Identifier is their P2P traffic information ("IP:Gnutella Port")
	 */
	public RawPeer(String identifier) {
		this.identifier = identifier;
		key = keyCounter;
		keyCounter++;
	}
	
	/**
	 * Gets the unique identifier of this peer ("IP:Gnutella Port")
	 * @return The Unique ID of this peer.
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the key value (index) of this Peer
	 * @return The key value of this peer.
	 */
	public int getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		return "Key Index: "+key+"\nIdentifier: "+identifier;
	}
	
	/**
	 * for debugging, a reset allows the server to restart 
	 * without actually shutting down (ease of use)
	 */
	public static void reset() {
		keyCounter = 0;
	}
}
