/*
 * File:         RawQuery.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

/**
 * RawQuery is created when a peer sends out a query, 
 * the query is given a key value as a way of indexing all querys that are ever output.
 * 
 * The community is the community the query is requested in.
 * The queryString is what was actually queried
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class RawQuery {
	private String community;
	private String queryString;
	private int key;
	private static int keyCounter = 0;
	
	/**
	 * Keys the query.
	 * @param community the community the query is requested in.
	 * @param queryString String which was actually queried
	 */
	public RawQuery(String community, String queryString) {
		this.community = community;
		this.queryString = queryString;
		this.key = keyCounter;
		keyCounter += 1;
	}	
	
	/**
	 * Gets the String that was queried
	 * @return The text string which was queried
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * Gets the community this query comes from.
	 * @return The community of this query.
	 */
	public String getCommunity() {
		return community;
	}
	
	/**
	 * Gets the key value (index) of this query
	 * @return The key value of this query.
	 */
	public int getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		return "Key Index:     "+key+"\n" +
		       "Community:     "+community+"\n"+
		       "Query String:  "+queryString+"\n";
	}
}
