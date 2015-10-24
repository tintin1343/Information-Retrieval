package com.ccs.ir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class GetRootSet {
	static Map<String, String> linkMap = new HashMap<String, String>();
	static int count = 0;
	static Node node = nodeBuilder().client(true).clusterName("IR").node();
	
	public static void main(String[] args) {
		try{
			
		Client client = node.client();
		Map<String, String> inLinkMap = new HashMap<String, String>();
		Map<String, String> outLinkMap = new HashMap<String, String>();
		

		QueryBuilder qb = QueryBuilders.matchQuery("text", "Obama");

		SearchResponse scrollResp = client.prepareSearch("spider")
				.setSearchType(SearchType.SCAN).setTypes("document").setQuery(qb)
				.setScroll(new TimeValue(60000)).setSize(1000).execute()
				.actionGet(); // 1000 hits per shard will be returned for each
								// scroll

		// Scroll until no hits are returned
		while (true) {
			System.out.println("In While");

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				System.out.println("In hit");
				// Handle the hit...
				String doc_no = (String) hit.getSource().get("docno");
				String inLinks = (String) hit.getSource().get("in_links");
				String outLinks = (String) hit.getSource().get("out_links");

			
						inLinkMap.put(doc_no, inLinks);
						outLinkMap.put(doc_no, outLinks);
						count++;

						System.out.println("Count :: " + count);
		

			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(600000)).execute().actionGet();
			// Break condition: No hits are returned
			if (count<=1000) {
				break;
			}
		}

		if (node != null) {
			node.close();
			node = null;
		}	
		
		}catch(Exception e){
			System.out.println("In Error...");
			e.printStackTrace();
			node.close();
		}

	}

}
