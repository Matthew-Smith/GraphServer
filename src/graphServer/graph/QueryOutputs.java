/*
 * File:         QueryOutput.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

import java.util.LinkedList;

public class QueryOutputs {
	
	private LinkedList<String> queryIDs;
	
	public QueryOutputs() {
		queryIDs = new LinkedList<String>();
	}
	
	public int add(String queryID) {
		if(!queryIDs.contains(queryID)) {
			queryIDs.add(queryID);
			return queryIDs.size()-1;
		}
		return queryIDs.indexOf(queryID);
	}
	
	public String getQueryID(int key) {
		return queryIDs.get(key);
	}
	
	public int getKey(String queryID) {
		return queryIDs.indexOf(queryID);
	}
}
