package data.type;

import java.io.Serializable;
import java.lang.String;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

@SuppressWarnings("serial")
public class Gowalla extends Data implements Serializable {
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
			getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));
	// convert time to hourly interval by divide value with 3600000
	// convert time to day interval by divide value with 86400000
	private static final int scaling = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.gowalla.scaling"));
	//private static final int scaling = 3600000;
	
	//private long id;
	private float checkInTime;
	private String latitude;
	private String longitude;
	private String locationId;
	// Used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;
	
	@Override 
	public String toRawString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (checkInTime < 0) {
			return String.format("%s" + "\t" + "%s" + "\t" + "%s" + "\t" + "%s" + "\t" + "%s",
					id, checkInTime, latitude, longitude, locationId);
		}else {
			return String.format("%s" + "\t" + "%s" + "\t" + "%s" + "\t" + "%s" + "\t" + "%s",
					id, simpleDateFormat.format(checkInTime * scaling), latitude, longitude, locationId);
		}
	}
	
	@Override
	public String getClassName() {
		return Gowalla.class.toString();
	}
	
	public Gowalla() {
		this.id = 0;
		this.indexedAttribute = 0;
		this.leafNode = null;
		this.checkInTime = 0;
		this.latitude = "";
		this.longitude = "";
		this.locationId = "";
	}
	
	public Gowalla(int checkInTime, String latitude, 
			String longitude, String locationId) {
		// normalize this attribute
		this.checkInTime = checkInTime;
		if (normalized == true) {
			this.indexedAttribute = Comlib.normalizedValue(this.checkInTime, Constants.DataSource.Gowalla.DOMAIN_MIN, 
					Constants.DataSource.Gowalla.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = checkInTime;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationId = locationId;
		this.leafNode = null;
	}
	
	public Gowalla(long id, int checkInTime, String latitude, 
			String longitude, String locationId) {
		this.id = id;
		// normalize this attribute
		this.checkInTime = checkInTime;
		if (normalized == true) {
			this.indexedAttribute = 
					Comlib.normalizedValue(this.checkInTime, Constants.DataSource.Gowalla.DOMAIN_MIN, 
					Constants.DataSource.Gowalla.DOMAIN_MAX, newMin, newMax);
		}else {
			this.indexedAttribute = checkInTime;
		}
		// limit precision of float for comparison
		this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.indexedAttribute));
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationId = locationId;
		this.leafNode = null;
	}
	
	public Gowalla(long id, String line) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String [] parts = line.split("\t");
		try {
			this.id = id;
			// convert time to hourly interval by divide value with 3600000
			// convert time to day interval by divide value with 86400000
			this.checkInTime = (int) (simpleDateFormat.parse(parts[1]).getTime() / scaling);
			// normalize this attribute
			if (normalized == true) {
				this.indexedAttribute = Comlib.normalizedValue(this.checkInTime, Constants.DataSource.Gowalla.DOMAIN_MIN, 
						Constants.DataSource.Gowalla.DOMAIN_MAX, newMin, newMax);
			}else {
				this.indexedAttribute = this.checkInTime;
			}
			// limit precision of float for comparison
			this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
					this.indexedAttribute));
			this.latitude = parts[2];
			this.longitude = parts[3];
			this.locationId = parts[4];
			this.leafNode = null;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static Gowalla parseFromString(String line) {
		String [] parts = line.split(Constants.REGEX_STRING);
		try {
			return new Gowalla(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Gowalla(String line) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String [] parts = line.split("\t");
		try {
			this.id = Long.parseLong(parts[0]);
			// convert time to hour interval by divide value with 3600000
			// convert time to day interval by divide value with 86400000
			if (Comlib.isNumeric(parts[1].trim()) == true) {
				this.checkInTime = Float.parseFloat(parts[1]);
			}else {
				this.checkInTime = (int) (simpleDateFormat.parse(parts[1]).getTime() / scaling);
			}
			this.latitude = parts[2];
			this.longitude = parts[3];
			this.locationId = parts[4];
			// normalize this attribute
			if (normalized == true) {
				this.indexedAttribute = Comlib.normalizedValue(this.checkInTime, Constants.DataSource.Gowalla.DOMAIN_MIN, 
						Constants.DataSource.Gowalla.DOMAIN_MAX, newMin, newMax);
			}else {
				this.indexedAttribute = this.checkInTime;
			}
			// limit precision of float for comparison
			this.indexedAttribute = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
					this.indexedAttribute));
			
			this.leafNode = null;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override 
	public String toString() {
		String str = (checkInTime * scaling) + Constants.REGEX_STRING 
				+ latitude + Constants.REGEX_STRING
				+ longitude + Constants.REGEX_STRING 
				+ locationId;
		return str;
//		return String.format("%s\t%s\t%s\t%s\t%s",
//				0, checkInTime, latitude, longitude, locationId);
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
	public float getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(float checkInTime) {
		this.checkInTime = checkInTime;
	}
}