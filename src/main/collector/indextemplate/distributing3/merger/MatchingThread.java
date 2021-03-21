package main.collector.indextemplate.distributing3.merger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.hadoop.util.Time;

import base.AppProperties;
import base.Constants;
import base.Node;
import data.Data;
import main.BigLog;

public class MatchingThread extends Thread{
//	private PrintWriter outToCloud = null;
//	private Merger merger = null;
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	private static int rangeInterval = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("range.interval"));
	private static int branchingFactor = Integer.parseInt(AppProperties.getInstance()
			.getProperties().get("branching.factor").toString());

//	public MatchingThread(Merger merger, PrintWriter outToCloud) throws IOException {
//		this.merger = merger;
//		this.outToCloud = outToCloud;
//	}
	
	 /**
	 * @param merger2
	 * @param outToCloud2
	 * @param currentPublicationNumber
	 * @param currentIndexLeafCountString
	 * @param currentIndexCountString
	 * @param listLeafCounts
	 * @param listMatchingTables
	 * @param removedTupleBuffer
	 * @param numOfServerNode
	 * @throws Exception 
	 */
	public MatchingThread(Merger merger, PrintWriter outToCloud, int currentPublicationNumber,
			String currentIndexLeafCountString, String currentIndexCountString, 
			String indexLeafRealCountString, List<String> removedTupleBuffer, 
			ArrayList<Data> overFlowArrays[]){
		try {
//		System.out.println("==> Start matching");
//		System.out.println("currentIndexCountString:" + currentIndexCountString);
//		System.out.println("indexLeafRealCountString:" + indexLeafRealCountString);
//		System.out.println("metaString:" + metaString);
//		System.out.println("size of listMatcher:" + listMatcher.size());
		// matching
		StringBuilder str = new StringBuilder();
		str.append(currentPublicationNumber + ",");
		long startTime = Time.now();
		String parts[];
		// matching time
		startTime = Time.now();
		// build secure index
		String leafNoiseCountsString[] = currentIndexLeafCountString.split(
				Constants.Node.INDEX_COUNT_SEPERATOR);	
		String leafRealCountsString[] = indexLeafRealCountString.split(
				Constants.Com2.DATA_SEPARATOR);
		// remove counts of dummy tuples from leafRealCounts
		int leafRealCounts[] = new int[leafRealCountsString.length];
		int noise = 0;
		int count = 0;
		for (int i = 0; i < leafNoiseCountsString.length; i++) {
			noise = (int) Float.parseFloat(leafNoiseCountsString[i]);
			count = Integer.parseInt(leafRealCountsString[i]);
			if (noise > 0) {
				leafRealCounts[i] = count - noise;
			}else {
				leafRealCounts[i] = count;
			}
		}
		// build index layout
		List<Node> leafNodes = Node.buildLeafNodes(Data.getDomainMin(
				sourceType), 
				Data.getDomainMax(sourceType), 
				rangeInterval, 
				Constants.DataSource.BOUND_DEVIATION);
		// insert leaf counts received from Checker
		for (int i = 0; i < leafNodes.size(); i++) {
			leafNodes.get(i).setCount(leafRealCounts[i]);
		}
		Node index = Node.buildIndex(leafNodes, branchingFactor);
		// convert counts of index to array of counts
		parts = currentIndexCountString.split(Constants.Node.INDEX_COUNT_SEPERATOR);
		int indexSize = parts.length;
		int arrayIndexNoiseCounts[] = new int[indexSize];
		// ignore positive counts since Checker also counts dummy tuples
		for (int i = 0; i < indexSize; i++) {
			arrayIndexNoiseCounts[i] = Integer.parseInt(parts[i]);
		}
		index.mergeAllCounts(arrayIndexNoiseCounts);
		// merging time
		str.append((Time.now() - startTime) + ",");
		startTime = Time.now();
		// process removed tuples
		int leafSize = leafNodes.size();
		int removedTupleSize = removedTupleBuffer.size();
		@SuppressWarnings("unchecked")
		ArrayList<String>[] matchingTable = new ArrayList[leafSize];
		for (int i = 0; i < leafSize; i++) {
			matchingTable[i] = new ArrayList<>();
		}
		// insert removed tuples into overflow arrays
		// Format of removed tuple: indexLeaf$etuple
		int indexLeafId = -1;
		Data data;
		for (int i = 0; i < removedTupleSize; i++) {
			parts = removedTupleBuffer.get(i).split(Constants.Com2.DATA_SEPARATOR, 2);
			indexLeafId = Integer.parseInt(parts[0]);
			data = new Data();
			data.setEncryptedText(parts[1]);
			leafNodes.get(indexLeafId).getOverFlowArray().add(data);
		}
		// generate overflow arrays for leaf nodes
		int remainder = 0;
		for (int i = 0; i < leafSize; i++) {
			remainder = BigLog.getInstance().getOverFlowArraySize() 
					- leafNodes.get(i).getOverFlowArray().size();
			for (int j = 0; j < remainder; j++) {
				leafNodes.get(i).getOverFlowArray().add(overFlowArrays[i].get(j));
			}
			Collections.shuffle(leafNodes.get(i).getOverFlowArray());
		}
		// building overflow arrays time
		str.append((Time.now() - startTime) + ",");
		startTime = Time.now();
		// outToCloud
		// send publicationNumber 
		outToCloud.println(Constants.Com2.MERGER_PUBLICATION_NUMBER + currentPublicationNumber);
		outToCloud.flush();
		// send index
		outToCloud.println(Constants.Com2.MERGER_INDEX + index.convertCountsToString());
		outToCloud.flush();
		// send overflow arrays. Format; leafId$EncryptedText
		for (Node leaf : leafNodes) {
			for(Data overflowArray : leaf.getOverFlowArray()) {
				outToCloud.println(Constants.Com2.MERGER_OVERFLOW_ARRAY
				+ leaf.getId() + Constants.Com2.DATA_SEPARATOR
				+ overflowArray.getEncryptedText());
				outToCloud.flush();
			}
		}
		// send done message + id of the first leaf
//		outToCloud.println(Constants.Com2.MERGER_DONE);
//		outToCloud.flush();
		// publishing time
		str.append((Time.now() - startTime));
		merger.appendMetricFile(str.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	 public void run() {
		 
	 }	 
}
