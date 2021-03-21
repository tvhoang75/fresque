package data.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.String;
import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

@SuppressWarnings("serial")
public class Electricity extends Data implements Serializable {
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
			getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));
	
	//private long id;
	private int appartmentId;
	private String timestamp;
	private float power;
	// Used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;
	
	@Override
	public String getClassName() {
		return Electricity.class.toString();
	}
	
	public static void main(String[] args) throws IOException {
		Process p = Runtime.getRuntime().exec("python /Users/hoang/Documents/python/zipf.py");
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String parts[];
		RandomAccessFile file;
		RandomAccessFile fileSkew = new RandomAccessFile(
				"/Users/hoang/Documents/data/apartment/apt2016_100k","rw");
		while ((line = in.readLine()) != null) {
			//System.out.println(line);
			// parts[0]: id, parts[1]: size
			parts = line.split(",");
			file = new RandomAccessFile("/Users/hoang/Documents/data/apartment/2016/Apt" + parts[0] + "_2016.csv","r");
			int size = Integer.parseInt(parts[1]);
			for (int j = 0; j < size; j++) {
				fileSkew.write((parts[0] + "," + file.readLine() + "\n").getBytes());
			}
			file.close();
		}
		fileSkew.close();
		p.destroy();
		
		
	}
	
	public Electricity() {
		this.id = 0;
		this.indexedAttribute = Constants.DataSource.Electricity.DOMAIN_MIN;
		this.leafNode = null;
		this.appartmentId = 0;
		this.timestamp = "";
		this.power = 0;
	}
	
	public Electricity(int appartmentId, String timestamp, float power) {
		this.timestamp = timestamp;
		this.power = power;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.appartmentId, 
					Constants.DataSource.Electricity.DOMAIN_MIN, 
					Constants.DataSource.Electricity.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.appartmentId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." 
									+ DataSource.PRECISION_LEVEL + "f", this.indexedAttribute));
		this.leafNode = null;
	}
	
	public Electricity(long id, int appartmentId, String timestamp, float power) {
		this.id = id;
		this.timestamp = timestamp;
		this.power = power;
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.appartmentId, 
					Constants.DataSource.Electricity.DOMAIN_MIN, 
					Constants.DataSource.Electricity.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.appartmentId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public Electricity(long id, String line) {
		String [] parts = line.split(",");
		this.id = id;
		this.appartmentId = Integer.parseInt(parts[0]);
		this.timestamp = parts[1];
		this.power = Float.parseFloat(parts[2]);
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.appartmentId, 
					Constants.DataSource.Electricity.DOMAIN_MIN, 
					Constants.DataSource.Electricity.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.appartmentId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	public static Electricity parseFromString(String line) {
		// data format: appartmentId;time;power
		String [] parts = line.split(Constants.REGEX_STRING);
		try {
			return new Electricity(Integer.parseInt(parts[0]), parts[1], Float.parseFloat(parts[2]));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Electricity(String line) {
		String [] parts = line.split(",");
		this.id = 0;
		this.appartmentId = Integer.parseInt(parts[0]);
		this.timestamp = parts[1];
		this.power = Float.parseFloat(parts[2]);
		// normalize this attribute
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.appartmentId, 
					Constants.DataSource.Electricity.DOMAIN_MIN, 
					Constants.DataSource.Electricity.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = this.appartmentId;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.leafNode = null;
	}
	
	@Override 
	public String toString() {
		String str = Comlib.zeroPadding(appartmentId + "", Constants.DataSource.Electricity.ID_LEN) 
				+ Constants.REGEX_STRING 
				+ timestamp + Constants.REGEX_STRING
				+ Comlib.round(power, 2);
		return str;
	}
	
	public String toRawString() {
		return String.format("%s" + "," + "%s" + "," + "%s",
				appartmentId, timestamp, power);
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