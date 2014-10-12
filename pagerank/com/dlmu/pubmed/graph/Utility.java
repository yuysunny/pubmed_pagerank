package com.dlmu.pubmed.graph;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility<br/>
 * This class contains static utility methods used by other classes.
 * 
 * @author Scott Jensen - San Jose State University
 *
 */
public class Utility {
	// Logger object for logging in this class
	protected static Logger log = Logger.getLogger(Utility.class.getName());
	protected final static String log4jPathProperty = "pm.log4j";

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
