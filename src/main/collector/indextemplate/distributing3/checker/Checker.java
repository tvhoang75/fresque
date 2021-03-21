package main.collector.indextemplate.distributing3.checker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.hadoop.util.Time;

import base.AppProperties;
import base.Constants;
import base.DynamicProperties;
import main.BigLog;
//checking node
public class Checker {
    private Socket  connCloud = null;
    private PrintWriter outToCloud = null;
    private Socket  connMerger = null;
    private PrintWriter outToMerger = null;
    private static int publicationSize = 0;
	private static String indexLeafCountString = "";
	private static int newPublicationNumber = 0;
	private static int currentPublicationNumber = 0;
	private static int numOfServerNode = 0;
	private static int numPubMessageReceived = 0;
	private static int[] arrayLeafCounts;
	//  stores real count of data set, including counts of dummy tuples
	private static int[] arrayLeafRealCounts;
	private static int upperBound = 0;
	protected static List<ServerNodeHandler> serverNodeHandlers;
	private static FileWriter fw;
	private static BufferedWriter bw;
    private static PrintWriter metricFile;
    private static String metricFileName;
//	private static StringBuilder[] arrayOfRecordIds = null;
	private static int firstLeafId = 0;
	private static String randomer[];
	private static int currentRandomerSize = 0;
	private static Random random = new Random();
	private static final int randomerSizeCoefficient = 
						Integer.parseInt(AppProperties.getInstance().getProperties()
									.get("randomer.size.coefficient").toString());
	private static int randomerDefinedSize = 0;
	
	public static void main(String[] args) throws IOException {
		new Checker();
	}
	
	// increase numPubMessageReceived whenever Checker receives a publish.begin message
	// from ServerNode
	synchronized public void increaseNumPubMessageReceived() throws IOException {
		numPubMessageReceived++;
		initiateNewPublication();
	}
	
	public void setFirstLeafId(String firstLeafIdString) {
		firstLeafId = Integer.parseInt(firstLeafIdString);
	}
	
	synchronized public void setIndexLeafCountString(String indexLeafCountString2) {
		indexLeafCountString = indexLeafCountString2;
	}

	synchronized public void setPublicationNumberString(String publicationNumberString) {
		newPublicationNumber = Integer.parseInt(publicationNumberString);
	}
	
	// initiate a new publication
	// and send a publish.end message to all ServerNode (from the second publication)
	synchronized public void initiateNewPublication() throws IOException {
		if ((currentPublicationNumber == 0)
				|| ((newPublicationNumber > currentPublicationNumber) 
				&& (numPubMessageReceived == numOfServerNode))) {
			System.out.println("=> Current publication size = " + publicationSize);
			System.out.println("-  Next publication number = " + newPublicationNumber);
			long startTime = Time.now();
			// System.out.println("New publication number = " + newPublicationNumber);
			// parse string of count to array of leaf counts
			String[] counts = indexLeafCountString.split(Constants.Node.INDEX_COUNT_SEPERATOR);
			int leafSize = counts.length;
			if (currentPublicationNumber == 0) {
				arrayLeafRealCounts = new int[leafSize];
				Arrays.fill(arrayLeafRealCounts, 0);
			}
			arrayLeafCounts = new int[leafSize];
			int count;
			for (int i = 0; i < leafSize; i++) {
				count = (int) Float.parseFloat(counts[i]);
				arrayLeafCounts[i] = count;
			}
			upperBound = leafSize - 1;
			// reset for the next publication
			int metricPublicationNumber = currentPublicationNumber;
			currentPublicationNumber = newPublicationNumber;
			numPubMessageReceived = 0;
			int publicationSizeMetric = publicationSize;
			publicationSize = 0;
			// send Randomer to cloud
			if (currentPublicationNumber > 0) {
				// send Randomer to cloud
				if (currentRandomerSize != 0) {
					for (int i = 0; i < currentRandomerSize; i++) {
						this.updateIT(randomer[i]);
					}
				}
				currentRandomerSize = 0;
			}
			// initiate new Randomer
//			int metricRandomerDefinedSize = randomerDefinedSize;
//			randomerDefinedSize = sumPositiveNoise * randomerSizeCoefficient;
//			// TODO: re-check this assumption
//			if (randomerDefinedSize == 0) {
//				randomerDefinedSize = 1;
//			}
			randomer = new String[randomerDefinedSize];
			System.out.println("-  Randomer size = " + randomerDefinedSize);
			// send publicationNumber#leafNodeSize to Cloud first
			this.outToCloud.println(Constants.Com2.PUBLISH_BEGIN + currentPublicationNumber
					+ Constants.Com2.DATA_SEPARATOR + leafSize
					+ Constants.Com2.DATA_SEPARATOR + firstLeafId);
			this.outToCloud.flush();
			// from the second publication, send published message to all ServerNodes
			if (currentPublicationNumber > 1) {
				// send leafCounts to Merger
				StringBuilder stringLeafRealCounts = new StringBuilder();
				for (int i = 0; i < leafSize; i++) {
					stringLeafRealCounts.append(arrayLeafRealCounts[i]);
					stringLeafRealCounts.append(Constants.Com2.DATA_SEPARATOR);
				}
				this.outToMerger.println(Constants.Com2.CHECKER_LEAF_COUNTS_PREFIX 
						+ stringLeafRealCounts.toString());
				this.outToMerger.flush();
				// reset all data structure for a new publication
				Arrays.fill(arrayLeafRealCounts, 0);
				// send signals to server nodes
				for (int i = 0; i < serverNodeHandlers.size(); i++) {
					serverNodeHandlers.get(i).getOutToServerNode().println(Constants.Com2.PUBLISH_END);
					serverNodeHandlers.get(i).getOutToServerNode().flush();
				}
			}
			if (metricPublicationNumber > 0) {
				this.appendMetricFile(serverNodeHandlers.size() + "," 
						+ metricPublicationNumber + "," + publicationSizeMetric + ","
						+ randomerSizeCoefficient + ","
						+ randomerDefinedSize + ","
						+ (Time.now() - startTime));
			}
		}
	}
	
	synchronized public void updateRandomer(String receivedLine) {
		// insert new tuple into the Randomer
		randomer[currentRandomerSize] = receivedLine;
		currentRandomerSize++;
		String pickedValue = "";
		if (currentRandomerSize == randomerDefinedSize) {
			// get random value
			int randomPos = random.nextInt(currentRandomerSize);
			pickedValue = randomer[randomPos];
			this.updateIT(pickedValue);
			// remove picked etuple from randomer
			randomer[randomPos] = randomer[currentRandomerSize - 1];
			randomer[currentRandomerSize - 1] = "";
			currentRandomerSize--;
		}
	}
	
	// update arrayLeafCounts when receive data from ServerNodes
	public void updateIT(String pickedValue) {
		// receivedLine format : indexLeaf¥etuple
		String parts[] = pickedValue.split(Constants.Com2.DATA_SEPARATOR, 2);
		// => result: parts[0] = indexLeaf; parts[1] = etuple
		// update arrayLeafCounts
		int leafOffset = Integer.parseInt(parts[0]);
		if (arrayLeafCounts[leafOffset] < 0) {
			arrayLeafCounts[leafOffset]++;
			arrayLeafRealCounts[leafOffset]++;
			// send the removed tuple to Merger: REMOVED_PREFIXleafOffset$etuple
			this.outToMerger.println(Constants.Com2.REMOVED_PREFIX 
					+ leafOffset + Constants.Com2.DATA_SEPARATOR + parts[1]);
			this.outToMerger.flush();
		}else {
			// send leafOffset$etuple met the condition to Cloud
			this.outToCloud.println(leafOffset + Constants.Com2.DATA_SEPARATOR + parts[1]);
			this.outToCloud.flush();
			// these counts including counts of dummy tuples
			// such counts will be removed at the matching step
			arrayLeafRealCounts[leafOffset]++;
		}
		publicationSize++;
	}
	
	protected void appendMetricFile(String value) throws IOException {
		if (metricFile != null) {
			metricFile.println(value);
			metricFile.flush();
		}
	}
	
	public Checker() throws IOException {
		randomerDefinedSize = randomerSizeCoefficient
				* BigLog.getInstance().getOverFlowArraySize()
				* BigLog.getInstance().getNumOfBins();
		System.out.println("randomerSize = " + randomerDefinedSize);
		// open file for storing metrics at cloud
		File folder = new File(Constants.Cloud.METRICS_FOLDER_3);
		if (!folder.exists()) {
			if (folder.mkdirs()) {
				System.out.println(Constants.Cloud.METRICS_FOLDER_3 + " directory is created!");
			} else {
				System.out.println("Failed to create directory: " + Constants.Cloud.METRICS_FOLDER_3);
			}
		}
		metricFileName = Constants.Cloud.METRICS_FOLDER_3 + "checker_merging" + Constants.Metrics.METRIC_FILE_EXTENSION;
		fw = new FileWriter(metricFileName, true);
		bw = new BufferedWriter(fw);
		metricFile = new PrintWriter(bw);
		appendMetricFile("numberNode,publicationNumber,datasetSize,randomerCoefficient,randomerSize,mergingTime(ms)");
		ServerSocket listenerChecker = null;
	    try {
	    		// open connection to Cloud
	    		this.connCloud = new Socket(DynamicProperties.getInstance().getProperties().
	    				getProperty("cloud.host"), Constants.Cloud.PORT);
	    		this.outToCloud = new PrintWriter(connCloud.getOutputStream());
	    		this.outToCloud.println("Hello from Checker");
	    		this.outToCloud.flush();
	    		// open connection to Merger
	    		this.connMerger = new Socket(DynamicProperties.getInstance().getProperties()
		        		.getProperty("merger.host"), Constants.Merger.PORT);
	    		this.outToMerger = new PrintWriter(connMerger.getOutputStream());
	    		// send a signal to Merger for opening a thread
	    		this.outToMerger.println(Constants.Com2.CHECKER_PREFIX);
	    		this.outToMerger.flush();
	    		// open socket channel
	    		Socket connection = null;
	    		BufferedReader in = null;
	    		listenerChecker = new ServerSocket(Constants.Checker.PORT);
	    		serverNodeHandlers = new ArrayList<ServerNodeHandler>();
			System.out.println("Checker is running at port ["+ Constants.Checker.PORT +"] ... ");
			DispatcherHandler dispatcherHandler = null;
			ServerNodeHandler serverNodeHandler = null;
			Checker checker = this;
			while (true) {
				connection = listenerChecker.accept();
				System.out.println(connection.getInetAddress().getHostName() + " connected");
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String receivedLine = in.readLine();
				if (receivedLine != null) {
					if (receivedLine.contains(Constants.Com2.DISPATCHER_PREFIX)){
						// create a new thread for receiving new IT + publication number from Dispatcher
						dispatcherHandler = new DispatcherHandler(checker, in, outToMerger);
						dispatcherHandler.start();
					}else if(receivedLine.contains(Constants.Com2.SERVERNODE_PREFIX)) {
						serverNodeHandler = new ServerNodeHandler(checker, connection, outToMerger, 
								outToCloud, upperBound);
						serverNodeHandlers.add(serverNodeHandler);
						serverNodeHandler.start();
						numOfServerNode++;
					}
				}
			}
	    } catch (UnknownHostException e) {
	    		listenerChecker.close();
	        e.printStackTrace();
	    } catch (IOException e) {
	    		listenerChecker.close();
	        e.printStackTrace();
	    }
	}
}
