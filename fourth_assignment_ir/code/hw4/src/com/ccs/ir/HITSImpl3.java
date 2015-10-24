package com.ccs.ir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.engine.Engine.Get;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class HITSImpl3 {

	public static void main(String[] args) {
		// preparing a Set of all Crawledd Links
		Set<String> crawledLinks = generateCrawledLinks("mycrawl_inlinks_final");
		// Elastic Properties
		Node node = nodeBuilder().client(true).clusterName("IR").node();
		Client client = node.client();

		// build inLinks and outLinks Map on the base set
		Map<String, Set<String>> inLinksMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> outLinksMap = new HashMap<String, Set<String>>();

		// create auth and hub Maps
		Map<String, Double> authsMap = new HashMap<String, Double>();
		Map<String, Double> hubsMap = new HashMap<String, Double>();

		// Set of Query Words
		Set<String> queryWords = new HashSet<String>();
		queryWords.add("Barack");
		queryWords.add("Obama");
		queryWords.add("ObamaCare");
		queryWords.add("Family");

		// Build a query
		QueryBuilder qb = QueryBuilders.boolQuery()
				.must(QueryBuilders.termsQuery("title", "obama"))
				.should(QueryBuilders.matchQuery("text", queryWords))
				.minimumNumberShouldMatch(3);

		// Search Response
		SearchResponse scrollResp = client.prepareSearch("spider")
				.setTypes("document")
				.addFields("docno", "in_links", "out_links")
				.setSearchType(SearchType.SCAN).setScroll(new TimeValue(6000))
				.setQuery(qb).execute().actionGet();

		int count = 0;

		// Calculate Root Set from the Crawled Links Map
		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {

				String url = (String) hit.getSource().get("docno");
				String inlinks = (String) hit.getSource().get("in_links");
				String in[] = inlinks.split("\\|\\|");
				Set<String> inSet = new HashSet<String>(Arrays.asList(in));
				inLinksMap.put(url, inSet);

				String outlinks = (String) hit.getSource().get("out_links");
				String out[] = outlinks.split("\\|\\|");
				Set<String> outSet = new HashSet<String>(Arrays.asList(out));
				outLinksMap.put(url, outSet);

				count++;

				if (count >= 1000)
					break;
			}

			if (count >= 1000)
				break;

			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(60000)).execute().actionGet();

			// when no search response,break
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

		// create Base Set
		// Step1: add URL's from rootset (Inlinks and OutLinks)
		for (String link : inLinksMap.keySet()) {
			Set<String> inlinks = inLinksMap.get(link);
			Set<String> outlinks = outLinksMap.get(link);
			authsMap.put(link, 1.0);
			hubsMap.put(link, 1.0);

			for (String li : outlinks) {
				if (crawledLinks.contains(li)) {
					authsMap.put(li, 1.0);
					hubsMap.put(li, 1.0);
				}
			}

			count = 0;
			
			for (String li : inlinks) {
				// can add a condition to get Obama Links..
				if (li.toLowerCase().contains("obama")
						|| li.toLowerCase().contains("family")
						|| li.toLowerCase().contains("barack_obama")
						|| li.toLowerCase().contains("obamacare"))
					authsMap.put(li, 1.0);
				hubsMap.put(li, 1.0);
				count++;
				if (count == 50)
					break;
			}
			
			for(String li:inlinks){
				if(count==50)
					break;
				if(!authsMap.containsKey(inlinks)){
					authsMap.put(li, 1.0);
					hubsMap.put(li,1.0);
					count++;
				}
			}

		}

		System.out.println("Base Set Size:: " + authsMap.size());

		// Step 2: Add the inlinks and outlinks from Elastic
		for (String li : authsMap.keySet()) {
			if (!inLinksMap.containsKey(li)) {

				QueryBuilder q = QueryBuilders.matchQuery("docno", li);

				SearchResponse response = client.prepareSearch("spider")
						.setTypes("document").setQuery(q).get();

				Set<String> inSet = new HashSet<String>();
				Set<String> outSet = new HashSet<String>();

				if (response.getHits().getHits().length > 0) {
					String outLinks = (String) response.getHits().getHits()[0]
							.getSource().get("out_links");
					String inLinks = (String) response.getHits().getHits()[0]
							.getSource().get("in_links");

					if (inLinks != null) {
						String in[] = inLinks.split("\\|\\|");
						for (int i = 0; i < in.length; i++) {
							if (i == 50)
								break;
							else {
								if (authsMap.containsKey(in[i])) {
									inSet.add(in[i]);
								}
							}
						}
					}
					if (outLinks != null) {
						String out[] = outLinks.split("\\|\\|");
						for (int i = 0; i < out.length; i++) {
							if (authsMap.containsKey(out[i])) {
								outSet.add(out[i]);
							}
						}
					}

				}

				inLinksMap.put(li, inSet);
				outLinksMap.put(li, outSet);
			}
		}

		// initialize flags for convergence decision
		boolean auth = true;
		boolean hub = true;

		int iter = 0; // too get the number of iterations

		Set<String> baseSet = hubsMap.keySet();
		Map<String, Double> tempA = new HashMap<String, Double>();
		Map<String, Double> tempH = new HashMap<String, Double>();

		tempA = new HashMap<String, Double>(authsMap);
		tempH = new HashMap<String, Double>(hubsMap);

		while (auth || hub) {
			// Initialize a normalization value

			Double normFactor = 0.0;
			// calculate for auths
			if (auth) {
				for (String authUrl : hubsMap.keySet()) {
					Double authWeight = 0.0;
					// get Inlinks for this auth
					Set<String> inlinks = inLinksMap.get(authUrl);
					/*
					 * for each inLink get hub Score and add it, assign it to
					 * the authWeight
					 */
					for (String in : inlinks)
						if (hubsMap.containsKey(in))
							authWeight += hubsMap.get(in);
					// calculate the normalizing factor
					normFactor += authWeight * authWeight;
					authsMap.put(authUrl, authWeight);
				}
				normFactor = Math.sqrt(normFactor);
				// Normalize al auth weights
				for (String authUrl : hubsMap.keySet()) {
					authsMap.put(authUrl, authsMap.get(authUrl) / normFactor);
				}
			}

			// calculate for hubs now.
			// reset norm to zero
			Double norm = 0.0;

			if (hub) {
				for (String hubUrl : authsMap.keySet()) {
					Double hubsWeight = 0.0;
					// get Inlinks for this auth
					Set<String> outLinks = outLinksMap.get(hubUrl);
					/*
					 * for each inLink get hub Score and add it, assign it to
					 * the authWeight
					 */
					for (String out : outLinks)
						if (authsMap.containsKey(out))
							hubsWeight += authsMap.get(out);
					// calculate the normalizing factor
					norm += hubsWeight * hubsWeight;
					hubsMap.put(hubUrl, hubsWeight);
				}
				norm = Math.sqrt(norm);
				// Normalize al auth weights
				for (String hubUrl : authsMap.keySet()) {
					hubsMap.put(hubUrl, hubsMap.get(hubUrl) / norm);
				}
			}

			// checking for convergence
			/*
			 * int hubsNorm = norm.intValue() % 10; // if previous norm value
			 * matches the new norm, increment count if (tempHub == hubsNorm) {
			 * countHub++; } else { countHub = 1; } // when count is 4, set flag
			 * to false, to break if (countHub == 4) { hub = false; } else {
			 * tempHub = hubsNorm; }
			 */

			/* check for convergence */

			Double tmpA = 0.0;
			Double tmpH = 0.0;
			Double a = 0.0;
			Double h = 0.0;

			for (String url : baseSet) {

				tmpA += tempA.get(url);
				tmpH += tempH.get(url);

				a += authsMap.get(url);
				h += hubsMap.get(url);

			}

			System.out.println("tmpA::: " + tmpA);
			System.out.println("a::: " + a);
			System.out.println("tmpH::: " + tmpH);
			System.out.println("h::: " + h);

			if ((a.intValue() % 10) == (tmpA.intValue() % 10)
					&& (h.intValue() % 10) == (tmpH.intValue() % 10)) {
				auth = false;
				hub = false;
			} else {
				tempA = new HashMap<String, Double>(authsMap);
				tempH = new HashMap<String, Double>(hubsMap);
			}

			iter++;
			System.out.println("Iteration:: " + iter);

		}
		// sort the auth and hub Maps to get the top 500
		authsMap = sortHITSMap(authsMap);
		hubsMap = sortHITSMap(hubsMap);

		// Once Sortedd, write output to file
		writeFile(authsMap, "AuthTop500");
		writeFile(hubsMap, "HubsTop500");

	}

	private static Map<String, Set<String>> getLinksMap(String fileName) {
		Map<String, Set<String>> urlToinLinks = new HashMap<String, Set<String>>();

		int count = 0;
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

			System.out.println("Total " + fileName + " Files Crawled:: "
					+ count);
			br.close();

		} catch (Exception e) {
			System.out.println("In Error..");
			e.printStackTrace();
		}

		return urlToinLinks;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortHITSMap(
			Map<K, V> hitsMap) {
		System.out.println("Started Sorting...@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				hitsMap.entrySet());
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
		System.out.println("Stopped Sorting...@ " + new Date());
		return result;
	}

	private static void writeFile(Map<String, Double> maps, String fileName) {

		try {
			File file = new File("C:/Users/Nitin/Assign4/HITSOutput/"
					+ fileName + ".txt");
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String str = "";

			int pageCount = 0;

			for (Map.Entry<String, Double> pr : maps.entrySet()) {
				str = pr.getKey() + "\t" + pr.getValue();
				out.write(str);
				out.newLine();
				pageCount++;

				if (pageCount == 500) {
					break;
				}
			}
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Set<String> generateCrawledLinks(String fileName) {
		Set<String> urlToinLinks = new HashSet<String>();
		int count = 0;
		File file = new File("C:/Users/Nitin/Assign4/" + fileName + ".txt");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			String str = "";
			while ((str = br.readLine()) != null) {
				String[] inUrls = str.split(" ");
				String url = inUrls[0];

				/*
				 * Set<String> inLinksforUrl = new HashSet<String>(); for (int i
				 * = 1; i < inUrls.length; i++) { inLinksforUrl.add(inUrls[i]);
				 * }
				 */

				urlToinLinks.add(url);
				count++;
			}

			System.out.println("Total Files Crawled:: " + count);
			br.close();

		} catch (Exception e) {
			System.out.println("In Error..");
			e.printStackTrace();
		}

		return urlToinLinks;
	}

}
