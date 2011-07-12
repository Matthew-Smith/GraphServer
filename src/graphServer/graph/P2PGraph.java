package graphServer.graph;

import graphServer.UDPListener;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

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
	private Hashtable<String, RawQuery> queryTable;
	private Hashtable<String, RawQueryHit> queryHitTable;
	private Hashtable<String, QueryOutput> queryOutputTable;

	public P2PGraph() {
		referenceGraph = new P2PNetworkGraph();
		graph = new P2PNetworkGraph();
		logEvents = new LinkedList<LogEvent>();
		simulationTime = 0L;
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
		String peerIdentifier = IPAddress+"-"+port;
		String delim = "[ ]+";
		String[] token = rawEvent.split(delim);
		String rawType = token[1].toLowerCase();
		long time = Long.parseLong(token[0]);
		String type = rawType;
		int param1 = 0;
		int param2 = 0;
		
		if(rawType.equals("queryHit")) {
			
		} else if(rawType.equals("query")) {
			
		} else if(rawType.equals("online")) {
			RawPeer p = new RawPeer(peerIdentifier);
			peerTable.put(IPAddress+":"+token[2], p);
			type = rawType;
			param1 = p.getKey();
			
		} else if(rawType.equals("connect")) {
			
		} else if(rawType.equals("offline")) {
			
		} else if(rawType.equals("query_reaches_peer")) {
			type="queryreachespeer";
		} else if(rawType.equals("publish")) {
			
		} else if(rawType.equals("remove")) {
			
		}
		
		
		LogEvent evt = new LogEvent(time, type, param1, param2);
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