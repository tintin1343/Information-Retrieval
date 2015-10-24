package com.ir.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;

public class GetTFs {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Client client = new TransportClient().addTransportAddress(new
				 InetSocketTransportAddress("localhost", 9300));
		
		StatisticalFacet stats =new StatsElastic().getStatsOnTextTerms(client,
				"ap_dataset_4", "document", null, null);

		Double avg_doc_length = stats.getMean();
		
		GetFinalQueries qu = new GetFinalQueries();
		List<String> queries = new ArrayList<String>();
		
		queries.add("1. dai door divid invok");
		
		
		List<List<String>> final_query = new ArrayList<List<String>>();
		List<String> stop_words_final = qu.getStopWords();

		final_query = qu.getFinalQueryList(queries, stop_words_final);

		List<List<String>> result = new ArrayList<List<String>>();

		//System.out.println("Query Size::: "+ final_query.size());
		
		for (List<String> query : final_query) {
			//System.out.println("Inside Query List...");
			for (int y=1;y<query.size();y++) {
				//System.out.println("Inside query number:::"+1);
				System.out.println("__________________________________________________");
				System.out.println("For Query Word:: "+ query.get(y).toLowerCase());
				
				Map<String, Integer> tfMap = new HashMap<String, Integer>();
				
				tfMap =OkapiIDF(client, query.get(y).toLowerCase(), avg_doc_length);
				int idf = tfMap.size();
				
				System.out.println("doc_Freq for "+ query.get(y).toLowerCase() +":: "+ idf);

				//System.out.println("Size of TF Results:: " + tfMap.size() +"for :"+query.get(y).toLowerCase());

			}
			
		}

	}
	
	public static Map<String, Integer> OkapiIDF(Client client, String word, Double avg_doc_length)
			throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		QueryBuilder qb = QueryBuilders.matchQuery("text", word);

		results = queryTF(client, qb, "ap_dataset_4", "document");

		
		
		long ttf =0;
		
		for(Map.Entry<String, Integer> t : results.entrySet()){
			ttf+= t.getValue();
		}
		System.out.println("ttf for "+ word +":: "+ ttf);
		
		for (Map.Entry<String, Integer> term : results.entrySet()) {
			if(term.getKey().equals("AP890406-0045")){
				System.out.println("tf for term "+ word+ " :: "+term.getValue());
			}
		}
		
		// System.out.println("Ranking for the words::::: " + word);
		return results;
	}
	
	public static Map<String, Integer> queryTF(Client client, QueryBuilder qb,
			String index, String type) {
		SearchResponse scrollResp = client.prepareSearch(index).setTypes(type)
				.setScroll(new TimeValue(6000)).setQuery(qb).setExplain(true)
				.setSize(1000).execute().actionGet();

		// no query matched
		if (scrollResp.getHits().getTotalHits() == 0) {
			return new HashMap<String, Integer>();
		}
		Map<String, Integer> results = new HashMap<>();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String docno = (String) hit.getSource().get("docno");
				// int doc_length = (int) hit.getSource().get("doc_length");
				int tf = (int) hit.getExplanation().getDetails()[0]
						.getDetails()[0].getDetails()[0].getValue();
				
					results.put(docno, tf);
				
				
				
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(6000)).execute().actionGet();
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		return results;
	}
	

}
