/**
 * 
 */
package main.consumer.indextemplate.distributing3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.util.Time;

import base.AppProperties;
import base.Constants;
import base.DynamicProperties;
import base.Constants.Com;
import data.Data;
import main.crypto.AESEncryptor;

/**
 * @author vtran
 *
 */
public class CloudConsumer extends Thread{
	private static int a = Integer.MAX_VALUE;
	private static int b = Integer.MIN_VALUE;
	private final static int numQueries = Integer.parseInt(AppProperties.getInstance().
											getProperties().getProperty("query.number"));
	private final static int incommingDataRate = Integer.parseInt(DynamicProperties.getInstance()
			.getProperties().getProperty("incoming.data.rate"));
	private final static int domainMax = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("data.domain.max"));
	private final static String sourceType = AppProperties.getInstance()
			.getProperties().getProperty("source.type");
//	private final static int queryDelay = Integer.parseInt(AppProperties.getInstance()
//			.getProperties().getProperty("query.delay"));
	
	public static void main(String[] args) {
		new CloudConsumer();
	}
	
	public CloudConsumer() {
		try {
			File folder = new File(Constants.Cloud.METRICS_FOLDER_0);
			if (!folder.exists()) {
				if (folder.mkdirs()) {
					System.out.println(Constants.Cloud.METRICS_FOLDER_0 + " directory is created!");
				} else {
					System.out.println("Failed to create directory: " 
								+ Constants.Cloud.METRICS_FOLDER_0);
				}
			}
			String metricFileName = Constants.Cloud.METRICS_FOLDER_0 + "consumer_cloud" 
					+ Constants.Metrics.METRIC_FILE_EXTENSION;
			FileWriter fw = new FileWriter(metricFileName, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter metricFile = new PrintWriter(bw);
			// open connection to Cloud
			Socket connCloud = new Socket(DynamicProperties.getInstance().getProperties()
					.getProperty("cloud1.host"), Com.INDEX_TEMPLATE_PORT);
			PrintWriter outToCloud = new PrintWriter(connCloud.getOutputStream());
			// send a signal to cloud
			BufferedReader inFromCloud = 
					new BufferedReader(new InputStreamReader(connCloud.getInputStream()));
			outToCloud.println("consumer");
			outToCloud.flush();
			metricFile.println("incommingDataRate,rangeSize,numQuery,avgResultSize(ms),"
					+ "avgResponseTime(ms),avgFilteringTime(ms),avgTotalTime(ms),precision");
			metricFile.flush();
			int queryDelay = 1000; //millisecond
			int domain = domainMax;
			int randValue;
			// get delta data set from collector	
			float precision = 0;
			int totalSize = 0;
			int totalTime = 0;
			long reponseTime = 0;
			long filteredTime = 0;
			for (int range : Constants.Query.queryRanges) {
				for (int i = 0; i < numQueries; i++) {
					System.out.println("Cloud: range:" + range + "; times : " + i);
					randValue = (int) (Math.random() * (domain - (range * domain / 100)));
					// TODO pay attention
					a = randValue + 0;
					b = (range * domain / 100) + a;
					// TODO pay attention
					b = Math.min(b, 100);
					System.out.println(a + "-" + b);
					// send query to collector
					long startTime = Time.now();
					long delayStart = Time.now();
					outToCloud.println(a + Constants.QUERY_SEPERATOR + b);
					outToCloud.flush();
					// read delta data set from collector
					List<String> delta = new ArrayList<String>();
					String receivedLine = "";
					while (true) {
						receivedLine = inFromCloud.readLine();
						if (receivedLine != null) {
							if (receivedLine.compareTo("done") == 0) {
								break;
							}
							delta.add(receivedLine);
						}
					}
					System.out.println("=> returned size : " + delta.size());
					reponseTime += Time.now() - startTime;
					startTime = Time.now();
					//TODO decrypt and filter out non-overlapped records
					Data data;
					String plainText = "";
					List<Data> filteredResults = new ArrayList<>();
					for (String cipherText : delta) {
						plainText = AESEncryptor.CBCDecrypt(cipherText, 
								Constants.Enryption.KEY_VALUE_1);
						//System.out.println(plainText);
						if (! plainText.contains("dummy")) {
							data = Data.parseData(plainText, sourceType);
							if (data.getIndexedAttribute() >= a 
									&& data.getIndexedAttribute() <= b) {
								filteredResults.add(data);
							}
						}
					}
					filteredTime += Time.now() - startTime;
					startTime = Time.now();
					System.out.println("=> filtered size : " + filteredResults.size());
					totalSize += delta.size();
					if (delta.size() == 0) {
						precision += 1;
					}else {
						precision += (float) filteredResults.size() / delta.size();
						System.out.println("=> precision : "
									+ (float) filteredResults.size() / delta.size());
					}
					// delay
					long timeProcess = Time.now() - delayStart;
					if (timeProcess <= queryDelay) {
						TimeUnit.MILLISECONDS.sleep(queryDelay - timeProcess);
					}
				}
				totalTime = (int) (reponseTime + filteredTime);
				String metric = incommingDataRate
						+ "," + range 
						+ "," + numQueries 
						+ "," + (totalSize / numQueries)
						+ "," + (reponseTime / numQueries) 
						+ "," + (filteredTime / numQueries) 
						+ "," + (totalTime / numQueries)
						+ "," + (precision / numQueries);
				metricFile.println(metric);
				metricFile.flush();
				precision = 0;
				totalSize = 0;
				totalTime = 0;
				reponseTime = 0;
				filteredTime = 0;
			}
			// closing resources
			outToCloud.close();
			inFromCloud.close();
			connCloud.close();
			metricFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
