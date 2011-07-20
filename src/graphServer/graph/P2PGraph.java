/*
 * File:         P2PGraph.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

import graphServer.UDPListener;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import spiderweb.graph.*;
import spiderweb.graph.savingandloading.P2PNetworkGraphSaver;
import spiderweb.graph.LogEvent;

/**
 * P2PGraph maintains the P2PNetworkGraph and log events received from the UDP Connection.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011 
 */
public class P2PGraph implements UDPListener {
									  
	public static final long EPOCH = 1309514400000L; //Happy Canada Day 2011 (July 1, 2011 12:00am GMT)
	
	private P2PNetworkGraph graph;
	private P2PNetworkGraph referenceGraph;
	private LinkedList<LogEvent> logEvents;
	private long simulationTime;
	private Hashtable<String, RawPeer> peerTable;			// IP-UDPPort -> IP:Gnutella Port + key
	private Hashtable<String, RawDocument> documentTable;	// CommunityID/DocumentID -> Community + Document + key
	private Hashtable<Integer, RawQuery> queryTable;		// QueryOutput key -> Community + QueryString + key
	private Hashtable<Integer, RawQueryHit> queryHitTable;	// QueryOutput key -> document + query + key
	private QueryOutputs queryOutputTable;					// index(QueryOutput key) -> QueryID
	private LinkedList<String> incomingMessages;
	private StringBuffer graphLog;
	
	//[start] Constructor
	/**
	 * Construct the P2PGraph
	 * Sets up the graph and lists/tables for storing and handling UDP Messages.
	 */
	public P2PGraph() {
		referenceGraph = new P2PNetworkGraph();
		graph = new P2PNetworkGraph();
		logEvents = new LinkedList<LogEvent>();
		simulationTime = 0L;
		
		peerTable = new Hashtable<String, RawPeer>();
		documentTable = new Hashtable<String, RawDocument>();
		queryTable = new Hashtable<Integer, RawQuery>();
		queryHitTable = new Hashtable<Integer, RawQueryHit>();
		queryOutputTable = new QueryOutputs();
		
		incomingMessages = new LinkedList<String>();
		graphLog = new StringBuffer();
		log("Constructor Completed.");
	}
	//[end] Constructor

	//[start] Create Documents for sending over the web
	/**
	 * Creates a String with the graph information at the current network time.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @return The XML document containing all vertices and edges of the graph, as a String.
	 */
	public String createCurrentGraphDocument() {
		log("\n\tCreating Document for Current Graph.\n");
		return P2PNetworkGraphSaver.saveGraphForWeb(graph, simulationTime);
	}

	/**
	 * Iterates over the list of log events and creates a String containing all the log events after the passed time.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @param time the time for which to get log events after.
	 * @return The XML document containing the log events, as a string.
	 */
	public String createLogEventDocument(long time) {
		log("\n\tCreating Document for Log Events after time: "+time+"ms");
		LinkedList<LogEvent> toSend = new LinkedList<LogEvent>();
		
		ListIterator<LogEvent> it = logEvents.listIterator(logEvents.size()-1);
		
		log("\tEvents to send: ");
		
		while(it.hasPrevious()) { //go backwards through list
			LogEvent evt = it.previous();
			if(time<evt.getTime()) { //don't add the event at the passed time as they will already have it
				toSend.addFirst(evt); //add event to the beginning of the list of events to send because of iterating in reverse.
				log("\t\t"+evt);
			}
			else {
				break; //since Log Events are ordered in the list, there will be no point continuing the loop
			}
		}
		log(""); // put a new line in the log
		
		String s = P2PNetworkGraphSaver.saveEventsForWeb(toSend, simulationTime);
		return s;
	}
	//[end] Create Documents for sending over the web

	//[start] Parsing Methods
	
	/**
	 * Creates and returns a LogEvent corresponding to the query reaches peer message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed query reaches peer log event for the network graph.
	 */
	private LogEvent parseQueryReachesPeer(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time	 IP:gnutellaPort	 QUERY_REACHES_PEER	 QueryID
		
		String queryID = token[3];
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		int param1 = peerTable.get(peerMappingKey).getKey();
		int param2 = queryOutputTable.getKey(queryID);// getKey will get the key value if it exists, or key it and return that new key
		LogEvent evt = new LogEvent(time, "queryreachespeer",param1, param2);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the query hit message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed query hit log event for the network graph.
	 */
	private LogEvent parseQueryHit(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time	IP:gnutellaPort	QUERYHIT	QueryID		communityID	 documentID	 documentName
		
		
		//[start] get Document info
		String documentMappingKey = token[4]+"/"+token[5]; // "communityID/documentID"
		RawDocument doc;
		if(documentTable.containsKey(documentMappingKey)) {
			doc = documentTable.get(documentMappingKey); //if the value exists, get it
			doc.setTitle(token[6]);
		}
		else { //otherwise add a new entry
			doc = new RawDocument(token[4],token[5]);
			doc.setTitle(token[6]);
			
			documentTable.put(documentMappingKey, doc);
		}
		//[end] get Document info
		
		//[start] get Query info
		String queryID = token[3];
		int queryKey = queryOutputTable.getKey(queryID);// getKey will get the key value if it exists, or key it and return that new key
		
		RawQuery query;
		if(queryTable.containsKey(queryKey)) {
			query = queryTable.get(queryKey);
		}
		else {
			query = new RawQuery(token[4],"[unknown]"); //since the queryString is not known, give it an arbitrary value 
			queryTable.put(queryKey, query);
		}
		//[end] get Query info
		
		RawQueryHit rqh = new RawQueryHit(doc,query);
		queryHitTable.put(queryKey,rqh);
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		int param1 = peerTable.get(peerMappingKey).getKey();
		int param2 = doc.getKey();
		//int param3 = queryKey;
		
		LogEvent evt = new LogEvent(time, "queryhit",param1, param2);
		return evt; 
	}
	
	
	/**
	 * Creates and returns a LogEvent corresponding to the query message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed query log event for the network graph.
	 */
	private LogEvent parseQuery(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time IP:gnutellaPort 	QUERY 	QueryID 	Community:communityID	Query:queryString
		
		String queryID = token[3];
		String community = token[4].split("[:]+")[1];
		String queryString = token[5].split("[:]+")[1];
		
		int queryKey = queryOutputTable.getKey(queryID);
		RawQuery rq = new RawQuery(community,queryString);
		queryTable.put(queryKey, rq);
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		int param1 = peerTable.get(peerMappingKey).getKey();
		int param2 = rq.getKey();
		//int param3 = queryKey;
		
		LogEvent evt = new LogEvent(time, "query",param1, param2);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the online message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed online log event for the network graph.
	 */
	private LogEvent parseOnline(long time, String peerMappingKey, String uniquePeerID) {
		// time IP:gnutellaPort ONLINE
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		RawPeer p = peerTable.get(peerMappingKey);
		int param1 = p.getKey();
		LogEvent evt = new LogEvent(time, "online",param1, 0);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the offline message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed offline log event for the network graph.
	 */
	private LogEvent parseOffline(long time, String peerMappingKey, String uniquePeerID) {
		// time IP:gnutellaPort OFFLINE
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		RawPeer p = peerTable.get(peerMappingKey);
		int param1 = p.getKey();
		LogEvent evt = new LogEvent(time, "offline",param1, 0);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the publish message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed publish log event for the network graph.
	 */
	private LogEvent parsePublish(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time	IP:gnutellaPort	PUBLISH communityID documentID
		
		String documentMappingKey = token[3]+"/"+token[4];
		RawDocument d;
		if(documentTable.containsKey(documentMappingKey)) {
			d = documentTable.get(documentMappingKey);
		}
		else {
			d = new RawDocument(token[3],token[4]);
			documentTable.put(documentMappingKey, d);
		}
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		int param1 = peerTable.get(peerMappingKey).getKey();
		int param2 = d.getKey();
		LogEvent evt = new LogEvent(time, "publish",param1, param2);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the remove message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed remove log event for the network graph.
	 */
	private LogEvent parseRemove(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time	IP:gnutellaPort	REMOVE communityID documentID
		
		String documentMappingKey = token[3]+"/"+token[5];
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		int param1 = peerTable.get(peerMappingKey).getKey();
		
		int param2 = documentTable.get(documentMappingKey).getKey();
		LogEvent evt = new LogEvent(time, "remove",param1, param2);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the connect message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed connect log event for the network graph.
	 */
	private LogEvent parseConnect(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		// time	IP:gnutellaPort	CONNECT (other peer)IP:gnutellaPort
		
		//The peer definitely exists because of being added in the parseEvent method before this method was called
		RawPeer p1 = peerTable.get(peerMappingKey); 
		int param1 = p1.getKey();
		int param2 = 0;
		for(Entry<String, RawPeer> entry : peerTable.entrySet()) {
			if(entry.getValue().getIdentifier().equals(token[2])) {
				param2 = entry.getValue().getKey();
				break;
			}
		}
		LogEvent evt = new LogEvent(time, "connect",param1, param2);
		return evt; 
	}
	
	/**
	 * Creates and returns a LogEvent corresponding to the disconnect message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed disconnect log event for the network graph.
	 */
	private LogEvent parseDisconnect(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		RawPeer p1;
		if(peerTable.containsKey(peerMappingKey)) {
			p1 = peerTable.get(peerMappingKey);
		}
		else {
			p1 = new RawPeer(token[2]);
			peerTable.put(peerMappingKey, p1);
		}
		int param1 = p1.getKey();
		int param2 = 0;
		for(Entry<String, RawPeer> entry : peerTable.entrySet()) {
			if(entry.getValue().getIdentifier().equals(token[2])) {
				param2 = entry.getValue().getKey();
				break;
			}
		}
		LogEvent evt = new LogEvent(time, "disconnect",param1, param2);
		return evt; 
	}
	
	
	
	/**
	 * Takes in information of the received UDP message and delegates the parsing to 
	 * specialised methods to return a usable log event for the network graph.
	 * Maps the peer in the peerTable if peer sending the message is not already present.
	 * @param rawEvent	The raw Event message from the UDP connection.
	 * @param IPAddress	The IP Address that the message was received from over the UDP Connection.
	 * @param port	The Port which the UDP Connection used to send the message.
	 * @return	The parsed log event for the network graph.
	 */
	private LogEvent parseEvent(String rawEvent, String IPAddress, int port) {
		log("\n\tbegin parsing Event: ");
		StringBuffer b = new StringBuffer();
		b.append("\n\tIP("+IPAddress.toString()+") Port("+port+")\n\t"+rawEvent);
		String delim = "\\s+";
		String[] token = rawEvent.split(delim);
		String rawType = token[2].toLowerCase();
		long time = Long.parseLong(token[0]);
		time = time-EPOCH;
		String peerMappingKey = IPAddress+"-"+port;
		String uniquePeerID = token[1];
		
		log("\tTime: "+time+" \n\tType: "+rawType + "\n\tTokens: ");
		for(int i=0; i<token.length; i++) {
			log("\t\t"+token[i]);
		}
		
		if(!peerTable.containsKey(peerMappingKey)) {
			RawPeer p = new RawPeer(uniquePeerID);
			peerTable.put(peerMappingKey, p);
			log("\tPeer not found in mapping table.\n\tPut: "+peerMappingKey+"->"+uniquePeerID);
		}
		
		if(rawType.equals("queryHit")) {
			return  parseQueryHit(time, peerMappingKey, uniquePeerID, token);
		} 
		else if(rawType.equals("query_reaches_peer")) {
			return  parseQueryReachesPeer(time, peerMappingKey, uniquePeerID, token);
		} 
		else if(rawType.equals("query")) {
			return  parseQuery(time, peerMappingKey, uniquePeerID, token);
		}
		else if(rawType.equals("online")) {
			return  parseOnline(time, peerMappingKey, uniquePeerID);
		} 
		else if(rawType.equals("offline")) {
			return  parseOffline(time, peerMappingKey, uniquePeerID);
		}
		else if(rawType.equals("connect")) {
			return  parseConnect(time, peerMappingKey, uniquePeerID, token);
		}
		else if(rawType.equals("disconnect")) {
			return  parseDisconnect(time, peerMappingKey, uniquePeerID, token);
		}		
		else if(rawType.equals("publish")) {
			return  parsePublish(time, peerMappingKey, uniquePeerID, token);
		} 
		else if(rawType.equals("remove")) {
			return  parseRemove(time, peerMappingKey, uniquePeerID, token);
		}
		
		LogEvent evt = new LogEvent(-1, "ERROR, Event not parsed Properly.", 0, 0);
		b.append("\n\t"+evt);
		incomingMessages.add(b.toString());
		logError("\tMessage not parsed Properly.\n");
		return evt;
	}
	//[end] Parsing Methods

	//[start] Getters for the HTTP servlet
	/**
	 * Creates a String with all the log events after the passed time.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @return The XML document containing the log events, as a string.
	 */
	public String getLogEventsAfter(long time) {
		System.out.println("in P2PGraph");
		return createLogEventDocument(time);
	}

	/**
	 * Creates a String with the graph information at the current network time.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @return The XML document containing the graph, as a string.
	 */
	public String getCurrentGraph() {
		return createCurrentGraphDocument();
	}
	
	/**
	 * Returns a String with debug information about the P2P Graph.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with debug information.
	 */
	public String getDebugInfo() { 
		StringBuffer b = new StringBuffer();
		
		
		b.append("\t<H3>LogEvents</H3>\n");
		b.append("<dd>Total Events: "+logEvents.size()+"<br />\n");
		b.append("\t<OL>\n");
		for(LogEvent event : logEvents) {
			b.append("\t\t<LI>"+event.toString()+"</LI>\n");
		}
		b.append("\t</OL>\n");
		
		b.append("\t<H3>Graph Info</H3>\n");
		b.append("<dd>Number of Vertices: "+graph.getVertexCount()+"<br />\n");
		b.append("<dd>Number of Edges: "+graph.getEdgeCount()+"<br />\n");
		
		b.append("\t<H3>Incoming UDPMessages</H3>\n");
		
		b.append("\t<pre>");
		for(String message : incomingMessages) {
			b.append("\n"+message);
		}
		b.append("\t</pre>\n");
		
		b.append("\t<H3>Log of Events</H3>\n");
		
		b.append("\t<table bgcolor=\"#999999\">\n\t<tr><td>\n\t<p>\n");
		
		b.append("\t<pre>\n\t"+graphLog+"\t</pre></p></td></tr></table>\n");
		return b.toString();
	}
	//[end] Getters for the HTTP servlet

	//[start] UDP Listener
	@Override
	public synchronized void receiveMessage(DatagramPacket receivePacket) {
		log("\tMessage Received.");
		String rawEvent = new String(receivePacket.getData());
		rawEvent = rawEvent.substring(0,rawEvent.indexOf("\n")); //the packet's payload ends with a new line
		InetAddress IPAddress = receivePacket.getAddress(); //get the origin IP Address from the received packet
		int port = receivePacket.getPort(); //get the port from the received packet, this is the UDP Connection port, not the gnutella port
		LogEvent evt = parseEvent(rawEvent, IPAddress.toString(), port);
		
		if(!evt.getType().toLowerCase().startsWith("error")) {
			referenceGraph.graphConstructionEvent(evt); //build a graph with all peers and connections ever made
			
			if(evt.isColouringEvent()) {
				int delay = 2000;
				LogEvent opposite = LogEvent.createOpposingLogEvent(evt, delay); //after 2 seconds put in a opposite log event to decolour the node
				new Timer(true).schedule(new DecolourTask(opposite), delay);
			}
			
			graph.robustGraphEvent(evt); //build the primary graph taking all events into account
			synchronized(logEvents) {
				logEvents.add(evt);
				log("\tEvent Added to list.\n");
			}
			simulationTime = evt.getTime(); //update the server's time to the latest incoming event
		}
	}
	//[end] UDP Listener
	
	//[start] Loggers
	/**
	 * Places the passed String into the graphLog StringBuffer.
	 * @param toLog The String to log.
	 */
	private void log(Object toLog) {
		graphLog.append(toLog.toString()+"\n");
	}
	
	/**
	 * Places the passed String into the graphLog StringBuffer, surrounds with HTML bold.
	 * @param error the String to log.
	 */
	private void logError(Object error) {
		graphLog.append("<font color=\"#FF3333\"><b>"+error.toString()+"</b></font>\n");
	}
	//[end] Loggers
	
	//[start] Decolour Task Class
	/**
	 * The DecolourTask executes a set time after a log event is added so that any colouring event has an opposite.
	 */
	private class DecolourTask extends TimerTask {
		private LogEvent toAdd;
		
		public DecolourTask(LogEvent toAdd) {
			this.toAdd = toAdd;
		}
		
		@Override
		public void run() {
			
			synchronized(logEvents) {
				logEvents.add(toAdd);
				Collections.sort(logEvents);
			}
		}
	}
	//[end] Decolour Task Class
}