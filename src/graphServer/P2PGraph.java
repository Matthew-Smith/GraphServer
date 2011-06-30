package graphServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Timer;

import spiderweb.graph.*;
import spiderweb.graph.savingandloading.P2PNetworkGraphLoader;
import spiderweb.graph.savingandloading.P2PNetworkGraphSaver;
import spiderweb.visualizer.eventplayer.LogEvent;

public class P2PGraph implements ActionListener, UDPListener {
	
	private P2PNetworkGraph graph;
	private LinkedList<LogEvent> logEvents;
	private long simulationTime;
	private Timer timer;
	
	public P2PGraph() {
		graph = new P2PNetworkGraph();
		logEvents = new LinkedList<LogEvent>();
		simulationTime = 0;
		timer = new Timer(1,this);
		timer.start();
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
	public void actionPerformed(ActionEvent arg0) {
		//simulationTime += ;
		
	}

	@Override
	public synchronized void receiveMessage(String message, InetAddress IPAddress, int port) {
		LogEvent evt = parseEvent(message);
		
		
	}
}