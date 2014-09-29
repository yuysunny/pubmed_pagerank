package com.dlmu.pubmed.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class VolumePageRank {
	public static String filePath = "./";
	public static PrintWriter out2;
	public static double EDGE_PARA=0.1;
	public static BufferedWriter bufOut;
	public static Graph<MyNode, MyLink> g = new SparseMultigraph<MyNode, MyLink>();
	public static void main(String[] args) throws Exception {
		System.out.println("loading Nodes file......");
		loadingNodes("");
		System.out.println("loading Edges file......");
		loadingEdges( ".pubmed_volumeCiteVolume");
		runPageRank();

	}
	
	public static void runPageRank() throws Exception
	{
		File file = new File(filePath+"PaperRelevanceInTopic/");
		System.out.println("load graph start...");
		String[] filelist = file.list();
		for ( int j = 0; j <filelist.length ; j++) 
		{
			if(filelist[j].contains("keyword"))
			{
				FileInputStream fis=new FileInputStream(filePath+"ppGraph.ser");
				ObjectInputStream ois=new ObjectInputStream(fis);
				Graph<MyNode, MyLink> g =  (Graph<MyNode, MyLink>)ois.readObject();
				System.out.println("load graph end...");
				ois.close();
				bufOut = new BufferedWriter(new FileWriter(filePath+"log.txt",true));
				bufOut.write("\r\n"+"page rank j="+j+" filename=  "+filelist[j]);
				bufOut.close();
				String s[]=filelist[j].split("_");
				System.out.println("keyword =="+filelist[j]);
				loadingNodesScore(filePath+"PaperRelevanceInTopic/"+filelist[j]);
//				System.out.println("The graph g = " + g.toString());
				pageRankPrior(filePath+"PaperContributeToTopic/",s[1],g);
			}
		}
		
		
	}
	private static void pageRankPrior(String path,String kid,Graph<MyNode, MyLink> g ) throws IOException{
		System.out.println("run page rank: "+kid);
		PrintWriter out1 = new PrintWriter(new FileWriter(path+"keyword_"+kid));
		// primitives
		// Two disconnected community on graph
		// Let's see what we have. Note the nice output from the
		// SparseMultigraph<V,E> toString() method
		
		Collection<MyNode> vertices = g.getVertices();
		System.out.println("g.getVertices34");
		// Pagerank with priors
		// Node Priors
		org.apache.commons.collections15.Transformer<MyNode, Double> vertex_priors = new org.apache.commons.collections15.Transformer<MyNode, Double>() {
			public Double transform(MyNode node) {
				return node.node_prior;
			}
		};
		// Edge Priors
//		org.apache.commons.collections15.Transformer<MyLink, Double> edge_priors = new org.apache.commons.collections15.Transformer<MyLink, Double>() {
//			public Double transform(MyLink edge) {
//				return edge.weight;
//			}
//		};
		PageRankWithPriors ranker_priors = new PageRankWithPriors(g,vertex_priors, 0.15);
		ranker_priors.setMaxIterations(10);
		System.out.println("before 500 loop");
		for (int i = 0; i < 500; i++) {
			ranker_priors.step();
		}
		System.out.println("after 500 loop");
		for (MyNode vertex : vertices) {
			double result=Double.valueOf( ranker_priors.getVertexScore(vertex).toString());
			
		
//			System.out.println("The ranker_priors score of " + vertex + " is: "
//					+result );
			out1.println( vertex + ","+ result);
			out1.flush();
		}	
		out1.close();
//		System.out.println(" out put finish:"+path);
	}
	
	
	
	public static void loadingNodesScore(String path_loading_name ) throws Exception{
		System.out.println("load node score: "+path_loading_name);
		Iterator nodes=g.getVertices().iterator();
		while(nodes.hasNext()){
		    MyNode myNode=(MyNode)nodes.next();
		    myNode.setPrior(0d);
		}
		BufferedReader bin = new BufferedReader(new InputStreamReader(
				new FileInputStream(path_loading_name)));
		String readline;
		Double sum=0.0;
		while ((readline = bin.readLine()) != null) {
			// articleId, type, sorces[]
			String linestring[] = readline.split(",");
			double score = Double.valueOf(linestring[1]);
			sum+=score;
		}
		System.out.println("load node score= "+sum);
		bin.close();
		bin = new BufferedReader(new InputStreamReader(
				new FileInputStream(path_loading_name)));
		while ((readline = bin.readLine()) != null) {
			// articleId, type, sorces[]
			String linestring[] = readline.split(",");
			double score = Double.valueOf(linestring[1]);
			MyNode n=getNode(linestring[0]);
			if(n!=null)
			{
				n.setPrior(score/sum);
			}
			
		}
		bin.close();
//		for(MyNode node:g.getVertices())
//		{
//			if(node.node_prior>0)
//			{
//				bufOut = new BufferedWriter(new FileWriter(filePath+"log.txt",true));
//				 bufOut.write("\r\n"+node.id + "-----" + node.node_prior);
//				 bufOut.close();
//			}
//		}

	}

	public static void loadingEdges(String path_loading_name) throws Exception {
		try {
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(path_loading_name)));
			String readline;
			int i = 0;
			// out2 = new PrintWriter(new
			// FileWriter(filePath+"acm200truePaperCitePaper.csv"));
			while ((readline = bin.readLine()) != null) {
				if (!readline.contains("id")) {
					String linestring[] = readline.split(",");

					MyNode beginNode = getNode(linestring[0]);
					MyNode endNode = getNode(linestring[1]);
					System.out.println(beginNode.id+","+endNode.id);
					if (beginNode != null && endNode != null) {
						MyLink edge = new MyLink(i, 0);
						g.addEdge(edge, beginNode, endNode, EdgeType.DIRECTED);
						
						// out2.flush();
						i++;
					}
					System.out.println("load edges at line : " + i);
				}
				
			}
			bin.close();
			// out2.close();
		} catch (Exception e) {
			System.out.println("loading error");
			e.printStackTrace();
		}

	}

	public static void loadingNodes(String path_loading_name) throws Exception {
		HashMap keyScore = new HashMap();
		BufferedReader bin = new BufferedReader(new InputStreamReader(
				new FileInputStream(path_loading_name)));
		String readline;
		while ((readline = bin.readLine()) != null) {
			// articleId, type, sorces[]
			if (!readline.contains("id")) {
				double score = 0.0;
				MyNode node1 = new MyNode(readline, score);
//				 System.out.println(node1.id + "-----" + node1.node_prior );
				g.addVertex(node1);
			}

		}
		bin.close();
	}

	public static MyNode getNode(String nodeId) {
		Iterator nodes = g.getVertices().iterator();
		while (nodes.hasNext()) {
			MyNode myNode = (MyNode) nodes.next();
			if (nodeId.hashCode() == myNode.id.hashCode()) {
				return myNode;
			}
		}
		return null;
	}
	
}

