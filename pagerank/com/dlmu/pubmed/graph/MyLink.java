package com.dlmu.pubmed.graph;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;


public class MyLink implements Serializable{
	// double capacity; // should be private
	double weight; // should be private for good practice
	int id;

	public MyLink(int id, double weight) { // , double capacity
		this.id = id; // This is defined in the outer class.
		this.weight = weight;
		// this.capacity = capacity;
	}

	public String toString() { // Always good for debugging
		return "E" + id;
	}
}
