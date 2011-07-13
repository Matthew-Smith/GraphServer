package graphServer.graph;

import graphServer.UDPListener;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import spiderweb.graph.*;
import spiderweb.graph.savingandloading.P2PNetworkGraphSaver;
import spiderweb.visualizer.eventplayer.LogEvent;

public class P2PGraph implements UDPListener {

	public static final long EPOCH = 1309392000000L;
	private P2PNetworkGraph graph;
	private P2PNetworkGraph referenceGraph;
	private LinkedList<LogEvent> logEvents;
	private long simulationTime;
	private Hashtable<String, RawPeer> peerTable;
	private Hashtable<String, RawDocument> documentTable;
	private Hashtable<String, RawQuery> queryTable;
	private Hashtable<String, RawQueryHit> queryHitTable;
	private Hashtable<String, QueryOutput> queryOutputTable;

	public P2PGraph() {
		referenceGraph = new P2PNetworkGraph();
		graph = new P2PNetworkGraph();
		logEvents = new LinkedList<LogEvent>();
		simulationTime = 0L;
		
		peerTable = new Hashtable<String, RawPeer>();
		documentTable = new Hashtable<String, RawDocument>();
		queryTable = new Hashtable<String, RawQuery>();
		queryHitTable = new Hashtable<String, RawQueryHit>();
		queryOutputTable = new Hashtable<String, QueryOutput>();
	}

	//[start] Create Documents for sending over the web
	public String createCurrentGraphDocument() {
		return P2PNetworkGraphSaver.saveGraphForWeb(graph, simulationTime);
	}

	public String createLogEventDocument(long time) {
		LinkedList<LogEvent> toSend = new LinkedList<LogEvent>();

		ListIterator<LogEvent> it = logEvents.listIterator(logEvents.size()-1);

		while(it.hasPrevious()) {
			LogEvent evt = it.previous();
			if(time<evt.getTime()) {
				toSend.add(evt);
			}
			else {
				break; //since Log Events are ordered in the list, there will be no point continuing the loop
			}
		}

		for(LogEvent evt : logEvents) {
			if(time<evt.getTime()) {
				toSend.add(evt);
			}
		}
		return P2PNetworkGraphSaver.saveEventsForWeb(toSend, simulationTime);
	}
	//[end] Create Documents for sending over the web

	private LogEvent parseEvent(String rawEvent, String IPAddress, int port) {
		String peerMappingKey = IPAddress+"-"+port;
		String delim = "\\s+";
		String[] token = rawEvent.split(delim);
		String rawType = token[1].toLowerCase();
		long time = Long.parseLong(token[0]);
		time = time-EPOCH;
		String type = rawType;
		int param1 = 0;
		int param2 = 0;
		
		if(rawType.equals("queryHit")) {
			
		} 
		else if(rawType.equals("query")) {
			String community = token[3].split("[:]+")[1];
			String queryString = token[4].split("[:]+")[1];
			
			RawQuery rq = new RawQuery(community,queryString);
		}
		else if(rawType.equals("online")) {
			String uniquePeerID = IPAddress+":"+token[2];
			RawPeer p;
			if(peerTable.containsKey(peerMappingKey)) {
				p = peerTable.get(peerMappingKey);
			}
			else {
				p = new RawPeer(uniquePeerID);
				peerTable.put(peerMappingKey, p);
			}
			param1 = p.getKey();
			
		} 
		else if(rawType.equals("connect")) {
			param1 = peerTable.get(peerMappingKey).getKey();
			for(Entry<String, RawPeer> entry : peerTable.entrySet()) {
				if(entry.getValue().getIdentifier().equals(token[2])) {
					param2 = entry.getValue().getKey();
					break;
				}
			}
		}
		else if(rawType.equals("offline")) {
			//String uniquePeerID = IPAddress+":"+token[2];
			RawPeer p = peerTable.get(peerMappingKey);
			param1 = p.getKey();
		} 
		else if(rawType.equals("query_reaches_peer")) {
			type="queryreachespeer";
		} 
		else if(rawType.equals("publish")) {
			param1 = peerTable.get(peerMappingKey).getKey();
			String documentMappingKey = token[2]+"/"+token[3];
			RawDocument d;
			if(documentTable.containsKey(documentMappingKey)) {
				d = documentTable.get(documentMappingKey);
			}
			else {
				d = new RawDocument(token[2],token[3]);
				documentTable.put(documentMappingKey, d);
			}
			param2 = d.getKey();
		} 
		else if(rawType.equals("remove")) {
			
		}
		
		
		LogEvent evt = new LogEvent(time, type, param1, param2);
		//System.out.println(evt);
		return evt;
	}


	public String getLogEventsAfter(long time) {
		return createLogEventDocument(time);
	}

	public String getCurrentGraph() {
		return createCurrentGraphDocument();
	}

	@Override
	public synchronized void receiveMessage(DatagramPacket receivePacket) {
		String rawEvent = new String(receivePacket.getData());
		rawEvent = rawEvent.substring(0,rawEvent.indexOf("\n"));
		InetAddress IPAddress = receivePacket.getAddress();
		int port = receivePacket.getPort();
		LogEvent evt = parseEvent(rawEvent, IPAddress.toString(), port);
		
		referenceGraph.graphConstructionEvent(evt);
		graph.graphEvent(evt, true, referenceGraph);
		logEvents.add(evt);
	}
}