package com.dlmu.pubmed.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;


/**
 * PubMedGraph
 * This class extends the Jung SparseMultigraph
 * class to load the nodes and edges for the 
 * Pubmed pagerank graph from csv files.
 * 
 * @author Scott Jensen - San Jose State University
 *
 */
public class PubMedGraph extends SparseMultigraph<MyNode, MyLink> {
	protected static Logger log = Logger.getLogger(PubMedGraph.class.getName());
	
	private HashMap<String,MyNode> nodes = null;
	private boolean initialized = false;
			
	
	public PubMedGraph(String nodeFileName, String edgeFileName) {
		super();
		try {
			nodes = new HashMap<String,MyNode>(100000);
			loadNodes(nodeFileName.trim());
			loadEdges(edgeFileName.trim());
			initialized = true;
			return;
		} catch (Exception e) {
			log.error("PubMedGraph-constructor: An exception occurred and the graph was not initialized.");
		}
	} //end of constructor

	public boolean isInitialized() {
		return initialized;
	} //end of isInitialized
	
	public void clear() {
		try {nodes.clear();} catch (Exception ex){}
	} //end of clear
	
	
	private MyNode getNode(String nodeId) {
		MyNode myNode = nodes.get(nodeId);
		return(myNode);
	} //end of getNode
	
	public void loadNodesScore(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		String readline = null;
		
		try {
			//clear the node values
			for (MyNode node: nodes.values())
				node.reset();

			// Get the sum of the scores for all nodes using
			// this MeSH descriptor and qualifier as a major topic.
			bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(path_loading_name)));
			Double sum = 0.0;
			while ((readline = bin.readLine()) != null) {
				// articleId, type, sources[]
				String linestring[] = readline.split(",",2);
				double score = Double.valueOf(linestring[1]);
				sum+=score;
			}
			log.debug("MeshPaperPageRank-loadingNodesScore: load node score = " + 
					sum + " for keyword = " + path_loading_name);
			bin.close();
			// We are reading the file twice, but the files
			// are fairly short since there is only a record
			// for a paper if it used the MeSH descriptor and qualifier
			// as a major topic.
			bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(path_loading_name)));
			while ((readline = bin.readLine()) != null) {
				String linestring[] = readline.split(",",2);
				double score = Double.valueOf(linestring[1]);
				MyNode n=getNode(linestring[0]);
				if(n!=null)
				{
					n.setPrior(score/sum);
				} else {
					log.error("MeshPaperPageRank-loadNodesScore: in the file " +
							path_loading_name + " there was a record  for the node " +
							linestring[0] + " but that node was not in the tree.");
				}

			}
			bin.close();
			return;
		} catch (Exception e) {
			throw new Exception("An exception occurred in loadNodesScore for " +
					path_loading_name + ". Exception: " + e.getMessage(), e);
		} finally {
			try {bin.close();} catch(Exception ex){}
		}
	} //end of loadNodesScore
	

	private void loadEdges(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		int i = 0;
		
		log.debug("MeshPaperPageRank-loadingEdges: starting to load edges from " +  
				path_loading_name);
		long startTime=System.currentTimeMillis();
		try {
			bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(path_loading_name)));
			String readline;
			
			while ((readline = bin.readLine()) != null) { //each line contains a Node ID
				if (!readline.contains("id")) { //first line contains the heading
					i++; //record count of edge lines being processed
					String linestring[] = readline.split(",",2);
					MyNode beginNode = getNode(linestring[0]);
					MyNode endNode = getNode(linestring[1]);
					if (beginNode != null && endNode != null) {
						MyLink edge = new MyLink(i, 0);
						this.addEdge(edge, beginNode, endNode, EdgeType.DIRECTED);
					} else {
						log.error("MeshPaperPageRank-loadingEdges: an edge was encountered with an invalid node." +
								" begin node = " + beginNode + ", end node = " + endNode);
					}
				}
			} //loop through the edge records
			bin.close();
		} catch (Exception e) {
			throw new Exception("MeshPaperPageRank-loadingEdges: an exception was encountered loading the " +
					i +" edge record from the file: " + path_loading_name +
					".  Exception: " + e.getMessage(), e);
		} finally {
			log.info("MeshPaperPageRank-loadingEdges: time to load " + i + " edges = " + 
					(System.currentTimeMillis() - startTime) + "ms");
			try {bin.close();} catch(Exception ex){}
		}
	} //end of loadEdges
	

	private void loadNodes(String path_loading_name) throws Exception {
//		HashMap keyScore = new HashMap();
		BufferedReader bin = null;
		int i = 0;
		
		log.debug("MeshPaperPageRank-loadingNodes: starting to load nodes from " +  
				path_loading_name);
		long startTime=System.currentTimeMillis();
		try {
			bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(path_loading_name)));
			String readline;
			while ((readline = bin.readLine()) != null) { //each line contains a Node ID
				// articleId, type, sources[]
				if (!readline.contains("id")) { //first line contains the heading
					i++; //count of nodes added
					MyNode node = new MyNode(readline);
					//				 System.out.println(node1.id + "-----" + node1.node_prior );
					nodes.put(readline, node);
					this.addVertex(node);
				}
			} //loop through all of the nodes records
			return;
		} catch (Exception e) {
			throw new Exception("An exception occurred in loading line " + i +
					" from the nodes file at " + 
					path_loading_name + ". Exception = " + e.getMessage(), e);
		} finally {
			log.info("MeshPaperPageRank-loadingNodes: time to load " + 
					i + " nodes = " + (System.currentTimeMillis() - startTime) + "ms");
			try {bin.close();} catch(Exception ex){}
		}
	} //end of loadNodes

	
}  //end of class PubMedGraph
