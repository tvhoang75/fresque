package main.collector.indextemplate.distributing3.merger;

import java.io.BufferedReader;

import base.Constants;

public class CheckerHandler extends Thread{
	private BufferedReader inFromChecker = null;
	private Merger merger = null;
	
	public CheckerHandler(Merger merger, BufferedReader inFromChecker) {
		this.merger = merger;
		this.inFromChecker = inFromChecker;
	}
	
    @Override
    public void run() {
	    	String receivedLine;
	    	String indexCountString = "";
	    	String indexLeafCountString = "";
	    	String indexLeafRealCountString = "";
	    	String publicationNumberString = "";
	    	int newPublicationNumber = 0;
	    	while (true) {
	    		try {
	    			receivedLine = inFromChecker.readLine();
	    			if (receivedLine != null) {
	    				if(receivedLine.contains(Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX)) {
	    					indexCountString = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX.length());
	    					merger.updateNewIndexCountString(indexCountString);
	    				}else if (receivedLine.contains(Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX)) {
	    					indexLeafCountString = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX.length());
	    					merger.updateNewIndexLeafCountString(indexLeafCountString);
	    				}else if (receivedLine.contains(Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX)) {
	    					publicationNumberString = receivedLine.substring(
	    							Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX.length());
	    					newPublicationNumber = Integer.parseInt(publicationNumberString);
	    					merger.updateNewPublicationNumber(newPublicationNumber);
	    				}else if(receivedLine.contains(Constants.Com2.CHECKER_LEAF_COUNTS_PREFIX)) {
	    					indexLeafRealCountString = receivedLine.substring(
	    							Constants.Com2.CHECKER_LEAF_COUNTS_PREFIX.length());
	    					merger.updateIndexLeafRealCountString(indexLeafRealCountString);
	    				} else if (receivedLine.contains(Constants.Com2.REMOVED_PREFIX)){
	    					// store removed tuples into buffer
	    					receivedLine = receivedLine.substring(Constants.Com2.REMOVED_PREFIX.length());
	    					merger.updateRemovedTupleBuffer(receivedLine);
	    				}
	    				if (newPublicationNumber > 1
	    						&& indexCountString.length() != 0 && indexLeafCountString.length() != 0
	    						&& publicationNumberString.length() != 0
	    						&& indexLeafRealCountString.length() != 0) {
	    					merger.setPublished(true);
	    					merger.matchAndPublish();
	    					// reset all variables
	    					indexCountString = "";
	    					indexLeafCountString = "";
	    					publicationNumberString = "";
	    					indexLeafRealCountString = "";
	    				}
		    		}
	    		} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
    }
}