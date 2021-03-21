package base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.iq80.leveldb.DB;
import base.Constants.Com;
import base.Constants.Index;
import data.Data;
import data.type.Log;
import main.BigLog;
import main.MappingItem;
import main.crypto.AESEncryptor;

/**
 * @author tvhoang46
 *
 */
public class Node {
	// relationship
	protected Node parent = null;
	protected List<Node> children = null;
	// data
	protected long nodeId = -1;
	protected float lowerBound = -1;
	protected float upperBound = -1;
	protected float count = -1;
	// data items for leaves
	protected List<Data> dataList = null;
	// overflow array for leaves
	protected List<Data> overflowArray = null;
	protected int overflowArraySize = 0;
	private int realDataSize = 0;
	private int dummyDataSize = 0;
	
	// offset of the node stored on disk
	private long offset = -1;
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	private static int rangeInterval = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("range.interval"));
	private static float totalPrivacyBudget = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.budget.total"));
	private static float privacySensitivity = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.laplace.sensitivity"));
	private final static float inverseProbability = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("laplace.probability.inverse"));
	private static int branchingFactor = Integer.parseInt(AppProperties.getInstance()
			.getProperties().get("branching.factor").toString());
	
	public void initiateDataList() {
		dataList = new ArrayList<>();
	}
	
	public Node(Node parent, List<Node> children, long nodeId, float lowerBound, float upperBound, int count,
			List<Data> dataList, List<Data> overflowArray, long offset) {
		super();
		this.parent = parent;
		this.children = children;
		this.nodeId = nodeId;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.count = count;
		this.dataList = dataList;
		this.overflowArray = overflowArray;
		this.offset = offset;
	}
	
	public Node() {
		this.children = new ArrayList<>();
		this.nodeId = -1;
		this.lowerBound = -1;
		this.upperBound = -1;
		this.count = -1;
	}
	
	public static void showInfo(Node index) {
		System.out.println("=> Data source = " + sourceType);
		System.out.println("=> Domain size (bins) = " + index.getLeafNodes().size());
		System.out.println("=> Index level = " + index.getLevel());
		LaplaceDistribution laplace = new LaplaceDistribution(0, privacySensitivity 
				/ (totalPrivacyBudget/index.getLevel()));
		// System.out.println(laplace.inverseCumulativeProbability(0.99)); // 11
		// System.out.println("sample: " + laplace.sample());
		String randomerSizeCoefficient = AppProperties.getInstance().getProperties()
				.getProperty("randomer.size.coefficient");
		System.out.println("Randomer size coefficient = " + randomerSizeCoefficient);
		System.out.println("Total privacy budget = " + totalPrivacyBudget);
		int overFlowArraySize = (int) laplace.inverseCumulativeProbability(inverseProbability / 100);
		System.out.println("=> Overflow array size = " + overFlowArraySize);
	}
	
	public static String getInfo(Node index) {
		StringBuilder str = new StringBuilder();
		str.append("Data source = " + sourceType + "\n");
		str.append("Domain size (bins) = " + index.getLeafNodes().size() + "\n");
		str.append("Index level = " + index.getLevel() + "\n");
		LaplaceDistribution laplace = new LaplaceDistribution(0, privacySensitivity 
				/ (totalPrivacyBudget / index.getLevel()));
		int overFlowArraySize = (int) laplace.inverseCumulativeProbability(inverseProbability / 100);
		str.append("Overflow array size = " + overFlowArraySize + "\n");
		String randomerSizeCoefficient = AppProperties.getInstance().getProperties()
				.getProperty("randomer.size.coefficient");
		str.append("Randomer size coefficient = " + randomerSizeCoefficient + "\n");
		str.append("Total privacy budget = " + totalPrivacyBudget + "\n");
		return str.toString();
	}
	
	/**
	 * Traverse the index and return data of the leaves between lowerRange and upperRange
	 * @param lowerRange
	 * @param upperRange
	 * @return 
	 */
	public List<Data> getListData(float a, float b) {
		Queue<Node> queue = new LinkedList<>();
		List<Node> leaves = new ArrayList<>();
		List<Data> result = new ArrayList<>();
		if (this != null) {
			queue.add(this);
		}
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			// if not a leaf, check the count, and then compare the range with query's range
			// if there is an intersection, continue on that node
			if (!((a < n.getLowerBound() && b < n.getLowerBound()) || 
						(a > n.getUpperBound() && b > n.getUpperBound()))) {
				if (n.getChildren().size() == 0) {
					leaves.add(n);
				}else {
					for(Node child: n.getChildren()) {
						queue.add(child);
					}
				}
			}
		}
		
		// return the results
		int size = leaves.size();
		for (int i = 0; i < size; i++) {
			result.addAll(leaves.get(i).getDataList());
			result.addAll(leaves.get(i).getOverFlowArray());
		}
		return result;
	}
	
	/**
	 * Traverse the index and return the id of the leaves between lowerRange and upperRange
	 * @param lowerRange
	 * @param upperRange
	 * @return 
	 */
	public Map<Long,Integer> getListLeafId(float a, float b) {
		Queue<Node> queue = new LinkedList<>();
		List<Node> leaves = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Map<Long,Integer> result = new HashedMap();
		if (this != null) {
			queue.add(this);
		}
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			// if not a leaf, check the count, and then compare the range with query's range
			// if there is an intersection, continue on that node
			if (!((a < n.getLowerBound() && b < n.getLowerBound()) || 
						(a > n.getUpperBound() && b > n.getUpperBound()))) {
				if (n.getChildren().size() == 0) {
					leaves.add(n);
				}else {
					for(Node child: n.getChildren()) {
						queue.add(child);
					}
				}
			}
		}
		
		// return the results
		int size = leaves.size();
		for (int i = 0; i < size; i++) {
			result.put(leaves.get(i).getId(), (int)leaves.get(i).getCount());
		}
		return result;
	}
	
	/**
	 * Return data to which the value belongs
	 * data may stay in DataList or OverFlowArray
	 * @param value
	 * @return Data
	 */
	public Data getLeaf(float value) {
		Data data = null;
		Node leaf = this;
		Node child;
		int size;
		int i;
		if (leaf != null) {
			if (leaf.getLowerBound() <= value 
							&& leaf.getUpperBound() >= value) {
				while (leaf.getChildren().size() > 0) {
					size = leaf.getChildren().size();
					for (i = 0; i < size; i++) {
						child = leaf.getChildren().get(i);
						if (child.getLowerBound() <= value 
								&& child.getUpperBound() >= value) {
							leaf = child;
							break;
						}
					}
					if (i == size) {
						return null;
					}
				}
			}else {
				return null;
			}
		}
		// find in dataList
		size = leaf.getDataList().size();
		for (i = 0; i < size; i++) {
			data = leaf.getDataList().get(i);
			if (data.getIndexedAttribute() == value) {
				// System.out.println("dataList");
				return data;
			}
		}
		// find in overflow arrays
		size = leaf.getOverFlowArray().size();
		for (i = 0; i < size; i++) {
			data = leaf.getOverFlowArray().get(i);
			if (data.getIndexedAttribute() == value) {
				//System.out.println("overflow array");
				return data;
			}
		}
		return null;
	}
	
	/**
	 * Return id of the leaf node to which the value belongs
	 * @param value
	 * @return long
	 */
	public long getLeafId(int value) {
		Node node = this;
		Node child;
		int size;
		int i;
		if (node != null) {
			if (node.getLowerBound() <= value 
							&& node.getUpperBound() >= value) {
				while (node.getChildren().size() > 0) {
					size = node.getChildren().size();
					for (i = 0; i < size; i++) {
						child = node.getChildren().get(i);
						//System.out.println("value=" + value + " : lowerBound=" + child.getLowerBound()
						//+ " : upperBound=" + child.getUpperBound());
						if (child.getLowerBound() <= value 
								&& child.getUpperBound() >= value) {
							node = child;
							break;
						}
					}
					if (i == size) {
						return -1;
					}
				}
			}else {
				return -1;
			}
		}
		return node.getId();
	}
	
//	public static void buildOverFlowArray(List<Node> leafNodes, int overFlowArraySize) {
//		int leafNodesSize = leafNodes.size();
//		for (int i = 0; i < leafNodesSize; i++) {
//			leafNodes.get(i).getOverFlowArray().addAll(Log.generateSetOfDummy(overFlowArraySize));
//		}
//	}

	// return the sum of count of all leaf nodes, which represent noisy size of delta
	// Note: one pre-condition is that noisy count is equal to the size of delta published
	public int getNoisySizeDelta() {
		int noisyCount = 0;
		List<Node> leafNodes = this.getLeafNodes();
		int size = leafNodes.size();
		for (int i = 0; i < size; i++) {
			if (leafNodes.get(i).getCount() > 0) {
				noisyCount += leafNodes.get(i).getCount();
			}
		}
		return noisyCount;
	}
	
	public int getSize() {
		int size = 0;
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		Node node;
		while (queue.isEmpty() == false) {
			node = queue.poll();
			size++;
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
		return size;
	}
	
	public int[] getCounts() {
		List<Float> listCounts = new ArrayList<>();
		int order = 0;
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		Node node;
		while (queue.isEmpty() == false) {
			node = queue.poll();
			listCounts.add(order, node.getCount());
			order++;
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
		return listCounts.stream().mapToInt(Float::intValue).toArray();
	}
	
	// if count2 is larger than counts1, get the increment, otherwise, set zero
	public static List<Integer> incrementalCounts(List<Integer> counts1, List<Integer> counts2) {
		List<Integer> incrementalCounts = new ArrayList<>();
		int size = counts1.size();
		for (int i = 0; i < size; i++) {
			if (counts2.get(i) > counts1.get(i)) {
				incrementalCounts.add(i, counts2.get(i) - counts1.get(i));
				//System.out.println("position: " + i + " - " + incrementalCounts.get(i));
			}else {
				incrementalCounts.add(i, 0);
			}
		}
		return incrementalCounts;
	}
	
	public static String convertCountsToString(int[] counts) {
		String countsString = "";
		int size = counts.length;
		for (int i = 0; i < size; i++) {
			countsString += counts[i] + Constants.Node.INDEX_COUNT_SEPERATOR;
		}
		return countsString;
	}
	
	// result format (assuming level-order): count1;count2;count3,...
	public String convertCountsToString() {
		StringBuilder countsString = new StringBuilder();
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		Node node;
		while (queue.isEmpty() == false) {
			node = queue.poll();
			countsString.append((int) node.getCount() + Constants.Node.INDEX_COUNT_SEPERATOR);
			for(Node child : node.getChildren()) {
				queue.add(child);
			}
		}
		return countsString.toString();
	}
	
	// result format: count1;count2;count3,...
	public String convertLeafCountsToString() {
		StringBuilder countsString = new StringBuilder("");
		List<Node> leaves = this.getLeafNodes();
		int size = leaves.size();
		for (int i = 0; i < size; i++) {
			countsString.append(leaves.get(i).getCount() + Constants.Node.INDEX_COUNT_SEPERATOR);
		}
		return countsString.toString();
	}
	
	/**
	 * Merge array of counts to existing counts of index (level-order)
	 * @param counts
	 */
	public void mergeAllCounts(int []counts) {
		int order = 0;
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		Node node;
		while (queue.isEmpty() == false) {
			node = queue.poll();
			node.setCount(node.getCount() + counts[order]);
			order++;
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
	}
	
	public void replaceWithCounts(int []counts) {
		int order = 0;
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		Node node;
		while (queue.isEmpty() == false) {
			node = queue.poll();
			node.setCount(counts[order]);
			order++;
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
	}
	
	/** This similar to @convertIndexToString, but the size of overflow arrays are included
	 * @return format: nodeId;lowerBound;upperBound;count;parentLineNumber;overflowArraySize
	 */
	public String convertIndexToString2(List<Integer> listOverFlowArraySize) {
		StringBuilder indexString = new StringBuilder();
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		long parentId = Index.INDEX_ROOT_PARENT_ID;
		int overflowArraySize = 0;
		int leafIndex = 0;
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			if (n.getParent() != null) {
				parentId = n.getParent().getId();
			}else {
				parentId = Index.INDEX_ROOT_PARENT_ID;
			}
			indexString.append(n.getId() + Constants.FileConfig.FILE_FIELD_SEPARATOR);
			indexString.append(n.getLowerBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getUpperBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getCount());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + parentId);
			if (n.getChildren().size() == 0) {
				overflowArraySize = listOverFlowArraySize.get(leafIndex);
				leafIndex++;
			}else {
				overflowArraySize = 0;
			}
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + overflowArraySize);
			indexString.append(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
		return indexString.toString();
	}
	
	/** This similar to @convertIndexToString, but the size of overflow arrays are included
	 * @return format: nodeId;lowerBound;upperBound;count;parentLineNumber;overflowArraySize
	 */
	public String convertIndexToString2() {
		StringBuilder indexString = new StringBuilder();
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		long parentId = Index.INDEX_ROOT_PARENT_ID;
		int overflowArraySize = 0;
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			if (n.getParent() != null) {
				parentId = n.getParent().getId();
			}else {
				parentId = Index.INDEX_ROOT_PARENT_ID;
			}
			indexString.append(n.getId() + Constants.FileConfig.FILE_FIELD_SEPARATOR);
			indexString.append(n.getLowerBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getUpperBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getCount());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + parentId);
			overflowArraySize = 0;
			if (n.getOverFlowArray() != null) {
				overflowArraySize = n.getOverFlowArray().size();
			}
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + overflowArraySize);
			indexString.append(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
		return indexString.toString();
	}
	
	
	// result format: nodeId;lowerBound;upperBound;count;parentLineNumber
	public String convertIndexToString() {
		StringBuilder indexString = new StringBuilder();
		Queue<Node> queue = new LinkedList<>();
		if (this != null) {
			queue.add(this);
		}
		long parentId = Index.INDEX_ROOT_PARENT_ID;
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			if (n.getParent() != null) {
				parentId = n.getParent().getId();
			}else {
				parentId = Index.INDEX_ROOT_PARENT_ID;
			}
			indexString.append(n.getId() + Constants.FileConfig.FILE_FIELD_SEPARATOR);
			indexString.append(n.getLowerBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getUpperBound());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + n.getCount());
			indexString.append(Constants.FileConfig.FILE_FIELD_SEPARATOR + parentId);
			indexString.append(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
		return indexString.toString();
	}

	public static void setAllCounts(Node index, int countValue) {
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		while (queue.isEmpty() == false) {
			Node node = queue.poll();
			node.setCount(countValue);
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
	}
	
	// uniformly perturb index 
	public static void perturbIndex(Node index, float sensitivity, float privacyBudget) {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				sensitivity / (privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		while (queue.isEmpty() == false) {
			Node node = queue.poll();
			// set noise to the count
			node.setCount((int) (node.getCount() + laplace.sample()));
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
	}
	
	public static void main(String[] args) {
		List<Node> leafNodes = Node.buildLeafNodes(0, 4, rangeInterval, 
				Constants.DataSource.BOUND_DEVIATION);
		for (Node leaf : leafNodes) {
			leaf.setCount(10);
		}
		Node index = Node.buildIndex(leafNodes, 2);
		System.out.println("before pertubred: " + index.convertCountsToString());
		Node.perturbIndex(index, privacySensitivity, totalPrivacyBudget);
		System.out.println("after  pertubred: " + index.convertCountsToString());
		Node.applyConstraintsOnIndex(index);
		System.out.println("after constraint: " + index.convertCountsToString());
	}
	
	/**
	 * Apply constraints on the index, as described in the paper
	 * (Boosting the Accuracy of Differentially Private Histograms Through Consistency)
	 * @param index is the root of the index
	 * @param k is the branching factor
	 */
	public static void applyConstraintsOnIndex(Node index) {
		int k = branchingFactor;
		Queue<Node> queue = new LinkedList<>();
		List<Node> leafNodes = index.getLeafNodes();
		Node parentNode = null;
		Node leaf = null;
		// insert the parent of leafNodes into the queue
		for (int i = 0; i < leafNodes.size(); i++) {
			leaf = leafNodes.get(i);
			if ((leaf.getParent() != null) &&  (leaf.getParent() != parentNode)) {
				parentNode = leaf.getParent();
				queue.add(leaf.getParent());
			}
		}
		// System.out.println(queue.size());
		Node currNode = null;
		float sum = 0;
		float newCount = 0;
		int l = 0;
		// step 1: from leaves to the root
		parentNode = null;
		while (!queue.isEmpty()) {
			currNode = queue.poll();
			l = currNode.getHeight();
			// sum count of children first
			sum = 0;
			for (Node child : currNode.getChildren()) {
				sum += child.getCount();
			}
			newCount = (currNode.getCount() * (float) ((Math.pow(k, l) - Math.pow(k, l-1)) / (Math.pow(k, l) - 1)))
					+ (sum * (float) ((Math.pow(k, l-1) - 1) / (Math.pow(k, l) - 1)));			
			currNode.setCount(newCount);
			if ((currNode.getParent() != null && currNode.getParent() != parentNode)) {
				parentNode = currNode.getParent();
				queue.add(parentNode);
			}
		}
		// step 2: from the root to leaves
		if (index != null) {
			queue.add(index);
		}
		float remaining;
		while (!queue.isEmpty()) {
			currNode = queue.poll();
			if (currNode.getChildren() != null) {
				sum = 0;
				for (Node child : currNode.getChildren()) {
					sum += child.getCount();
				}
				remaining = ((currNode.getCount() - sum) / k);
				for (Node child : currNode.getChildren()) {
					child.setCount(child.getCount() + remaining);
					if (child.getChildren() != null) {
						queue.add(child);
					}
				}
			}
			// convert floatCount to IntCount
			currNode.setCount((int) currNode.getCount());
		}		
	}
	
	/**
	 * (1) Perturb index by using Laplace noise, 
	 * (2) apply constraint on the counts of perturbed index,
	 * (3) link data set to the index
	 * @param index
	 * @param dataSet
	 * @param sensitivity
	 * @param privacyBudget
	 * @throws IOException
	 */
	public static void perturbAndConstraintIndex2(Node index, List<Data> dataSet, float sensitivity, 
			float privacyBudget, int batchNum) throws IOException {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				sensitivity / ((float) privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		double noise = 0;
		Node node;
		// perturb index with Laplace noise
		while (queue.isEmpty() == false) {
			node = queue.poll();
			// add noise to the count
			noise = laplace.sample();
			node.setCount(node.getCount() + (float) noise);
			for(Node child : node.getChildren()) {
				queue.add(child);
			}
		}
		// apply constraints to the perturbed index
		applyConstraintsOnIndex(index);
		// link data set to the index
		int noisyCount;
		int diff;
		int randPosition = 0;
		int realCount;
		for (Node leaf : index.getLeafNodes()) {
			noisyCount = (int) leaf.getCount();
			realCount = leaf.getDataList().size();
			// if noisyCount is negative
			if (noisyCount < realCount) {
				diff = realCount - noisyCount;
				for (int i = 0; i < diff; i++) {
					if (leaf.getDataList().size() == 0) {
						break;
					}
					// move some tuples to removed array 
					randPosition = (int) (Math.random() * leaf.getDataList().size());
					leaf.getOverFlowArray().add(leaf.getDataList().get(randPosition));
					leaf.getDataList().remove(randPosition);
				}
			}else {
				diff = noisyCount - realCount;
				MappingItem mappingItem;
				List<BigInteger> listOfItems;
				for (int i = 0; i < diff; i++) {
					// generate dummies with indexedAttributes set to -1
					mappingItem = new MappingItem(-1);
					listOfItems = new ArrayList<>();
					for (int k = 0; k < batchNum; k++) {
						listOfItems.add(BigInteger.valueOf(0));
					}
					mappingItem.setListOfItems(listOfItems);
					mappingItem.setLeafNode(leaf);
					// link dummy to leaf node
					// add randomly a dummy to data set
					// random value is between [0, Size(dataSet)]
					randPosition = (int)(Math.random() * dataSet.size());
					dataSet.add(randPosition, mappingItem);
				}
			}
			
			// TODO merge all removed items of the same leaf
			List<BigInteger> listOfItem = new ArrayList<>();
			Data d;
			BigInteger bigInteger;
			if (leaf.getOverFlowArray().size() > 1) {
				for (int i = 0; i < batchNum; i++) {
					listOfItem.add(BigInteger.valueOf(0));
					for (Data data : leaf.getOverFlowArray()) {
						bigInteger = listOfItem.get(i).add(data.getListOfItems().get(i));
						listOfItem.set(i, bigInteger);
					}
				}
				leaf.overflowArray.clear();
				d = new Data();
				d.setListOfItems(listOfItem);
				leaf.overflowArray.add(d);
			}
			// padding removed items with zero size
			if (leaf.getOverFlowArray().size() == 0) {
				for (int i = 0; i < batchNum; i++) {
					listOfItem.add(BigInteger.valueOf(0));
				}
				d = new Data();
				d.setListOfItems(listOfItem);
				leaf.overflowArray.add(d);
			}
		}
	}
	
	/**
	 * (1) Perturb index by using Laplace noise, 
	 * (2) apply constraint on the counts of perturbed index,
	 * (3) link data set to the index
	 * (4) write data to db (LevelDB)
	 * @param index
	 * @param dataSet
	 * @param sensitivity
	 * @param privacyBudget
	 * @throws Exception 
	 */
	public static void perturbAndConstraintIndexDB(Node index, List<Data> dataSet, float sensitivity, 
			float privacyBudget, DB db, List<Integer> listOverFlowArraySize) throws Exception {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				(double) sensitivity / ((double) privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		double noise = 0;
		Node node;
		// perturb index with Laplace noise
		while (queue.isEmpty() == false) {
			node = queue.poll();
			// add noise to the count
			noise = laplace.sample();
			node.setCount(node.getCount() + (float) noise);
			for(Node child : node.getChildren()) {
				queue.add(child);
			}
		}
		// apply constraints to the perturbed index
		applyConstraintsOnIndex(index);
		// link data set to the index
		int noisyCount;
		int diff;
		int randPosition = 0;
		int realCount;
		// dynamic sensitivity
		BigLog bigLog = BigLog.getInstance(sensitivity);
		for (Node leaf : index.getLeafNodes()) {
			noisyCount = (int) leaf.getCount();
			realCount = leaf.getDataList().size();
			// if noisyCount is negative
			if (noisyCount < realCount) {
				diff = realCount - noisyCount;
				for (int i = 0; i < diff; i++) {
					if (leaf.getDataList().size() == 0) {
						break;
					}
					randPosition = (int) (Math.random() * leaf.getDataList().size());
					leaf.getOverFlowArray().add(leaf.getDataList().get(randPosition));
					leaf.getDataList().remove(randPosition);
				}
			}else {
				diff = noisyCount - realCount;
				for (int i = 0; i < diff; i++) {
					// generate dummies
					Data dummies = Data.generateDummy(sourceType);
					// link dummy to leaf node
					dummies.setLeafNode(leaf);
					// add randomly a dummy to data set
					// random value is between [0, Size(dataSet)]
					randPosition = (int)(Math.random() * dataSet.size());
					dataSet.add(randPosition, dummies);
				}
			}
			String key;
			Data data;
			//TODO write data to database
			int leafSize = leaf.getDataList().size();
			for (int j = 0; j < leafSize; j++) {
				data = leaf.getDataList().get(j);
				// key (pubNum.leafId.tupleOrdering.1) ; 0: not overflow array
				key = "0." + leaf.getId() + "." + j + ".0";
				db.put(key.getBytes(), 
						AESEncryptor.CBCEncryptByte(data.toString(), Constants.Enryption.KEY_VALUE_1));
			}
			leaf.getDataList().clear();
			
			if (leaf.getOverFlowArray().size() < bigLog.getOverFlowArraySize()) {
				int remainder = bigLog.getOverFlowArraySize() 
						- leaf.getOverFlowArray().size();
				data = null;
				for (int i = 0; i < remainder; i++) {
					// generate a dummy
					data = Data.generateDummy(sourceType);
					leaf.getOverFlowArray().add(data);
				}
			}
			//TODO write overflow array to database
			int overFlowArraySize = leaf.getOverFlowArray().size();
			listOverFlowArraySize.add(overFlowArraySize);
			for (int j = 0; j < overFlowArraySize; j++) {
				data = leaf.getOverFlowArray().get(j);
				// key (pubNum.leafId.tupleOrdering.1) ; 1: overflow array
				key = "0." + leaf.getId() + "." + j + ".1";
				db.put(key.getBytes(), 
						AESEncryptor.CBCEncryptByte(data.toString(), Constants.Enryption.KEY_VALUE_1));
			}
			leaf.getOverFlowArray().clear();
		}
	}

	
	/**
	 * (1) Perturb index by using Laplace noise, 
	 * (2) apply constraint on the counts of perturbed index,
	 * (3) link data set to the index
	 * @param index
	 * @param map
	 * @param sensitivity
	 * @param privacyBudget
	 * @throws IOException
	 */
	public static void perturbAndConstraintIndex(Node index, float sensitivity, 
			float privacyBudget) throws IOException {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				(double) sensitivity / ((double) privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		double noise = 0;
		Node node;
		// perturb index with Laplace noise
		while (queue.isEmpty() == false) {
			node = queue.poll();
			// add noise to the count
			noise = laplace.sample();
			node.setCount(node.getCount() + (float) noise);
			for(Node child : node.getChildren()) {
				queue.add(child);
			}
		}
		// apply constraints to the perturbed index
		applyConstraintsOnIndex(index);
		// link data set to the index
		
		int noisyCount;
		int diff;
		int realCount;
		// dynamic sensitivity
		BigLog bigLog = BigLog.getInstance(sensitivity);
		for (Node leaf : index.getLeafNodes()) {
			noisyCount = (int) leaf.getCount();
			realCount = leaf.getRealDataSize();
			// if noisyCount is negative
			if (noisyCount < realCount) {
				diff = realCount - noisyCount;
				for (int i = 0; i < diff; i++) {
					if (leaf.getRealDataSize() == 0) {
						break;
					}
					//leaf.setRealDataSize(leaf.getRealDataSize() - 1);
					leaf.setOverflowArraySize(leaf.getOverflowArraySize() + 1);
				}
			}else {
				diff = noisyCount - realCount;
				leaf.setDummyDataSize(leaf.getDummyDataSize() + diff);
			}
			
			if (leaf.getOverFlowArray().size() < bigLog.getOverFlowArraySize()) {
				int remainder = bigLog.getOverFlowArraySize() 
						- leaf.getOverflowArraySize();
				leaf.setDummyDataSize(leaf.getDummyDataSize() + remainder);
				leaf.setOverflowArraySize(leaf.getOverflowArraySize() + remainder);
			}
		}
	}
	
	/**
	 * (1) Perturb index by using Laplace noise, 
	 * (2) apply constraint on the counts of perturbed index,
	 * (3) link data set to the index
	 * @param index
	 * @param dataSet
	 * @param sensitivity
	 * @param privacyBudget
	 * @throws IOException
	 */
	public static void perturbAndConstraintIndex(Node index, List<Data> dataSet, float sensitivity, 
			float privacyBudget) throws IOException {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				(double) sensitivity / ((double) privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		double noise = 0;
		Node node;
		// perturb index with Laplace noise
		while (queue.isEmpty() == false) {
			node = queue.poll();
			// add noise to the count
			noise = laplace.sample();
			node.setCount(node.getCount() + (float) noise);
			for(Node child : node.getChildren()) {
				queue.add(child);
			}
		}
		// apply constraints to the perturbed index
		applyConstraintsOnIndex(index);
		// link data set to the index
		int noisyCount;
		int diff;
		int randPosition = 0;
		int realCount;
		// dynamic sensitivity
		BigLog bigLog = BigLog.getInstance(sensitivity);
		for (Node leaf : index.getLeafNodes()) {
			noisyCount = (int) leaf.getCount();
			realCount = leaf.getDataList().size();
			// if noisyCount is negative
			if (noisyCount < realCount) {
				diff = realCount - noisyCount;
				for (int i = 0; i < diff; i++) {
					if (leaf.getDataList().size() == 0) {
						break;
					}
					randPosition = (int) (Math.random() * leaf.getDataList().size());
					leaf.getOverFlowArray().add(leaf.getDataList().get(randPosition));
					leaf.getDataList().remove(randPosition);
				}
			}else {
				diff = noisyCount - realCount;
				for (int i = 0; i < diff; i++) {
					// generate dummies
					Data dummies = Data.generateDummy(sourceType);
					// link dummy to leaf node
					dummies.setLeafNode(leaf);
					// add randomly a dummy to data set
					// random value is between [0, Size(dataSet)]
					randPosition = (int)(Math.random() * dataSet.size());
					dataSet.add(randPosition, dummies);
				}
			}
			if (leaf.getOverFlowArray().size() < bigLog.getOverFlowArraySize()) {
				int remainder = bigLog.getOverFlowArraySize() 
						- leaf.getOverFlowArray().size();
				Data data = null;
				for (int i = 0; i < remainder; i++) {
					// generate a dummy
					data = Data.generateDummy(sourceType);
					leaf.getOverFlowArray().add(data);
				}
			}
		}
	}

	// uniformly perturb index and randomly add dummy records
	public static void perturbIndexWithDataSet(Node index, List<Data> dataSet, float sensitivity, 
			float privacyBudget) throws IOException {
		LaplaceDistribution laplace = new LaplaceDistribution(0, 
				sensitivity / ((float) privacyBudget / index.getLevel()));
		Queue<Node> queue = new LinkedList<>();
		if (index != null) {
			queue.add(index);
		}
		int noise = 0;
		int randPosition = 0;
		while (queue.isEmpty() == false) {
			Node node = queue.poll();
			// add noise to the count
			noise = (int) laplace.sample();
			// if node is leaf, remove tuple / add dummy
			if (node.getChildren().size() == 0) {
				if (noise < 0) {
					int absNoise = Math.abs(noise);
					for (int i = 0; i < absNoise; i++) {
						if (node.getDataList().size() == 0) {
							break;
						}
						randPosition = (int) (Math.random() * node.getDataList().size());
						node.getOverFlowArray().add(node.getDataList().get(randPosition));
						node.getDataList().remove(randPosition);
					}
				}else {
					for (int i = 0; i < noise; i++) {
						// generate dummies
						Data data = Data.generateDummy(sourceType);
						// link dummy to leaf node
						data.setLeafNode(node);
						// add randomly a dummy to data set
						// random value is between [0, Size(dataSet)]
						randPosition = (int)(Math.random() * dataSet.size());
						dataSet.add(randPosition, data);
					}
				}
				if (node.getOverFlowArray().size() < BigLog.getInstance().getOverFlowArraySize()) {
					int remainder = BigLog.getInstance().getOverFlowArraySize() 
							- node.getOverFlowArray().size();
					Data data = null;
					for (int i = 0; i < remainder; i++) {
						// generate a dummy
						data = Data.generateDummy(sourceType);
						node.getOverFlowArray().add(data);
					}
				}
				// random over flow array of the leaf
				Collections.shuffle(node.getOverFlowArray());
			}
			node.setCount(node.getCount() + noise);
			for(int i = 0; i < node.getChildren().size(); i++) {
				queue.add(node.getChildren().get(i));
			}
		}
	}

	/**
	 * replace old records with new ones (or dummy ones), including overflow arrays
	 * @param dataSet
	 * @param leafNodes
	 */
	public static void updateDataSetLeafNodes(List<Data> dataSet, List<Node> leafNodes, int batchNum) {
		float indexedAttribute = 0;
		if (dataSet == null) {
			return;
		}
		// update all leaf data with dummy item
		MappingItem mappingItem;
		List<BigInteger> listOfBuckIds;
		Data d;
		// set all real records to zero before updating them
		for (Node leaf : leafNodes) {
			for (int i = 0; i < leaf.getDataList().size(); i++) {
				d = leaf.getDataList().get(i);
				indexedAttribute = d.getIndexedAttribute();
				if (indexedAttribute > 0) {
					// generate dummies with indexedAttributes set to -1
					mappingItem = new MappingItem(indexedAttribute);
					listOfBuckIds = new ArrayList<>();
					for (int k = 0; k < batchNum; k++) {
						listOfBuckIds.add(BigInteger.valueOf(0));
					}
					mappingItem.setListOfItems(listOfBuckIds);
					leaf.getDataList().set(i, mappingItem);
				}
			}
		}
		
		// first clear old overflow array
		for (Node leaf : leafNodes) {
			leaf.getOverFlowArray().clear();
		}
		
		// 1. find a right leaf node 
		// 2. find a the right batch and assign it to that node
		// 3. if found, add it to the data set, otherwise add it to overflow array
		Data data2;
		for (Data data : dataSet) {
			indexedAttribute = data.getIndexedAttribute();
			for (Node leaf : leafNodes) {
				if (indexedAttribute >= leaf.getLowerBound() &&
						indexedAttribute <= leaf.getUpperBound()) {
					// leaf is found
					boolean isFound = false;
					for (int j = 0; j < leaf.getDataList().size(); j++) {
						data2 = leaf.getDataList().get(j);
						if (data2.getIndexedAttribute() == data.getIndexedAttribute()) {
							// replace the old one by the new one
							leaf.getDataList().remove(j);
							data.setLeafNode(leaf);
							isFound = true;
							break;
						}
					}
					// otherwise, add it to the overflow array
					if (!isFound) {
						leaf.getOverFlowArray().add(data);
					}
				}
			}
		}
		// merge all removed records together
		BigInteger bigInteger;
		for (Node leaf : leafNodes) {
			// TODO merge all removed items of the same leaf
			List<BigInteger> listOfBucIds = new ArrayList<>();
			if (leaf.getOverFlowArray().size() > 1) {
				for (int i = 0; i < batchNum; i++) {
					listOfBucIds.add(BigInteger.valueOf(0));
					for (Data data : leaf.getOverFlowArray()) {
						bigInteger = listOfBucIds.get(i).add(data.getListOfItems().get(i));
						listOfBucIds.set(i, bigInteger);
					}
				}
				leaf.overflowArray.clear();
				d = new Data();
				d.setListOfItems(listOfBucIds);
				leaf.overflowArray.add(d);
			}
			// padding removed items with zero size
			if (leaf.getOverFlowArray().size() == 0) {
				for (int i = 0; i < batchNum; i++) {
					listOfBucIds.add(BigInteger.valueOf(0));
				}
				d = new Data();
				d.setListOfItems(listOfBucIds);
				leaf.overflowArray.add(d);
			}
		}
		// regenerate dummy records: not necessary since they are zero
	}
	
	/**
	 * Assign a dataset (particularly data distribution) to leaves 
	 * @param map
	 * @param leafNodes
	 */
	public static void assignDataSetToLeafNodes(Map<Float, Integer> map, List<Node> leafNodes) {
		float indexedAttribute = 0;
		int leafNodeSetSize = 0;
		if (map == null) {
			return;
		}
		leafNodeSetSize = leafNodes.size();
		//System.out.println("size = " + leafNodeSetSize);
		// find a suitable leaf node and assign it to that node
		// add it to the data set
		for (Map.Entry<Float, Integer> entry : map.entrySet()) {
			indexedAttribute = entry.getKey();
			for (int j = 0; j < leafNodeSetSize; j++) {
				if (indexedAttribute >= leafNodes.get(j).getLowerBound() &&
						indexedAttribute <= leafNodes.get(j).getUpperBound()) {
					// increase count
					leafNodes.get(j).setCount(leafNodes.get(j).getCount() + entry.getValue());
					leafNodes.get(j).setRealDataSize((int) leafNodes.get(j).getCount());
					leafNodes.get(j).setOverflowArraySize(0);
					break;
				}
			}
		}
	}
	
	public static void assignDataSetToLeafNodes(List<Data> dataSet, List<Node> leafNodes) {
		float indexedAttribute = 0;
		int dataSetSize = 0;
		int leafNodeSetSize = 0;
		Data data = null;
		if (dataSet == null) {
			return;
		}
		dataSetSize = dataSet.size();
		leafNodeSetSize = leafNodes.size();
		// find a suitable leaf node and assign it to that node
		// add it to the data set
		int j, i;
		for(i = 0; i < dataSetSize; i++) {
			data = dataSet.get(i);
			indexedAttribute = data.getIndexedAttribute();
			//System.out.println(indexedAttribute);
			for (j = 0; j < leafNodeSetSize; j++) {
				if (indexedAttribute >= leafNodes.get(j).getLowerBound() &&
						indexedAttribute <= leafNodes.get(j).getUpperBound()) {
					data.setLeafNode(leafNodes.get(j));
					// increase count
					leafNodes.get(j).setCount(leafNodes.get(j).getCount() + 1);
					break;
				}
			}
		}
	}
	
	/*
	 * get height of a balance tree
	 * */
	public int getHeight() {
		int height = 0;
		Node node = this;
		while (node != null) {
			height ++;
			if (node.getChildren().size() != 0) {
				node = node.getChildren().get(0);
			}else {
				node = null;
			}
		}
		return height - 1;
	}
	
	public int getLevel() {
		int height = 0;
		Node node = this;
		while (node != null) {
			height ++;
			if (node.getChildren().size() != 0) {
				node = node.getChildren().get(0);
			}else {
				node = null;
			}
		}
		return height;
	}
	
	
	/**
	 * Get a list of leaf node (level-order). 
	 * This means leaf nodes are arranged in order from left to right
	 * @return
	 */
	public List<Node> getLeafNodes(){
		List<Node> leaves = new ArrayList<>();
		Queue<Node> queue = new LinkedList<>();
		Node index = this;
		if (index != null) {
			queue.add(index);
		}
		Node n;
		while (queue.isEmpty() == false) {
			n = queue.poll();
			// leaf node has no children
			if (n.children.size() == 0) {
				leaves.add(n);
			}
			// System.out.println(n.children);
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
		return leaves;
	}
	
	public static void levelOrder(Node node){
		Queue<Node> queue = new LinkedList<>();
		if (node != null) {
			queue.add(node);
		}
		long parentId = -1;
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			if (n.getParent() != null) {
				parentId = n.getParent().getId();
			}else {
				parentId = -1;
			}
			System.out.println(n.getId() + ";" + n.lowerBound + ";" + n.upperBound + ";" 
					+ n.getCount() + ";" + parentId);
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
	}
	
	public static void writeIndexToFile(Node node, String fileName) throws IOException {
		Queue<Node> queue = new LinkedList<>();
		if (node != null) {
			queue.add(node);
		}
		long parentId = -1;
		FileWriter fileWriter = 
                new FileWriter(fileName);
		BufferedWriter bufferedWriter = 
                new BufferedWriter(fileWriter);
		String str = null;
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			if (n.getParent() != null) {
				parentId = n.getParent().getId();
			}else {
				parentId = -1;
			}
			// Format: nodeId;lowerBound;upperBound;count;parentLineNumber
			str = n.getId() + ";" + n.lowerBound + ";" + n.upperBound + ";" 
					+ n.getCount() + ";" + parentId;
			bufferedWriter.write(str);
			bufferedWriter.newLine();
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
		
		if (bufferedWriter != null) {
			bufferedWriter.close();
		}
		if (fileWriter != null) {
			fileWriter.close();
		}
	}
	
	public static void readIndexFromFile(Node index, List<Log> dataSet, 
						List<Log> overFlowArray, String fileName) throws IOException {
		FileReader fileReader = 
                new FileReader(fileName);
		BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
		String line = null;
		while((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
		bufferedReader.close(); 
		
	}
	
	public Node(int id, float lowerBound, float upperBound, int count) {
		this.children = new ArrayList<>();
		this.nodeId = id;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.count = count;
	}
	
	public static List<Node> buildLeafNodes(int domainMin, int domainMax,
			int rangeInterval, float boundDeviation) {
		// create n nodes over the given domain
		int nodeId = 0;
		Node node = null;
		// generate leaves
		List<Node> leafNodes = new ArrayList<>();
		float lowerBound = domainMin;
		float upperBound = 0.0f;
		while (true) {
			upperBound =  lowerBound + (float) rangeInterval - boundDeviation;
			// special case: if the left of domain < RANGE_INTERVAL
			// 				create an extra node for the left
			node = new Node(nodeId, lowerBound, upperBound, 0);
			node.setDataList(new ArrayList<>());
			node.setOverflowArray(new ArrayList<>());
			leafNodes.add(node);
			nodeId++;
			if ((upperBound + boundDeviation) >= domainMax) {
				break;
			}
			lowerBound += rangeInterval;
		}
		// the final leaf's bucket without boundDeviation
		leafNodes.get(leafNodes.size() - 1).setUpperBound(domainMax);
		return leafNodes;
	}
	
	public static Node buildIndexLayOut(int branchingFactor) {
		// build index
		List<Node> leafNodes = Node.buildLeafNodes(Data.getDomainMin(sourceType), 
				Data.getDomainMax(sourceType), rangeInterval, 
				Constants.DataSource.BOUND_DEVIATION);
		Node indexLayout = Node.buildIndex(leafNodes, branchingFactor);
		return indexLayout;
	}
	
	public static Node buildIndex(List<Node> leafNodes, int branchingFactor) {
		int nodeId = leafNodes.size();
		// create internal nodes and root from leafNodes
		// combine ranges and counts of the children
		while (leafNodes.size() > 1) {
			List<Node> newListNode = new ArrayList<>();
			Node newNode = null;
			Node leaf = null;
			// loop list of current nodes and assign a node to its parent
			for (int i = 0; i < leafNodes.size(); i++) {
				leaf = leafNodes.get(i);
				// create a new parent when start a new group
				if (i % branchingFactor == 0) {
					newNode = new Node(nodeId, leaf.getLowerBound(), -1, 0);
					newListNode.add(newNode);
					nodeId++;
				}
				newNode.addChild(leaf);
				newNode.setUpperBound(leaf.getUpperBound());
				newNode.setCount(newNode.getCount() + leaf.getCount());
			}
			// update leafNodes by its parents, recur to the root
			leafNodes = newListNode;
		}
		Node root = leafNodes.get(0);
		root.setParent(null);
		// order the id of index's nodes
		regenerateNodeId(root);
		return root;
	}
	
	// regenerate id for index's nodes, that is used for quickly rebuilding tree
	// this method is similar to levelOrder
	public static void regenerateNodeId(Node node){
		Queue<Node> queue = new LinkedList<>();
		int id = 0;
		if (node != null) {
			queue.add(node);
		}
		while (queue.isEmpty() == false) {
			Node n = queue.poll();
			n.setNodeId(id);
			id++;
			for(int i = 0; i < n.getChildren().size(); i++) {
				queue.add(n.getChildren().get(i));
			}
		}
	}
	
	public static void preOrder(Node node) {
		if (node != null) {
			// System.out.println(node.getId()+"-["+node.lowerBound+","+node.upperBound+"] = " + node.getCount());
			if (node.getChildren() != null) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					Node n = node.getChildren().get(i);
					preOrder(n);
				}
			}
		}
	}
	
	/**
	 * Reconstruct index from meta and data file, data (data set + overflow arrays) 
	 * is stored into setEncryptedText property
	 * @param metaFileName
	 * @param dataFileName
	 * @return the root of the index
	 */
	public static Node loadIndexWithData(String metaFileName, String dataFileName) {
		
		try {
			RandomAccessFile metaFile = new RandomAccessFile(metaFileName, "r");
			RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "r");
			String index = metaFile.readLine();
			String arr[] = index.split(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			String[] nodeFields;
			Node node = null;
			List <Node> nodeList = new ArrayList<>();
			// build the index from file, assume the file written by orderLevel method
			for (String nodeStr : arr) {
				node = new Node();
				nodeFields = nodeStr.split(Constants.FileConfig.FILE_FIELD_SEPARATOR);
				node.setNodeId(Integer.parseInt(nodeFields[0]));
				node.setLowerBound(Float.parseFloat(nodeFields[1]));
				node.setUpperBound(Float.parseFloat(nodeFields[2]));
				node.setCount(Float.parseFloat(nodeFields[3]));
				if (nodeFields[4].compareTo("-1") != 0) {
					// not root, parent at the line represented by the 5-th field
					node.setParent(nodeList.get(Integer.parseInt(nodeFields[4])));
				}else {
					// root, parent is null
					node.setParent(null);
				}
				nodeList.add(node);
			}
			Node root = nodeList.get(0);
			List<Node> leafNodes = root.getLeafNodes();
			String recordFields[];
			String parts[];
			String records[];
			byte b[];
			int i = 0;
			Node leaf;
			String line = metaFile.readLine();
			while(line != null) {
				parts = line.split(Constants.ComPINED.DATA_SEPARATOR_3);
				records = parts[1].split(Constants.ComPINED.DATA_SEPARATOR_2);
				leaf = leafNodes.get(i);
				leaf.initiateDataList();
				i++;
				for (String record : records) {
					recordFields = record.split(Constants.ComPINED.DATA_SEPARATOR_1);
					dataFile.seek(Long.parseLong(recordFields[0]));
					b = new byte[Integer.parseInt(recordFields[1]) - 1];
					dataFile.read(b);
					//Data.parseData(new String(b), sourceType);
					Data data = new Data();
					data.setEncryptedText(new String(b));
					//System.out.print(new String(b));
					leaf.getDataList().add(data);
				}
				line = metaFile.readLine();
			}
			dataFile.close();
			metaFile.close();
			return root;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reconstruct index from meta and data file (without data), but included overflow array size
	 * @param metaFileName
	 * @return the root of the index
	 */
	public static Node loadIndexWithoutData2(String metaFileName) {
		try {
			RandomAccessFile metaFile = new RandomAccessFile(metaFileName, "r");
			String index = metaFile.readLine();
			String arr[] = index.split(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			String[] nodeFields;
			Node node = null;
			List <Node> nodeList = new ArrayList<>();
			// build the index from file, assume the file written by orderLevel method
			for (String nodeStr : arr) {
				node = new Node();
				nodeFields = nodeStr.split(Constants.FileConfig.FILE_FIELD_SEPARATOR);
				node.setNodeId(Integer.parseInt(nodeFields[0]));
				node.setLowerBound(Float.parseFloat(nodeFields[1]));
				node.setUpperBound(Float.parseFloat(nodeFields[2]));
				node.setCount(Float.parseFloat(nodeFields[3]));
				if (nodeFields[4].compareTo("-1") != 0) {
					// not root, parent at the line represented by the 5-th field
					node.setParent(nodeList.get(Integer.parseInt(nodeFields[4])));
				}else {
					// root, parent is null
					node.setParent(null);
				}
				int overflowArraySize = Integer.parseInt(nodeFields[5]);
				node.overflowArraySize = overflowArraySize;
				/*
				if (overflowArraySize > 0) {
					List<Data> overflowArray = new ArrayList<>();
					for (int i = 0; i < overflowArraySize; i++) {
						overflowArray.add(new Data());
					}
					node.setOverflowArray(overflowArray);
				}
				*/
				nodeList.add(node);
			}
			Node root = nodeList.get(0);
			
			metaFile.close();
			return root;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getOverflowArraySize() {
		return overflowArraySize;
	}

	public void setOverflowArraySize(int overflowArraySize) {
		this.overflowArraySize = overflowArraySize;
	}

	/**
	 * Reconstruct index from meta and data file (without data)
	 * @param metaFileName
	 * @return the root of the index
	 */
	public static Node loadIndexWithoutData(String metaFileName) {
		
		try {
			RandomAccessFile metaFile = new RandomAccessFile(metaFileName, "r");
			String index = metaFile.readLine();
			String arr[] = index.split(Com.INDEX_TEMPLATE_CONTENT_SEPERATOR);
			String[] nodeFields;
			Node node = null;
			List <Node> nodeList = new ArrayList<>();
			// build the index from file, assume the file written by orderLevel method
			for (String nodeStr : arr) {
				node = new Node();
				nodeFields = nodeStr.split(Constants.FileConfig.FILE_FIELD_SEPARATOR);
				node.setNodeId(Integer.parseInt(nodeFields[0]));
				node.setLowerBound(Float.parseFloat(nodeFields[1]));
				node.setUpperBound(Float.parseFloat(nodeFields[2]));
				node.setCount(Float.parseFloat(nodeFields[3]));
				if (nodeFields[4].compareTo("-1") != 0) {
					// not root, parent at the line represented by the 5-th field
					node.setParent(nodeList.get(Integer.parseInt(nodeFields[4])));
				}else {
					// root, parent is null
					node.setParent(null);
				}
				nodeList.add(node);
			}
			Node root = nodeList.get(0);
			
			metaFile.close();
			return root;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	/**
	 * Add a new child to this node, and create pointer from this child to the parent
	 * @param child
	 */
	public void addChild(Node child) {
		children.add(child);
		child.parent = this;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	public List<Node> getChildren(){
		return this.children;
	}
	
	public float getCount() {
		return this.count;
	}
	
	public void setCount(float count) {
		this.count = count;
	}
	
	public long getId() {
		return this.nodeId;
	}
	
	public float getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(float lowerBound) {
		this.lowerBound = lowerBound;
	}

	public float getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(float upperBound) {
		this.upperBound = upperBound;
	}
	
	public List<Data> getDataList() {
		return dataList;
	}

	public void setDataList(List<Data> dataList) {
		this.dataList = dataList;
	}
	
	public List<Data> getOverFlowArray() {
		return overflowArray;
	}

	public void setOverflowArray(List<Data> overflowArray) {
		this.overflowArray = overflowArray;
	}
	
	public int getRealDataSize() {
		return realDataSize;
	}

	public void setRealDataSize(int realDataSize) {
		this.realDataSize = realDataSize;
	}

	public int getDummyDataSize() {
		return dummyDataSize;
	}

	public void setDummyDataSize(int dummyDataSize) {
		this.dummyDataSize = dummyDataSize;
	}


}