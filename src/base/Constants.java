package base;

public interface Constants {
	// global params
	public static final String LOCAL_HOST 				= "localhost";
	public static final String HADOOP_HOST 				= LOCAL_HOST;
//	public static final String HADOOP_HOST 				= "10.0.1.74";
	public static final String CLOUD_HOST 				= LOCAL_HOST;
//	public static final String CLOUD_HOST 				= "10.0.1.74";
	// Index's params
	public static final String INDEX_FILE_NAME 			= "output/index";
	public static final String DATASET_FILE_NAME 		= "output/dataset";
	public static final String OVERFLOW_ARRAY_FILE_NAME	= "output/overflowarray";
	public static final String HDFS_INDEX_FILE_NAME 		= "index";
	public static final String HDFS_DATASET_FILE_NAME 	= "dataset";
	public static final String MATCHING_FILE_NAME 		= "matching";
	public static final String META_FILE_NAME 			= "meta";
	public static final String HDFS_OVERFLOW_ARRAY_FILE_NAME	= "overflowarray";
	public static final String HDFS_HOST_PORT			= "hdfs://" + HADOOP_HOST + ":9000";
	public static final String REGEX_STRING				= ";";
	// transfer data to hdfs
	// public static String LOCAL_FILE_NAME				= "output/dataset";
	public static final String HDFS_FILE_NAME			= HDFS_HOST_PORT + "/user";
	public interface DataSource{
		public interface Log{
			public static final int 	DOMAIN_MIN 			= 0;
			// min: 0 - max: 3421- range: 3421 (kb)
			public static final int 	DOMAIN_MAX 			= 3421; // max: 3421948
		}
		public interface Gowalla{
			// min = 342701 : max = 357725 : range = 15024 (hourly)
			// min: 14279- max: 14905- range: 626 (daily)
			public static final int 	DOMAIN_MIN 			= 14279;
			public static final int 	DOMAIN_MAX 			= 14905;
			// min = 14900 : max = 14905 : range = 5 (daily) : 95276 records
			// min = 357600 : max = 357725 : range = 125 (hourly)
			// min = 14895 : max = 14905 : range = 10 (daily) : 304055 records
			// min = 357480 : max = 357725 : range = 245 (hourly)
			// min = 14880 : max = 14905 : range = 25 (daily) : 837402 records		
			// min = 357120 : max = 357725 : range = 605 (hourly)
			// min = 14855 : max = 14905 : range = 50 (daily)
			// min = 356520 : max = 357725 : range = 1205 (hourly)
			// min = 14805 : max = 14905 : range = 100
			// min = 355320 : max = 357725 : range = 2405 (hourly)
			// min = 14705 : max = 14905 : range = 200
			// min = 352920 : max = 357725 : range = 4805 (hourly)
			// min = 14605 : max = 14905 : range = 300
			// min = 14505 : max = 14905 : range = 400
			// min = 14405 : max = 14905 : range = 500
			// min = 14308 : max = 14905 : range = 597 (600 days)
		}
		public interface USPS{
			// min: 26889- max:291650- range:264761 (annual, original)
			public static final int 	DOMAIN_MIN 			= 26889;
			public static final int 	DOMAIN_MAX 			= 291650;
			// min: 26889- max:210700- range:183811 (annual size: 387984)
			// min: 26889- max:198790- range:171901 (500k)
			// min: 26889- max:199960- range:173071 (1M)
			// min: 26889- max:199960- range:173071 (2M)
			// min: 0- max:68- range:68 (hourly size: 245801)
		}
		public interface Electricity{
			// min: 1; max:150 => range: 150 (apartments)
			public static final int 	DOMAIN_MIN 			= 1;
			public static final int 	DOMAIN_MAX 			= 114;
			public static final int ID_LEN				= 5;
		}
		public interface User{
			// min: 0; max:59 => range: 60 (users)
			public static final int 	DOMAIN_MIN 			= 0;
			public static final int 	DOMAIN_MAX 			= 59;
			public static final int MAX_SOURCE_SIZE		= 377346;// records
			public static final int MAX_RECORD_SIZE		= 2316;
		}
		public interface Auth{
			// 16763710 tuples
			public static final int 	DOMAIN_MIN 			= 1;
			public static final int 	DOMAIN_MAX 			= 9920;
			public static final int ID_LEN				= 5;
		}
//		public static final int 	RANGE_INTERVAL 				= 10000;// log
//		public static final int 	RANGE_INTERVAL 				= 100000;// log for test
//		public static final int 	RANGE_INTERVAL 				= 1;// gowalla: day
//		public static final int 	RANGE_INTERVAL 				= 1000;// gowalla for test
//		public static final int 	RANGE_INTERVAL 				= 1; // electricity
//		public static final int 	BRANCHING_FACTOR 			= 16;
		public static final int PRECISION_LEVEL 				= 2; // related to BOUND_DEVIATION
		public static final float BOUND_DEVIATION 			= 0.01f;
	}
	
	public interface iot{
		public static final String KEY_SEPERATION 			= ".";
		public interface com{
			/**
			 * This separator is used for communication among
			 * collector
			 * clouds,...
			 */
			public static final String CONSUMER_PREFIX 				= "consumer";
			public static final String ID_SEPARATOR 					= " ";// space
			public static final String PUBLISH 						= "publish";
			public static final String PUBLISH_INDEX 				= "publish.index";
			public static final String PUBLISH_OVERFLOW_ARRAY 		= "publish.overflow.array";
			public static final String PUBLISH_LTUPLES 				= "publish.ltuples";
			public static final String PUBLISH_LEAF_NODE_IDS 		= "publish.leaf.nodes";
			public static final String META_COUNT_SEPARATOR 			= "@";
			public static final String QUERY_SEPARATOR 				= "@";
			public static final String COLLECTOR_PREFIX 				= "collector";
			public static final String CLOUD1_PREFIX 				= "cloud1";
			public static final String KEY_SEPARATION 				= " ";
			public static final String DATA_SEPARATION 				= "_";
			public static final String BUCKET_DATA_SEPARATION 		= "@";
		}
		public interface cloud1{
			//public static final String HOST 						= LOCAL_HOST;
			public static final int 	PORT 						= 9944;
			
			// Distributing (1)
			public static final int LISTEN_PORT 					= 9966;
		}
		public interface cloud2{
			//public static final String HOST 						= LOCAL_HOST;
			public static final int 	PORT 						= 9955;
		}
		public interface consumer{
			//public static final String HOST 						= LOCAL_HOST;
			public static final int 	PORT 						= 9911;
		}
		public interface collector{
			public static final String COLLECTOR_PREFIX 			= "$";
			//public static final String HOST 						= LOCAL_HOST;
			public static final int 	PORT 						= 9900;
			public static final String METRICS_FOLDER 			= "metrics_iot/";
		}
		public interface ltuple{
			public static final int 	FIXED_SIZE 					= 64; // ciphertexts
			/**
			 * Number of update per time interval
			 */
			public static final int MAX_UPDATE_SIZE 				= Constants.iot.datagenerator.DATA_RATE; 
			/**
			 * Dummy char is used to generate dummy id (physical location of a tuple)
			 */
			public static final char DUMMY_CHAR 						= 'd';
			/**
			 * Id is offset of tuple in cloud 2.
			 * This parameter changes when size of database
			 */
			public static final int OFFSET_LEN 						= 10; // FileOffset length
			/**
			 * physical location of a tuple at cloud 2.
			 * This address may include ServerNode + FileName + FileOffset 
			 */
			public static final int PHYSICAL_LOCATION_LEN 			= 0 + OFFSET_LEN; // sizeOf(FileName) + FileOffset length
			public static final String ID_SEPARATOR 					= "%";
		}
		public interface data{
			/**
			 * Size of an encrypted tuple (byte), 
			 * this parameter changes when data type changes
			 */
//			public static final int 	FIXED_SIZE 					= 24; // un-encrypted
			public static final int 	FIXED_SIZE 					= 48; // binary
			/**
			 * Defaut value for indexed attribute of dummy data
			 */
			public static final int 	DEFAULT_DUMMY_ATTR_VALUE 	= -1; 
			
		}
		public interface datagenerator{
			public static final int 	DATA_RATE 					= 3; // times per TIME_INTERVAL (not per second)
			public static final int 	TIME_INTERVAL 				= 60000; // mili seconds 
		}
		public interface metrics{
			public static final String METRIC_FILE_EXTENSION 	= ".csv";
			
		}
	}
	
	public interface Dispatcher{
		public static final int 	PORT 						= 9944;
	}
	
	public interface Cloud{
		public static final int 	PORT 						= 9955;
		// Distributing (1)
		public static final String METRICS_FOLDER_1 			= "metrics1/";
		// Distributing (2)
		public static final int LISTEN_PORT 						= 9988;
		public static final String DATASET_FOLDER_2			= "output/distributing2/cloud/";
		public static final String INDEX_FOLDER_2 			= "output/distributing2/index/";
		public static final String MATCHING_TABLE_FOLDER_2 	= "output/distributing2/matchingtable/";
		public static final String META_FOLDER_2 			= "output/distributing2/meta/";
		public static final String MATCHED_FOLDER_2 			= "output/distributing2/matched/";
		public static final String METRICS_FOLDER_2 			= "metrics2/";
		// Distributing (3)
		public static final String DATASET_FOLDER_3			= "output/distributing3/cloud/";
		public static final String INDEX_FOLDER_3 			= "output/distributing3/index/";
		public static final String MATCHING_TABLE_FOLDER_3 	= "output/distributing3/matchingtable/";
		public static final String META_FOLDER_3 			= "output/distributing3/meta/";
		public static final String MATCHED_FOLDER_3 			= "output/distributing3/matched/";
		public static final String METRICS_FOLDER_3 			= "metrics3/";
		public static final String METRICS_FOLDER_0 			= "metrics0/";
		public static final String METRICS_FOLDER_PINED_RQ 	= "metrics_pined/";
		public static final String METRICS_FOLDER_PINED_RQ2 	= "metrics_pined2/";
	}
	
	public interface Checker{
		public static final int 	PORT 						= 9966;
	}
	
	public interface Merger{
		public static final int 	PORT 						= 9977;
	}
	
	public interface StorageManagement{
		public static final int 	PORT 						= 9988;
	}

	public interface DataSourceType{
		public static final String LOG						= "log";
		public static final String GOWALLA					= "gowalla";
		public static final String USPS						= "usps";
		public static final String ELECTRICITY				= "electricity";
		public static final String AUTH 						= "auth";
		public static final String USER 						= "user";
	}
	
	//data generator
	public interface DataGenerator{
		public static final int NUM_GENERATOR 					= 1;
		public static final int TIME_GENERATE 					= 1000; // (ms)
		//unit: millisecond
		public static final int TIME_INTERVAL 					= 1000; // (ms)
		public static final int 	NUM_RECORD_PER_TIME_INTERVAL 	= 1000; // target 200k/1s
	}
	//used to normalize from byte to kilobyte
	//public static final int 	SCALING_FACTOR 				= 1000;
	//data source
	// Laplace parameters
	public interface Laplace{
//		public static final float 	TOTAL_PRIVAY_BUDGET 		= (float) 1.0;
		public static final float 	INITIAL_PRIVAY_BUDGET 	= (float) 1.0;
//		public static final int 	SENSITIVITY 					= 1;
		public static int 	    OVERFLOW_ARRAY_SIZE_DEFAULT  = 15;
	}
	// file
	public interface FileConfig{
		public static final String FILE_FIELD_SEPARATOR 		= ";";
	}
	// encryption parameters
	public interface Enryption{
		// 128 bit key = 16 characters
		public static final String KEY_VALUE_1 				= "TheBestKeyValue@";
		public static final String KEY_VALUE_2 				= "TheBestKeyValue@2";
	}
	// query
	public static final String QUERY_SEPERATOR 				= ";";
	public static final String QUERY_SIZE_SEPERATOR 			= ",";
	
	// communication for pined-rq
	public interface ComPINED{
		public static final String DATA_SEPARATOR_1 			= ",";
		public static final String DATA_SEPARATOR_2 			= ";";
		public static final String DATA_SEPARATOR_3 			= " ";
	}
	public interface Com{
		public static final int 	INDEX_TEMPLATE_PORT 			= 9977;
		public static final String DATA_SEPARATOR 			= "@";
		public static final String MATCHER_DATA_SEPARATOR 		= ":";
		public static final String INDEX_TEMPLATE_DATA_PREFIX = "template";
		public static final String INDEX_TEMPLATE_RECORDIDS_PREFIX = "recordids";
		public static final String INDEX_TEMPLATE_OVERFLOW_PREFIX = "overflowids";
		public static final String INDEX_TEMPLATE_CONTENT_PREFIX = "indexcontent";
		public static final String INDEX_TEMPLATE_DATASIZE_PREFIX = "datasize";
		public static final String INDEX_TEMPLATE_MATCHER_PREFIX = "matcher";
		public static final String INDEX_TEMPLATE_CONTENT_SEPERATOR = "@";
		public static final String INDEX_TEMPLATE_COUNT_PREFIX = "count.index.template";
//		public static final String INDEX_COUNT_SEPERATOR = "§";
		public static final String INDEX_TEMPLATE_RECEIVED_LINE_SEPERATOR = "§";
	}
	
	public interface Node{
		public static final String INDEX_COUNT_SEPERATOR = "§";
	}
	
	// communication for pined-rq++ (2)
	public interface Com2{
		public static final String PUBLISH_BEGIN 		= "publish.begin";
		public static final String PUBLISH_END 			= "publish.end";
		public static final String ENRICHED_DATA_SEPERATOR 		= "%";
		public static final String DATA_SEPARATOR 				= "#";
		public static final String MATCHER_SEPARATOR_1 			= "@";
		public static final String MATCHER_SEPARATOR_2 			= "&";
		public static final String MATCHER_SEPARATOR_3 			= "£";
		public static final String MATCHER_DATA_SEPARATOR 		= "§";
		public static final String DISPATCHER_PREFIX 	= "dispatcher.";
		public static final String MERGER_PREFIX 		= "merger.";
		public static final String CHECKER_PREFIX 		= "checker.";
		public static final String SERVERNODE_PREFIX 	= "servernode";
		public static final String WORKER_PREFIX 		= "worker.";
		public static final String DISPATCHER_INDEX_COUNTS_PREFIX = DISPATCHER_PREFIX + "indexcount";
		public static final String DISPATCHER_LEAF_COUNTS_PREFIX = DISPATCHER_PREFIX + "leafcount";
		public static final String DISPATCHER_PUBLICATION_NUMBER_PREFIX = DISPATCHER_PREFIX 
																			+ "publicationnumber";
		public static final String SERVER_NODE_MATCHING_TABLE_PREFIX = "matching.table";
		public static final String SERVER_NODE_LEAF_COUNTS_PREFIX = "leaf.counts";
		public static final String MERGER_PUBLICATION_NUMBER = "merger.publication.number";
		public static final String MERGER_MATCHING_TABLE 	= "merger.matching.table";
		public static final String MERGER_DONE 				= "merger.done";
		public static final String WORKER_DONE 				= "worker.done";
		public static final String MERGER_INDEX 				= "merger.index";
		public static final String MERGER_OVERFLOW_ARRAY 	= "merger.overflow.array";
		public static final String DUMMY_SEPERATOR 			= "@";
		public static final String WORKER_PUBLICATION_NUMBER = "worker.publication.number";
		public static final String WORKER_META_STRING 		= "worker.meta.string";
		public static final String WORKER_MATCHER 			= "worker.matcher";
		public static final String WORKER_FIRST_LEAF_ID 		= "worker.first.leaf.id";
		public static final String CHECKER_MATCHING_TABLE_PREFIX = "checker.matcher";
		public static final String CHECKER_LEAF_COUNTS_PREFIX = "checker.leaf.counts";
		public static final String REMOVED_PREFIX 				= "r.pre";
		public static final String DUMMY_PREFIX 					= "dummy.pre";
		public static final String CLOUD_PREFIX 				= "cloud.prefix";
		public static final String MERGER_MATCHED_META 		= "merger.matched.meta";
		public static final String CONSUMER_DONE_PREFIX		= "consume.done";
	}
	
	// index
	public interface Index{
		public static final long INDEX_ROOT_PARENT_ID 			= -1;
	}
	// collector of distributing 1
	public interface Collector{
		public static final int LISTEN_PORT 						= 9999;
		public static final String TEMP_FOLDER_PINED_RQ 			= "temp_pined/";
	}
	// PID controller
	public interface PIDController{
		public static final int PID_DESIRABLE_SIZE_PER_DELTA 	= 30;
	}

	//query
	public interface Query{
		public static final int LOWER_BOUND 						= 0;
		public static final int RANGE_PERCENTAGE 				= 50;
		public static final int UPPER_BOUND 						= (int) (RANGE_PERCENTAGE * 
																		DataSource.Log.DOMAIN_MAX / 100);
		public static final int[] queryRanges 					= new int[]{1,5,10,25,50,75,100};
	}
	
	public interface IndexTemplate{
		public static final String HOST 							= CLOUD_HOST;
	}
	
	public interface Matcher{
		public static final long NUMBER_SIZE 					= 10000000; // size of Long
	}
	
	public interface Metrics{
		public static final String METRIC_FILE_EXTENSION 			= ".csv";
	}
}