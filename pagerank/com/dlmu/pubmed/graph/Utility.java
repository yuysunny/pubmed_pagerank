package com.dlmu.pubmed.graph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility<br/>
 * This class contains static utility methods used by other classes.
 * 
 * @author Scott Jensen - San Jose State University
 *
 */
public class Utility  {
	// Logger object for logging in this class
	protected static Logger log = Logger.getLogger(Utility.class.getName());
	protected final static String log4jPathProperty = "pm.log4j";

	
	/**
	 * Loads a property file from the directory and file name specified.
	 * If the directory name is not a valid directory, it checks
	 * if it is valid relative to the user directory.  If not, then it
	 * checks for the file in the user directory.
	 * @param fileDir
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Properties loadProperties(String fileDir, String fileName) throws IOException {
		Properties properties = new Properties();
		if (fileDir == null)
			fileDir = System.getProperty("user.dir");
		File dir = new File(fileDir);
		if (!dir.isDirectory()) { //then use the user directory - it must be a directory
			String userDirName = System.getProperty("user.dir");
			File userDir = new File(userDirName);
			dir = new File(userDir, fileDir); //in case it was not an absolute path, but relative to the user directory
			if (!dir.isDirectory())
				dir = new File(userDirName);
		}
		// Open the properties file
		File propFile = new File(dir, fileName);
		if (!propFile.isFile()) {
			throw new IOException("The property file named " + 
					fileName + " does not exist in the directory " + 
					dir.getAbsolutePath());
		}
		try {
			properties.load( new FileReader(propFile) );
		} catch (Exception e) {
			throw new IOException("The property file named " + 
					fileName + " could not be loaded.  Exception: " + 
					e.getMessage(), e);
		}
		return(properties);
	} //end of loadProperties
	
	
	/**
	 * setupLogging
	 * This method is used to setup the Log4J logging and is called by the constructor.
	 */
	public static void setupLogging(String log4jPath) {
		//set up the logging
		try {
			if (log4jPath != null)
				log4jPath = log4jPath + File.separator + "log4j.properties";
			else 
				log4jPath = System.getProperty(log4jPathProperty);
			if (log4jPath == null)
				log4jPath = System.getProperty("user.dir") + File.separator + "log4j.properties";
			System.out.println("log4jPath: " + log4jPath);
			PropertyConfigurator.configure(log4jPath);
			log.debug("Utility.setuplogging: logging started");
			return;
		} catch (Exception e) {
			System.err.println("Utility-setupLogging: an error ocurred in starting the logging: " + e.getMessage() );
		}
	} //end of setupLogging
} //end of class Utility
