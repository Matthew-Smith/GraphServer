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
