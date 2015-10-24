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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/* Confirmed working code for TrecEval. Also gets the correct graph values.
 * Includes NDCG as well. Printing output. New Code from Scratch */
public class TrecEvalTest1 {

	static Map<Integer, Map<String, Integer>> qrelsList = new TreeMap<Integer, Map<String, Integer>>();
	static Map<Integer, Map<String, Integer>> resultsList = new TreeMap<Integer, Map<String, Integer>>();

	static Map<Integer, Double> avgPrecMap = new TreeMap<Integer, Double>(); // QueryWise

	// Maps which store precision,recall,f1 values for each URL.
	static Map<Integer, List<Double>> precisionMap = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> recallMap = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> f1Map = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> ndcgMap = new TreeMap<Integer, List<Double>>();

	static double rPrecisionValue = 0.0;

	public static void main(String[] args) throws IOException {

		// read the Qrel File
		qrelsList = readQrelsFile("qrels.adhoc.51-100.AP89", qrelsList);
		// /printQrel(qrelsList);

		readResultsFile("okapi-FINAL-1", resultsList);

		// calculate Precision Values
		getPrecision();

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

		avg = (double) avg / 25.0;
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

	private static Map<Integer, Map<String, Integer>> readQrelsFile(
			String fileName, Map<Integer, Map<String, Integer>> qrelsList2) {

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

				if (qrelsList2.containsKey(queryNo)) {
					Map<String, Integer> tempMap = qrelsList2.get(queryNo);
					// tempMap = updateList(tempMap, url, rank);
					tempMap.put(url, rank);
					qrelsList2.put(queryNo, tempMap);
				} else {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap.put(url, rank);
					qrelsList2.put(queryNo, tempMap);
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("Qrels Map Size::: " + maps.size());
		return qrelsList2;
	}

	private static Map<String, Integer> updateList(
			Map<String, Integer> tempMap, String url, int rank) {

		for (Map.Entry<String, Integer> q : tempMap.entrySet()) {
			if (q.getKey().equals(url)) {
				if (q.getValue() < rank)
					tempMap.put(url, rank);
			}
		}
		return tempMap;
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

	private static void getPrecision() {
		int totalRelevant=0;
		int totalRetrieved=0;
		int total_Rel_ret=0;

		for (Map.Entry<Integer, Map<String, Integer>> result : resultsList
				.entrySet()) {

			int queryNum = result.getKey();

			List<String> retrievedDocs = new ArrayList(result.getValue()
					.keySet());
			List<String> relevantDocs = getRelevantDocs(qrelsList.get(queryNum));
			/* Set<String> relevantDocs = (Set<String>) relevant.ge values(); */

			int tp = 0, fp = 0, count = 0;
			int relevantCount = relevantDocs.size();
			totalRelevant+=relevantCount;
			totalRetrieved+=retrievedDocs.size();

			
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
				List<Integer> ranksList = new ArrayList<Integer>(qrelsList.get(
						queryNum).values());
				double dcg = getDcgValue(ranksList);
				double ndcg = calculateNdcg(dcg, ranksList);

				if (precision == recall && precision > 0.0) {
					rPrecision = precision;
					/*
					 * System.out.println("R-Precision @ " + count + " is " +
					 * rPrecision);
					 */

				}

				if (count == 5 || count == 10 || count == 20 || count == 25
						|| count == 30 || count == 40 || count == 50
						|| count == 60 || count == 90 || count == 100
						|| count == 110) {
					/*
					 * System.out.println("Precision @ " + count + " for query "
					 * + queryNum + " is:" + precision);
					 * System.out.println("Recall @ " + count + " is:" +
					 * recall); System.out.println("F1 @ " + count + " is:" +
					 * f1); System.out.println("NDCG @ " + count + " is:" +
					 * ndcg);
					 */

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
						List<Double> precList = ndcgMap.get(queryNum);
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
			printQueryResults(queryNum, retrievedDocs, relevantDocs,
					precisionMap.get(queryNum), recallMap.get(queryNum),
					f1Map.get(queryNum), ndcgMap.get(queryNum), rPrecision, tp);

			System.out.println("Average Precision for query " + queryNum
					+ " is :" + (double) avgPrecision / relevantCount);
			System.out
					.println("--------------------------------------------------");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			avgPrecMap.put(queryNum, (double) avgPrecision / relevantCount);

		}
		printAverageValues(totalRelevant,totalRetrieved,total_Rel_ret,getAveragedValue(precisionMap).get(-1),
				getAveragedValue(recallMap).get(-1), getAveragedValue(f1Map)
						.get(-1), getAveragedValue(ndcgMap).get(-1));

	}

	private static void printAverageValues(int totalRelevant, int totalRetrieved, int total_Rel_ret, List<Double> list5,
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
		recallMap2.put(-1, l);

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

	private static double calculateNdcg(double dcg, List<Integer> map) {

		double dcg1 = 0.0;

		Collections.sort(map);
		Collections.reverse(map);

		dcg1 = getDcgValue(map);

		double ndcg = (double) dcg / dcg1;

		return ndcg;
	}

	private static List<QrelObj> sortListofQrel(List<QrelObj> list) {

		Collections.sort(list, new Comparator<QrelObj>() {
			@Override
			public int compare(final QrelObj o1, final QrelObj o2) {
				return Integer.parseInt(o1.getRank()) > Integer.parseInt(o2
						.getRank()) ? -1
						: Integer.parseInt(o1.getRank()) == Integer.parseInt(o2
								.getRank()) ? 0 : 1;
			}
		});

		return list;
	}

	private static double getDcgValue(List<Integer> list) {

		double dcg = 0.0;
		double r1 = 0.0;
		/*
		 * int count = 1; for (QrelObj q : list) { if (count > 1) dcg = dcg +
		 * (double) Double.parseDouble(q.getRank()) / Math.log10(count); else r1
		 * = Double.parseDouble(q.getRank()); count++; }
		 */
		List<Integer> ranks = (List<Integer>) list;

		r1 = ranks.get(0);
		for (int i = 1; i < list.size(); i++) {
			dcg = dcg
					+ (((double) list.get(i) * (double) Math.log(2)) / (double) Math
							.log(i + 1));
		}

		dcg = r1 + dcg;

		return dcg;
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
