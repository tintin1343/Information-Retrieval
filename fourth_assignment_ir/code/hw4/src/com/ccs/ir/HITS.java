package com.ccs.ir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class HITS {

	public static void main(String[] args) {

		Map<String, Double> authMap = new HashMap<String, Double>();
		Map<String, Double> hubMap = new HashMap<String, Double>();
		
		Map<String,Set<String>> inLinkMap = new HashMap<String, Set<String>>();
		Map<String,Set<String>> outLinkMap = new HashMap<String, Set<String>>();
		
		/*Set<String> allURLs = readMap("mycrawl_inlinks_final");

		Node node = nodeBuilder().client(true).clusterName("IR").node();

		Client client = node.client();

		QueryBuilder qb = QueryBuilders.matchQuery("text","obama");

		SearchResponse scrollResp = client.prepareSearch("spider").setTypes("document")
				.addFields("docno","in_links","out_links")
				.setSearchType(SearchType.SCAN)
				.setScroll(new TimeValue(6000))
				.setQuery(qb)
				.execute().actionGet(); 
		int count = 0;

		//Set<String> crawledLinks = new HashSet<String>();
		

		// get root set
		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {

				String url = (String) hit.field("docno").getValue();
				//crawledLinks.add(url);

				String inlinks = (String) hit.field("in_links").getValue();
				String il[] = inlinks.split("\\|\\|");
				Set<String> ilset = new HashSet<String>(Arrays.asList(il));
				ilset.remove(url);
				inLinkMap.put(url, ilset);

				String outlinks = (String) hit.field("out_links").getValue();
				String ol[] = outlinks.split("\\|\\|");
				Set<String> olset = new HashSet<String>(Arrays.asList(ol));
				olset.remove(url);
				outLinkMap.put(url, olset);

				count++;
				if(count>=1000)
					break;
			}
			//System.out.println(count);
			if(count>=1000)
				break;
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();

			//Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

		System.out.println("retrieved:"+inLinkMap.size());

		// Creating base set
		for(String link: inLinkMap.keySet()){
			Set<String> inlinks = inLinkMap.get(link);
			Set<String> outlinks = outLinkMap.get(link);

			for(String outlink:outlinks){
				if(allURLs.contains(outlink)){
					authMap.put(outlink, 1.0);
					hubMap.put(outlink, 1.0);
				}
			}

			count = 0;
			for(String inlink:inlinks){
				authMap.put(inlink, 1.0);
				hubMap.put(link, 1.0);
				count++;
				if(count==50)
					break;
			}
			authMap.put(link, 1.0);
			hubMap.put(link, 1.0);
		}

		System.out.println("base set created with size:"+authMap.size());
		// get inlinks and outlinks for urls in base set
		for(String url:authMap.keySet()){

			if(!inLinkMap.containsKey(url)){
				System.out.println("hitting ES");

				QueryBuilder qb1 = QueryBuilders.matchQuery("docno", url);
				SearchResponse response = client
						.prepareSearch("spider")
						.setTypes("document").setQuery(qb1)
						.get();

				Set<String> olset = new HashSet<String>();
				Set<String> ilset = new HashSet<String>();

				if (response.getHits().getHits().length > 0) {

					String outLinks1 = (String) response.getHits().getHits()[0].getSource().get("out_links");

					if(outLinks1!=null){
						String ol[] = outLinks1.split("\\|\\|");

						for(int i=0;i<ol.length;i++)
							if(authMap.containsKey(ol[i]))
								olset.add(ol[i]);
					}

					String inLinks1 = (String) response.getHits().getHits()[0].getSource().get("in_links");

					if(inLinks1!=null){
						String il[] = inLinks1.split("\\|\\|");

						for(int i=0;i<il.length;i++){
							if(i==50)
								break;
							if(authMap.containsKey(il[i]))
								ilset.add(il[i]);
						}
					}

				}
				outLinkMap.put(url, olset);
				inLinkMap.put(url, ilset);
				System.out.println("iteration:"+outLinkMap.size()+" "+inLinkMap.size());
			}
		}

		node.close();

		System.out.println("obtained all inlinks and outlinks");
		writeInLinkMap(inLinkMap,"baseSetInlinkmap");
		writeInLinkMap(outLinkMap,"baseSetOutlinkmap");*/
		
		inLinkMap = readLinksMap("baseSetInlinkmap");
		outLinkMap = readLinksMap("baseSetOutlinkmap");
		
		System.out.println("In Links COunt:: "+inLinkMap.size());
		System.out.println("Out Links COunt:: "+outLinkMap.size());
		
		for(String link: inLinkMap.keySet()){
			authMap.put(link, 1.0);
			hubMap.put(link, 1.0);
		}

		//Map<String, Double> tempAuthMap = new HashMap<String, Double>();
		//Map<String, Double> tempHubMap = new HashMap<String, Double>();
		int tempAuth = 10;
		int tempHub = 10;
		int authCount = 1;
		int hubCount = 1;
		
		boolean authflag = true, hubflag = true;

		int cc = 0;

		System.out.println("starting while loop...");
		//update till convergance
		while(authflag || hubflag){

			Double norm = 0.0;

			if(authflag){
				for(String url: hubMap.keySet()){
					Double auth = 0.0;
					Set<String> inlinks = inLinkMap.get(url);
					for(String inlink: inlinks)
						if(hubMap.containsKey(inlink))
							auth += hubMap.get(inlink);
					norm += auth*auth;
					authMap.put(url, auth);
				}
				norm = Math.sqrt(norm);
				for(String url: hubMap.keySet()){
					authMap.put(url, authMap.get(url)/norm);
				}
			}
			int authNorm = norm.intValue()%10;
			if(tempAuth==authNorm)
				authCount++;
			else
				authCount = 1;
			if(authCount==4)
				authflag = false;
			else
				tempAuth = authNorm;

			norm = 0.0;
			if(hubflag){
				for(String url: authMap.keySet()){
					Double hub = 0.0;
					Set<String> outlinks = outLinkMap.get(url);
					for(String outlink: outlinks)
						if(authMap.containsKey(outlink))
							hub += authMap.get(outlink);
					norm += hub*hub;
					hubMap.put(url, hub);
				}
				norm = Math.sqrt(norm);
				for(String url: authMap.keySet()){
					hubMap.put(url, hubMap.get(url)/norm);
				}
			}
			
			int hubNorm = norm.intValue()%10;
			if(tempHub==hubNorm)
				hubCount++;
			else
				hubCount = 1;
			if(hubCount==4)
				hubflag = false;
			else
				tempHub = hubNorm;

			cc++;
			System.out.println(cc);

		}

		System.out.println("sorting");


		authMap = sortScoreMap(authMap);
		hubMap = sortScoreMap(hubMap);

		System.out.println("writing score maps");

		writeScoreMap(authMap,"mycrawl_auths");
		writeScoreMap(hubMap,"mycrawl_hubs");

		/*writeInLinkMap(inLinkMap,"top1kInlinkmap");
		writeInLinkMap(outLinkMap,"top1kOutlinkmap");*/
		System.out.println("done");

		

	}

	private static void writeScoreMap(Map<String, Double> map, String fileName) {

		BufferedWriter output = null;
		try {
			File file = new File("C:/Users/Nitin/Assign4/HITSOutput/"+fileName+".txt");
			output = new BufferedWriter(new FileWriter(file));
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for(Map.Entry<String, Double> e: map.entrySet()){
				sb.append(e.getKey());
				sb.append(" ");
				sb.append(e.getValue());
				sb.append("\r\n");
				count++;
				if(count==500)
					break;
			}
			output.write(sb.toString());
			output.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static Map<String, Set<String>> readLinksMap(String fileName) {

		Map<String, Set<String>> result = new HashMap<String, Set<String>>();

		File file = new File("C:/Users/Nitin/Assign4/"+fileName+".txt");
		try {
			Scanner input = new Scanner(file);
			while(input.hasNext()) {
				String nextLine = input.nextLine();
				String[] urls = nextLine.split(" ");
				String url = urls[0];
				Set<String> inLinks = new HashSet<String>();

				for(int i=1;i<urls.length;i++)
					inLinks.add(urls[i]);
				result.put(url, inLinks);

			}
			input.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static Set<String> readMap(String fileName) {

		Set<String> urls = new HashSet<String>();

		File file = new File("C:/Users/Nitin/Assign4/"+fileName+".txt");
		try {
			Scanner input = new Scanner(file);
			while(input.hasNext()) {
				String nextLine = input.nextLine();
				String links[] = nextLine.split(" ");
				for(String link:links)
					if(!urls.contains(link))
						urls.add(link);
			}
			input.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
	}


	private static <K, V extends Comparable<V>> Map<K, V> sortScoreMap(final Map<K, V> scoreMap) {

		Comparator<K> valueComparator =  new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = scoreMap.get(k2).compareTo(scoreMap.get(k1));
				if (compare == 0) 
					return 1;
				else 
					return compare;
			}
		};
		Map<K, V> sortedScoreMap = new TreeMap<K, V>(valueComparator);
		sortedScoreMap.putAll(scoreMap);
		return sortedScoreMap;
	}

	private static void writeInLinkMap(Map<String, Set<String>> inLinkMap, String fileName) {

		BufferedWriter output = null;
		try {
			File file = new File("C:/Users/Nitin/Assign4/HITSOutput/"+fileName+".txt");
			output = new BufferedWriter(new FileWriter(file));

			for(Map.Entry<String, Set<String>> e: inLinkMap.entrySet()){
				StringBuilder sb = new StringBuilder();
				String url = e.getKey();
				sb.append(url);
				Set<String> inLinks = e.getValue();
				for(String link:inLinks)
					sb.append(" "+link);
				sb.append("\r\n");
				output.write(sb.toString());
			}

			output.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
