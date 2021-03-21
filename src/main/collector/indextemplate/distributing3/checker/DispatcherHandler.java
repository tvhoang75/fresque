package main.collector.indextemplate.distributing3.checker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import base.Constants;

public class DispatcherHandler extends Thread{
	private BufferedReader inFromDispatcher = null;
	private Checker checker = null;
	private PrintWriter outToMerger = null;
	
	public DispatcherHandler(Checker checker, BufferedReader inFromDispatcher, PrintWriter outToMerger) {
		this.checker = checker;
		this.inFromDispatcher = inFromDispatcher;
		this.outToMerger = outToMerger;
	}
	
    @Override
    public void run() {
	    	String receivedLine = "";
	    	String indexCountString = null;
	    	String indexLeafCountString = null;
	    	String publicationNumberString = null;
	    	String firstLeafIdString = null;
	    	
	    	while (true) {
	    		try {
	    			receivedLine = inFromDispatcher.readLine();
	    			if (receivedLine != null) {
	    				if(receivedLine.contains(Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX)) {
	    					indexCountString = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX.length());
	    					// forward new publication to Merger
	    					this.outToMerger.println(receivedLine);
	    					this.outToMerger.flush();
	    				}else if (receivedLine.contains(Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX)) {
	    					indexLeafCountString = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX.length());
	    					this.outToMerger.println(receivedLine);
	    					this.outToMerger.flush();
	    				}else if (receivedLine.contains(Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX)) {
	    					String str = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX.length());
	    					// publicationNumber$firstLeafId
	    					String parts[] = str.split(Constants.Com2.DATA_SEPARATOR);
	    					publicationNumberString = parts[0];
	    					firstLeafIdString = parts[1];
	    					this.outToMerger.println(Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX 
	    							+ publicationNumberString);
	    					this.outToMerger.flush();
	    				}
    					if (indexLeafCountString != null && publicationNumberString != null) {
    						// initiate a new publication
    						checker.setIndexLeafCountString(indexLeafCountString);
    						checker.setPublicationNumberString(publicationNumberString);
    						checker.setFirstLeafId(firstLeafIdString);
    						checker.initiateNewPublication();
    						// reset 
    						indexLeafCountString = null;
    						publicationNumberString = null;
    						firstLeafIdString = null;
    					}
		    		}
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    	}
    }
}
