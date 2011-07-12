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
