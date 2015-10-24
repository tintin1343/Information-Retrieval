package com.ccs.ir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class ReadIndexAndBuild {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Map<String, String> linkMap = new HashMap<String, String>();
		int count = 0;
		Node node = nodeBuilder().client(true).clusterName("IR").node();

		try{
			
		
		Client client = node.client();

		QueryBuilder qb = QueryBuilders.matchAllQuery();
		
			SearchResponse scrollResp = client.prepareSearch("spider")
				.setSearchType(SearchType.SCAN).setTypes("document").setQuery(qb)
				.setScroll(new TimeValue(60000)).setSize(1000).execute()
				.actionGet(); // 1000 hits per shard will be returned for each
								// scroll

		File file = new File("C:/Users/Nitin/Assign4/mycrawl_inlinks_final.txt");

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		// Scroll until no hits are returned
		while (true) {
			System.out.println("In While");

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				System.out.println("In hit");
				// Handle the hit...
				String doc_no = (String) hit.getSource().get("docno");
				String inLinks = (String) hit.getSource().get("in_links");
				
			
					if(!linkMap.containsKey(doc_no)){
						linkMap.put(doc_no, inLinks);
						String[] out = inLinks.split("\\|\\|");
						
						String inLinkList="";
						
						for(String in:out){
							inLinkList += in+" ";
						}
						
						bw.write(doc_no + " " + inLinkList.trim());

						bw.newLine();
						// System.out.println("Added in Map");
						count++;

						System.out.println("Count :: " + count);
						// System.out.println("LinkMap Size:: "+ linkMap.size());
					}
		

			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(600000)).execute().actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

		System.out.println("LinkMap Size:: " + linkMap.size());
		bw.flush();
		bw.close();
		fw.close();
		
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
