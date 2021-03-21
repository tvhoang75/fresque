package main.collector.indextemplate.distributing3.merger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import base.Constants;
import base.DynamicProperties;
import data.Data;

public class Merger {
	private static ServerSocket listenerMerger = null;
	private static Socket connection = null;
	private static BufferedReader in = null;
	private static Socket connCloud = null;
	private static PrintWriter outToCloud = null;
	private static List<String> removedTupleBuffer;
//	private static List<String> listMatcher;
//	private static List<String> listMatcherCopy;
	private static String currentIndexCountString = "";
	private static String newIndexCountString = "";
	private static String currentIndexLeafCountString = "";
	private static String newIndexLeafCountString = "";
	private static int currentPublicationNumber = 1;
	protected static int newPublicationNumber = 0;
	private static List<String> listLeafCounts;
	private boolean isPublished = false;
	private static FileWriter fw;
	private static BufferedWriter bw;
    private static PrintWriter metricFile;
	private static String metricFileName;
	private static PrintWriter metricOAFile;
	private static String metricOAFileName;
	private String indexLeafRealCountString = "";
	private ArrayList<Data> overFlowArrays[];
	
	public static void main(String[] args) {
		new Merger();
	}
	
	public boolean isPublished() {
		return isPublished;
	}

	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}

	/**
	 * @param indexLeafRealCountString
	 */
	public void updateIndexLeafRealCountString(String indexLeafRealCountString) {
		this.indexLeafRealCountString = indexLeafRealCountString;
		// copy listMatcher to listMatcherCopy which is used for matching thread
//		listMatcherCopy.addAll(listMatcher);
//		listMatcher.clear();
	}
	
	/**
	 * @param indexCountString
	 */
	public void updateNewIndexCountString(String indexCountString) {
		// for the first publication
		if (currentIndexCountString.length() == 0) {
			currentIndexCountString = indexCountString;
		}
		newIndexCountString = indexCountString;
	}

	/**
	 * @param indexLeafCountString
	 */
	public void updateNewIndexLeafCountString(String indexLeafCountString) {
		// build overflow arrays for all leaves
		String countsString[] = indexLeafCountString.split(
				Constants.Node.INDEX_COUNT_SEPERATOR);
		int size = countsString.length;
		if (currentIndexLeafCountString.length() == 0) {
			currentIndexLeafCountString = indexLeafCountString;
		}
		OverFlowArrayBuilder overFlowArrayBuilder = new OverFlowArrayBuilder(this, size);
		overFlowArrayBuilder.start();
		newIndexLeafCountString = indexLeafCountString;
	}
	
	@SuppressWarnings("unchecked")
	public void updateOverFlowArrays(ArrayList<Data> overFlowArrays2[], String metric) throws IOException {
		appendBuildingOAMetricFile(currentPublicationNumber + "," + metric);
		int size = overFlowArrays2.length;
		if (this.overFlowArrays == null) {
			this.overFlowArrays = new ArrayList[size];
			for (int i = 0; i < size; i++) {
	            overFlowArrays[i] = new ArrayList<>();
	        }
		}
		for (int i = 0; i < size; i++) {
			this.overFlowArrays[i].clear();
			this.overFlowArrays[i].addAll(overFlowArrays2[i]);
		}
	}
	
	/**
	 * @param pubNumber
	 */
	public synchronized void updateNewPublicationNumber(int pubNumber) {
		newPublicationNumber = pubNumber;
	}
	
	public synchronized void updateRemovedTupleBuffer(String removedTuple) {
		removedTupleBuffer.add(removedTuple);
	}
	
	public synchronized void matchAndPublish() throws Exception {
		if (isPublished && overFlowArrays.length != 0) {
			// start a new thread that will process the current publication
			MatchingThread mergingThread = new MatchingThread(this, outToCloud, currentPublicationNumber,
					currentIndexLeafCountString, currentIndexCountString, indexLeafRealCountString,
					removedTupleBuffer, overFlowArrays);
			mergingThread.start();
			// reset all variables
			this.setPublished(false);
			currentPublicationNumber = newPublicationNumber;
			currentIndexLeafCountString = newIndexLeafCountString;
			currentIndexCountString = newIndexCountString;
			removedTupleBuffer.clear();
			listLeafCounts.clear();
			indexLeafRealCountString = "";
		}
	}
	
	protected synchronized void appendMetricFile(String value) throws IOException {
		if (metricFile != null) {
			metricFile.println(value);
			metricFile.flush();
		}
	}
	
	protected void appendBuildingOAMetricFile(String value) throws IOException {
		if (metricOAFile != null) {
			metricOAFile.println(value);
			metricOAFile.flush();
		}
	}
	
	public Merger() {
		try {
			// open file for storing metrics at cloud
			File folder = new File(Constants.Cloud.METRICS_FOLDER_3);
			if (!folder.exists()) {
				if (folder.mkdirs()) {
					System.out.println(Constants.Cloud.METRICS_FOLDER_3 + " directory is created!");
				} else {
					System.out.println("Failed to create directory: " + Constants.Cloud.METRICS_FOLDER_3);
				}
			}
			metricFileName = Constants.Cloud.METRICS_FOLDER_3 + "merger_merging"  
							+ Constants.Metrics.METRIC_FILE_EXTENSION;
			fw = new FileWriter(metricFileName, true);
			bw = new BufferedWriter(fw);
			metricFile = new PrintWriter(bw);
			appendMetricFile("publicationNumber,mergingTime,buildingOATime(ms),publishingTime(ms)");
			metricOAFileName = Constants.Cloud.METRICS_FOLDER_3 + "merger_buildingOA" 
			+ Constants.Metrics.METRIC_FILE_EXTENSION;
			fw = new FileWriter(metricOAFileName, true);
			bw = new BufferedWriter(fw);
			metricOAFile = new PrintWriter(bw);
			appendBuildingOAMetricFile("publicationNumber,OASize,buildingFullOATime(ms)");
			listenerMerger = new ServerSocket(Constants.Merger.PORT);
			System.out.println("Merger is listening at port ["+ Constants.Merger.PORT +"] ... ");
			// open connection to Merger
			System.out.println("Connect to " + DynamicProperties.getInstance().getProperties().
    				getProperty("cloud.host"));
	    		connCloud = new Socket(DynamicProperties.getInstance().getProperties().
	    				getProperty("cloud.host"), Constants.Cloud.PORT);
	    		outToCloud = new PrintWriter(connCloud.getOutputStream());
	    		// send signal to Cloud
	    		outToCloud.println(Constants.Com2.MERGER_PREFIX);
	    		outToCloud.flush();
			String receivedLine;
			CheckerHandler checkerHandler = null;
			removedTupleBuffer = new ArrayList<>();
			listLeafCounts = new ArrayList<>();
			while (true) {
				connection = listenerMerger.accept();
				System.out.println(connection.getInetAddress().getHostName() + " connected");
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				receivedLine = null;
				receivedLine = in.readLine();
				if (receivedLine != null) {
					if (receivedLine.contains(Constants.Com2.CHECKER_PREFIX)){
						// create a new thread for receiving data from Checker
						checkerHandler = new CheckerHandler(this, in);
						checkerHandler.start();
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();			
		}
		finally {
			try {
				metricOAFile.close();
				metricFile.close();
				listenerMerger.close();
				// closing resources
				// outToCloud.close();
				in.close();
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
