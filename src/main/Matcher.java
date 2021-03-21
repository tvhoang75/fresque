package main;

import java.util.List;
import org.json.JSONObject;

import base.Node;
import base.Constants.Com;

// this class represents matching table
public class Matcher {

	private JSONObject recordIdsJSON = null;
	private JSONObject overFlowIdsJSON = null;
	private JSONObject dummyIdsJSON = null;
	
	public static void main(String[] args) 
	{
		String str = "123@123";
		System.out.println(Matcher.lengthEntryJSON(str));
	}
	
	public static int lengthEntryJSON(String entry) {
		int length = 0;
		if (entry.length() == 0) {
			return 0;
		}
		length = entry.split(Com.MATCHER_DATA_SEPARATOR).length;
		return length;
	}

	// format (key : id1@id2@id3...)
	public void addRecordIdJSON(long leafNodeId, long recordId) {
		String key = leafNodeId + "";
		String newValue = "";
		String currentValue = recordIdsJSON.get(key).toString();
		if (currentValue.length() == 0) {
			newValue = recordId + "";
		}else {
			newValue = currentValue + Com.MATCHER_DATA_SEPARATOR + recordId;
		}
		recordIdsJSON.put(key, newValue);
	}
	
	public void addListRecordIdsJSON(long leafNodeId, String listRecordIds) {
		String key = leafNodeId + "";
		String newValue = "";
		if (listRecordIds.length() == 0) {
			return;
		}
		String currentValue = recordIdsJSON.get(key).toString();
		if (currentValue.length() == 0) {
			newValue = listRecordIds;
		}else {
			newValue = currentValue + Com.MATCHER_DATA_SEPARATOR + listRecordIds;
		}
		recordIdsJSON.put(key, newValue);
	}

	public void addOverFlowIdJSON(long leafNodeId, long recordId) {
		String key = leafNodeId + "";
		String newValue = "";
		String currentValue = overFlowIdsJSON.get(key).toString();
		if (currentValue.length() == 0) {
			newValue = recordId + "";
		}else {
			newValue = currentValue + Com.MATCHER_DATA_SEPARATOR + recordId;
		}
		overFlowIdsJSON.put(key, newValue);
	}
	
	public void addDummyIdJSON(long leafNodeId, long recordId) {
		String key = leafNodeId + "";
		String newValue = "";
		if (dummyIdsJSON.has(key) == true) {
			String currentValue = dummyIdsJSON.get(key).toString();
			if (currentValue.length() == 0) {
				newValue = recordId + "";
			}else {
				newValue = currentValue + Com.MATCHER_DATA_SEPARATOR + recordId;
			}
			dummyIdsJSON.put(key, newValue);
		}else {
			System.out.println("Not exist leaf node " + leafNodeId + " in matcher!");
		}
	}
	
	public Matcher() {
		recordIdsJSON = new JSONObject();
		overFlowIdsJSON = new JSONObject();
		dummyIdsJSON = new JSONObject();
	}
	
	public Matcher(List<Node> leafNodes) {
		long size = leafNodes.size();
		recordIdsJSON = new JSONObject();
		overFlowIdsJSON = new JSONObject();
		dummyIdsJSON = new JSONObject();
		for (int i = 0; i < size; i++) {
			recordIdsJSON.put(leafNodes.get(i).getId() + "", "");
			overFlowIdsJSON.put(leafNodes.get(i).getId() + "", "");
			dummyIdsJSON.put(leafNodes.get(i).getId() + "", "");
		}
	}
	
	public void resetRecordIdsJSON() {
		for (Object key : recordIdsJSON.keySet()) {
			String leafId = (String) key;
			this.recordIdsJSON.put(leafId, "");
		}
	}
	
	public void copyRecordIdsJSON(JSONObject recordIdsJSON) {
		for (Object key : recordIdsJSON.keySet()) {
			String leafId = (String) key;
			this.recordIdsJSON.put(leafId, recordIdsJSON.get(leafId));
		}
	}
	
	public void copyOverFlowIdsJSON(JSONObject overFlowIdsJSON) {
		for (Object key : overFlowIdsJSON.keySet()) {
			String leafId = (String) key;
			this.overFlowIdsJSON.put(leafId, overFlowIdsJSON.get(leafId));
		}
	}
	
	public JSONObject recordIdsJSON() {
		return recordIdsJSON;
	}
	
	public JSONObject overFlowIdsJSON() {
		return overFlowIdsJSON;
	}
	
	public JSONObject dummyIdsJSON() {
		return overFlowIdsJSON;
	}
}
