package graphServer.graph;

public class QueryOutput {
	private String id;
	private int key;
	private static int keyCounter=0;
	
	public QueryOutput(String id) {
		this.id = id;
		key = keyCounter;
		keyCounter++;
	}
	
	public String getID() {
		return id;
	}
	
	public int getKey() {
		return key;
	}
}
