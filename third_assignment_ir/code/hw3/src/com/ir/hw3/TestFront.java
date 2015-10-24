package com.ir.hw3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestFront {

	public TestFront() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String, List<String>> frontier = new LinkedHashMap<String, List<String>>();
		URLCanonicalizer uc = new URLCanonicalizer();
	

		frontier.put(
				uc.getCanonicalURL("https://en.wikipedia.org/wiki/Barack_Obama"),
				new ArrayList<String>());
		frontier.put(
				uc.getCanonicalURL("https://en.wikipedia.org/wiki/Barack_Obama_presidential_campaign,_2008"),
				new ArrayList<String>());
		frontier.put(
				uc.getCanonicalURL("https://en.wikipedia.org/wiki/United_States_presidential_election,_2008"),
				new ArrayList<String>());
		//seeds added by me
		frontier.put(
				uc.getCanonicalURL("http://www.biography.com/people/barack-obama-12782369"),
				new ArrayList<String>());
		frontier.put(
				uc.getCanonicalURL("https://www.whitehouse.gov/1600/presidents/barackobama"),
				new ArrayList<String>());
		frontier.put(
				uc.getCanonicalURL("http://self.gutenberg.org/articles/barack_obama_presidential_campaign,_2008"),
				new ArrayList<String>());
		
		SortMap sm = new SortMap();
		sm.getSortedMap(frontier);
		
		
		for(Map.Entry<String, List<String>> a:frontier.entrySet()){
			System.out.println(a.getKey());
		}

	}

}
