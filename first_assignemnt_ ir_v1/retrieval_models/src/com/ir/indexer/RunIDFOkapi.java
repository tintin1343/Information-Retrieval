
package com.ir.indexer;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilder.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

public class RunIDFOkapi {
	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length != 1) { throw new
		 * IllegalArgumentException("Only Need config file."); }
		 */
		try {
			//Config config = new Config("./configs/config.properties");
			//String clusterName = config.getString("cluster.name");
			// starts client
			/*Node node = nodeBuilder().client(true).node();
			Client client = node.client();*/
			Client client = new TransportClient().addTransportAddress(new
					 InetSocketTransportAddress("localhost", 9300));
			
			/* Map to store the results for every */

			StatisticalFacet stats =getStatsOnTextTerms(client,
					"ap_dataset_2", "document", null, null);

			Double avg_doc_length = stats.getMean();

			System.out.println("Avg Doc Length:::: " + avg_doc_length);

			/* Method to read the query file */

			String query_file_path = "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
			/* This will be later replaced by path from the config file */

			GetFinalQueries qu = new GetFinalQueries();
			List<String> queries = qu.readQueryFile(query_file_path);

			/*
			 * Method to get stopwords from the file and append the common words
			 * from query file
			 */
			
			/*Changed the split regex from space to space and hypen*/
			List<String> stop_words_final = qu.getStopWords();

			/*
			 * Method to remove stopwords from query and just get the final
			 * query
			 */

			/*
			 * Iterating the queries one by one. Each Query is a list of String
			 * (Query Words)
			 */
			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = qu.getFinalQueryList(queries, stop_words_final);

			List<List<String>> resultbm25 = new ArrayList<List<String>>();

			for (List<String> query : final_query) {
				
				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;
				//System.out.println("Query Minus stop words");
				System.out.println("=======================");
				// System.out.println(query.get(0));
				//int y = 0;
				
				querynum = query.get(0).replace(".", "");
				/*For every word in a query calculates the okapif value and sums it up*/	
				for (int y=1;y<query.size();y++) {
					
					
					Map<String, Integer> tfMap = new HashMap<String, Integer>();
					// System.out.println("Calculating for Word::: " + q);
					tfMap =
						OkapiIDF(client, rankTerm, query.get(y).toLowerCase(), avg_doc_length);
					
					
					int idf = tfMap.size();
					
					StatisticalFacet stats1 ;
					//System.out.println("Size of TF Results:: " + tfMap.size() +"for :"+query.get(y).toLowerCase());

					for (Map.Entry<String, Integer> term : tfMap.entrySet()) {

						stats1 = getStatsOnTextTerms(client,
								"ap_dataset_2", "document", "docno", term.getKey());

						if (rankTerm.get(term.getKey()) != null) {
							rankTerm.put(
									term.getKey(),
									(rankTerm.get(term.getKey()) + okapiIDFPerTerm(term.getValue(), stats1.getTotal(),
											avg_doc_length,idf)));
						} else {
							rankTerm.put(
									term.getKey(),
									okapiIDFPerTerm(term.getValue(), stats1.getTotal(),
											avg_doc_length,idf));
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
				resultbm25.add(queryResults);
			}
			WriteFile w =new WriteFile();
			w.writeToFile(resultbm25,"okapiIDF-Final-1.txt");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, Integer> OkapiIDF(Client client,/* Map<String, Integer> results, */
			Map<String, Double> rankTerm, String word, Double avg_doc_length)
			throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		QueryBuilder qb = QueryBuilders.matchQuery("text", word);

		results = queryTF(client, qb, "ap_dataset_2", "document");
		
		/*int idf = results.size();
		
		StatisticalFacet stats1 ;
		System.out.println("Size of TF Results:: " + results.size() +"for :"+word);

		for (Map.Entry<String, Integer> term : results.entrySet()) {

			stats1 = getStatsOnTextTerms(client,
					"ap_dataset_2", "document", "docno", term.getKey());

			if (rankTerm.get(term.getKey()) != null) {
				rankTerm.put(
						term.getKey(),
						(rankTerm.get(term.getKey()) + bm25perTerm(
								term.getValue(), stats1.getTotal(),
								avg_doc_length,idf)));
			} else {
				rankTerm.put(
						term.getKey(),
						bm25perTerm(term.getValue(), stats1.getTotal(),
								avg_doc_length,idf));
			}
		}*/
		
		return results;
		
		// System.out.println("Ranking for the words::::: " + word);

	}

	

	/* Function for calculating OkapiTF for individual query terms */
	public static double okapiTFPerTerm(Integer termFreq, double docLen,
			double avgLen) {

		double LenRatio = (docLen / avgLen);
		double oTF = (double) (termFreq / (termFreq + 0.5 + (1.5 * LenRatio)));

		return oTF;

	}
	
	/* Function for calculating OkapiIDF for individual query terms */
	public static double okapiIDFPerTerm(Integer termFreq, double docLen,
			double avgLen, double idf) {

		double oTF = (double) okapiTFPerTerm(termFreq,docLen,avgLen);
		double oIDF = (double) ( oTF * Math.log(84678/idf));
		return oIDF;

	}

	/**
	 * V is the vocabulary size – the total number of unique terms in the
	 * collection.
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param field
	 * @return
	 */
	/*private static long getVocabularySize(Client client, String index,
			String type, String field) {
		MetricsAggregationBuilder aggregation = AggregationBuilders
				.cardinality("agg").field(field);
		SearchResponse sr = client.prepareSearch(index).setTypes(type)
				.addAggregation(aggregation).execute().actionGet();

		Cardinality agg = sr.getAggregations().get("agg");
		long value = agg.getValue();
		return value;

	}*/

/*	private static long getAvgSize(Client client, String index, String type,
			String field) {
		MetricsAggregationBuilder aggregation = AggregationBuilders.avg(
				"avg_doc_length").field(field);

		SearchResponse sr = client.prepareSearch(index).setTypes(type)
				.addAggregation(aggregation).execute().actionGet();

		InternalAvg agg = sr.getAggregations().get("avg_doc_length");
		long value = (long) agg.getValue();
		return value;

	}*/

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
	 * POST ap_dataset_2/document/_search { "query": { "match": { "docno":
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
	 * Sense: POST /ap_dataset_2/document/_search { "query": {"match_all": {}},
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

