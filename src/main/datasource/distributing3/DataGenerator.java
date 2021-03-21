package main.datasource.distributing3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import base.AppProperties;
import base.Constants;
import base.DynamicProperties;

public class DataGenerator extends Thread{
	private static int UNIQUE_ID = 1;
	private static int count = 0;
	private static int numRecordPerSecond = Integer.parseInt(DynamicProperties.getInstance()
								.getProperties().getProperty("incoming.data.rate"));
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	
	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException 
	{
		 new DataGenerator().run();
	}
	
    @Override 
	public void run() {
    		Socket connectionSocket = null;
    			BufferedReader br = null;
	    		FileReader fr = null;
	    		String line = "";
	    		PrintWriter outToCollector = null;
	    		System.out.println("Default data rate = " + numRecordPerSecond);
	    	try {
	    		connectionSocket = new Socket(DynamicProperties.getInstance().getProperties()
		        		.getProperty("dispatcher.host"), 
	    				Constants.Dispatcher.PORT);
	    		String generatorID = "generator " + (UNIQUE_ID ++);
	    		outToCollector = new PrintWriter(connectionSocket.getOutputStream());
	    		// send id to collector
	    		outToCollector.println(generatorID);
	    		outToCollector.flush();
	    		// read data from sample
	    		if (sourceType.equalsIgnoreCase("log")) {
	    			fr = new FileReader(DynamicProperties.getInstance().getProperties().
							get("datasource.path").toString() + "log");
	    			System.out.println("Data source: " + sourceType);
	    		}else if (sourceType.equalsIgnoreCase("gowalla")) {
	    			fr = new FileReader(DynamicProperties.getInstance().getProperties().
							get("datasource.path").toString() + "gowalla");
	    			System.out.println("Data source: " + sourceType);
	    		}else if (sourceType.equalsIgnoreCase("usps")) {
	    			fr = new FileReader(DynamicProperties.getInstance().getProperties().
							get("datasource.path").toString() + "usps");
	    			System.out.println("Data source: " + sourceType);
	    		}else {
	    			System.out.println("No data source: " + sourceType + " !!!!!");
	    		}
	    		List<String> lineList = new ArrayList<String>();
	    		br = new BufferedReader(fr);
	    		while ((line = br.readLine()) != null) {
	    			lineList.add(line);
	    		}
	    		br.close();
			fr.close();
			//timer = new Timer();
			//timer.schedule(new PrintStatus(), 1000);
	    		int len = lineList.size();
	    		int i = 0;
	    		// int count = 0;
	    		// randomize number of records and send them to collector 
	    		// Random rand = new Random();
	    		while (true) {
	    			for (int j = 0; j < numRecordPerSecond; j++) {
	    				outToCollector.println(lineList.get(i));
	    				i++;
	    				count++;
	    				if (i == len) {
	    					i = 0;
	    				}
	    			}
	    			outToCollector.flush();
	    			// generate values with an mean of 1000 and a standard deviation of 500
	    			// int randInt = (int) Math.round(rand.nextGaussian() * 1000 
	    			//		+ Constants.DataGenerator.NUM_RECORD_PER_TIME_INTERVAL);
	    			// numRecordPerSecond = randInt;
	    			// logger.info("Tuples : " + numRecordPerSecond);
	    			try {
	    				//System.out.println("sent: " + count);
	    				count = 0;
	    				TimeUnit.MILLISECONDS.sleep(Constants.DataGenerator.TIME_INTERVAL);
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    				br.close();
	    				fr.close();
	    			}
	    		}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	} 
    }
}

