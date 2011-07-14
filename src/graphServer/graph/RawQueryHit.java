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
