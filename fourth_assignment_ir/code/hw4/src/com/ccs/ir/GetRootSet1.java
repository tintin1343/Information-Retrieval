package com.ccs.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetRootSet1 {

	public static void main(String[] args) {
		try{
			//generate All crawled links Graph
			 Set<String> crawledLinks = generateCrawledLinks("mycrawl_inlinks_final");
			
			//Generate Root set
			//Get All Inlinks from elastic
			Map<String, Set<String>> inLinkMap = getlinksMap("top1kInlinkmap");
			
			//Get All outLinks from elastic
			Map<String, Set<String>> outLinkMap = getlinksMap("top1kOutlinkmap");
			
			//Create a BaseSet
			//Create Auth
			Map<String, Double> Auth= new HashMap<String, Double>();
			//Create Hub
			Map<String, Double> Hub= new HashMap<String, Double>();
			
			//Add 
			for(String i:inLinkMap.keySet()){
				Auth.put(i, 1.0);
				Hub.put(i, 1.0);
			}
			//add outlinks
			for(Set<String> linkSet:outLinkMap.values()){
				for(String o:linkSet){
					Auth.put(o, 1.0);
					Auth.put(o, 1.0);
				}
			}
			
			
		}catch(Exception e){
			System.out.println("In Error...");
			e.printStackTrace();
		}

	}

	private static Map<String, Set<String>> getlinksMap(String fileName) {
		Map<String, Set<String>> urlToinLinks = new HashMap<String, Set<String>>();
		
		int count=0;
		File file = new File("C:/Users/Nitin/Assign4/" + fileName + ".txt");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			
			String str = "";
			while ((str = br.readLine()) != null) {
				String[] inUrls = str.split(" ");
				String url = inUrls[0];

				Set<String> inLinksforUrl = new HashSet<String>();
				for (int i = 1; i < inUrls.length; i++) {
					inLinksforUrl.add(inUrls[i]);
				}

				urlToinLinks.put(url, inLinksforUrl);
				count++;
			}
			
			System.out.println("Total"+ fileName +"Files Crawled:: "+ count);
			br.close();

		} catch (Exception e) {
			System.out.println("In Error..");
			e.printStackTrace();
		}

		return urlToinLinks;
	}


	private static Set<String> generateCrawledLinks(String fileName) {
		Set<String> urlToinLinks = new HashSet<String>();
		int count=0;
		File file = new File("C:/Users/Nitin/Assign4/" + fileName + ".txt");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			
			String str = "";
			while ((str = br.readLine()) != null) {
				String[] inUrls = str.split(" ");
				String url = inUrls[0];

				/*Set<String> inLinksforUrl = new HashSet<String>();
				for (int i = 1; i < inUrls.length; i++) {
					inLinksforUrl.add(inUrls[i]);
				}*/

				urlToinLinks.add(url);
				count++;
			}
			
			System.out.println("Total Files Crawled:: "+ count);
			br.close();

		} catch (Exception e) {
			System.out.println("In Error..");
			e.printStackTrace();
		}

		return urlToinLinks;
	}

}
