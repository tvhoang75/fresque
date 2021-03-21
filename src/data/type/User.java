package data.type;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.String;

import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

@SuppressWarnings("serial")
public class User extends Data implements Serializable {
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
			getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));

	//private long id;
	private int userId;
	private String timestamp;
	private String value;
	// Used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;
	
	@Override
	public String getClassName() {
		return User.class.toString();
	}
	
	public static void main(String[] args) throws IOException {
		// maximum : 377346
		String dataFolder = "/Users/hoang/Documents/data/apartment/apt2016";
 		RandomAccessFile file = new RandomAccessFile(dataFolder, "r");
 		
 		//********* open database 
		Options options = new Options();
		options.createIfMissing(true);
		DB db = factory.open(new File("/Users/hoang/Documents/data/apartment/levelDB"), options);
		String line;
		int id = 0;
		int size = 144000;
		while (((line = file.readLine()) != null) && id < size) {
			//Comlib.zeroPadding(line, 24);
			Data data = Data.parseData(line, "electricity");
			line = data.toString();
			//System.out.println(line);
			db.put(bytes(Comlib.zeroPadding(id + "", 5) + ""), 
					line.substring(0, Math.min(line.length(), 24)).getBytes());
			id++;
		}
		
		db.close();
 		file.close();
	}
	
	public User() {
		this.id = 0;
		this.indexedAttribute = Constants.DataSource.User.DOMAIN_MIN;
		this.leafNode = null;
		this.userId = 0;
		this.timestamp = "";
		this.value = "";
	}
	
	public User(int userId, String timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.userId, 
					Constants.DataSource.User.DOMAIN_MIN, 
					Constants.DataSource.User.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.userId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." 
									+ DataSource.PRECISION_LEVEL + "f", this.indexedAttribute));
		this.leafNode = null;
	}
	
	public User(long id, int userId, String timestamp, String value) {
		this.id = id;
		this.timestamp = timestamp;
		this.value = value;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.userId, 
					Constants.DataSource.User.DOMAIN_MIN, 
					Constants.DataSource.User.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.userId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public User(long id, String line) {
		String [] parts = line.split(",", 3);
		this.id = id;
		this.userId = Integer.parseInt(parts[0]);
		this.timestamp = parts[1];
		this.value = parts[2];
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.userId, 
					Constants.DataSource.User.DOMAIN_MIN, 
					Constants.DataSource.User.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.userId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public static User parseFromString(String line) {
		// data format: appartmentId;time;power
		String [] parts = line.split(Constants.REGEX_STRING, 3);
		try {
			return new User(Integer.parseInt(parts[0]), parts[1], parts[2]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public User(String line) {
		String [] parts = line.split(",", 3);
		this.id = 0;
		this.userId = Integer.parseInt(parts[0]);
		this.timestamp = parts[1];
		this.value = parts[2];
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.userId, 
					Constants.DataSource.User.DOMAIN_MIN, 
					Constants.DataSource.User.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.userId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	@Override 
	public String toString() {
		String str = userId
				+ Constants.REGEX_STRING 
				+ timestamp + Constants.REGEX_STRING
				+ value;
		return Comlib.zeroPadding(str, Constants.DataSource.User.MAX_RECORD_SIZE);
	}
	
	public String toRawString() {
		return String.format("%s" + "," + "%s" + "," + "%s",
				userId, timestamp, value);
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