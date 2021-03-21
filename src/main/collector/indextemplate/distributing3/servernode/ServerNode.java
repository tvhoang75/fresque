package main.collector.indextemplate.distributing3.servernode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import base.AppProperties;
import base.Constants;
import base.DynamicProperties;
import base.Node;
import data.Data;
import main.IndexTemplate;
import main.crypto.AESEncryptor;

public class ServerNode {
	private Socket connDispatcher;
    private BufferedReader inFromDispatcher = null;
    private Socket  connChecker = null;
    private PrintWriter outToChecker = null;
    private BufferedReader inFromChecker = null;
    private PrintWriter outToDispatcher = null;
    private static int deltaSize = 0;
	private Node incrementalIndexTemplate;
	private String name;
	private static int upperBound = 0;
	private static String sourceType = AppProperties.getInstance()
			.getProperties().getProperty("source.type").toString();
	private static int rangeInterval = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("range.interval"));
	// StringBuilder[] arrayOfRecordIds = null;
	// private static int[] arrayLeafCounts;
	
    public static void main(String[] args) throws Exception {
    		new ServerNode();
    	}
    
    public ServerNode() throws Exception {
		name = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("ServerNode is started: " + name);
	    try {
	    		List<Node> leafNodes = null;
	    		// open connection to Checker
	    		this.connChecker = new Socket(DynamicProperties.getInstance().getProperties()
		        		.getProperty("checker.host"), Constants.Checker.PORT);
	    		this.outToChecker = new PrintWriter(connChecker.getOutputStream());
	    		this.inFromChecker = new BufferedReader(
						new InputStreamReader(this.connChecker.getInputStream()));
	    		// waiting for publish.end message from Checker
	    		CheckerHandler checkerHandler = new CheckerHandler(this, this.inFromChecker);
	    		checkerHandler.start();
	    		// send a signal to Checker
	    		this.outToChecker.println(Constants.Com2.SERVERNODE_PREFIX);
	    		this.outToChecker.flush();
	    		// open connection from Dispatcher
	        this.connDispatcher = new Socket(DynamicProperties.getInstance().getProperties()
	        		.getProperty("dispatcher.host"), Constants.Dispatcher.PORT);
	        this.inFromDispatcher = new BufferedReader(
	        							new InputStreamReader(this.connDispatcher.getInputStream()));
	        outToDispatcher = new PrintWriter(this.connDispatcher.getOutputStream());
	        // initiate local clone
			incrementalIndexTemplate = IndexTemplate.buildIndexTemplateWithoutNoise();
			// get leaf nodes
			leafNodes = incrementalIndexTemplate.getLeafNodes();
			int sizeLeafNode = leafNodes.size();
			upperBound = sizeLeafNode - 1;
			// send a signal to Dispatcher
			outToDispatcher.println(Constants.Com2.SERVERNODE_PREFIX);
			outToDispatcher.flush();
	        String receivedLine = null;
	        List<String> buffer = new ArrayList<>();
	        Data data;
	        float value;
	        int leafIndex;
	        String parts[];
	        while ((receivedLine = inFromDispatcher.readLine()) != null) {
	        		// System.out.println(receivedLine);
	        		if(receivedLine.contains(Constants.Com2.PUBLISH_BEGIN)) {
	        			System.out.println("=> Publish index template size :" + deltaSize);
	        			deltaSize = 0;
	        			// forward publish.begin message to Checker
	        			this.outToChecker.println(Constants.Com2.PUBLISH_BEGIN);
	        			this.outToChecker.flush();
	        			receivedLine = null;
	        			// put new data into buffer and wait until receiving a published signal
	        			int bufferSize;
	        			while ((receivedLine = inFromDispatcher.readLine()) != null) {
	        				if (receivedLine.contains(Constants.Com2.DUMMY_PREFIX)) {
	        					// receivedLine: format: DUMMY_PREFIXleafIndex$raw data
	        					receivedLine = receivedLine.substring(Constants.Com2.DUMMY_PREFIX.length());
	        					// leafIndex$raw data
	        					parts = receivedLine.split(Constants.Com2.DATA_SEPARATOR, 2);
	        					buffer.add(parts[0] + Constants.Com2.DATA_SEPARATOR  
		        						+ AESEncryptor.CBCEncrypt(parts[1], 
		        								Constants.Enryption.KEY_VALUE_1));
//	        					buffer.add(parts[0] + Constants.Com2.DATA_SEPARATOR + parts[1]);
	        				}else {
	        					// parse
	        					// System.out.println(receivedLine);
		        				data = Data.parseData(receivedLine, sourceType);
		        				value = data.getIndexedAttribute(); 
		        				// calculate leafIndex
		        				leafIndex = (int) Math.min(upperBound, 
		        						(value - (float) Data.getDomainMin(sourceType)) 
		        						/ (float) rangeInterval);
		        				// store new encrypted data into buffer
		        				buffer.add(leafIndex + Constants.Com2.DATA_SEPARATOR  
		        						+ AESEncryptor.CBCEncrypt(data.toString(), 
		        								Constants.Enryption.KEY_VALUE_1));
//	        					buffer.add(leafIndex + Constants.Com2.DATA_SEPARATOR + data.toString());
		        				deltaSize++;
	        				}
	        				// when receive publish.end message from Checker,
	        				// send buffer to Checker, format: indexLeaf$etuple
	        				if(checkerHandler.isPublished() == true) {
	        					// process the first receivedLine
	        					bufferSize = buffer.size();
	        					System.out.println("-  buffer size for next publication = " + bufferSize);
	                			if (bufferSize != 0) {
	                				for (int i = 0; i < bufferSize; i++) {
	    	        						outToChecker.println(buffer.get(i));
	    	        						outToChecker.flush();
	                				}
	                				buffer.clear();
	                				checkerHandler.setPublished(false);
	                			}
	        					break;
	        				}
	        			}
	        		}else {
	        			if (receivedLine.contains(Constants.Com2.DUMMY_PREFIX)) {
        					// receivedLine: format: DUMMY_PREFIXleafIndex$raw data
        					receivedLine = receivedLine.substring(Constants.Com2.DUMMY_PREFIX.length());
        					// leafIndex$raw data
        					parts = receivedLine.split(Constants.Com2.DATA_SEPARATOR, 2);
        					// Encrypt and send to Checker, format: indexLeaf$etuple
        					outToChecker.println(parts[0] + Constants.Com2.DATA_SEPARATOR 
        							+ AESEncryptor.CBCEncrypt(parts[1], 
        									Constants.Enryption.KEY_VALUE_1));
//    	        				outToChecker.println(parts[0] + Constants.Com2.DATA_SEPARATOR + parts[1]);
        					outToChecker.flush();
        				}else {
        					// parse
        					data = Data.parseData(receivedLine, sourceType);
        					value = data.getIndexedAttribute();
        					leafIndex = (int) Math.min(upperBound, 
        							(value - (float) Data.getDomainMin(sourceType)) 
        							/ (float) rangeInterval);
        					// Encrypt and send to Checker, format: indexLeaf$etuple
        					outToChecker.println(leafIndex + Constants.Com2.DATA_SEPARATOR 
        							+ AESEncryptor.CBCEncrypt(data.toString(), 
        									Constants.Enryption.KEY_VALUE_1));
//    	        				outToChecker.println(leafIndex + Constants.Com2.DATA_SEPARATOR + data.toString());
        					outToChecker.flush();
        					deltaSize++;
        				}
	        		}
	        }
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}