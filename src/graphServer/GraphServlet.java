package graphServer;

import javax.servlet.http.*;

import java.io.*;

public class GraphServlet extends HttpServlet
{
	P2PGraph graph;
	private static final long serialVersionUID = -5953068352245332013L;	
    
	@Override
	public void init() {
	
		graph = new P2PGraph();
	}
	
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        	String responseString="";
        	OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
        	
        	// set the response code and write the response data
            resp.setStatus(HttpServletResponse.SC_OK);
        	
            if(req.getRequestURI().endsWith("getGraph")) {
            	responseString = doGetGraph(req);
            }
            else if(req.getRequestURI().endsWith("getLogEvents")) {
            	responseString = doGetLogEvents(req);            	
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
    
    private String doGetGraph(HttpServletRequest req) throws IOException {
    	return graph.getCurrentGraph();
    }
    
    private String doGetLogEvents(HttpServletRequest req) throws IOException {
    	return graph.getLogEventsAfter(Long.parseLong(req.getParameter("time")));
    }
}