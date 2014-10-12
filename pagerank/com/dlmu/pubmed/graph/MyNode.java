package com.dlmu.pubmed.graph;

import java.io.Serializable;

public class MyNode implements Serializable{
	String id; // good coding practice would have this as private
	double node_prior = 0.0;

	public MyNode(String id, double node_prior) {
		this.id = id;
		this.node_prior = node_prior;
	}
	
	public MyNode(String id) {
		this.id = id;
		node_prior = 0.0;
	}
	
	public void reset() {
		node_prior = 0.0;
	} //end of reset
	
	public String toString() { // Always a good idea for debuging
		return id; // JUNG2 makes good use of these.
	}
	public void setPrior(double prior){
		this.node_prior=prior;
	}
	public double getPrior() {
		return(node_prior);
	}
}