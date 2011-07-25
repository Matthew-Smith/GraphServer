/*
 * File:         RawDocument.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

/**
 * RawDocument is created when a new document is published, 
 * it is given a key value as a way of indexing all documents that are published.
 * 
 * The title of the document is not learned until it is a query hit so does not need to be passed in the constructor.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class RawDocument {
	private String community;
	private String document;
	private String title;
	private int key;
	private static int keyCounter=0;
	
	/**
	 * Keys the document.
	 * @param community the community this document resides in.
	 * @param document the document's identifier.
	 */
	public RawDocument(String community, String document) {
		this.community = community;
		this.document = document;
		title = "unknown";
		key = keyCounter;
		keyCounter++;
	}
	
	/**
	 * Gets the key value (index) of this Document
	 * @return The key value of this document.
	 */
	public int getKey() {
		return key;
	}
	
	/**
	 * Gets the community this document is in.
	 * @return the community this document is in.
	 */
	public String getCommunity(){
		return community;
	}
	
	/**
	 * Gets the document's identifier String.
	 * @return the document's identifier.
	 */
	public String getDocument() {
		return document;
	}
	
	/**
	 * Sets the title of the document.
	 * @param The title this document will be given.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the title of the document.
	 * @return The title of the document ("unknown" if title has not been set)
	 */
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return "Key Index: "+key+"\n" +
		       "Community: "+community+"\n"+
		       "Document:  "+document+"\n"+
		       "Title:     "+title;
	}
}
