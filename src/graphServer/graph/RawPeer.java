/*
 * File:         RawPeer.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

public class RawPeer {
	private String identifier;
	private int key;
	private static int keyCounter=0;
	
	public RawPeer(String identifier) {
		this.identifier = identifier;
		key = keyCounter;
		keyCounter++;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public int getKey() {
		return key;
	}
	
}
