/*
 * File:         QueryOutput.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

import java.util.LinkedList;

/**
 * Maps queryID Strings to a key index value.
 * Provides methods for adding new queryIDs and getting queryIDs from a key value and vice versa.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class QueryOutputs {
	
	private LinkedList<String> queryIDs = new LinkedList<String>();
	
	/**
	 * Gets the key value for the passed queryID. 
	 * If no such value exists, it is mapped and its key returned.
	 * @param queryID the query to get the key value of
	 * @return the key value for 
	 */
	public int getKey(String queryID) {
		if(!queryIDs.contains(queryID)) {
			queryIDs.add(queryID);
		}
		return queryIDs.indexOf(queryID);
	}
	
	/**
	 * Gets the QueryID for the passed key
	 * @param key the key to get the query value of
	 * @return The query ID for the requested key value.
	 */
	public String getQueryID(int key) {
		try {
			return queryIDs.get(key);
		} catch(IndexOutOfBoundsException e) {
			return "No query mapped to key "+key;
		}
		
	}

	/**
	 * Returns the number of Query IDs in the list.
	 * @return the number of Query IDs in the list.
	 */
	public int size() {
		return queryIDs.size();
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		return buffer.toString();
	}
	
	/**
	 * Returns the Query IDs formated as a list element for HTML display.
	 * @return the list information with HTML list element tags.
	 */
	public String toHTMLList() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\t<OL>\n");
		for(int i=0;i<queryIDs.size();i++) {
			buffer.append("\t\t<LI><PRE>"+getQueryID(i)+"</PRE></LI>\n");
		}
		buffer.append("\t</OL>\n");
		return buffer.toString();
	}
}
