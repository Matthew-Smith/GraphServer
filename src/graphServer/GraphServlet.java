/*
 * File:         GraphServlet.java
 * Created:      18/07/2011
 * Last Changed: Date: 18/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package graphServer;

import graphServer.graph.P2PGraph;

import javax.servlet.http.*;

import java.io.*;
import java.net.SocketException;

/**
 * GraphServlet is an HttpServlet which has a stored graph of P2P network events. 
 * These events come in over a UDP Connection in the servlet's UDP Receiver.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 18/07/2011
 */
public class GraphServlet extends HttpServlet
{
	P2PGraph graph;
	UDPReceiver udpReceiver;
	private static final long serialVersionUID = -5953068352245332013L;

	//[start] Overridden init and doGet from HttpServlet
	@Override
	public void init() {
		try {
			graph = new P2PGraph();
			udpReceiver = new UDPReceiver();
			udpReceiver.addListener(graph);
			udpReceiver.startReceiving();
		} catch(SocketException se) {
			se.printStackTrace();
		}
	}


	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String responseString="";
			OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());

			// set the response code and write the response data
			resp.setStatus(HttpServletResponse.SC_OK);

			if(req.getRequestURI().endsWith("getGraph")) {
				responseString = doGetGraph();
			}
			else if(req.getRequestURI().endsWith("getLogEvents")) {
				String timeString = req.getParameter("time");
				responseString = doGetLogEvents(Long.parseLong(timeString));
			}
			else if(req.getRequestURI().endsWith("debug")) {
				responseString = doGetDebug();
			}
			else {
				responseString = "<ERROR/>";
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			writer.write(responseString);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			try{
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().print(e.getMessage());
				resp.getWriter().close();
			} catch (IOException ignored) {
			}
		}

	}
	//[end] Overridden init and doGet from HttpServlet
	
	//[start] Getters for this servlet's specific requests
	/**
	 * Gets a String with the Vertices and Edges currently in the network graph.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @return XML String with the current graph's vertices and edges.
	 */
	private String doGetGraph() {
		return graph.getCurrentGraph();
	}

	/**
	 * Gets a Sring with all log events which happen after the passed time in milliseconds.
	 * 
	 * Formatted as XML for use with spiderweb visualizer.
	 * @param time <code>long</code> time in milliseconds, all events which take place after this time will be returned.
	 * @return XML String with all the log events that happen after the time passed in the request parameter
	 */
	private String doGetLogEvents(long time) {
		return graph.getLogEventsAfter(time);
	}
	
	/**
	 * Gets a String with various debug information from the graph.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with debug information.
	 */
	private String doGetDebug() {
		StringBuffer b = new StringBuffer();
		b.append("<HTML>\n\t<BODY>\n"); //add on HTML Tags for browser viewing
		b.append("\t<H1>Debug Info</H1>\n");
		
		//get the debug info from the graph and UDP Receiver
		b.append(graph.getDebugInfo()); 
		b.append(udpReceiver.getDebugInfo());
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}
	//[end] Getters for this servlet's specific requests
}