package data.type;

import java.io.Serializable;
import java.lang.String;
import java.text.NumberFormat;
import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

@SuppressWarnings("serial")
public class USPS extends Data implements Serializable {
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
			getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));
	
	//private long id;
	private float salary;
	private String name;
	private String occupation;
	private String grade;
	private String location;
	// used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;
	
	@Override public String toRawString() {
		return String.format("%s" + ";" + "%s" + ";" + "%s" + ";" + "$%s" + ";" + "%s",
				name, occupation, grade, salary, location);
	}
	
	@Override
	public String getClassName() {
		return USPS.class.toString();
	}
	
	public USPS() {
		this.id = 0;
		this.indexedAttribute = 0;
		this.leafNode = null;
		this.salary = 0;
		this.name = "";
		this.occupation = "";
		this.grade = "";
		this.location = "";
	}
	
	public USPS(String name, String occupation, String grade, int salary, String location) {
		this.name = name;
		this.occupation = occupation;
		this.grade = grade;
		// normalize this attribute
		if (normalized == true) {
			this.salary = Comlib.normalizedValue(salary, Constants.DataSource.USPS.DOMAIN_MIN, 
					Constants.DataSource.USPS.DOMAIN_MAX, newMin, newMax);
		}else {
			this.salary = salary;
		}
		// limit precision of float for comparison
		this.salary = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.salary));
		this.indexedAttribute = this.salary;
		this.location = location;
		this.leafNode = null;
	}
	
	public USPS(long id, String name, 
			String occupation, String grade, int salary, String location) {
		this.id = id;
		this.name = name;
		this.occupation = occupation;
		this.grade = grade;
		this.location = location;
		// normalize this attribute
		if (normalized == true) {
			this.salary = Comlib.normalizedValue(salary, Constants.DataSource.USPS.DOMAIN_MIN, 
					Constants.DataSource.USPS.DOMAIN_MAX, newMin, newMax);
		}else {
			this.salary = salary;
		}
		// limit precision of float for comparison
		this.salary = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.salary));
		this.indexedAttribute = this.salary;
		this.leafNode = null;
	}
	
	public USPS(long id, String line) {
		String [] parts = line.split(";");
		try {
			this.id = id;
			NumberFormat format = NumberFormat.getCurrencyInstance();
			this.name = parts[0];
			this.occupation = parts[1];
			this.grade = parts[2];
			Object value = format.parse(parts[3]);
			this.salary = (int) Float.parseFloat(value.toString());
			// normalize this attribute
			if (normalized == true) {
				this.indexedAttribute = Comlib.normalizedValue(this.salary, Constants.DataSource.USPS.DOMAIN_MIN, 
						Constants.DataSource.USPS.DOMAIN_MAX, newMin, newMax);
			}else {
				this.indexedAttribute = this.salary;
			}
			// limit precision of float for comparison
			this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
					this.indexedAttribute));
			this.location = parts[4];
			this.leafNode = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static USPS parseFromString(String line) {
		String [] parts = line.split(Constants.REGEX_STRING);
		try {
			return new USPS(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public USPS(String line) {
		String [] parts = line.split(";");
		try {
			this.id = 0;
			NumberFormat format = NumberFormat.getCurrencyInstance();
			this.name = parts[0];
			this.occupation = parts[1];
			this.grade = parts[2];
			Object value = format.parse(parts[3]);
			this.salary = (int) Float.parseFloat(value.toString());
			// normalize this attribute
			if (normalized == true) {
				this.indexedAttribute = Comlib.normalizedValue(this.salary, Constants.DataSource.USPS.DOMAIN_MIN, 
						Constants.DataSource.USPS.DOMAIN_MAX, newMin, newMax);
			}else {
				this.indexedAttribute = this.salary;
			}
			// limit precision of float for comparison
			this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
					this.indexedAttribute));
			this.location = parts[4];
			this.leafNode = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override 
	public String toString() {
		String str = name + Constants.REGEX_STRING 
				+ occupation + Constants.REGEX_STRING
				+ grade + Constants.REGEX_STRING 
				+ "$" + salary + Constants.REGEX_STRING
				+ location;
		return str;
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