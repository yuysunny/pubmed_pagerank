package com.dlmu.pubmed.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class MyPaperGraph implements Serializable {
	public static Graph<MyNode, MyLink> g = new SparseMultigraph<MyNode, MyLink>();
	public static String filePath = "./output/";

	public static void main(String[] args) throws Exception {
		MyPaperGraph mygraph = new MyPaperGraph();
		System.out.println("loading Nodes file......");
		loadingNodes(filePath + "paperPageRank/pubmed_id.csv");
		System.out.println("loading Edges file......");
		loadingEdges(filePath + "paperPageRank/only pubmedcentral/citation_new.csv");

		try {
			FileOutputStream fs = new FileOutputStream(filePath
					+ "paperPageRank/ppGraph.ser");
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(mygraph.g);
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
