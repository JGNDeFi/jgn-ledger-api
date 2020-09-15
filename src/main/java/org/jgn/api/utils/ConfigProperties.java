package org.jgn.api.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {
	
	public String getPropertyValue(String property) throws IOException {
		String value = "";
		InputStream inputStream = null;
		
		try {
			Properties properties = new Properties();
			String propFileName = "config.properties";
				
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
	
			
			boolean isDebugMode = java.lang.management.ManagementFactory.getRuntimeMXBean().
				    getInputArguments().toString().indexOf("jdwp") >= 0;
			
			String propFileExt = (isDebugMode) ? ".dev": ".test";
			
//			/System.out.println("Loading "+ propFileExt +" config file");
			
			if (inputStream == null) {
				inputStream = getClass().getClassLoader().getResourceAsStream(propFileName + propFileExt);
			}
			
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new FileNotFoundException("Unable to find '" + propFileName + "'");
			}
				
			value = properties.getProperty(property);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		
		return value;
	}

}
