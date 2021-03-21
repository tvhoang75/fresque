package data.type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import base.Constants.DataSource;
import data.Data;

@SuppressWarnings("serial")
public class Log extends Data implements Serializable {
	
	private static final Logger logger = Logger.getLogger("Log");
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
							getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));

	private long id;
	private String client;
	private String timestamp;
	private String protocol;
	private String httpReplyCode;
	private float replyBytes;
	// Used to pointer to the leaf it belongs to
	private Node leafNode = null;
	private String encryptedText;

	@Override
	public String getClassName() {
		return Log.class.toString();
	}
	
	public Log() {
		this.id = 0;
		this.client = "";
		this.timestamp = "";
		this.protocol = "";
		this.httpReplyCode = "";
		this.replyBytes = 0;
	}
	
	public Log(long id, String client, String timestamp, 
			String protocol, String httpReplyCode, int replyBytes) {
		this.id = id;
		this.client = client;
		this.timestamp = timestamp;
		this.protocol = protocol;
		this.httpReplyCode = httpReplyCode;
		this.replyBytes = replyBytes;
	}
	
	public Log(String client, String timestamp, 
			String protocol, String httpReplyCode, float replyBytes) {
		this.client = client;
		this.timestamp = timestamp;
		this.protocol = protocol;
		this.httpReplyCode = httpReplyCode;
		// normalize this attribute
		if (normalized == true) {
			this.replyBytes = Comlib.normalizedValue(replyBytes, Constants.DataSource.Log.DOMAIN_MIN, 
					Constants.DataSource.Log.DOMAIN_MAX, newMin, newMax);
		}else {
			this.replyBytes = replyBytes;
		}
		// limit precision of float for comparison
		this.replyBytes = Float.parseFloat(String.format("%." + DataSource.PRECISION_LEVEL + "f", 
				this.replyBytes));
		this.indexedAttribute = this.replyBytes;
	}
	
	public static void main(String[] args) {
		System.out.println(Data.getDomainMin("log") + ":" + Data.getDomainMax("log"));
		
		BufferedReader br = null;
		FileReader fr = null;
		String line = "";
		List<Data> logList = new ArrayList<>();
		
		try {
			fr = new FileReader("/Users/hoang/Documents/data/nasa_access_log_Aug95");
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				logList.add(Data.parseData(line, "log"));
			}
			System.out.println("number of logs : " + logList.size());
			float max = 0;
			float min = Integer.MAX_VALUE;
			for (Data log : logList) {
				if (max < log.getIndexedAttribute()) {
					max = log.getIndexedAttribute();
				}
				if(log.getIndexedAttribute() < min) {
					min = log.getIndexedAttribute();
				}
			}
			System.out.println("min: " + min + "- max:" + max + "- range:" + (max-min));
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// Example Apache log line:
	// 127.0.0.1 - - [21/Jul/2014:9:55:27 -0800] "GET /home.html HTTP/1.1" 200 2048
	private static final String LOG_ENTRY_PATTERN =
			"(\\S+) - - \\[(\\S+ [+-]\\d{4})\\] \"(.+)\" (\\d+) (-?\\d*|-)";
			//"(\\S+) - - \\[(\\S+ [+-]\\d{4})\\] \"(.+)\" (\\d+) (\\d+|-)";
			//"(\\S+) - - \\[(\\S+) -(\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d+) (\\d+|-)";

	private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);

	public static Log parseFromLogString(String logString) {
		try {
			String [] parts = logString.split(Constants.REGEX_STRING);
			// parse timestamp to useful format
			SimpleDateFormat timeParse = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			Date date = timeParse.parse(parts[1]);
			SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			String formattedDate = timeFormat.format(date);
			return new Log(parts[0], formattedDate.toString(), 
					parts[2], parts[3], Integer.parseInt(parts[4]));
		}catch(ParseException pareseE) {
			logger.log(Level.ALL, "Cannot parse logline");
			return null;
		}
	}
	
	public static Log parseFromLogLine(String logLine) {
		try {
			Matcher m = PATTERN.matcher(logLine);
			if (!m.matches()) {
				logger.log(Level.ALL, "Cannot parse logline" + logLine);
				return null;
			}
			// parse timestamp to useful format
			SimpleDateFormat timeParse = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			Date date = timeParse.parse(m.group(2));
			SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			String formattedDate = timeFormat.format(date);
			float repBytes = 0;
			if (m.group(5).contains("-") && (m.group(5).trim().length() == 1)) {
				repBytes = 0;
			}else {
				// convert size (byte) to Kbyte by dividing the value by 1000
				repBytes = Integer.parseInt(m.group(5)) / 1000;
			}
			return new Log(m.group(1), formattedDate.toString(), 
					m.group(3), m.group(4), repBytes);
		}catch(ParseException pareseE) {
			logger.log(Level.ALL, "Cannot parse logline");
			return null;
		}
	}
	
	public static Log parseFromLogLineNASA(String logLine) {
		try {
			Matcher m = PATTERN.matcher(logLine);
			if (!m.matches()) {
				logger.log(Level.ALL, "Cannot parse logline" + logLine);
				return null;
			}
			// parse timestamp to useful format
			SimpleDateFormat timeParse = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			Date date = timeParse.parse(m.group(2));
			SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
			String formattedDate = timeFormat.format(date);
			int repBytes = 0;
			// ignore this group if it contains "-"
			if (!m.group(5).contains("-")) {
				repBytes = Integer.parseInt(m.group(5)) / 1000;
			}
			return new Log(m.group(1), formattedDate.toString(), 
					m.group(3), m.group(4), repBytes);
		}catch(ParseException pareseE) {
			return null;
		}
	}
	
	@Override public String toRawString() {
		return String.format("%s - - [%s] \"%s\" %s %s",
				client, timestamp, protocol, httpReplyCode, replyBytes);
	}
	
	// toString : 127.0.0.1 - - [21/Jul/2014:9:55:27 -0800] "GET /home.html HTTP/1.1" 200 2048
	@Override public String toString() {
		String str = client + Constants.REGEX_STRING 
				+ timestamp + Constants.REGEX_STRING
				+ protocol + Constants.REGEX_STRING 
				+ httpReplyCode + Constants.REGEX_STRING
				+ replyBytes;
		return str;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
  	public String getClient() {
  		return client;
	}
	
	public void setClient(String host) {
		this.client = host;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getHttpReplyCode() {
		return httpReplyCode;
	}
	
	public void setHttpReplyCode(String httpReplyCode) {
		this.httpReplyCode = httpReplyCode;
	}
	
	public float getReplyBytes() {
		return replyBytes;
	}
	
	public void setReplyBytes(int replyBytes) {
		this.replyBytes = replyBytes;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Node getLeafNode() {
		return leafNode;
	}
	
	public String getEncryptedText() {
		return encryptedText;
	}

	public void setEncryptedText(String encryptedText) {
		this.encryptedText = encryptedText;
	}
}