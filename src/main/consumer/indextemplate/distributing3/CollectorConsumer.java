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

/**
 * @author vtran
 *
 */
public class CollectorConsumer extends Thread{
	private static int a = Integer.MAX_VALUE;
	private static int b = Integer.MIN_VALUE;
	private final static int numQueries = Integer.parseInt(AppProperties.getInstance().
											getProperties().getProperty("query.number"));
	private final static int incommingDataRate = Integer.parseInt(DynamicProperties.getInstance()
			.getProperties().getProperty("incoming.data.rate"));
	private static int totalSize = 0;
	private static int totalTime = 0;
	
	public static void main(String[] args) {
		new CollectorConsumer();
	}
	
	public CollectorConsumer() {
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
			String metricFileName = Constants.Cloud.METRICS_FOLDER_0 + "consumer_collector" 
					+ Constants.Metrics.METRIC_FILE_EXTENSION;
			FileWriter fw = new FileWriter(metricFileName, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter metricFile = new PrintWriter(bw);
			// open connection to Collector
			Socket socketCollector = new Socket(DynamicProperties.getInstance().getProperties()
					.getProperty("dispatcher.host"), Constants.Collector.LISTEN_PORT);
			PrintWriter outToCollector = 
					new PrintWriter(socketCollector.getOutputStream());
			BufferedReader inFromCollector = 
					new BufferedReader(new InputStreamReader(socketCollector.getInputStream()));
			outToCollector.println("consumer");
			outToCollector.flush();
			metricFile.println("incommingDataRate,rangeSize,numQuery,avgResultSize(ms),avgTimeLatency(ms)");
			metricFile.flush();
			int timeInterval = 1000; //millisecond
			int domain = 100;
			int randValue;
			// get delta data set from collector	
			for (int range : Constants.Query.queryRanges) {
				for (int i = 0; i < numQueries; i++) {
					System.out.println("range:" + range + "; times : " + i);
					randValue = (int) (Math.random() * (domain - (range * domain / 100)));
					// TODO pay attention
					a = randValue + 0;
					b = (range * domain / 100) + a;
					// TODO pay attention
					b = Math.min(b, 100);
					// System.out.println(a + "-" + b);
					// send query to collector
					long startTime = Time.now();
					outToCollector.println(a + Constants.QUERY_SEPERATOR + b);
					outToCollector.flush();
					// read delta data set from collector
					List<String> delta = new ArrayList<String>();
					String receivedLine = "";
					while (true) {
						receivedLine = inFromCollector.readLine();
						if (receivedLine != null) {
							if (receivedLine.compareTo("done") == 0) {
								break;
							}
							delta.add(receivedLine);
						}
					}
					System.out.println("=> Size of returned delta : " + delta.size());
					totalTime += (Time.now() - startTime);
					totalSize += delta.size();
					// delay
					TimeUnit.MILLISECONDS.sleep(timeInterval);
				}
				String metric = incommingDataRate
						+ "," + range 
						+ "," + numQueries 
						+ "," + (totalSize / numQueries) 
						+ "," + (totalTime / numQueries);
				metricFile.println(metric);
				metricFile.flush();
			}
			// closing resources
			outToCollector.close();
			inFromCollector.close();
			socketCollector.close();
			metricFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
