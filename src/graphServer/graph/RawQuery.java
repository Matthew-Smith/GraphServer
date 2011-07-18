/*
 * File:         RawQuery.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

public class RawQuery {
	private String community;
	private String queryString;
	private int key;
	private static int keyCounter = 0;
	
	public RawQuery(String community, String queryString) {
		this.community = community;
		this.queryString = queryString;
		this.key = keyCounter;
		keyCounter += 1;
	}	
	
	public String getQueryString() {
		return queryString;
	}

	public String getCommunity() {
		return community;
	}
	
	public int getKey() {
		return key;
	}
	
}
