package com.ir.hw5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TrecEvalTest {

	// Maps to store the values for QREL and Results file.
	/*
	 * static Map<Integer, Map<String, String>> qrelsList = new HashMap<Integer,
	 * Map<String, String>>(); static Map<Integer, Map<String, String>>
	 * resultsList = new HashMap<Integer, Map<String, String>>();
	 */

	static Map<Integer, List<QrelObj>> qrelsList = new HashMap<Integer, List<QrelObj>>();
	static Map<Integer, List<QrelObj>> resultsList = new HashMap<Integer, List<QrelObj>>();

	static Map<Integer, Double> avgPrecMap = new HashMap<Integer, Double>(); // QueryWise

	// Maps which store precision,recall,f1 values for each URL.
	static Map<Integer, List<Double>> precisionMap = new HashMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> recallMap = new HashMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> f1Map = new HashMap<Integer, List<Double>>();

	static double rPrecisionValue = 0.0;

	public static void main(String[] args) {

		// read the Qrel File
		qrelsList = readQrelsFile("qrels.adhoc.51-100.AP89");

		// /print Qrel Valuess
		// printQrel(qrelsList);
		// read the results File

		resultsList = readResultsFile("okapi-FINAL-1");
		// /print results Valuess
		// printQrel(resultsList);

		// calculate Precision Values
		getPrecision();

		// Print Precision Values
		calAvgPrecision();
	}

	public static double calAvgPrecision() {
		double avg = 0.0;
		int count = 0;
		for (double d : avgPrecMap.values()) {

			// System.out.println("Precision for query :: "+count+"is:: "+ d);
			avg = avg + d;
			count++;
		}
		avg = avg / 25;
		System.out.println("total avg precsion" + avg);
		return avg;
	}

	private static void printQrel(Map<Integer, Map<String, String>> qrelsList2) {

		for (Map.Entry<Integer, Map<String, String>> q : qrelsList2.entrySet()) {
			// System.out.println("For Query:: "+ q.getKey());

			Map<String, String> query = new HashMap<String, String>();
			query = q.getValue();
			// System.out.println("Values Size:: "+ query.size());

			/*
			 * for(Map.Entry<String,String> u:query.entrySet()){
			 * System.out.println(u.getKey()+" "+ u.getValue()); }
			 */
		}

	}

	private static Map<Integer, List<QrelObj>> readQrelsFile(String fileName) {
		// read the file
		File file = new File("C:/Users/Nitin/Assign5/Final/" + fileName
				+ ".txt");
		Map<Integer, List<QrelObj>> maps = new HashMap<Integer, List<QrelObj>>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");

				int queryNo = Integer.parseInt(line[0]);
				String url = line[2];
				String rank = line[3];

				if (maps.containsKey(queryNo)) {
					List<QrelObj> tempMap = maps.get(queryNo);
					QrelObj q = new QrelObj();
					q.setRank(rank);
					q.setUrl(url);
					tempMap.add(q);
					maps.put(queryNo, tempMap);
				} else {
					List<QrelObj> tempMap = new ArrayList<QrelObj>();
					QrelObj q = new QrelObj();
					q.setRank(rank);
					q.setUrl(url);
					tempMap.add(q);
					maps.put(queryNo, tempMap);
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Qrels Map Size::: " + maps.size());
		return maps;
	}

	private static Map<Integer, List<QrelObj>> readResultsFile(String fileName) {
		// read the file
		File file = new File("C:/Users/Nitin/Assign5/Final/" + fileName
				+ ".txt");
		// Map<Integer, Map<String,String>> maps = new HashMap<Integer,
		// Map<String,String>>();
		Map<Integer, List<QrelObj>> maps = new HashMap<Integer, List<QrelObj>>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";
			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				int queryNo = Integer.parseInt(line[0]);
				String url = line[2];
				String rank = line[3];

				if (maps.containsKey(queryNo)) {
					List<QrelObj> tempMap = maps.get(queryNo);
					QrelObj q = new QrelObj();
					q.setRank(rank);
					q.setUrl(url);
					tempMap.add(q);
					maps.put(queryNo, tempMap);
				} else {
					List<QrelObj> tempMap = new ArrayList<QrelObj>();
					QrelObj q = new QrelObj();
					q.setRank(rank);
					q.setUrl(url);
					tempMap.add(q);
					maps.put(queryNo, tempMap);
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//System.out.println("Results Map Size::: " + maps.size());
		return maps;
	}

	private static void getPrecision() {

		for (Map.Entry<Integer, List<QrelObj>> result : resultsList.entrySet()) {

			int queryNum = result.getKey();
			List<QrelObj> retrievedDocs = result.getValue();
			List<String> relevantDocs = getRelevantDocs(qrelsList.get(queryNum));
			/* Set<String> relevantDocs = (Set<String>) relevant.ge values(); */

			int tp = 0, fp = 0, count = 0;
			int relevantCount = relevantDocs.size();
			// System.out.println("Retrieved Docs::: "+retrievedDocs.size());
			// System.out.println("Relevant Docs::: "+relevantCount);

			double avgPrecision = 0.0;
			double rPrecision = 0.0;

			for (QrelObj url : retrievedDocs) {

				if (relevantDocs.contains(url.getUrl())) {
					//System.out.println(url.getUrl());
					tp++;
					avgPrecision = avgPrecision + (double) tp / (tp + fp);
				} else {
					fp++;
				}

				// System.out.println("Count:: " + count);
				count++;

				double precision = (double) tp / (double) (tp + fp);
				double recall = (double) tp / relevantCount;
				double f1 = (double) (2 * precision * recall)
						/ (double) (precision + recall);

				if (precision == recall && count > 1 && precision > 0.0) {
					rPrecision = precision;
					 System.out.println("R-Precision @ " + count + " is " +
					 rPrecision);
					 
				}

				if (count == 5 || count == 10 || count == 20 || count == 50
						|| count == 100) {
					System.out.println("Precision @ " + count + " for query "
							+ queryNum + " is:" + precision);
					 System.out.println("Recall @ " + count + " is:" +
					 recall);
					 System.out.println("F1 @ " + count + " is:" + f1);

					
				}
				
				
				if (precisionMap.containsKey(queryNum)) {
					List<Double> precList = precisionMap.get(queryNum);
					precList.add(precision);
					precisionMap.put(queryNum, precList);
				} else {
					List<Double> precList = new ArrayList<Double>();
					precList.add(precision);
					precisionMap.put(queryNum, precList);
				}

				if (recallMap.containsKey(url)) {
					List<Double> precList = recallMap.get(queryNum);
					precList.add(recall);
					recallMap.put(queryNum, precList);
				} else {
					List<Double> precList = new ArrayList<Double>();
					precList.add(recall);
					recallMap.put(queryNum, precList);
				}

				if (f1Map.containsKey(url)) {
					List<Double> precList = f1Map.get(queryNum);
					precList.add(precision);
					f1Map.put(queryNum, precList);
				} else {
					List<Double> precList = new ArrayList<Double>();
					precList.add(f1);
					f1Map.put(queryNum, precList);
				}

				

			}

			System.out.println("Average Precision for query " + queryNum
					+ " is :" + (double) avgPrecision / relevantCount);
			avgPrecMap.put(queryNum, (double) avgPrecision / relevantCount);

		}

	}

	private static List<String> getRelevantDocs(List<QrelObj> map) {

		List<String> relDocs = new ArrayList<String>();
		for (QrelObj d : map) {

			if (d.getRank().equals("1"))
				relDocs.add(d.getUrl());
		}
		// System.out.println("relevant Docs Size:: "+relDocs.size());
		return relDocs;
	}

}
