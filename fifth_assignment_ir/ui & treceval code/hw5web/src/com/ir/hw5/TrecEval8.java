package com.ir.hw5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/* Confirmed working code for TrecEval. Also gets the correct graph values.
 * Includes NDCG as well. Printing output. 
 * New Code from Scratch. Works for Okapi, Matches our values. added args functionality!. 
 * NDCG Needs Fixing*/
public class TrecEval8 {

	static Map<Integer, Map<String, Integer>> qrelsList = new LinkedHashMap<Integer, Map<String, Integer>>();
	static Map<Integer, Map<String, Integer>> resultsList = new LinkedHashMap<Integer, Map<String, Integer>>();

	static Map<Integer, Double> avgPrecMap = new TreeMap<Integer, Double>(); // QueryWise

	// Maps which store precision,recall,f1 values for each URL.
	static Map<Integer, List<Double>> precisionMap = new LinkedHashMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> recallMap = new LinkedHashMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> f1Map = new LinkedHashMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> ndcgMap = new LinkedHashMap<Integer, List<Double>>();

	static double rPrecisionValue = 0.0;

	public static void main(String[] args) throws IOException {
		boolean flag = false;

		String qrelFile = "";
		String resultsFile = "";

		if (args.length == 3) {
			System.out.println("Arguments passed:: " + args[0] + " " + args[1]
					+ " " + args[2]);
			flag = true;
			qrelFile = args[1];
			resultsFile = args[2];
		} else {
			System.out.println("Arguments passed:: " + args[0] + " " + args[1]);
			qrelFile = args[0];
			resultsFile = args[1];
		}

		// read the Qrel File
		readQrelsFile(qrelFile);
		// /printQrel(qrelsList);

		readResultsFile(resultsFile, resultsList);

		// calculate Precision Values
		getPrecision(flag);

		// Print Total Average Precision Value
		calAvgPrecision();

		// print precision values for Plotting Graph
		printPrecisionandRecallValues(precisionMap, recallMap);

	}

	private static void printPrecisionandRecallValues(
			Map<Integer, List<Double>> precisionMap2,
			Map<Integer, List<Double>> recallMap2) throws IOException {
		File file1 = new File("C:/Users/Nitin/Assign5/Final/"
				+ "precisionValues1" + ".txt");

		File file2 = new File("C:/Users/Nitin/Assign5/Final/" + "recallValues1"
				+ ".txt");

		BufferedWriter out1 = new BufferedWriter(new FileWriter(file1));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(file2));

		String precisionValues = "";

		for (Map.Entry<Integer, List<Double>> pr : precisionMap2.entrySet()) {
			String str = "";
			List<Double> p = pr.getValue();
			Integer qno = pr.getKey();

			for (int i = 0; i < p.size(); i++) {
				if (i > 2)
					str = str + "\t" + p.get(i);
			}

			precisionValues = String.valueOf(qno) + "\t" + str;

			out1.write(precisionValues);
			out1.newLine();
		}

		precisionValues = "";

		for (Map.Entry<Integer, List<Double>> pr : recallMap2.entrySet()) {
			String str = "";
			List<Double> p = pr.getValue();
			Integer qno = pr.getKey();

			for (int i = 0; i < p.size(); i++) {
				if (i > 2)
					str = str + "\t" + p.get(i);
			}

			precisionValues = String.valueOf(qno) + "\t" + str;

			out2.write(precisionValues);
			out2.newLine();
		}

		out1.close();
		out2.close();

	}

	public static double calAvgPrecision() {
		double avg = 0.0;
		// int count = 0;
		for (double ap : avgPrecMap.values()) {

			// System.out.println("Precision for query :: "+count+"is:: "+ d);
			avg += ap;
			// count++;
		}

		avg = (double) avg / 3.0;
		System.out.println("Total Avg Precision: " + avg);
		return avg;
	}

	private static void printQrel(Map<Integer, Map<String, Integer>> qrelsList2) {

		for (Map.Entry<Integer, Map<String, Integer>> q : qrelsList2.entrySet()) {
			// System.out.println("For Query:: "+ q.getKey());

			Map<String, Integer> query = new HashMap<String, Integer>();
			query = q.getValue();
			// System.out.println("Values Size:: "+ query.size());

			for (Map.Entry<String, Integer> u : query.entrySet()) {
				System.out.println("Qrels::: " + u.getKey() + " "
						+ u.getValue());
			}

		}

	}

	private static void readQrelsFile(String fileName) {

		File file = new File("C:/Users/Nitin/Assign5/Final/" + fileName
				+ ".txt");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");

				int queryNo = Integer.parseInt(line[0]);
				String url = line[2];
				int rank = Integer.parseInt(line[3]);

				if (qrelsList.containsKey(queryNo)) {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap = qrelsList.get(queryNo);

					if (tempMap.containsKey(url)) {
						int r = tempMap.get(url);
						if (r < rank) {
							tempMap.put(url, rank);
						}
					} else {
						tempMap.put(url, rank);
					}

					qrelsList.put(queryNo, tempMap);
				} else {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap.put(url, rank);
					qrelsList.put(queryNo, tempMap);
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("Qrels Map Size::: " + maps.size());
		/* return qrelsList2; */
	}

	private static void readResultsFile(String fileName,
			Map<Integer, Map<String, Integer>> resultsList2) {
		// read the file
		File file = new File("C:/Users/Nitin/Assign5/Final/" + fileName
				+ ".txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";
			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				int queryNo = Integer.parseInt(line[0]);
				String url = line[2];
				int rank = Integer.parseInt(line[3]);

				if (resultsList2.containsKey(queryNo)) {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap = resultsList2.get(queryNo);
					tempMap.put(url, rank);
					resultsList2.put(queryNo, tempMap);
				} else {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap.put(url, rank);
					resultsList2.put(queryNo, tempMap);
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("Results Map Size::: " + maps.size());
	}

	private static void getPrecision(boolean flag) {
		int totalRelevant = 0;
		int totalRetrieved = 0;
		int total_Rel_ret = 0;

		for (Map.Entry<Integer, Map<String, Integer>> result : resultsList
				.entrySet()) {

			int queryNum = result.getKey();

			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<String> retrievedDocs = new ArrayList(result.getValue()
					.keySet());
			List<String> relevantDocs = getRelevantDocs(qrelsList.get(queryNum));
			/* Set<String> relevantDocs = (Set<String>) relevant.ge values(); */

			int tp = 0, fp = 0, count = 0;
			int relevantCount = relevantDocs.size();
			totalRelevant += relevantCount;
			totalRetrieved += retrievedDocs.size();

			/*
			 * System.out.println("Retrieved Docs::: " + retrievedDocs.size());
			 * System.out.println("Relevant Docs::: " + relevantCount);
			 */

			double avgPrecision = 0.0;
			double rPrecision = 0.0;

			for (String url : retrievedDocs) {

				if (relevantDocs.contains(url)) {
					// System.out.println(url.getUrl());
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
				

				if (precision == recall && precision > 0.0) {
					rPrecision = precision;
				}

				if (count == 5 || count == 10 || count == 20 || count == 25
						|| count == 30 || count == 40 || count == 50
						|| count == 60 || count == 90 || count == 100
						|| count == 110) {

					List<Integer> ranksList = new ArrayList<Integer>(qrelsList.get(
							queryNum).values());
					double dcg = getDcgValue(ranksList, count);
					double ndcg = calculateNdcg(dcg, ranksList, count);
					/* Calculate Precision and store it in a Map */
					if (precisionMap.containsKey(queryNum)) {
						List<Double> precList = precisionMap.get(queryNum);
						precList.add(precision);
						precisionMap.put(queryNum, precList);
					} else {
						List<Double> precList = new ArrayList<Double>();
						precList.add(precision);
						precisionMap.put(queryNum, precList);
					}
					/* Calculate Recall and store it in a Map */
					if (recallMap.containsKey(queryNum)) {
						List<Double> precList = recallMap.get(queryNum);
						precList.add(recall);
						recallMap.put(queryNum, precList);
					} else {
						List<Double> precList = new ArrayList<Double>();
						precList.add(recall);
						recallMap.put(queryNum, precList);
					}
					/* Calculate F1 and store it in a Map */
					if (f1Map.containsKey(queryNum)) {
						List<Double> precList = f1Map.get(queryNum);
						precList.add(f1);
						f1Map.put(queryNum, precList);
					} else {
						List<Double> precList = new ArrayList<Double>();
						precList.add(f1);
						f1Map.put(queryNum, precList);
					}

					/* Calculate NDCG and store it in a Map */
					if (ndcgMap.containsKey(queryNum)) {
						List<Double> precList = new ArrayList<Double>();
						precList = ndcgMap.get(queryNum);
						precList.add(ndcg);
						ndcgMap.put(queryNum, precList);
					} else {
						List<Double> precList = new ArrayList<Double>();
						precList.add(ndcg);
						ndcgMap.put(queryNum, precList);
					}

				}

			}

			total_Rel_ret += tp;
			if (flag == true)
				printQueryResults(queryNum, retrievedDocs, relevantDocs,
						precisionMap.get(queryNum), recallMap.get(queryNum),
						f1Map.get(queryNum), ndcgMap.get(queryNum), rPrecision,
						tp);

			System.out.println("Average Precision for query " + queryNum
					+ " is :" + (double) avgPrecision / relevantCount);
			System.out
					.println("--------------------------------------------------");
			System.out.println("");
			System.out.println("");
			avgPrecMap.put(queryNum, (double) avgPrecision / relevantCount);

		}
		printAverageValues(totalRelevant, totalRetrieved, total_Rel_ret,
				getAveragedValue(precisionMap).get(0),
				getAveragedValue(recallMap).get(0), getAveragedValue(f1Map)
						.get(0), getAveragedValue(ndcgMap).get(0));

	}

	private static void printAverageValues(int totalRelevant,
			int totalRetrieved, int total_Rel_ret, List<Double> list5,
			List<Double> list6, List<Double> list7, List<Double> list8) {
		System.out.println("Total number of documents over all queries:");
		System.out.println(" Retrieved:" + "\t" + totalRetrieved);
		System.out.println(" Relevant:" + "\t" + totalRelevant);
		System.out.println(" Rel_ret:" + "\t" + total_Rel_ret);
		System.out.println("------------------------------------------------");
		System.out.println("Average Precision:");
		System.out.println("At 5 Docs " + list5.get(0));
		System.out.println("At 10 Docs " + list5.get(1));
		System.out.println("At 20 Docs " + list5.get(2));
		System.out.println("At 50 Docs " + list5.get(6));
		System.out.println("At 100 Docs " + list5.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average Recall:");
		System.out.println("At 5 Docs " + list6.get(0));
		System.out.println("At 10 Docs " + list6.get(1));
		System.out.println("At 20 Docs " + list6.get(2));
		System.out.println("At 50 Docs " + list6.get(6));
		System.out.println("At 100 Docs " + list6.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average f1:");
		System.out.println("At 5 Docs " + list7.get(0));
		System.out.println("At 10 Docs " + list7.get(1));
		System.out.println("At 20 Docs " + list7.get(2));
		System.out.println("At 50 Docs " + list7.get(6));
		System.out.println("At 100 Docs " + list7.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average NDCG:");
		System.out.println("At 5 Docs " + list8.get(0));
		System.out.println("At 10 Docs " + list8.get(1));
		System.out.println("At 20 Docs " + list8.get(2));
		System.out.println("At 50 Docs " + list8.get(6));
		System.out.println("At 100 Docs " + list8.get(9));
		System.out.println("------------------------------------------------");

	}

	private static Map<Integer, List<Double>> getAveragedValue(
			Map<Integer, List<Double>> recallMap2) {
		List<Double> tempValues = new ArrayList<Double>();
		int i = 0;

		for (Map.Entry<Integer, List<Double>> map : recallMap2.entrySet()) {
			List<Double> v = map.getValue();
			List<Double> l = new ArrayList<Double>();

			for (Double d : v) {
				double val = 0.0;
				if (tempValues.size() > 1) {
					val = tempValues.get(i) + d;
				} else {
					val = d;
				}
				l.add(val);
				i++;
			}
			i = 0;
			tempValues = l;
			l = new ArrayList<Double>();

		}

		List<Double> l = new ArrayList<Double>();
		for (Double d : tempValues) {
			double val = d / recallMap2.size();
			l.add(val);
		}
		recallMap2.put(0, l);

		return recallMap2;
	}

	private static void printQueryResults(int queryNum,
			List<String> retrievedDocs, List<String> relevantDocs,
			List<Double> list, List<Double> list2, List<Double> list3,
			List<Double> list4, double rPrecision, int tp) {

		System.out.println("Queryid (Num): " + "\t" + queryNum);
		System.out.println("Total number of documents over all queries:");
		System.out.println(" Retrieved:" + "\t" + retrievedDocs.size());
		System.out.println(" Relevant:" + "\t" + relevantDocs.size());
		System.out.println(" Rel_ret:" + "\t" + tp);
		System.out.println("------------------------------------------------");
		System.out
				.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");
		System.out.println("Exact:" + "\t" + rPrecision);
		System.out.println("------------------------------------------------");
		System.out.println("Precision:");
		System.out.println("At 5 Docs " + list.get(0));
		System.out.println("At 10 Docs " + list.get(1));
		System.out.println("At 20 Docs " + list.get(2));
		System.out.println("At 50 Docs " + list.get(6));
		System.out.println("At 100 Docs " + list.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Recall:");
		System.out.println("At 5 Docs " + list2.get(0));
		System.out.println("At 10 Docs " + list2.get(1));
		System.out.println("At 20 Docs " + list2.get(2));
		System.out.println("At 50 Docs " + list2.get(6));
		System.out.println("At 100 Docs " + list2.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("f1:");
		System.out.println("At 5 Docs " + list3.get(0));
		System.out.println("At 10 Docs " + list3.get(1));
		System.out.println("At 20 Docs " + list3.get(2));
		System.out.println("At 50 Docs " + list3.get(6));
		System.out.println("At 100 Docs " + list3.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("NDCG:");
		System.out.println("At 5 Docs " + list4.get(0));
		System.out.println("At 10 Docs " + list4.get(1));
		System.out.println("At 20 Docs " + list4.get(2));
		System.out.println("At 50 Docs " + list4.get(6));
		System.out.println("At 100 Docs " + list4.get(9));

		System.out.println("------------------------------------------------");

	}

	private static double calculateNdcg(double dcg, List<Integer> map, int count) {

		double dcg1 = 0.0;

		/* dcg = getDcgValue(map, count); */
		int i=0;
		
		List<Integer> ranks = new ArrayList<Integer>();
		
		for (int l : map) {
			if (i < count)
				ranks.add(l);
			i++;

		}

		Collections.sort(ranks);
		Collections.reverse(ranks);

		dcg1 = getDcgValue(ranks, count);

		double ndcg = (double) dcg / dcg1;

		return ndcg;
	}

	private static double getDcgValue(List<Integer> list, int count) {

		double dcg = 0.0;
		double finaldcg = 0.0;
		int r1 = 0;
		int j = 0;
		
		List<Integer> ranks = new ArrayList<Integer>();
		
		for (int l : list) {
			if (j < count)
				ranks.add(l);
			j++;

		}

		r1 = ranks.get(0);

		/* for (int i = 1; i < count; i++) { */
		for (int i = 1; i < ranks.size(); i++) {
			dcg += (((double) ranks.get(i) * (double) Math.log(2)) / (double) Math
					.log(i + 1));
		}

		finaldcg = r1 + dcg;

		return finaldcg;
	}

	private static List<String> getRelevantDocs(Map<String, Integer> map) {

		List<String> relDocs = new ArrayList<String>();
		for (Map.Entry<String, Integer> d : map.entrySet()) {

			if (d.getValue() == 1 || d.getValue() == 2)
				relDocs.add(d.getKey());
		}
		// System.out.println("relevant Docs Size:: "+relDocs.size());
		return relDocs;
	}

}
