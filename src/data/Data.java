package data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import base.AppProperties;
import base.Comlib;
import base.Constants;
import base.Node;
import data.type.Electricity;
import data.type.Gowalla;
import data.type.Log;
import data.type.USPS;
import data.type.User;
import data.type.UserAuth;
import main.crypto.AESEncryptor;

public class Data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected long id;
	protected float indexedAttribute;
	// used to pointer to the leaf it belongs to
	protected Node leafNode;
	protected String encryptedText;
	// used for storing clear ltuple
	protected StringBuilder clearText;
	// declare BigInteger for IoT index
	protected BigInteger mapping;
	protected List<BigInteger> listOfItems;

	private final static String sourceType = AppProperties.getInstance().getProperties()
			.getProperty("source.type");
	private static final boolean normalized = Boolean.parseBoolean(AppProperties.
			getInstance().getProperties().getProperty("data.normolized"));
	private static final int newMin = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.min"));
	private static final int newMax = Integer.parseInt(AppProperties.
			getInstance().getProperties().getProperty("data.domain.max"));
	public static void main(String[] args) throws Exception {
		System.out.println(Data.parseData("dummy;;;;-1", sourceType));
		
	}
	
	public String getClassName() {
		return Data.class.toString();
	}
	
	public Data() {
		this.id = 0;
		this.indexedAttribute = 0;
		this.leafNode = null;
		this.encryptedText = null;
		this.clearText = new StringBuilder();
	}
	
	public static int getDomainMin(String sourceType) {
		if (sourceType == null) {
			return 0;
		}
		int val = 0;
		if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
			val = Constants.DataSource.Log.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Log.DOMAIN_MIN, 
						Constants.DataSource.Log.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
			val = Constants.DataSource.Gowalla.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Gowalla.DOMAIN_MIN, 
						Constants.DataSource.Gowalla.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
			val = Constants.DataSource.USPS.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.USPS.DOMAIN_MIN, 
						Constants.DataSource.USPS.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
			val = Constants.DataSource.Electricity.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Electricity.DOMAIN_MIN, 
						Constants.DataSource.Electricity.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
			val = Constants.DataSource.Auth.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Auth.DOMAIN_MIN, 
						Constants.DataSource.Auth.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
			val = Constants.DataSource.User.DOMAIN_MIN;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.User.DOMAIN_MIN, 
						Constants.DataSource.User.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}
		else{
			System.out.println("Data.getDomainMin: Does not exist data source named: " + sourceType);
		}
		return 0;
	}
	
	public static int getDomainMax(String sourceType) {
		if (sourceType == null) {
			return 0;
		}
		int val = 0;
		if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
			val = Constants.DataSource.Log.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Log.DOMAIN_MIN, 
						Constants.DataSource.Log.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
			val = Constants.DataSource.Gowalla.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Gowalla.DOMAIN_MIN, 
						Constants.DataSource.Gowalla.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
			val = Constants.DataSource.USPS.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.USPS.DOMAIN_MIN, 
						Constants.DataSource.USPS.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
			val = Constants.DataSource.Electricity.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Electricity.DOMAIN_MIN, 
						Constants.DataSource.Electricity.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
			val = Constants.DataSource.Auth.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.Auth.DOMAIN_MIN, 
						Constants.DataSource.Auth.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
			val = Constants.DataSource.User.DOMAIN_MAX;
			if (normalized == true) {
				val = (int) Comlib.normalizedValue(val, Constants.DataSource.User.DOMAIN_MIN, 
						Constants.DataSource.User.DOMAIN_MAX, 
						newMin, newMax);
			}
			return val;
		}
		else {
			System.out.println("Data.getDomainMax: Does not exist data source named: " + sourceType);
		}
		return val;
	}
	
	public static Data parseData(String line, String sourceType) {
		if (sourceType == null || line == null) {
			System.out.println("Data.parseData: SourceType or input is null");
			return null;
		}
		if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
			return Log.parseFromLogLine(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
			return new Gowalla(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
			return new USPS(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
			return new Electricity(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
			return new UserAuth(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
			return new User(line);
		}
		else {
			System.out.println("Data.parseData: Does not exist source type named: " + sourceType);
		}
		return null;
	}
	
	// parse line coded by toString() function
	public static Data parseDataFromString(String line, String sourceType) {
		if (sourceType == null || line == null) {
			System.out.println("SourceType or input is null");
			return null;
		}
		if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
			return Log.parseFromLogString(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
			return Gowalla.parseFromString(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
			return USPS.parseFromString(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
			return Electricity.parseFromString(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
			return UserAuth.parseFromString(line);
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
			return User.parseFromString(line);
		}
		else {
			System.out.println("Does not exist source type named: " + sourceType);
		}
		return null;
	}
	
	
	public static Data generateDummy(String sourceType) {
		if (sourceType == null) {
			return null;
		}
		if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
			Log log = new Log("dummy", "01/Jul/1900:0:00:00 -0000", "resource", "0000", 
					Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			log.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return log;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
			Gowalla gowalla = new Gowalla("0\t-1\t00.0000000000\t-00.0000000000\t00000");
			gowalla.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return gowalla;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
			USPS usps = new USPS("noname", "0", "unknown", Constants.DataSource.USPS.DOMAIN_MIN,"dummy");
			usps.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return usps;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
			Electricity electricity = new Electricity(-1, "dummy00-00 00:00:00", -1);
			electricity.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return electricity;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
			UserAuth userAuth = new UserAuth(1111, 0, "dummy");
			userAuth.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return userAuth;
		}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
			User user = new User(1111, "dummy", "1.000811,0.002082,-0.001922,0.003575,0.999653,1.000928,1.002032,1.960286,6.684610,5.043892,0.000029,0.000084,0.000233,0.001885,0.429754,0.115731,0.163364,0.003077,-0.000170,-1.000802,0.002262,0.001760,0.002086,0.111225,0.488020,0.154312,0.002432,0.009142,0.027166,0.047330,0.001305,0.001823,0.002545,0.028810,6.016422,3.823204,3.596534,4.966467,4.875743,6.376642,5.772607,5.571801,0.023369,-0.000096,0.000025,0.000307,0.001488,0.001581,0.009201,-0.433114,0.032062,-0.168144,633.856464,0.303380,-0.346959,0.502758,633.712688,633.905475,634.046780,2.210081,6.684612,5.043399,0.000009,0.000018,0.000011,0.000045,0.429583,2.212941,0.101626,174.194298,102.963450,-600.690103,0.375410,0.396511,0.291072,-0.774734,-0.633681,0.687044,1.000000,0.999999,0.999999,0.999999,0.999998,995.369977,18.906504,-10.419867,29.284151,984.292639,992.290280,1008.000000,2.180543,5.926745,5.047303,0.002511,0.027416,0.021507,0.127325,0.433366,2.510848,0.123665,-7.104000,-10.346667,-992.960000,16.448623,53.149122,43.547442,-0.651385,-0.616377,0.883437,4.963815,4.013884,5.128257,4.536012,4.686713,5.105830,3.009070,4.579963,3.923300,4.887330,5.049121,0.002771,0.030358,0.024353,0.134255,0.997661,0.997595,0.997111,0.995289,0.963455,nan,nan,nan,nan,nan,nan,nan,nan,nan,7.000000,0.051085,0.042162,107.505554,108.744263,nan,nan,65.000000,10.000000,6.027438,1.796322,0.000016,0.000015,0.000031,-0.000022,0.000077,0.000093,-7.418426,2.016611,-1.061945,1.195417,-0.593168,0.041554,-0.588281,-0.620592,-0.289123,-0.104321,0.083421,0.198781,-0.299491,2.406004,1.396577,1.281903,0.800262,0.490133,0.495246,0.540785,0.569112,0.463722,0.515975,0.464771,0.436589,0.527716,-1.418762,1.418805,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,1.000000,0.000000,0.000000,nan,nan,nan,0.000000,nan,0.890000,0.427701,nan,0.000000,0.000000,1.000000,1.000000,0.000000,0.000000,0.000000,0.000000,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,0,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,-1");
			user.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			return user;
		}
		else {
			System.out.println("Data.generateDummy: Does not exist data source named: " + sourceType);
		}
		return null;
	}
	
	/**
	 * Generate a set of clear dummy tuples with default id (-1)
	 * @param numDummy
	 * @param key is used to encrypt dummy tuple
	 * @return
	 * @throws Exception
	 */
	public static List<Data> generateSetOfDummy(int numDummy, String sourceType) {
		List<Data> setOfDummy = new ArrayList<>();
		Data data = null;
		for (int i = 0; i < numDummy; i++) {
			if (sourceType == null) {
				return null;
			}
			if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
				data = new Log("dummy", "01/Jul/1900:0:00:00 -0000", "resource", "0000", 
						Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			} else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
				data = new Gowalla("0\t-1\tdummy\t-00.0000000000\t00000");
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
				data = new USPS("noname", "0", "unknown", Constants.DataSource.USPS.DOMAIN_MIN,"dummy");
				data.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
				data = new Electricity(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, 
						"dummy-00-00 00:00:00", -1);
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
				data = new UserAuth(1111, Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "dummy");
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USER)){
				data = new User(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "dummy", "1.000811,0.002082,-0.001922,0.003575,0.999653,1.000928,1.002032,1.960286,6.684610,5.043892,0.000029,0.000084,0.000233,0.001885,0.429754,0.115731,0.163364,0.003077,-0.000170,-1.000802,0.002262,0.001760,0.002086,0.111225,0.488020,0.154312,0.002432,0.009142,0.027166,0.047330,0.001305,0.001823,0.002545,0.028810,6.016422,3.823204,3.596534,4.966467,4.875743,6.376642,5.772607,5.571801,0.023369,-0.000096,0.000025,0.000307,0.001488,0.001581,0.009201,-0.433114,0.032062,-0.168144,633.856464,0.303380,-0.346959,0.502758,633.712688,633.905475,634.046780,2.210081,6.684612,5.043399,0.000009,0.000018,0.000011,0.000045,0.429583,2.212941,0.101626,174.194298,102.963450,-600.690103,0.375410,0.396511,0.291072,-0.774734,-0.633681,0.687044,1.000000,0.999999,0.999999,0.999999,0.999998,995.369977,18.906504,-10.419867,29.284151,984.292639,992.290280,1008.000000,2.180543,5.926745,5.047303,0.002511,0.027416,0.021507,0.127325,0.433366,2.510848,0.123665,-7.104000,-10.346667,-992.960000,16.448623,53.149122,43.547442,-0.651385,-0.616377,0.883437,4.963815,4.013884,5.128257,4.536012,4.686713,5.105830,3.009070,4.579963,3.923300,4.887330,5.049121,0.002771,0.030358,0.024353,0.134255,0.997661,0.997595,0.997111,0.995289,0.963455,nan,nan,nan,nan,nan,nan,nan,nan,nan,7.000000,0.051085,0.042162,107.505554,108.744263,nan,nan,65.000000,10.000000,6.027438,1.796322,0.000016,0.000015,0.000031,-0.000022,0.000077,0.000093,-7.418426,2.016611,-1.061945,1.195417,-0.593168,0.041554,-0.588281,-0.620592,-0.289123,-0.104321,0.083421,0.198781,-0.299491,2.406004,1.396577,1.281903,0.800262,0.490133,0.495246,0.540785,0.569112,0.463722,0.515975,0.464771,0.436589,0.527716,-1.418762,1.418805,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,1.000000,0.000000,0.000000,nan,nan,nan,0.000000,nan,0.890000,0.427701,nan,0.000000,0.000000,1.000000,1.000000,0.000000,0.000000,0.000000,0.000000,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,0,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,-1");
			}
			else {
				System.out.println("Data.generateDummy: Does not exist data source named: " + sourceType);
			}
			setOfDummy.add(data);
		}
		return setOfDummy;
	}
	
	/**
	 * Generate a set of encrypted dummy tuples with default id (-1)
	 * @param numDummy
	 * @param key is used to encrypt dummy tuple
	 * @return
	 * @throws Exception 
	 */
	public static List<Data> generateSetOfEncryptedDummy(int numDummy, String sourceType, String key) throws Exception {
		List<Data> setOfDummy = new ArrayList<>();
		Data data = null;
		for (int i = 0; i < numDummy; i++) {
			if (sourceType == null) {
				return null;
			}
			if(sourceType.equalsIgnoreCase(Constants.DataSourceType.LOG)){
				data = new Log("dummy", "", "", "", Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			} else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.GOWALLA)){
				data = new Gowalla(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "-1", "-1", "dummy");
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.USPS)){
				data = new USPS("noname", "0", "unknown", 0,"dummy");
				data.setIndexedAttribute(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE);
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.ELECTRICITY)){
				data = new Electricity(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "0000-00-00 00:00:00", -1);
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
				data = new UserAuth(1111, Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "dummy");
			}else if(sourceType.equalsIgnoreCase(Constants.DataSourceType.AUTH)){
				data = new User(Constants.iot.data.DEFAULT_DUMMY_ATTR_VALUE, "dummy", "1.000811,0.002082,-0.001922,0.003575,0.999653,1.000928,1.002032,1.960286,6.684610,5.043892,0.000029,0.000084,0.000233,0.001885,0.429754,0.115731,0.163364,0.003077,-0.000170,-1.000802,0.002262,0.001760,0.002086,0.111225,0.488020,0.154312,0.002432,0.009142,0.027166,0.047330,0.001305,0.001823,0.002545,0.028810,6.016422,3.823204,3.596534,4.966467,4.875743,6.376642,5.772607,5.571801,0.023369,-0.000096,0.000025,0.000307,0.001488,0.001581,0.009201,-0.433114,0.032062,-0.168144,633.856464,0.303380,-0.346959,0.502758,633.712688,633.905475,634.046780,2.210081,6.684612,5.043399,0.000009,0.000018,0.000011,0.000045,0.429583,2.212941,0.101626,174.194298,102.963450,-600.690103,0.375410,0.396511,0.291072,-0.774734,-0.633681,0.687044,1.000000,0.999999,0.999999,0.999999,0.999998,995.369977,18.906504,-10.419867,29.284151,984.292639,992.290280,1008.000000,2.180543,5.926745,5.047303,0.002511,0.027416,0.021507,0.127325,0.433366,2.510848,0.123665,-7.104000,-10.346667,-992.960000,16.448623,53.149122,43.547442,-0.651385,-0.616377,0.883437,4.963815,4.013884,5.128257,4.536012,4.686713,5.105830,3.009070,4.579963,3.923300,4.887330,5.049121,0.002771,0.030358,0.024353,0.134255,0.997661,0.997595,0.997111,0.995289,0.963455,nan,nan,nan,nan,nan,nan,nan,nan,nan,7.000000,0.051085,0.042162,107.505554,108.744263,nan,nan,65.000000,10.000000,6.027438,1.796322,0.000016,0.000015,0.000031,-0.000022,0.000077,0.000093,-7.418426,2.016611,-1.061945,1.195417,-0.593168,0.041554,-0.588281,-0.620592,-0.289123,-0.104321,0.083421,0.198781,-0.299491,2.406004,1.396577,1.281903,0.800262,0.490133,0.495246,0.540785,0.569112,0.463722,0.515975,0.464771,0.436589,0.527716,-1.418762,1.418805,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,1.000000,0.000000,0.000000,nan,nan,nan,0.000000,nan,0.890000,0.427701,nan,0.000000,0.000000,1.000000,1.000000,0.000000,0.000000,0.000000,0.000000,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,0,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,nan,-1");
			}
			else {
				System.out.println("Data.generateDummy: Does not exist data source named: " + sourceType);
			}
			data.setEncryptedText(AESEncryptor.CBCEncrypt(data.toString(), key));
			setOfDummy.add(data);
		}
		return setOfDummy;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public float getIndexedAttribute() {
		return this.indexedAttribute;
	}

	public void setIndexedAttribute(float indexedAttribute) {
		this.indexedAttribute = indexedAttribute;
	}

	public Node getLeafNode() {
		return leafNode;
	}

	public void setLeafNode(Node leafNode) {
		this.leafNode = leafNode;
		if(leafNode.getDataList() == null) {
			leafNode.setDataList(new ArrayList<>());
		}
		leafNode.getDataList().add(this);
	}

	public String getEncryptedText() {
		return encryptedText;
	}

	public void setEncryptedText(String encryptedText) {
		this.encryptedText = encryptedText;
	}

	public StringBuilder getClearText() {
		return clearText;
	}

	public void setClearText(StringBuilder clearText) {
		this.clearText = clearText;
	}
	
	public List<BigInteger> getListOfItems() {
		return listOfItems;
	}

	public void setListOfItems(List<BigInteger> listOfItems) {
		this.listOfItems = listOfItems;
	}
	
	/**
	 * Return a string having the same format with the original data
	 * @return
	 */
	public String toRawString() {
		return String.format("Data.class");
	}
}