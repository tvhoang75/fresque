package main;

import java.io.IOException;
import org.apache.commons.math3.distribution.LaplaceDistribution;

import base.AppProperties;
import base.Constants;
import base.Node;

public class BigLog {
	private int overFlowArraySize = 0;
	private static BigLog singleInstance = null;
	private int numOfBins = 0;
	
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	private final static float inverseProbability = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("laplace.probability.inverse"));
	private float totalPrivacyBudget = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.budget.total"));
	private float privacySensitivity = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.laplace.sensitivity"));
	
	public float getPrivacySensitivity() {
		return privacySensitivity;
	}

	public void setPrivacySensitivity(float privacySensitivity) {
		this.privacySensitivity = privacySensitivity;
	}

	public static void main(String [] args) throws IOException {
		System.out.println("overflow array size: " 
						+ BigLog.getInstance(10));
	}
	
	private BigLog() throws IOException {
		//initConfig();
	}
	
	public static BigLog getInstance() throws IOException 
    { 
        if (singleInstance == null) {
        		singleInstance = new BigLog();
        		singleInstance.initConfig();
        }
        return singleInstance;
    }
	
	public static BigLog getInstance(float sensitivity) throws IOException 
    { 
        if (singleInstance == null) {
        		singleInstance = new BigLog();
        		singleInstance.setPrivacySensitivity(sensitivity);
        		singleInstance.initConfig();
        }
        return singleInstance;
    }
	
	private void initConfig() throws IOException {
		System.out.println("- Initiate BigLog config ...");
		Node indexTemplate = IndexTemplate.buildIndexTemplate();
		System.out.println("=> Data source = " + sourceType);
		numOfBins = indexTemplate.getLeafNodes().size();
		System.out.println("=> Domain size (bins) = " + numOfBins);
		System.out.println("=> Index level = " + indexTemplate.getLevel());
		System.out.println("=> Default sensitivity = " + privacySensitivity);
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				(double) privacySensitivity / ((double) totalPrivacyBudget / indexTemplate.getLevel()));
		overFlowArraySize = (int) laplace.inverseCumulativeProbability(inverseProbability / 100);
		System.out.println("=> Overflow array size = " + overFlowArraySize);
		// Initiate dummy numbers
		System.out.println("=> Dummy number size (shuffled) = " + Constants.Matcher.NUMBER_SIZE);
	}
	
	public int getOverFlowArraySize() {
		return overFlowArraySize;
	}

	public void setOverFlowArraySize(int overFlowArraySize) {
		this.overFlowArraySize = overFlowArraySize;
	}

	public int getNumOfBins() {
		return numOfBins;
	}

	public void setNumOfBins(int numOfBins) {
		this.numOfBins = numOfBins;
	}

}