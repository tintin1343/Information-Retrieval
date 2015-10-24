package com.ir.hw5;

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

public class RunBM25 {
	public static void main(String[] args) throws IOException {
		
		try {
			
			Node node = nodeBuilder().client(true).clusterName("QREL").node();
			Client client = node.client();
						
			/* Map to store the results for every */

			StatisticalFacet stats = getStatsOnTextTerms(client,
					"spider", "document", null, null);

			Double avg_doc_length = stats.getMean();

			System.out.println("Avg Doc Length:::: " + avg_doc_length);

			
			List<List<String>> final_query = new ArrayList<List<String>>();

			/*final_query = getFinalQueryList(queries, new ArrayList<String>());
			*/
			List<String> qu = new ArrayList<String>();
			qu.add("151501");
			qu.add("Obama");
			qu.add("family");
			final_query.add(qu);
			List<String> qu1 = new ArrayList<String>();
			qu1.add("151502");
			qu1.add("Obama");
			qu1.add("election");
			qu1.add("2008");
			final_query.add(qu1);
			List<String> qu2 = new ArrayList<String>();
			qu2.add("151503");
			qu2.add("ObamaCare");
			final_query.add(qu2);
			
			System.out.println("Final Query Size::: "+final_query.size());

			List<List<String>> resultOkapi = new ArrayList<List<String>>();
			/* for (String query : queries) { */
			for (List<String> query : final_query) {
				
				Map<String, Double> rankTerm = new HashMap<String, Double>();
				System.out.println("Query::: "+ query);
				String querynum = null;
				System.out.println("Query Minus stop words");
				System.out.println("=======================");
				// System.out.println(query.get(0));
				int y = 0;
				
				
				/*For every word in a query calculates the okapif value and sums it up*/	
				for (String q : query) {
					
					System.out.println("Calculating for Word::: " + q);
					//System.out.println("Query words size::: "+query.size());
					/* Method to calculate OkapiTF */
					if (y == 0) {
						// System.out.println("Nummber::: "+ q);
						querynum = q.replace(".", "");
						System.out.println("Query Number::: " + querynum);
					}
					if (y > 0) {
						System.out.println("Calculating for Word::: " + q);
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
						System.out.println(querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP");

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
			node.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	private static void writeToFile(List<List<String>> resultOkapi) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Started File Creation..." + "@ " + new Date());
			File file = new File("C:/Users/Nitin/Assign5/Final/BM-25.txt");
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

		results = queryTF(client, qb, "spider", "document");
		int idf = results.size();
		System.out.println("Size of TF Results:: " + results.size() +"for :"+word);

		StatisticalFacet stats1 ;
		
		for (Map.Entry<String, Integer> term : results.entrySet()) {

			stats1 = getStatsOnTextTerms(client,
					"spider", "document", "docno", term.getKey());

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
		}

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

	

	
	
	/* Function for calculating OkapiIDF for individual query terms */
	public static double bm25perTerm(Integer termFreq, double docLen,
			double avgLen, double idf) {
		double k1 = 1.2;
		double k2 = 300;
		double b = 0.75;
		
		
		double a_bm25 = (double) Math.log((53147+0.5)/(idf+0.5));
		double b_bm25 = (double) termFreq+ (k1*termFreq);
		double b_d_bm25=(double) termFreq + (k1 * ((1-b)+ b*(docLen/avgLen)));
		double c_bm25 = 1 /*(double) (1+ k2 * 1)/(1+ k2);*/;

		
		double bm25 = (double) (a_bm25* (b_bm25/b_d_bm25)* c_bm25);
		return bm25;

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

		System.out.println("Total hits:: "+ scrollResp.getHits().getHits());
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
