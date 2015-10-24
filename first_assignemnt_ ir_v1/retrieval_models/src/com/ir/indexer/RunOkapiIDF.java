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
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

public class RunOkapiIDF {
	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length != 1) { throw new
		 * IllegalArgumentException("Only Need config file."); }
		 */
		try {
			/*Config config = new Config("./configs/config.properties");
			String clusterName = config.getString("cluster.name");*/
			// starts client
			/*Node node = nodeBuilder().client(true).node();
			Client client = node.client();*/
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

			List<String> queries = readQueryFile(query_file_path);

			/*
			 * Method to get stopwords from the file and append the common words
			 * from query file
			 */
			
			/*Changed the split regex from space to space and hypen*/
			List<String> stop_words_final = getStopWords();

			/*List<String> stop_words_final = new ArrayList<String>();

			for (int i = 0; i < stop_words_custom.length; i++) {
				stop_words_final.add(stop_words_custom[i]);
			}*/

			/*
			 * Method to remove stopwords from query and just get the final
			 * query
			 */

			/*
			 * Iterating the queries one by one. Each Query is a list of String
			 * (Query Words)
			 */
			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = getFinalQueryList(queries, stop_words_final);

			/*
			 * for (List<String> query : final_query) {
			 * System.out.println("___________________________"); for (String q
			 * : query) { System.out.println(q); }
			 * System.out.println("___________________________"); }
			 */

			List<List<String>> resultOkapi = new ArrayList<List<String>>();
			/* for (String query : queries) { */
			for (List<String> query : final_query) {
				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;
				System.out.println("Query Minus stop words");
				System.out.println("=======================");
				// System.out.println(query.get(0));
				int y = 0;

				/*For every word in a query calculates the okapif value and sums it up*/	
				for (String q : query) {
					/* Method to calculate OkapiTF */
					if (y == 0) {
						// System.out.println("Nummber::: "+ q);
						querynum = q.replace(".", "");
						// System.out.println("Query Number::: " + querynum);
					}
					if (y > 0) {
						//System.out.println("Calculating for Word::: " + q);
						OkapiIDF(client, rankTerm, q, avg_doc_length);
					}
					y++;
				}

				/* Method to Sort Hashmap based on the value */
				LinkedHashMap<String, Double> sortedRanks = (LinkedHashMap<String, Double>) getSortedMap(rankTerm);

				int j = 1;
				List<String> queryResults = new ArrayList<String>();

				for (Entry<String, Double> term : sortedRanks.entrySet()) {

					if (j <= 100) {
						/*System.out.println(querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP");*/

						String toWrite = querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP";

						queryResults.add(toWrite);

					} else {
						// bw.newLine();
						break;
					}

					j++;
				}

				resultOkapi.add(queryResults);

			}

			writeToFile(resultOkapi);
			//node.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static List<List<String>> getFinalQueryList(List<String> queries,
			List<String> stop_words_final) {
		// TODO Auto-generated method stub

		List<List<String>> queries_Final = new ArrayList<List<String>>();

		for (String query : queries) {

			String[] query_minus_stop = query.split("\\s|-");
			/* calculate the query by removing the stop words */
			List<String> final_query = new ArrayList<String>();

			for (int i = 0; i < query_minus_stop.length; i++) {

				if (!(stop_words_final.contains(query_minus_stop[i]))
						&& !(query_minus_stop[i].equals(""))
				/* && !(final_query.contains(query_minus_stop[i])) */
				) {

					/*
					 * final_query.add(query_minus_stop[i].replaceAll("[.]",
					 * ""));
					 */
					final_query.add(query_minus_stop[i]);
				}
			}
			queries_Final.add(final_query);
		}

		return queries_Final;
	}

	private static void writeToFile(List<List<String>> resultOkapi) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Started File Creation..." + "@ " + new Date());
			File file = new File("C:/Users/Nitin/okapi-idf-3.txt");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			System.out.println("Started File write..." + "@ " + new Date());
			for (List<String> queryRes : resultOkapi) {
				for (String out : queryRes) {
					bw.write(out);
					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
			System.out.println("Closed File .." + "@ " + new Date());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void OkapiIDF(Client client,/* Map<String, Integer> results, */
			Map<String, Double> rankTerm, String word, Double avg_doc_length)
			throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		QueryBuilder qb = QueryBuilders.matchQuery("text", word);

		results = queryTF(client, qb, "ap_dataset_4", "document");
		int idf = results.size();

		//System.out.println("Size of TF Results:: " + results.size() +"for :"+word);

		for (Map.Entry<String, Integer> term : results.entrySet()) {

			StatisticalFacet stats1 = getStatsOnTextTerms(client,
					"ap_dataset_4", "document", "docno", term.getKey());

			if (rankTerm.get(term.getKey()) != null) {
				rankTerm.put(
						term.getKey(),
						(rankTerm.get(term.getKey()) + okapiIDFPerTerm(
								term.getValue(), stats1.getTotal(),
								avg_doc_length,idf)));
			} else {
				rankTerm.put(
						term.getKey(),
						okapiIDFPerTerm(term.getValue(), stats1.getTotal(),
								avg_doc_length,idf));
			}
		}

		// System.out.println("Ranking for the words::::: " + word);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> getSortedMap(
			Map<K, V> rankTerm) {
		System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Double.parseDouble(o1.getValue().toString()) > Double
						.parseDouble(o2.getValue().toString()) ? -1 : Double
						.parseDouble(o1.getValue().toString()) == Double
						.parseDouble(o2.getValue().toString()) ? 0 : 1;

			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	/*
	 * @SuppressWarnings("unchecked") private static LinkedHashMap<String,
	 * Double> getSortedMap(Map<String, Double> rankTerm) { // TODO
	 * Auto-generated method stub System.out.println("Started Sorting.."); List
	 * mapKeys = new ArrayList(rankTerm.keySet()); List mapValues = new
	 * ArrayList(rankTerm.values()); Collections.sort(mapValues,
	 * Collections.reverseOrder()); Collections.sort(mapKeys);
	 * 
	 * @SuppressWarnings("rawtypes") LinkedHashMap sortedMap = new
	 * LinkedHashMap();
	 * 
	 * Iterator valueIt = mapValues.iterator(); while (valueIt.hasNext()) {
	 * Object val = valueIt.next(); Iterator keyIt = mapKeys.iterator();
	 * 
	 * while (keyIt.hasNext()) { Object key = keyIt.next(); String comp1 =
	 * rankTerm.get(key).toString(); String comp2 = val.toString();
	 * 
	 * if (comp1.equals(comp2)){ rankTerm.remove(key); mapKeys.remove(key);
	 * sortedMap.put((String)key, (Double)val); break; }
	 * 
	 * }
	 * 
	 * } System.out.println("Finished Sorting.."); return sortedMap;
	 * 
	 * }
	 */

	private static List<String> getStopWords() {
		// TODO Auto-generated method stub

		File stop_words = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt");
		String stop;
		//StringBuffer stop_words_list = new StringBuffer();
		List<String> stop_words_final = new ArrayList<String>();

		/*for (int i = 0; i < stop_words_custom.length; i++) {
			stop_words_final.add(stop_words_custom[i]);
		}*/
		
		try {
			if (stop_words.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					/*stop_words_list.append(stop).append(" ");*/
					stop_words_final.add(stop.trim());

				}
				stop_words_final.add("discuss");
				stop_words_final.add("identify");
				stop_words_final.add("report");
				stop_words_final.add("include");
				stop_words_final.add("predict");
				stop_words_final.add("cite");
				stop_words_final.add("describe");
				stop_words_final.add("Document");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stop_words_final;
	}

	private static List<String> readQueryFile(String query_file_path)
			throws IOException {
		// TODO Auto-generated method stub

		String str;
		//StringBuffer query = new StringBuffer();
		List<String> query_list = new ArrayList<String>();

		try {
			File query_file = new File(query_file_path);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(query_file)));

			while ((str = br.readLine()) != null && (str != " ")) {
				// query.append(str.trim());
				int startIndex = 0;
				String q = str.trim();
				 //q = str.replaceAll("[,!?\\()\"]", "");

				if (q.length() > 0) {

					int endIndexofStop = q.lastIndexOf(".");

					query_list.add(q.substring(startIndex, endIndexofStop)
							.trim());
				}

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query_list;
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
	 * POST ap_dataset/document/_search { "query": { "match": { "docno":
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
	 * Sense: POST /ap_dataset/document/_search { "query": {"match_all": {}},
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
