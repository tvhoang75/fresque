package main;

import java.util.List;

import base.AppProperties;
import base.Constants;
import base.Node;
import data.Data;

public class IndexTemplate {
	//private Node template;
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	private static int rangeInterval = Integer.parseInt(AppProperties.getInstance()
			.getProperties().getProperty("range.interval"));
    private static float totalPrivacyBudget = Float.parseFloat(AppProperties.getInstance().
			getProperties().getProperty("privacy.budget.total"));
    private static float privacySensitivity = Float.parseFloat(AppProperties.getInstance().
 			getProperties().getProperty("privacy.laplace.sensitivity"));
    private static int branchingFactor = Integer.parseInt(AppProperties.getInstance()
			.getProperties().get("branching.factor").toString());

	public static void main(String[] args) 
	{
		// Node index = IndexTemplate.buildIndexTemplate();
	}
	
	public static Node buildIndexTemplate() {
		// build index
		List<Node> leafNodes = Node.buildLeafNodes(Data.getDomainMin(sourceType), 
				Data.getDomainMax(sourceType), 
				rangeInterval, 
				Constants.DataSource.BOUND_DEVIATION);
		//Node.buildOverFlowArray(leafNodes, Constants.Laplace.OVERFLOW_ARRAY_SIZE);
		Node indexTemplate = Node.buildIndex(leafNodes, branchingFactor);
		Node.perturbIndex(indexTemplate, privacySensitivity, totalPrivacyBudget);
		return indexTemplate;
	}
	
	public static Node buildIndexTemplateWithoutNoise() {
		// build index
		List<Node> leafNodes = Node.buildLeafNodes(Data.getDomainMin(sourceType), 
				Data.getDomainMax(sourceType), 
				rangeInterval, 
				Constants.DataSource.BOUND_DEVIATION);
		Node indexTemplate = Node.buildIndex(leafNodes, branchingFactor);
		return indexTemplate;
	}
}