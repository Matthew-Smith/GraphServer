package graphServer.graph;

public class RawDocument {
	private String community;
	private String document;
	private String title;
	private int key;
	private static int keyCounter=0;
	
	public RawDocument(String community, String document) {
		this.community = community;
		this.document = document;
		title = "unknown";
		key = keyCounter;
		keyCounter++;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getCommunity(){
		return community;
	}
	
	public String getDocument() {
		return document;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
}
