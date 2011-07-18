/*
 * File:         RawQueryHit.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

public class RawQueryHit {
	private RawDocument document;
	private RawQuery query;
	private int key;
	private static int keyCounter = 0;
	
	public RawQueryHit(RawDocument document, RawQuery query) {
		this.document = document;
		this.query = query;
		this.key = keyCounter;
		keyCounter += 1;
	}
	
	public RawDocument getDocument() {
		return document;
	}
	
	public RawQuery getQuery() {
		return query;
	}
	
	public int getKey() {
		return key;
	}
}
