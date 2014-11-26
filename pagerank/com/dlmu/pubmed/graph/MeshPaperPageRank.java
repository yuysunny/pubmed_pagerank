package com.dlmu.pubmed.graph;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;


import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

public class MeshPaperPageRank implements Runnable {
	// Log4J Logger object for logging in this class
	protected static Logger log = Logger.getLogger(MeshPaperPageRank.class.getName());

	private static double TOLERANCE = 0.001; 
	private static int MAX_ITERATIONS = 500;
	private static double ALPHA = 0.15;
	public static PrintWriter out2;
	public static double EDGE_PARA=0.1;
	private ProcessPaperPageRank  manager = null; // the manager who holds the work queue
	int workerId = -1;                   // set when the constructor is called
	private PubMedGraph g = null;
	private boolean initialized = false; // flag that everything was setup OK
	private boolean shutdown = false;    // should this worker be shutdown
	private int processedCnt = 0;        // how many jobs have been processed by this worker
	
	private static Transformer<MyNode, Double> vertex_priors = new Transformer<MyNode, Double>() {
		public Double transform(MyNode node) {
			return node.node_prior;
		}
	};
	
	/**
	 * The directory for all of the data files and output files can be passed as a parameter
	 * in the call to the main routine, or the default directory within the working directory
	 * will be used if null is passed as the parameter.
	 * Data files expected within that directory:
	 * The nodes file (default is pubmed_id.csv)
	 * The edges file (default is citation_new.csv)
	 * The weights for the papers given a keyword.  Each keyword is
	 * in a separate file with the MeSH descriptor and qualifier in the file name.
	 * These files are expected in a subdirectory named PaperRelevanceInTopic.
	 * @param args
	 */
	public MeshPaperPageRank(int workerId, ProcessPaperPageRank manager, String nodeFileName, String edgeFileName) {
		try {
			this.workerId = workerId;
			this.manager = manager;
			g = new PubMedGraph(nodeFileName, edgeFileName);
			initialized = g.isInitialized();
			return;
		} catch (Exception e) {
			initialized = false;
			log.error("MeshPaperPageRank-constructor: an exception occurred in setting up the MeshPaperPageRank: " +
					e.getMessage(), e);
			try {g.clear();} catch (Exception ex){}
		}
	} //end of constructor
	
	public boolean isInitialized() {
		return initialized;
	} //end of isInitialized
	
	public void shutdown() {
		log.info("Shutdown called for worker: " + workerId);
		shutdown = true;
	} //end of shutdown
	
	

	public void run() {
		PageRankJob job = null; //job being processed
		String keyword = null;
		
		try {Thread.sleep(2000);} catch (InterruptedException e){}
		
		
		try {
			while(!shutdown) {
				job = manager.getJob();
				if (job == null)
					shutdown = true;
				else { //process the job
					keyword = job.getKeyword();
					long startTime=System.currentTimeMillis();
					g.loadNodesScore(job.getNodeFileName());
					long nodeScoreTime = System.currentTimeMillis();
					pageRankPrior(job.getOutputFileName(), keyword, g);
					long endTime=System.currentTimeMillis();
					processedCnt++;
					if (log.isInfoEnabled() )
						log.info("MeshPaperPageRank-runPageRank: job # " + processedCnt + 
								" for keyword " + keyword + " the loading of nodes took " +
								(nodeScoreTime - startTime) +"ms and pagerank took " +
								(endTime - nodeScoreTime) +"ms");
				}
			} //end of while not shutdown loop
		} catch (Exception e) {
			log.error("An exception was caught when running pagerank for " +
					keyword + ". Exception: " + e.getMessage(), e);
		} finally {
			try {g.clear();} catch(Exception e){}
		}
	} //end of runPageRank
	
	
	private void pageRankPrior(String path, String kid,PubMedGraph g) throws Exception {
		PrintWriter out = null;
		try {
			PageRankWithPriors<MyNode, MyLink> rankerWithPriors = new PageRankWithPriors<MyNode, MyLink>(g, vertex_priors, ALPHA);
			rankerWithPriors.setMaxIterations(MAX_ITERATIONS);
			rankerWithPriors.setTolerance(TOLERANCE);
			rankerWithPriors.evaluate();
			if(log.isDebugEnabled()) {
				int iterations = rankerWithPriors.getIterations();
				log.debug("MeshPaperPageRank-pageRankPrior: iterations for keyword: " + 
						kid + " = " + iterations);
			}
			out = new PrintWriter(new FileWriter(path));
			Collection<MyNode> nodes = g.getVertices();
			
			for (MyNode node: nodes) {
				double score = rankerWithPriors.getVertexScore(node);
				if (score > 0.0)
					out.println( node + ","+ score);
			}
			return;
		} catch (Exception e) {
			throw new Exception("An exception occurred in pageRankPrior for keyword: " + 
					kid + ". Exception: " + e.getMessage(), e);
		} finally {
			try{out.flush();} catch (Exception ex){}
			try{out.close();} catch (Exception ex){}
		}
	} //end of pageRankPrior
}

