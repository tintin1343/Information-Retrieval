package com.ccs.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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

public class PageRank1 {

	public static void main(String[] args) {
		// hashmap to store inLinksMap

		Map<String, Set<String>> urlToinLinks = new HashMap<String, Set<String>>();

		Double d = 0.85; // fixed value

		// read inLinks from LinkGraph and insert into the Map.
		urlToinLinks = generateInLinks("mycrawl_inlinks_final");

		// get outlinks count for a url and store it in a Map
		Map<String, Integer> outLinksCount = new HashMap<String, Integer>();
		outLinksCount = getOutLinksCount(urlToinLinks);

		// create a set with all uniqueLinks
		Set<String> uniqueLinkSet = new HashSet<String>();
		uniqueLinkSet = getuniqueLinks(outLinksCount.keySet(),
				urlToinLinks.keySet());

		int N = uniqueLinkSet.size(); // total Number of docs
		// get sinkLinks = Links which have no outLinks
		Set<String> sinkLinks = new HashSet<String>();
		sinkLinks = getSinkLinks(outLinksCount, uniqueLinkSet);
		System.out.println("Inlinks Size:: "+urlToinLinks.size());
		System.out.println("Sink:: "+sinkLinks.size());
		System.out.println("Outlinks :: "+outLinksCount.size());
		System.out.println("Total::: "+ (sinkLinks.size()+outLinksCount.size()));
		System.out.println("Consolidated:: "+ uniqueLinkSet.size());

		Map<String, Double> pageRank = new HashMap<String, Double>();
		Map<String, Double> temp = new HashMap<String, Double>();

		Double ratio = 1.0 / N;
		int count = 0;
		int initVal = 10;

		// initialzing a pagerank value for all URL's
		for (String l : uniqueLinkSet) {
			pageRank.put(l, ratio);
		}

		while (true) {

			Double sink = 0.0;
			Double perplex = calPerplexity(pageRank);
			System.out.println("Perplexity::: " + perplex);

			int val = perplex.intValue() % 10;

			if (initVal == val) {
				count++;
			} else {
				count = 0;
			}
			if (count == 4) {
				break;
			}

			initVal = val;

			for (String s : sinkLinks) {
				sink += pageRank.get(s);
			}

			for (String link : uniqueLinkSet) {

				Double newPR = (1 - d) / N;
				newPR += d * sink / N;
				temp.put(link, newPR);

				if (urlToinLinks.containsKey(link)) {
					Set<String> inLinks = urlToinLinks.get(link);

					for (String s : inLinks) {
						newPR = temp.get(link) + d * pageRank.get(s)
								/ outLinksCount.get(s);
						temp.put(link, newPR);
					}

				}

			}

			for (String s : uniqueLinkSet) {
				pageRank.put(s, temp.get(s));
			}

		}

		pageRank = sortPageRanks(pageRank);

		writePageRanks(pageRank, "pageRankObama1");

	}

	private static void writePageRanks(Map<String, Double> pageRank,
			String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File("C:/Users/Nitin/Assign4/output/" + fileName
					+ ".txt");
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String str = "";

			int pageCount = 0;

			for (Map.Entry<String, Double> pr : pageRank.entrySet()) {
				str = pr.getKey() + " " + pr.getValue() + " ";
				out.write(str);
				out.newLine();
				pageCount++;

				if (pageCount == 500) {
					break;
				}
			}
			out.close();

		} catch (Exception e) {

		}

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortPageRanks(
			Map<K, V> pageRank) {
		System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				pageRank.entrySet());
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

	private static Double calPerplexity(Map<String, Double> pageRank) {

		Double perplex = 0.0;

		for (Double p : pageRank.values()) {
			perplex += p * (Math.log10(p) / Math.log10(2));
		}

		perplex = -1 * perplex;
		perplex = Math.pow(2, perplex);

		return perplex;
	}

	private static Set<String> getuniqueLinks(Set<String> out, Set<String> in) {
		Set<String> uniqueLinkSet = new HashSet<String>();

		for (String i : in) {
			uniqueLinkSet.add(i);
		}

		for (String o : out) {
			if (!uniqueLinkSet.contains(o))
				uniqueLinkSet.add(o);
		}

		return uniqueLinkSet;
	}

	private static Set<String> getSinkLinks(Map<String, Integer> outLinksCount,
			Set<String> allLinks) {
		Set<String> sinkLinks = new HashSet<String>();

		for (String l : allLinks) {
			if (!outLinksCount.containsKey(l)) {
				sinkLinks.add(l);
			}
		}

		return sinkLinks;
	}

	private static Map<String, Integer> getOutLinksCount(
			Map<String, Set<String>> urlToinLinks) {
		Map<String, Integer> outLinksMap = new HashMap<String, Integer>();

		for (Set<String> inLinks : urlToinLinks.values()) {
			for (String link : inLinks) {
				if (outLinksMap.containsKey(link)) {
					Integer outLinksCount = outLinksMap.get(link);
					outLinksCount++;
					outLinksMap.put(link, outLinksCount);
				} else {
					outLinksMap.put(link, 1);
				}
			}

		}

		return outLinksMap;
	}

	private static Map<String, Set<String>> generateInLinks(String fileName) {
		Map<String, Set<String>> urlToinLinks = new HashMap<String, Set<String>>();

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
			}

			br.close();

		} catch (Exception e) {
			System.out.println("In Error..");
			e.printStackTrace();
		}

		return urlToinLinks;
	}

}
