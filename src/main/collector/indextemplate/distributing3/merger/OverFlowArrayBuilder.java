package main.collector.indextemplate.distributing3.merger;

import java.util.ArrayList;

import org.apache.hadoop.util.Time;

import base.AppProperties;
import base.Constants;
import data.Data;
import main.BigLog;

public class OverFlowArrayBuilder extends Thread{

	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	

	/**
	 * @param merger
	 * @param size
	 */
	@SuppressWarnings("unchecked")
	public OverFlowArrayBuilder(Merger merger, int size){
		try {
			StringBuilder stringBuilder = new StringBuilder();
			int overFlowArraySize = BigLog.getInstance().getOverFlowArraySize();
			stringBuilder.append(overFlowArraySize + ",");
			long startTime = Time.now();
			ArrayList<Data> overFlowArrays[] = new ArrayList[size];
			for (int i = 0; i < size; i++) {
				overFlowArrays[i] = new ArrayList<>();
				overFlowArrays[i].addAll(Data.generateSetOfEncryptedDummy(overFlowArraySize, 
						sourceType, Constants.Enryption.KEY_VALUE_1));
			}
			stringBuilder.append((Time.now() - startTime));
			merger.updateOverFlowArrays(overFlowArrays, stringBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	 public void run() {
		 
	 }	 
}
