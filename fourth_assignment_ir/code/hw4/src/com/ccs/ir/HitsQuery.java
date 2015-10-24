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

import org.elasticsearch.index.engine.Engine.Get;

public class HitsQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// build inLinks and outLinks Map on the base set
		Map<String, Set<String>> inLinksMap = new HashMap<String, Set<String>>();
		inLinksMap = getLinksMap("baseSetInlinkmap");
		Map<String, Set<String>> outLinksMap = new HashMap<String, Set<String>>();
		outLinksMap = getLinksMap("baseSetOutlinkmap");

		// create auth and hubs
		Map<String, Double> authsMap = new HashMap<String, Double>();
		Map<String, Double> hubsMap = new HashMap<String, Double>();

		// iterate inLinks and create auths and hubs
		for (String li : inLinksMap.keySet()) {
			authsMap.put(li, 1.0);
			hubsMap.put(li, 1.0);
		}

		// initialize counts
		int countHub = 1;
		int countAuth = 1;

		// initialize temp variables for auth and Hubs
		int tempAuth = 10;
		int tempHub = 10;

		// initialize flags for convergence decision
		boolean auth = true;
		boolean hub = true;

		int iter = 0; // too get the number of iterations
		boolean change= true;
		Set<String> baseSet = hubsMap.keySet();

		while (change) {
			// Initialize a normalization value
			Double normAuth = 0.0,normHub = 0.0;
			Map<String,Double> tempA = new HashMap<String, Double>();
			Map<String,Double> tempH = new HashMap<String, Double>();
			for(String url : baseSet){
				
				tempA = authsMap;
				tempH = hubsMap;

				Set<String> inlinks = inLinksMap.get(url);
				Set<String> outLinks = outLinksMap.get(url);
				Double authWeight = 0.0,hubWeight = 0.0;
				
				for (String in : inlinks) {
					if(hubsMap.containsKey(in))
					authWeight += hubsMap.get(in);
				}
				normAuth+= (authWeight*authWeight);
				for (String out : outLinks) {
					if(authsMap.containsKey(out))
						hubWeight += authsMap.get(out);					
				}
				normHub+= (hubWeight*hubWeight);
				authsMap.put(url,authWeight);
				hubsMap.put(url,hubWeight);

			}
			for(String url : baseSet){
				authsMap.put(url,authsMap.get(url)/normAuth);
				hubsMap.put(url,hubsMap.get(url)/normHub);
			}
			Double tmpA = 0.0;
			Double tmpH = 0.0;
			Double a=0.0;
			Double h=0.0;
			/*check for convergence*/
			for(String url : baseSet){
	
				tmpA += tempA.get(url);
				tmpH += tempH.get(url);
				
				a+=authsMap.get(url);
				h+=hubsMap.get(url);
			}
			System.out.println("tmpA::: "+ tmpA);
			System.out.println("a::: "+ a);
			System.out.println("tmpH::: "+ tmpH);
			System.out.println("h::: "+ h);
			if(a==tmpA && h==tmpH)
				change=false;
			
			iter++;
			System.out.println(iter);
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

		}

	}

}
