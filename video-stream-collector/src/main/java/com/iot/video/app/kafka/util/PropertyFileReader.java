package com.iot.video.app.kafka.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Utility class to read property file
 * 
 * @author abaghel
 *
 */
public class PropertyFileReader {
	private static final Logger logger = Logger.getLogger(PropertyFileReader.class);
	private static Properties prop = new Properties();
	public static Properties readPropertyFile() throws Exception {
		if (prop.isEmpty()) {
			InputStream input = PropertyFileReader.class.getClassLoader().getResourceAsStream("stream-collector.properties");
			try {
				prop.load(input);
			} catch (IOException ex) {
				logger.error(ex);
				throw ex;
			} finally {
				if (input != null) {
					input.close();
				}
			}
		}
		return prop;
	}
}
