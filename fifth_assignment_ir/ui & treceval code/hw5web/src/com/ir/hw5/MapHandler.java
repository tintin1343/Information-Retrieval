package com.ir.hw5;

import java.util.Map;
import java.util.TreeMap;

public class MapHandler {
	static Map<String, QrelBean> qrel = new TreeMap<String, QrelBean>();
	
	public MapHandler() {
		
	}
	public MapHandler(String docId, QrelBean q) {
		
			this.qrel.put(docId,q);
			System.out.println("After put Size::: "+ qrel.size());
			
	}

	public Map<String, QrelBean> returnMap(){
		System.out.println("Total Map Size::: "+ qrel.size());
		return qrel;
	}

}
