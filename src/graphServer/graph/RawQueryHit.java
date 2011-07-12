package graphServer.graph;

public class RawQueryHit {
	private RawDocument document;
	private QueryOutput queryOutput;
	private int key;
	private static int keyCounter = 0;
	
	public RawQueryHit(RawDocument document, QueryOutput queryOutput) {
		this.document = document;
		this.queryOutput = queryOutput;
		this.key = keyCounter;
		keyCounter += 1;
	}
	
	public RawDocument getDocument() {
		return document;
	}
	
	public QueryOutput getQueryOutput() {
		return queryOutput;
	}
	
	public int getKey() {
		return key;
	}
}
