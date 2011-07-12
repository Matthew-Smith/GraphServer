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

	private static LogEvent parseEvent(String rawEvent) {
		String parsedEvent = rawEvent;
		//TODO Parse

		LogEvent evt = new LogEvent(parsedEvent);
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
		LogEvent evt = parseEvent(rawEvent);
		
		referenceGraph.graphConstructionEvent(evt);
		graph.graphEvent(evt, true, referenceGraph);

	}
}