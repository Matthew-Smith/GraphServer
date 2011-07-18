/*
 * File:         RawDocument.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
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
