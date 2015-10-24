package com.ir.indexer;


import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class RunOkapi {
	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length != 1) { throw new
		 * IllegalArgumentException("Only Need config file."); }
		 */
		try {
			
			Client client = new TransportClient().addTransportAddress(new
					 InetSocketTransportAddress("localhost", 9300));
			
			/* Map to store the results for every */

			StatisticalFacet stats = getStatsOnTextTerms(client,
					"ap_dataset_4", "document", null, null);

			Double avg_doc_length = stats.getMean();

			System.out.println("Avg Doc Length:::: " + avg_doc_length);

			/* Method to read the query file */

			String query_file_path = "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
			/* This will be later replaced by path from the config file */

			GetFinalQueries qu = new GetFinalQueries();
			List<String> queries = qu.readQueryFile(query_file_path);

			List<String> stop_words_final = qu.getStopWords();

			
			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = qu.getFinalQueryList(queries, stop_words_final);

			List<List<String>> okapi = new ArrayList<List<String>>();

			for (List<String> query : final_query) {
				
				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;
			
				System.out.println("=======================");
				
				
				querynum = query.get(0).replace(".", "");
				/*For every word in a query calculates the okapif value and sums it up*/	
				for (int y=1;y<query.size();y++) {
					
					/*PorterStemmer es =new PorterStemmer();
					es.setCurrent(query.get(y));
					String w = null;*/
					
					String w = query.get(y).toLowerCase();
					
					
					/*if(es.stem()){
						w=es.getCurrent();

					}else{
						w=query.get(y).toLowerCase();
						
					}*/
					
					Map<String, Integer> tfMap = new HashMap<String, Integer>();
					
					tfMap = Okapi(client, rankTerm, w, avg_doc_length);

					
					//System.out.println("Size of TF Results:: " + tfMap.size() +"for :"+w);

					for (Map.Entry<String, Integer> term : tfMap.entrySet()) {

						StatisticalFacet stats1 = getStatsOnTextTerms(client,
								"ap_dataset_4", "document", "docno", term.getKey());

						if (rankTerm.get(term.getKey()) != null) {
							rankTerm.put(
									term.getKey(),
									(rankTerm.get(term.getKey()) + okapiTFPerTerm(
											term.getValue(), stats1.getTotal(),
											avg_doc_length)));
						} else {
							rankTerm.put(
									term.getKey(),
									okapiTFPerTerm(term.getValue(), stats1.getTotal(),
											avg_doc_length));
						}
					}
					
					
				}
				SortMap sm =new SortMap();
				
				/* Method to Sort Hashmap based on the value */
				LinkedHashMap<String, Double> sortedRanks = (LinkedHashMap<String, Double>) sm.getSortedMap(rankTerm);

				int j = 1;
				List<String> queryResults = new ArrayList<String>();

				for (Entry<String, Double> term : sortedRanks.entrySet()) {

					if (j <= 100) {

						String toWrite = querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP";

						queryResults.add(toWrite);

					} else {
						break;
					}

					j++;
				}
				okapi.add(queryResults);
			}
			
			WriteFile w =new WriteFile();
			w.writeToFile(okapi, "okapi-FINAL-1.txt");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, Integer> Okapi(Client client,/* Map<String, Integer> results, */
			Map<String, Double> rankTerm, String word, Double avg_doc_length)
			throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		QueryBuilder qb = QueryBuilders.matchQuery("text", word);

		results = queryTF(client, qb, "ap_dataset_4", "document");

		return results;

	}

	

	/* Function for calculating OkapiTF for individual query terms */
	public static double okapiTFPerTerm(Integer termFreq, double docLen,
			double avgLen) {

		double LenRatio = (double)(docLen / avgLen);
		/*double oTF = (double) ((double) termFreq /(double) (termFreq + 0.5 + (1.5 * LenRatio)));*/

		return (double) ((double) termFreq /(double) (termFreq + 0.5 + (1.5 * LenRatio)));

	}
	
	

	/**
	 * return Pairs of <"decno", tf value> by given term query.
	 * 
	 * @param client
	 * @param qb
	 * @param index
	 * @param type
	 * @return
	 */
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

	/**
	 * get statistical facet by given docno or whole documents INFO including
	 * following: "facets": { "text": { "_type": "statistical", "count": 84678,
	 * "total": 18682561, "min": 0, "max": 802, "mean": 220.63063605659084,
	 * "sum_of_squares": 4940491417, "variance": 9666.573376838636,
	 * "std_deviation": 98.31873360066552 } }
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param matchedField
	 * @param matchedValue
	 * @return
	 * @throws IOException
	 */
	private static StatisticalFacet getStatsOnTextTerms(Client client,
			String index, String type, String matchedField, String matchedValue)
			throws IOException {
		XContentBuilder facetsBuilder;
		if (matchedField == null && matchedValue == null) { // match_all docs
			facetsBuilder = getStatsTermsBuilder();
		} else {
			facetsBuilder = getStatsTermsByMatchFieldBuilder(matchedField,
					matchedValue);
		}
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSource(facetsBuilder).execute().actionGet();
		StatisticalFacet f = (StatisticalFacet) response.getFacets()
				.facetsAsMap().get("text");
		return f;
	}

	/**
	 * builder for facets statistical terms length by given matched field, like
	 * docno. In Sense:
	 *
	 * POST ap_dataset_4/document/_search { "query": { "match": { "docno":
	 * "AP891216-0142" } }, "facets": { "text": { "statistical": { "script":
	 * "doc['text'].values.size()" } } } }
	 * 
	 * @param matchField
	 * @param matchValue
	 * @return
	 * @throws IOException
	 */
	private static XContentBuilder getStatsTermsByMatchFieldBuilder(
			String matchField, String matchValue) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().startObject("query").startObject("match")
				.field(matchField, matchValue).endObject().endObject()
				.startObject("facets").startObject("text")
				.startObject("statistical")
				.field("script", "doc['text'].values.size()").endObject()
				.endObject().endObject().endObject();
		return builder;
	}

	private static XContentBuilder getTF(
			String matchField, String matchValue) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().startObject("query").startObject("match")
				.field(matchField, matchValue).endObject().endObject()
				.startObject("facets").startObject("text")
				.startObject("statistical")
				.field("script", "_index[field][term].tf()").endObject()
				.endObject().endObject().endObject();
		return builder;
	}
	
	/**
	 * builder for the facets statistical terms length by whole documents. In
	 * Sense: POST /ap_dataset_4/document/_search { "query": {"match_all": {}},
	 * "facets": { "text": { "statistical": { "script":
	 * "doc['text'].values.size()" } } } }
	 * 
	 * @return
	 * @throws IOException
	 */
	private static XContentBuilder getStatsTermsBuilder() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().startObject("query").startObject("match_all")
				.endObject().endObject().startObject("facets")
				.startObject("text").startObject("statistical")
				.field("script", "doc['text'].values.size()").endObject()
				.endObject().endObject().endObject();
		return builder;
	}

}


