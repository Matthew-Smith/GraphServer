/*
 * File:         RawQueryHit.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

/**
 * RawQueryHit is created when a query has reached a peer and one of that peer's documents are a query hit, 
 * the query hit is given a key value as a way of indexing all query hits that ever come occur.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class RawQueryHit {
	private RawDocument document;
	private RawQuery query;
	private int key;
	private static int keyCounter = 0;
	
	/**
	 * Keys the query hit.
	 * @param document the document identifier which is a query hit
	 * @param query the raw query (keyed query string and community) corresponding to this query hit.
	 */
	public RawQueryHit(RawDocument document, RawQuery query) {
		this.document = document;
		this.query = query;
		this.key = keyCounter;
		keyCounter += 1;
	}
	
	/**
	 * Gets the Document which is a query hit.
	 * @return The Document which is a query hit.
	 */
	public RawDocument getDocument() {
		return document;
	}
	
	/**
	 * Gets the RawQuery this query hit corresponds with.
	 * @return The RawQuery corresponding to this query hit.
	 */
	public RawQuery getQuery() {
		return query;
	}
	
	/**
	 * Gets the key value (index) of this query hit
	 * @return The key value of this query hit.
	 */
	public int getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		String[] documentInfo = document.toString().split("[\\r\\n]+");
		String[] queryInfo = query.toString().split("[\\r\\n]+");
		StringBuffer buffer = new StringBuffer("Key Index: "+key+"\n");
		buffer.append("Document:\n");
		for(int i=0;i<documentInfo.length;i++) {
			buffer.append("\t"+documentInfo[i]+"\n");
		}
		buffer.append("Query:\n");
		for(int i=0;i<queryInfo.length;i++) {
			buffer.append("\t"+queryInfo[i]+"\n");
		}
		return buffer.toString();
	}
}
