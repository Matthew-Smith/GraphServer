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
