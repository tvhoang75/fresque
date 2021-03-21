package main.cloud.indextemplate.distributing3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.Time;

import base.Constants;

public class Cloud {
	private static ServerSocket listenerCloud = null;
	private static Socket connection = null;
	private static BufferedReader in = null;
	private static PrintWriter outToConsumer = null;
	private static int publicationSize = 0;
	private static FileWriter fw;
	private static BufferedWriter bw;
    private static PrintWriter metricFile;
	private static String metricFileName = "";
	private static List<String> currentMetaList = new ArrayList<>();
	private static RandomAccessFile metaFile = null;
	private static StringBuilder tupleLocations[];
	private static int currentFirstLeafId = 0;
	private static int newFirstLeafId = 0;
	
	public static void main(String[] args) throws IOException {
		new Cloud();
	}
	
	protected void initiateMatching(String publicationNumber, String firstLeafId, String leafNodeSize) throws IOException {
		// get index of metaString in List
		int index = Integer.parseInt(publicationNumber) - 1;
		
//		PartitionThread matchingThread = new PartitionThread(publicationNumber, matchingTableString, 
//				currentMetaList.get(index), 
//				firstLeafId, leafNodeSize, this, listWorkerHandler);
//		matchingThread.start();
		currentMetaList.set(index, "");
	}
	
	protected synchronized void appendMetricFile(String value) throws IOException {
		if (metricFile != null) {
			metricFile.println(value);
			metricFile.flush();
		}
	}
	
	private Cloud() throws IOException {
		// open file for storing metrics at cloud
		File folder = new File(Constants.Cloud.METRICS_FOLDER_3);
		if (!folder.exists()) {
			if (folder.mkdirs()) {
				System.out.println(Constants.Cloud.METRICS_FOLDER_3 + " directory is created!");
			} else {
				System.out.println("Failed to create directory: " + Constants.Cloud.METRICS_FOLDER_3);
			}
		}
		metricFileName = Constants.Cloud.METRICS_FOLDER_3 + "cloud_matching" 
						+ Constants.Metrics.METRIC_FILE_EXTENSION;
		fw = new FileWriter(metricFileName, true);
		bw = new BufferedWriter(fw);
		metricFile = new PrintWriter(bw);
		appendMetricFile("publicationNumber,size,time(ms)");
		// delete existing folders and create new ones
		folder = new File(Constants.Cloud.DATASET_FOLDER_3);
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder); 
		}else if (folder.mkdirs()) {
			System.out.println(Constants.Cloud.DATASET_FOLDER_3 + " directory is created!");
		} else {
			System.out.println("Failed to create directory: " + Constants.Cloud.DATASET_FOLDER_3);
		}
		folder = new File(Constants.Cloud.META_FOLDER_3);
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder); 
		}else if (folder.mkdirs()) {
			System.out.println(Constants.Cloud.META_FOLDER_3 + " directory is created!");
		} else {
			System.out.println("Failed to create directory: " + Constants.Cloud.META_FOLDER_3);
		}
		folder = new File(Constants.Cloud.INDEX_FOLDER_3);
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder);
		}else if (folder.mkdirs()) {
			System.out.println(Constants.Cloud.INDEX_FOLDER_3 + " directory is created!");
		} else {
			System.out.println("Failed to create directory: " + Constants.Cloud.INDEX_FOLDER_3);
		}
		folder = new File(Constants.Cloud.MATCHED_FOLDER_3);
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder);
		}else if (folder.mkdirs()) {
			System.out.println(Constants.Cloud.MATCHED_FOLDER_3 + " directory is created!");
		} else {
			System.out.println("Failed to create directory: " + Constants.Cloud.MATCHED_FOLDER_3);
		}
		try {
			listenerCloud = new ServerSocket(Constants.Cloud.PORT);
			System.out.println("Cloud is listening at port ["+ Constants.Cloud.PORT +"] ... ");
			String receivedLine;
			String publicationNumberString = "";
			RandomAccessFile dataFile = null;
			String parts[];
			String ciphertext;
			while (true) {
				connection = listenerCloud.accept();
				System.out.println(connection.getInetAddress().getHostName() + " connected");
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				receivedLine = in.readLine();
				System.out.println(receivedLine);
				if (receivedLine.contains(Constants.Com2.MERGER_PREFIX)){
					// initiate a new thread for processing Merger
					MergerHandler mergerHandler = new MergerHandler(this, in);
					mergerHandler.start();
				}else {
					// data come from Checker
					while ((receivedLine = in.readLine()) != null){
						if (receivedLine.contains(Constants.Com2.PUBLISH_BEGIN)){
					    		// initiate a new publication
							// receivedLine: parts[0] = publicationNumber; parts[1] = leafSize;
							// 					parts[2] = firstLeafId
							long startTime = Time.now();
							StringBuilder metricString = new StringBuilder();
							String str = receivedLine.substring(
									Constants.Com2.PUBLISH_BEGIN.length());
							parts = str.split(Constants.Com2.DATA_SEPARATOR, 3);
							publicationNumberString = parts[0];
							int leafSize = Integer.parseInt(parts[1]);
							newFirstLeafId = Integer.parseInt(parts[2]);
							int publicationNumber = Integer.parseInt(publicationNumberString);
							if (publicationNumber == 0) {
								currentFirstLeafId = newFirstLeafId;
							}
							if (tupleLocations == null) {
								tupleLocations = new StringBuilder[leafSize];
								for (int i = 0; i < leafSize; i++) {
									tupleLocations[i] = new StringBuilder();
								}
							}
							System.out.println("    Size = " + publicationSize);
							System.out.println("==> New publication number = " + publicationNumberString);							
							metricString.append(publicationNumber - 1);
							metricString.append(",");
							metricString.append(publicationSize);
							metricString.append(",");
							int pubNumber = Integer.parseInt(publicationNumberString);
							if (pubNumber > 1) {
								for (int i = 0; i < leafSize; i++) {
									metaFile.write(((i + currentFirstLeafId) + ":" 
											+ tupleLocations[i].toString() + "\n").getBytes());
									tupleLocations[i].setLength(0);
								}
							}
							currentFirstLeafId = newFirstLeafId;
							// create a new file with name including publicationNumberString
							if (dataFile != null) {
								dataFile.close();
							}
							dataFile = new RandomAccessFile(Constants.Cloud.DATASET_FOLDER_3 
									+ publicationNumberString + ".txt", "rw");
							if (metaFile != null) {
								metaFile.close();
							}
							metaFile = new RandomAccessFile(Constants.Cloud.META_FOLDER_3 
									+ publicationNumberString + ".txt", "rw");
							publicationSize = 0;
							System.out.println("   Time for writing meta (ms) = " 
												+ (Time.now() - startTime));
							metricString.append((Time.now() - startTime));
							appendMetricFile(metricString.toString());
						}else {
							publicationSize++;
							// parts[0] : leafOffset - parts[1]: cipher-text
							parts = receivedLine.split(Constants.Com2.DATA_SEPARATOR, 2);
							ciphertext = (parts[1] + "\n");
//							buffer = Base64.decodeBase64(parts[1]); // this takes time
							// get pointer and write id#offset#size to meta-data file
							int leafOffset = Integer.parseInt(parts[0]);
							tupleLocations[leafOffset].append(dataFile.getFilePointer());
							tupleLocations[leafOffset].append(Constants.Com2.DATA_SEPARATOR);
							tupleLocations[leafOffset].append(ciphertext.length());
							tupleLocations[leafOffset].append(Constants.Com2.MATCHER_SEPARATOR_2);
							// write new data to file
							dataFile.write(ciphertext.getBytes());
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		finally {
			try {
				listenerCloud.close();
				// closing resources
				outToConsumer.close();
				in.close();
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}