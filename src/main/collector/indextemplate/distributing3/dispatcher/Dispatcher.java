package main.collector.indextemplate.distributing3.dispatcher;

import data.Data;
import main.IndexTemplate;

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
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.hadoop.util.Time;
import base.AppProperties;
import base.Constants;
import base.DynamicProperties;
import base.Node;

public class Dispatcher
{
	// unit: millisecond
	private static int defaultTimeInterval = Integer.parseInt(AppProperties.getInstance()
			.getProperties().get("common.time.interval").toString());
	private static Timer timer;
	private static Timer timerDummy;
	private static Node indexTemplate = null;
	private static List<Node> leafNodes = null;
	private static Socket  connChecker = null;
	private static PrintWriter outToChecker = null;
	private static List<String> listDummyString = null;
	private static List<Integer> dummyTimePoints = null;
	private static int newTimeInterval = defaultTimeInterval;
	// private static int deltaSize = 0;
	private static int times = 1;
    protected static List<ServerNodeConnection> serverNodes;
    private static int publicationNumber = 0;
    private static long ingestionThroughput = 0;
    private static long totalIngestionThroughput = 0;
    private static int serverNodeSize = 0;
    private static int dummyTurn = 0;
    private static FileWriter fw;
	private static BufferedWriter bw;
    private static PrintWriter metricFile;
    private static String metricFileName;
    private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
    private final static int numPublication = Integer.parseInt(AppProperties.getInstance().getProperties()
			.getProperty("num.publication"));
    private static float totalPrivacyBudget = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.budget.total"));
    private static float privacySensitivity = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.laplace.sensitivity"));
	private static final int publicationSizeMax = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("publication.size.max"));
    
	public static void appendMetricFile(String value) throws IOException {
		if (metricFile != null) {
			metricFile.println(value);
			metricFile.flush();
		}
	}
    
 	public static void main(String[] args) throws InterruptedException, IOException 
	{
		// load logger configurations
		File folder = new File(Constants.Cloud.METRICS_FOLDER_3);
		if (!folder.exists()) {
			if (folder.mkdirs()) {
				System.out.println(Constants.Cloud.METRICS_FOLDER_3 + " directory is created!");
			} else {
				System.out.println("Failed to create directory: " + Constants.Cloud.METRICS_FOLDER_3);
			}
		}
		metricFileName = Constants.Cloud.METRICS_FOLDER_3 + "dispatcher_merging" 
		+ Constants.Metrics.METRIC_FILE_EXTENSION;
		fw = new FileWriter(metricFileName, true);
		bw = new BufferedWriter(fw);
		metricFile = new PrintWriter(bw);
		
		ServerSocket listenerDispatcher = null;
		try {
			// build index template
			indexTemplate = IndexTemplate.buildIndexTemplate();
			Node.showInfo(indexTemplate);
			appendMetricFile(Node.getInfo(indexTemplate) + ",,,,,");
			appendMetricFile("numberNode,publicationNumber,datasetSize,publishingTime(ms),initiatingTime(ms)");
			// get leaf nodes
			leafNodes = indexTemplate.getLeafNodes();
			// initiate publicationNumber
			publicationNumber++;
			// open connection to Checker
			connChecker = new Socket(DynamicProperties.getInstance().getProperties()
	        		.getProperty("checker.host"), Constants.Checker.PORT);
			outToChecker = new PrintWriter(connChecker.getOutputStream());
			// send signal to Checker
			System.out.println("send signal to Checker");
			outToChecker.println(Constants.Com2.DISPATCHER_PREFIX);
			// send index template + publication number to Checker
			outToChecker.println(Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX 
					+ indexTemplate.convertCountsToString());
			outToChecker.println(Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX 
					+ indexTemplate.convertLeafCountsToString());
			// publicationNumber$firstLeafId
			outToChecker.println(Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX 
					+ publicationNumber + Constants.Com2.DATA_SEPARATOR
					+ leafNodes.get(0).getId());
			outToChecker.flush();
			// open listen socket
			listenerDispatcher = new ServerSocket(Constants.Dispatcher.PORT);
			System.out.println("Dispatcher is running at port ["+ Constants.Dispatcher.PORT +"] ... ");
			serverNodes = new ArrayList<ServerNodeConnection>();
			BufferedReader in = null;
			String receivedLine = null;
			timer = new Timer();
			while (true) {
				Socket connection = listenerDispatcher.accept();
				System.out.println(connection.getInetAddress().getHostName() + " connected");
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				receivedLine = in.readLine();
				if (receivedLine.contains(Constants.Com2.SERVERNODE_PREFIX)){
					// initiate server nodes
					ServerNodeConnection newServerNode = new ServerNodeConnection(connection);
		            serverNodes.add(newServerNode);
				} else if (receivedLine.contains("generator")){
					// initiate timer as soon as receiving data from  generator
					timer.schedule(new PublishingIndexTemplate(), defaultTimeInterval);
					timerDummy = new Timer();
					schedulePublishingDummies(leafNodes);
					// create a new thread object for receiving data generator
					// receive data from DataGenerator
					int turn = 0;
					serverNodeSize = serverNodes.size();
					ServerNodeConnection serverNode;
					while (true) {
						receivedLine = in.readLine();
						if (receivedLine != null) {
							if (ingestionThroughput < publicationSizeMax) {
								ingestionThroughput++;
								serverNode = serverNodes.get(turn);
								serverNode.getOut().println(receivedLine);
								serverNode.getOut().flush();
								turn++;
								if(turn == serverNodeSize) {
									turn = 0;
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			listenerDispatcher.close();
			if (metricFile != null) {
				metricFile.close();
			}
		}
	}

	private static class PublishingIndexTemplate extends TimerTask
	{
		public synchronized void run()
		{
			try {
				long startTime = Time.now();
				int turn = 0;
				ServerNodeConnection serverNode;
				// if listDymmyString != null, send the rest to server nodes
				if (timerDummy != null) {
					timerDummy.purge();
				}
				if (listDummyString.size() != 0) {
					for (int i = 0; i < listDummyString.size(); i++) {
						serverNode = serverNodes.get(turn);
						// leafOffset%rawdata
						serverNode.getOut().println(listDummyString.get(i));
						serverNode.getOut().flush();
						turn++;
						if(turn == serverNodeSize) {
							turn = 0;
						}
					}
				}
				listDummyString.clear();
				totalIngestionThroughput += ingestionThroughput;
				System.out.println("=> Publication "+ publicationNumber);
				System.out.println("- Size: " + ingestionThroughput 
						+ " - Average: " + (totalIngestionThroughput / (times)));
				times++;
				// reset time interval
				timer.purge();
				System.out.println("Time interval = " + newTimeInterval + " ms");
				timer.schedule(new PublishingIndexTemplate(), newTimeInterval);
				// inform publishing time to all server nodes
				for (ServerNodeConnection serverNode2 : serverNodes) {
					serverNode2.getOut().println(Constants.Com2.PUBLISH_BEGIN);
					serverNode2.getOut().flush();
				}
				//appendMetricFile("numberNode,publicationNumber,dataset size,publishingTime,unit\n");
				StringBuilder str = new StringBuilder();
				str.append(serverNodes.size());
				str.append(",");
				str.append(publicationNumber);
				str.append(",");
				str.append(ingestionThroughput);
				str.append(",");
				str.append((Time.now() - startTime) + ",");
				startTime = Time.now();
				// prepare new publication
				ingestionThroughput = 0;
				publicationNumber++;
				// build new index template
				Node.perturbIndex(indexTemplate, privacySensitivity, totalPrivacyBudget);
				timerDummy.purge();
				schedulePublishingDummies(indexTemplate.getLeafNodes());
				// send a new index template + publication number to Checker
				outToChecker.println(Constants.Com2.DISPATCHER_INDEX_COUNTS_PREFIX 
						+ indexTemplate.convertCountsToString());
				outToChecker.println(Constants.Com2.DISPATCHER_LEAF_COUNTS_PREFIX 
						+ indexTemplate.convertLeafCountsToString());
				// publicationNumber$firstLeafId
				outToChecker.println(Constants.Com2.DISPATCHER_PUBLICATION_NUMBER_PREFIX 
						+ publicationNumber + Constants.Com2.DATA_SEPARATOR
						+ leafNodes.get(0).getId());
				outToChecker.flush();
				str.append((Time.now() - startTime));
				appendMetricFile(str.toString());
				if (publicationNumber == numPublication + 1) {
					System.exit(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized void schedulePublishingDummies(List<Node> leafNodes) {
		// generate dummy record and stick them to the corresponding leaf nodes
		// with a positive count
		int leafNodeSize = leafNodes.size();
		if (listDummyString == null) {
			listDummyString = Collections.synchronizedList(new ArrayList<>());
		}else {
			listDummyString.clear();
		}
		List<Data> dummies = null;
		Node leaf;
		for (int i = 0; i < leafNodeSize; i++) {
			leaf = leafNodes.get(i);
			if (leaf.getCount() > 0) {
				dummies = Data.generateSetOfDummy((int) leaf.getCount(), sourceType);
				for (Data dummy : dummies) {
					try {
						dummy.setIndexedAttribute(-1);
						// format: DUMMY_PREFIXleafIndex$raw data
						String str = Constants.Com2.DUMMY_PREFIX 
								+ i
								+ Constants.Com2.DATA_SEPARATOR
								+ dummy.toRawString();
						listDummyString.add(str);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (listDummyString.size() == 0) {
			return;
		}
		// dummy is randomly sent later
		System.out.println("Dymmies for the next time: " + listDummyString.size());
		// create a set of time points: uniquely random [0-newTimeInterval]
		List<Integer> randTimePoints = new ArrayList<>();
		dummyTimePoints = Collections.synchronizedList(new ArrayList<Integer>());
		for (int i = 0; i < newTimeInterval; i++) {
			randTimePoints.add(i);
		}
		Collections.shuffle(randTimePoints);
		// pick a random time point for each dummy
		// at the time point, there may be more than one dummy
		int randomTimePoint = 0;
		int dummySize = listDummyString.size();
		for (int i = 0; i < dummySize; i++) {
			// number of dummies may larger than time interval
			randomTimePoint = randTimePoints.get(i % (newTimeInterval));
			dummyTimePoints.add(randomTimePoint);
		}
		// sort time points in non-decreasing order
		Collections.sort(dummyTimePoints);
		timerDummy.schedule(new PublishingDummies(), dummyTimePoints.get(0));
	}

	private static class PublishingDummies extends TimerTask
	{
		public synchronized void run()  
		{
			int previousTimePoint = 0;
			ServerNodeConnection serverNode;
			try {
				synchronized(listDummyString) {
					if (listDummyString.size() != 0 && dummyTimePoints.size() != 0) {
						// since more than one dummies having the same time point
						previousTimePoint = dummyTimePoints.get(0);
						while (listDummyString.size() != 0 && dummyTimePoints.size() != 0 
								&& dummyTimePoints.get(0) == previousTimePoint) {
							serverNode = serverNodes.get(dummyTurn);
							// send data to server nodes
							// leafOffset%rawdata
							serverNode.getOut().println(listDummyString.get(0));
							serverNode.getOut().flush();
							dummyTurn++;
							if(dummyTurn == serverNodeSize) {
								dummyTurn = 0;
							}
							listDummyString.remove(0);
							dummyTimePoints.remove(0);
						}
					}
					if (dummyTimePoints.size() != 0) {
						int delay = dummyTimePoints.get(0) - previousTimePoint;
						// set new trigger for next dummy time point
						// delay time for next dummy
						timerDummy.schedule(new PublishingDummies(), delay);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}