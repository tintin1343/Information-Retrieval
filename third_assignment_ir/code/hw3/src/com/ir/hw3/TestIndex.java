package com.ir.hw3;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class TestIndex {

	public TestIndex() {
		// TODO Auto-generated constructor stub
	}

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Node node = nodeBuilder().client(true).clusterName("rnh").node();
		Client client = node.client();
		QueryBuilder qb = QueryBuilders.matchQuery("_id", 1001);
		queryTF(client,qb,"crawler","document");
		node.close();

	}*/
	
	public String checkURL(String url, Client client){
		QueryBuilder qb = QueryBuilders.matchQuery("_id", url);
		String inLinks = queryTF(client,qb,"crawler","document");
		return inLinks;
	}
	
	
	public String queryTF(Client client, QueryBuilder qb,
			String index, String type) {
		SearchResponse scrollResp = client.prepareSearch(index).setTypes(type)
				.setScroll(new TimeValue(6000)).setQuery(qb).setExplain(true)
				.setSize(1000).execute().actionGet();

		// no query matched
		if (scrollResp.getHits().getTotalHits() == 0) {
			//System.out.println("No Query matched..");
			return "";
		}else{
			while (true) {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String inLinks = (String) hit.getSource().get("in_links");
					
					if(inLinks != null){
						System.out.println("Got Old InLinks for:: "+ qb.toString());
						return inLinks;
					}else{
						return "";
					}
					
				
				}
				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
						.setScroll(new TimeValue(6000)).execute().actionGet();
				if (scrollResp.getHits().getHits().length == 0) {
					break;
				}
			}
		}
		
		
		return "";
		
	}
	
	

}
