/*
 * File:         P2PGraph.java
 * Created:      18/07/2011
 * Last Changed: $Date: 18/07/2011 $
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer.graph;

import graphServer.UDPListener;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
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

	//[start] Create Documents for sending over the web
	public String createCurrentGraphDocument() {
		log("\n\tCreating Document for Current Graph.\n");
		return P2PNetworkGraphSaver.saveGraphForWeb(graph, simulationTime);
	}

	public String createLogEventDocument(long time) {
		log("\n\tCreating Document for Log Events after time: "+time+"ms");
		LinkedList<LogEvent> toSend = new LinkedList<LogEvent>();
		
		ListIterator<LogEvent> it = logEvents.listIterator(logEvents.size()-1);
		
		log("\tEvents to send: ");
		
		while(it.hasPrevious()) {
			LogEvent evt = it.previous();
			if(time<evt.getTime()) {
				toSend.add(evt);
				log("\t\t"+evt);
			}
			else {
				break; //since Log Events are ordered in the list, there will be no point continuing the loop
			}
		}
		log("");
		
		String s = P2PNetworkGraphSaver.saveEventsForWeb(toSend, simulationTime);
		return s;
	}
	//[end] Create Documents for sending over the web

	//[start] Parsing Methods
	/**
	 * Creates and returns a LogEvent corresponding to the query hit message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed query hit log event for the network graph.
	 */
	private LogEvent parseQueryHit(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		String queryID = token[3];
		int queryKey = queryOutputTable.getKey(queryID);
		
		String documentMappingKey = token[4]+"/"+token[5];
		RawDocument d = documentTable.get(documentMappingKey);
		d.setTitle(token[6]);
		
		RawQueryHit rqh = new RawQueryHit(d,queryTable.get(queryKey));
		queryHitTable.put(queryKey,rqh);
		
		int param1 = peerTable.get(peerMappingKey).getKey();
		int param2 = d.getKey();
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
		String queryID = token[3];
		String community = token[4].split("[:]+")[1];
		String queryString = token[5].split("[:]+")[1];
		
		int queryKey = queryOutputTable.add(queryID);
		RawQuery rq = new RawQuery(community,queryString);
		queryTable.put(queryKey, rq);
		
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
		String documentMappingKey = token[3]+"/"+token[4];
		RawDocument d;
		if(documentTable.containsKey(documentMappingKey)) {
			d = documentTable.get(documentMappingKey);
		}
		else {
			d = new RawDocument(token[3],token[4]);
			documentTable.put(documentMappingKey, d);
		}
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

		String documentMappingKey = token[3]+"/"+token[5];
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
	 * Creates and returns a LogEvent corresponding to the query reaches peer message for the 
	 * network graph given the data from the received UDP Message.
	 * @param time	The Network time.
	 * @param peerMappingKey	The mapping key for this given peer "IP-UDPport".
	 * @param uniquePeerID	The unique ID for this peer "IP:GnutellaPort".
	 * @return	The parsed query reaches peer log event for the network graph.
	 */
	private LogEvent parseQueryReachesPeer(long time, String peerMappingKey, String uniquePeerID, String[] token) {
		String queryID = token[3];
		RawPeer p;
		if(peerTable.containsKey(peerMappingKey)) {
			p = peerTable.get(peerMappingKey);
		}
		else {
			p = new RawPeer(uniquePeerID);
			peerTable.put(peerMappingKey, p);
		}
		int param1 = p.getKey();
		int param2 = queryOutputTable.getKey(queryID);
		LogEvent evt = new LogEvent(time, "queryreachespeer",param1, param2);
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

	/**
	 * Creates an XML document with all the log events after the passed time.
	 * @return The XML document containing the log events, as a string.
	 */
	public String getLogEventsAfter(long time) {
		System.out.println("in P2PGraph");
		return createLogEventDocument(time);
	}

	/**
	 * Creates an XML document with the graph at the current time.
	 * @return The XML document containing the graph, as a string.
	 */
	public String getCurrentGraph() {
		return createCurrentGraphDocument();
	}
	
	/**
	 * Creates a formatted String with the data of the current Graph.
	 * @return String with the current graph info.
	 */
	public String getDebugInfo() { 
		StringBuffer b = new StringBuffer();
		
		b.append("\t<H1>Debug Info</H1>\n");
		b.append("\t<H3>LogEvents</H3>\n");
		b.append("\tTotal Events: "+logEvents.size()+"\n");
		b.append("\t<OL>\n");
		for(LogEvent event : logEvents) {
			b.append("\t\t<LI>"+event.toString()+"</LI>\n");
		}
		b.append("\t</OL>\n");
		
		b.append("\t<H3>Graph Info</H3>\n");
		b.append("\tNumberVertices: "+graph.getVertexCount()+"\n");
		
		b.append("\t<H3>Incoming UDPMessages</H3>\n");
		
		b.append("\t<pre>");
		for(String message : incomingMessages) {
			b.append("\n"+message);
		}
		b.append("\t</pre>\n");
		
		b.append("\t<H3>Log of Events</H3>\n");
		
		b.append("\t<p><pre>\n\t"+graphLog+"\t</pre></p>\n");
		return b.toString();
	}

	@Override
	public synchronized void receiveMessage(DatagramPacket receivePacket) {
		log("\tMessage Received.");
		String rawEvent = new String(receivePacket.getData());
		rawEvent = rawEvent.substring(0,rawEvent.indexOf("\n"));
		InetAddress IPAddress = receivePacket.getAddress();
		int port = receivePacket.getPort();
		LogEvent evt = parseEvent(rawEvent, IPAddress.toString(), port);
		
		if(!evt.getType().toLowerCase().equals("error")) {
			referenceGraph.graphConstructionEvent(evt);
			
			if(evt.isColouringEvent()) {
				int delay = 2000;
				LogEvent opposite = LogEvent.createOpposingLogEvent(evt, delay);
				new Timer(true).schedule(new DecolourTask(opposite), delay);
			}
			
			graph.robustGraphEvent(evt);
			synchronized(logEvents) {
				logEvents.add(evt);
				log("\tEvent Added to list.\n");
			}
			simulationTime = evt.getTime();
		}
	}
	
	/**
	 * 
	 * @param toLog The String to log.
	 */
	private void log(String toLog) {
		graphLog.append(toLog+"\n");
	}
	
	private void logError(String error) {
		graphLog.append("<b>"+error+"</b>\n");
	}
	
	private class DecolourTask extends TimerTask {
		private LogEvent toAdd;
		
		public DecolourTask(LogEvent toAdd) {
			this.toAdd = toAdd;
		}
		
		public void run() {
			
			synchronized(logEvents) {
				logEvents.add(toAdd);
				Collections.sort(logEvents);
			}
		}
	}
}