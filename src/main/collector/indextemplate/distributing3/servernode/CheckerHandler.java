package main.collector.indextemplate.distributing3.servernode;

import java.io.BufferedReader;
import java.io.IOException;

public class CheckerHandler extends Thread{
	private BufferedReader inFromChecker = null;
//	private ServerNode servernode = null;
	private boolean published = false;
	
	public synchronized boolean isPublished() {
		return published;
	}

	public synchronized void setPublished(boolean published) {
		this.published = published;
	}

	public CheckerHandler(ServerNode servernode, BufferedReader inFromChecker) {
//		this.servernode = servernode;
		this.inFromChecker = inFromChecker;
	}
	
    @Override
    public void run() {
	    	String receivedLine = "";
    	
	    	while (true) {
	    		try {
	    			if ((receivedLine = inFromChecker.readLine()) != null) {
	    				//System.out.println(receivedLine);
	    				this.setPublished(true);
		    		}
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    	}
    }
}
