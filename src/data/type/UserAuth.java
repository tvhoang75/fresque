package data.type;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

@SuppressWarnings("serial")
public class UserAuth extends Data implements Serializable {
	private static final boolean normalized = Boolean.parseBoolean(AppProperties
			.getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties
			.getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties
			.getInstance().getProperties().getProperty("data.domain.max"));
	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	
	// time,user,computer
	private long time;
	private int user;
	private String computer;
	// Used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;
	
	@Override
	public String getClassName() {
		return UserAuth.class.toString();
	}
	
	public static void main(String[] args) throws Exception {
		String rootFolder = "/Users/hoang/Documents/data/LANL/";
		RandomAccessFile file = new RandomAccessFile(rootFolder + "auth2","r");
		//RandomAccessFile out = new RandomAccessFile("/Users/hoang/Documents/data/LANL/auth2","rw");
		String line;
		int count = 0;
		Data data;
		float min = Integer.MAX_VALUE;
		float max = Integer.MIN_VALUE;
		int value;
		Map<Float, Integer> mapDistribution = new HashMap<Float, Integer>();
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = file.readLine()) != null) {
			count++;
			line = file.readLine();
			data = Data.parseData(line, sourceType);
			if (data.getIndexedAttribute() > max) {
				max = data.getIndexedAttribute();
			}
			if (data.getIndexedAttribute() < min) {
				min = data.getIndexedAttribute();
			}
			if (mapDistribution.get(data.getIndexedAttribute()) != null) {
				value = mapDistribution.get(data.getIndexedAttribute());
				mapDistribution.put(data.getIndexedAttribute(), value + 1);
			}else {
				mapDistribution.put(data.getIndexedAttribute(), 1);
			}
			if (count == 10000) {
				//break;
			}
		}
		System.out.println("=> size = " + count);
		System.out.println("=> min = " + min);
		System.out.println("=> max = " + max);
		for (Map.Entry<Float, Integer> entry :  mapDistribution.entrySet()) {
			//System.out.println("key : " + entry.getKey() + ", value : " + entry.getValue());
			stringBuilder.append(Math.round(entry.getKey()));
			stringBuilder.append(",");
			stringBuilder.append(entry.getValue());
			stringBuilder.append("\n");
		}
		Comlib.appendFile(rootFolder + "auth_distribution.csv", stringBuilder.toString());
		file.close();
		//out.close();
	}
	
	public UserAuth() {
		this.id = 0;
		this.indexedAttribute = Constants.DataSource.Auth.DOMAIN_MIN;
		this.leafNode = null;
		this.user = 0;
		this.computer = "";
		this.time = 0;
	}
	
	public UserAuth(long time, int user, String computer) {
		this.computer = computer;
		this.time = time;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.user, 
					Constants.DataSource.Auth.DOMAIN_MIN, 
					Constants.DataSource.Auth.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.user;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." 
							+ DataSource.PRECISION_LEVEL + "f", this.indexedAttribute));
		this.leafNode = null;
	}
	
	public UserAuth(long id, long time, int user, String timestamp) {
		this.id = id;
		this.computer = timestamp;
		this.time = time;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.user, 
					Constants.DataSource.Auth.DOMAIN_MIN, 
					Constants.DataSource.Auth.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.user;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public UserAuth(long id, String line) {
		String [] parts = line.split(",");
		this.id = id;
		this.user = Integer.parseInt(parts[1]);
		this.computer = parts[2];
		this.time = Long.parseLong(parts[0]);
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.user, 
					Constants.DataSource.Auth.DOMAIN_MIN, 
					Constants.DataSource.Auth.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.user;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public static UserAuth parseFromString(String line) {
		// data format: time;use;computer
		String [] parts = line.split(Constants.REGEX_STRING);
		try {
			return new UserAuth(Long.parseLong(parts[0]), 
					Integer.parseInt(parts[1]), parts[2]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public UserAuth(String line) {
		String [] parts = line.split(",");
		this.id = 0;
		this.user = Integer.parseInt(parts[1]);
		this.computer = parts[2];
		this.time = Long.parseLong(parts[0]);
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.user, 
					Constants.DataSource.Electricity.DOMAIN_MIN, 
					Constants.DataSource.Electricity.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.user;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	@Override 
	public String toString() {
		String str = time
				+ Constants.REGEX_STRING 
				+ user + Constants.REGEX_STRING
				+ computer;
		return str;
	}
	
	public String toRawString() {
		return String.format("%s" + "," + "%s" + "," + "%s",
				time, user, computer);
	}
	
	@Override
	public long getId() {
		return id;
	}
	@Override
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public Node getLeafNode() {
		return leafNode;
	}
	@Override
	public String getEncryptedText() {
		return encryptedText;
	}
	@Override
	public void setEncryptedText(String encryptedText) {
		this.encryptedText = encryptedText;
	}
}