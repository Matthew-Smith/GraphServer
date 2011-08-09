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
import graphServer.graph.RawDocument;
import graphServer.graph.RawPeer;
import graphServer.graph.RawQuery;
import graphServer.graph.RawQueryHit;

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
			else if(req.getRequestURI().endsWith("getPeerInfo")) {
				responseString = doGetPeerInfo();
			}
			else if(req.getRequestURI().endsWith("getQueryOutputInfo")) {
				responseString = doGetQueryOutputInfo();
			}
			else if(req.getRequestURI().endsWith("getDocumentInfo")) {
				responseString = doGetDocumentInfo();
			}
			else if(req.getRequestURI().endsWith("getQueryInfo")) {
				responseString = doGetQueryInfo();
			}
			else if(req.getRequestURI().endsWith("getQueryHitInfo")) {
				responseString = doGetQueryHitInfo();
			}
			else if(req.getRequestURI().endsWith("getRawData")) {
				responseString = doGetRawData();
			}
			else if(req.getRequestURI().endsWith("reset")) {
				responseString = doReset();
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
		//Add a CSS Script for setting a section of text to be separately scrollable
		b.append(getScrollableCSS());
		
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		b.append("\t<H1>Debug Info</H1>\n");
		
		//get the debug info from the graph and UDP Receiver
		b.append(graph.getDebugInfo()); 
		b.append(udpReceiver.getDebugInfo());
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}
	
	/**
	 * Returns a String with information on all the query hits.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with all query hit information.
	 */
	private String doGetQueryHitInfo() {
		StringBuffer b = new StringBuffer();
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		
		//get the query hit info from the graph
		b.append(graph.getQueryHitInfo()); 
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}

	/**
	 * Returns a String with information on all the queries ever placed.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with query information.
	 */
	private String doGetQueryInfo() {
		StringBuffer b = new StringBuffer();
		b.append(getScrollableCSS());
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		
		//get the query info from the graph
		b.append(graph.getQueryInfo()); 
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}

	/**
	 * Returns a String with information on all the documents that are ever published.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with document information.
	 */
	private String doGetDocumentInfo() {
		StringBuffer b = new StringBuffer();
		b.append(getScrollableCSS());
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		
		//get the document info from the graph
		b.append(graph.getDocumentInfo()); 
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}

	/**
	 * Returns a String with information on all queryies placed and their Identifers
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with query output information.
	 */
	private String doGetQueryOutputInfo() {
		StringBuffer b = new StringBuffer();
		b.append(getScrollableCSS());
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		
		//get the query output info from the graph
		b.append(graph.getQueryOutputInfo()); 
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}

	/**
	 * Returns a String with information on all the peers who ever come online.
	 * 
	 * Contains HTML attributes for being displayed in a browser.
	 * @return HTML String with peer information.
	 */
	private String doGetPeerInfo() {
		StringBuffer b = new StringBuffer();
		b.append(getScrollableCSS());
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		
		//get the peer info from the graph
		b.append(graph.getPeerInfo()); 
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}
	
	/**
	 * Returns a String with information on all the raw events that happen to the network.
	 * 
	 * Contains HTML attributes for being displaed in a browser,
	 * @return HTML String with raw network information.
	 */
	private String doGetRawData() {
		StringBuffer b = new StringBuffer();
		b.append(getScrollableCSS());
		b.append("<HTML>\n\t<BODY>\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"2\">"); //add on HTML Tags for browser viewing
		b.append("\t<H1>Raw Data</H1>\n");
		
		b.append(graph.getPeerInfo()); 
		b.append(graph.getDocumentInfo()); 
		b.append(graph.getQueryInfo()); 
		b.append(graph.getQueryOutputInfo()); 
		b.append(graph.getQueryHitInfo()); 		
		
		b.append("\t</BODY>\n</HTML>");
		return b.toString();
	}
	
	/**
	 * Resets the classes of the graph to make it easier to 'restart' the server for testing.
	 * 
	 * @return message stating the success of the reset.
	 */
	private String doReset() {
		try {
			udpReceiver.reset();
			graph = new P2PGraph();
			RawPeer.reset();
			RawDocument.reset();
			RawQuery.reset();
			RawQueryHit.reset();
			
			udpReceiver.addListener(graph);
			return 	"<HTML>" +
					"\n\t<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"1; URL=/graphServer/debug\">" +
					"\n\t<BODY>" +
					"\n\t\tReset okay" +
					"\n\t</BODY>"+
					"</HTML>";
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	private static String getScrollableCSS() {
		return 	"<style type=\"text/css\">\n" +
				"<!--\n" +
				"div.scroll {\n" +
				"height: 300px;\n" +
				"overflow: auto;\n" +
				"border: 1px solid #666;\n" +
				"background-color: #ccc;\n" +
				"padding: 8px;\n" +
				"}\n" +
				"-->\n" +
				"</style>";
	}
	//[end] Getters for this servlet's specific requests
}