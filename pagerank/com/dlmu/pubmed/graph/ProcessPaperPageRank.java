package com.dlmu.pubmed.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * PageRankJob
 * This class is used to queue up the keyword jobs
 * to be processed so we can pass them to workers
 * @author Scott Jensen - San Jose State University
 *
 */
class PageRankJob {
	private String keyword = null;
	private String nodeFileName = null;
	private String outputFileName = null;
	
	public PageRankJob(String keyword, String nodeFileName, String outputFileName) {
		this.keyword = keyword;
		this.nodeFileName = nodeFileName;
		this.outputFileName = outputFileName;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getNodeFileName() {
		return nodeFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}
} //end of class PageRankJob



public class ProcessPaperPageRank {
	private static String PAGERANK_PROPERTIES = "pagerank.properties";
	private static String PROPERTY_THREADCNT = "thread_cnt";
	private static String PROPERTY_PROCESSDIR = "process_dir";
	//path to look for in the user directory if no directory is specified in the properties
	private static String DEFAULT_PATH = "PageRank"; 
	private static String PROPERTY_NODE_FILE = "node_file_name";
	private static String PROPERTY_EDGE_FILE = "edge_file_name";
	private static String PROPERTY_NODE_SCORE_DIR = "node_score_dir";
	private static String PROPERTY_OUTPUT_DIR = "output_dir";
	private static String PROPERTY_OUTPUT_PREFIX = "output_prefix";
	protected static Logger log = Logger.getLogger(ProcessPaperPageRank.class.getName());
	private Properties properties = null;
	private int threadCnt = -1; //number of threads
	private String nodeScoresDirName = null;
	private String outputDirName = null;
	private String outputPrefix = null;
	private String filePath = null;
	private MeshPaperPageRank[] pageRankWorkers = null;
	private ArrayList<PageRankJob> pageRankJobs = null; //the work queue of prior files to process
	

	 /**
	  * This method expects an optional String parameter with the directory where
	  * the pagerank.properties file could be found.  The directory could be
	  * an absolute path or a path relative to the user directory.
	  * If the directory is not specified, it looks for pagerank.properties in the
	  * user directory.
	  * @param dir
	  * @throws PubmedPagerankException
	  */
	public ProcessPaperPageRank(String dir) throws PubmedPagerankException {
		try {
			properties = Utility.loadProperties(dir, PAGERANK_PROPERTIES);
		} catch (Exception e) { throw new PubmedPagerankException("An exception occurred loading the pagerank properties file", e); }
		
		threadCnt = Integer.parseInt(properties.getProperty(PROPERTY_THREADCNT));
		filePath = properties.getProperty(PROPERTY_PROCESSDIR, System.getProperty("user.dir") + File.separator + DEFAULT_PATH);
		Utility.setupLogging(filePath); //Setup the Log4J logging
		log.info("ProcessPaperPageRank: path used for all file access = " + filePath);
		String nodeFileName = filePath + File.separator + properties.getProperty(PROPERTY_NODE_FILE);
		String edgeFileName = filePath + File.separator + properties.getProperty(PROPERTY_EDGE_FILE);
		nodeScoresDirName = properties.getProperty(PROPERTY_NODE_SCORE_DIR);
		outputDirName = properties.getProperty(PROPERTY_OUTPUT_DIR);
		outputPrefix = properties.getProperty(PROPERTY_OUTPUT_PREFIX);
		// Create the array of workers
		pageRankWorkers = new MeshPaperPageRank[threadCnt];
		if (setupWorkers(threadCnt, nodeFileName, edgeFileName) != threadCnt) {
			throw new PubmedPagerankException("The page rank workers were not successfully created.");
		}
		// Load the job queue
		loadWorkQueue();
		return;
	}// end of constructor
	
	
	public void process () {
		Thread[] workerThreads = new Thread[threadCnt];
		for (int i = 0; i < threadCnt; i++) {
			workerThreads[i] = new Thread(pageRankWorkers[i]);
			workerThreads[i].start();
			try{ Thread.sleep(2000);} catch(InterruptedException e){}
		}
		boolean workerAlive = true;
		// Wait until the workers are done
		while (workerAlive) {
			workerAlive = false; //still false at the end if no worker is alive
			try {
				for (int i = 0; i < threadCnt; i++) {
					if (workerThreads[i].isAlive()) {
						workerAlive = true;
						workerThreads[i].join();
					} 
				}
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
	} //end of process
	
	
	private void loadWorkQueue() throws PubmedPagerankException {
		// Get the list of files to process
		String nodeFilePath = filePath + File.separator + nodeScoresDirName;
		String outputPath = filePath + File.separator + outputDirName;
		String[]filelist = null;
		
		try{
			File file = new File(nodeFilePath); //directory containing node scores
			filelist = file.list(); //list of keyword files with node scores
			pageRankJobs = new ArrayList<PageRankJob>(filelist.length);
			for ( int i = 0; i <filelist.length ; i++) {
				if(filelist[i].contains("keyword")) {
					String fileName = filelist[i];
					String nodeFile = nodeFilePath + File.separator + fileName; //full path to the node prior file to be processed
					String s[]=fileName.split("d_",2);
					String keyword = s[1];
					String outFile = outputPath + File.separator + outputPrefix + keyword;
					PageRankJob job = new PageRankJob(keyword, nodeFile, outFile);
					pageRankJobs.add(job);
				}

			}
		} catch (Exception e) {
			throw new PubmedPagerankException ("exception in loadWorkQueue: " + e.getMessage(), e);
		}
	} //end of loadWorkQueue
	
	
	public PageRankJob getJob() {
		PageRankJob job = null;
		synchronized(pageRankJobs) {
			if (pageRankJobs.size() > 0)
				 job = pageRankJobs.remove(0);
		}
		return(job);
	} //end of getJob
	
	
	private int setupWorkers(int threadCnt, String nodeFileName, String edgeFileName) throws PubmedPagerankException {
		int initializedCnt = 0; //how many workers were successfully setup
		
		try {
			pageRankWorkers = new MeshPaperPageRank[threadCnt];
			for (int i = 0; i < threadCnt; i++) {
				pageRankWorkers[i] = new MeshPaperPageRank(i+1, this, nodeFileName, edgeFileName);
				if (pageRankWorkers[i].isInitialized() )
					initializedCnt++;
			}
		} finally {
			if (initializedCnt != threadCnt && pageRankWorkers != null) {
				try {
					for (int i = 0; i < threadCnt; i++) {
						if (pageRankWorkers[i] != null) {
							pageRankWorkers[i].shutdown();
							pageRankWorkers[i] = null;
						}
					}
				} catch (Exception e){}
				log.error("Only " + initializedCnt + " of the " + threadCnt + 
						" workers were successfully initialized, so shutting down without running.");
			} //end of shutting down a bad startup
		} //end of finally
		return(initializedCnt);
	} //end of setupWorkers
	
	
	/**
	 * This is the main routine for processing the PageRank for citations
	 * in a multi-threaded approach.
	 * @param args
	 * 1) Optional path to the directory containing the pagerank.properties file.
	 * @throws PubmedPagerankException
	 */
	public static void main(String[] args) throws PubmedPagerankException {
		String dir = (args.length > 0)? args[0] : null;
		ProcessPaperPageRank processPR = new ProcessPaperPageRank(dir);
		processPR.process();
	} //end of main

} //end of class ProcessPaperPageRank
