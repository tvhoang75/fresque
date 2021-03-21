package main.cloud.indextemplate.distributing3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import base.Constants;

public class MergerHandler extends Thread{
	private BufferedReader inFromMerger = null;
	private Cloud cloud;
	// store secure index + overflow arrays at cloud
	private String mergedIndexFolder = Constants.Cloud.INDEX_FOLDER_3;
	public MergerHandler(Cloud cloud, BufferedReader inFromChecker) {
		this.inFromMerger = inFromChecker;
		this.cloud = cloud;
		// create directory for merger
		File file = new File(mergedIndexFolder);
		if (!file.exists()) {
            if (file.mkdirs()) {
                System.out.println(mergedIndexFolder + " directory is created!");
            } else {
                System.out.println("Failed to create directory: " + mergedIndexFolder);
            }
        }
	}
	
    @Override
    public void run() {
	    	String newPublicationNumber = "";
	    	String indexString = "";
	    	String overflowArray = "";
	    	String receivedLine;
	    	RandomAccessFile file = null;
	    	while (true) {
	    		try {
	    			receivedLine = inFromMerger.readLine();
	    			if (receivedLine != null) {
	    				if (receivedLine.contains(Constants.Com2.MERGER_PUBLICATION_NUMBER)) {
	    					// create a new file for merged matching table and overflow arrays
	    					newPublicationNumber = 
	    							receivedLine.substring(Constants.Com2.MERGER_PUBLICATION_NUMBER.length());
	    					if (file != null) {
	    						file.close();
	    					}
	    					file = new RandomAccessFile(mergedIndexFolder 
	    							+ newPublicationNumber + ".txt", "rw");
	    				}else if (receivedLine.contains(Constants.Com2.MERGER_INDEX)) {
	    					indexString = receivedLine.substring(
	    							Constants.Com2.MERGER_INDEX.length());
	    					file.write((indexString + "\n").getBytes());
	    				}else if (receivedLine.contains(Constants.Com2.MERGER_OVERFLOW_ARRAY)) {
	    					overflowArray = receivedLine.substring(Constants.Com2.MERGER_OVERFLOW_ARRAY.length());
	    					file.write((overflowArray + "\n").getBytes());
	    				}
	    			}
	    		} catch (IOException e) {
	    			if (file != null) {
	    				try {
	    					file.close();
	    				} catch (IOException e1) {
	    					e1.printStackTrace();
	    				}
	    			}
	    			e.printStackTrace();
	    		}
	    	}
    }
}