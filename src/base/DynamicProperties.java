package base;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author vtran
 *
 */
public class DynamicProperties{
	private static DynamicProperties singleInstance = null;
	private Properties properties = null;
	private DynamicProperties() {
		try {
		FileReader reader = null;
		String env = AppProperties.getInstance().getProperties().get("environment").toString();
		System.out.println("==> Environment : " + env);
		if (env.contains("local")) {
			reader = new FileReader("properties/local.properties");
		}else if (env.contains("remote")) {
			reader = new FileReader("properties/remote.properties");
		}
		
	    properties = new Properties();
	    properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	public static DynamicProperties getInstance()
    {
        if (singleInstance == null) {
        		singleInstance = new DynamicProperties();
        }
        return singleInstance;
    }
	
	public static void main(String args[]) throws IOException {
		System.out.println((String) DynamicProperties.
				getInstance().getProperties().get("datasource.path"));
	}
}
