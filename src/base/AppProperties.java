package base;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 */

/**
 * @author vtran
 *
 */
public class AppProperties{
	private static AppProperties singleInstance = null;
	private Properties properties = null;
	private AppProperties() {
		try {
		FileReader reader = null;
		reader = new FileReader("properties/app.properties");
	    properties = new Properties();
	    properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	public static AppProperties getInstance()
    {
        if (singleInstance == null) {
        		singleInstance = new AppProperties();
        }
        return singleInstance;
    }
	
	public static void main(String args[]) throws IOException {
		System.out.println(Integer.parseInt((String) AppProperties.
				getInstance().getProperties().get("pid.max.time.interval")));
	}
}
