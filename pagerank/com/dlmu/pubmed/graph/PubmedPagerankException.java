package com.dlmu.pubmed.graph;

public class PubmedPagerankException extends Exception {

	public PubmedPagerankException() {
		super("An exception occurred in processing pagerank for the PubMed data");
	}

	public PubmedPagerankException(String message) {
		super("An exception occurred in processing pagerank for the PubMed data: " + message);
	}
	
	public PubmedPagerankException(String message, Exception e) {
		super("An exception occurred in processing pagerank for the PubMed data: " + message, e);
	}
}
