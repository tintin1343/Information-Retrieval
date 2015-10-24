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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/* Confirmed working code for TrecEval. Also gets the correct graph values.
 * Includes NDCG as well. Printing output */
public class TrecEval5 {

	static Map<Integer, List<QrelObj>> qrelsList = new TreeMap<Integer, List<QrelObj>>();
	static Map<Integer, List<QrelObj>> resultsList = new TreeMap<Integer, List<QrelObj>>();

	static Map<Integer, Double> avgPrecMap = new TreeMap<Integer, Double>(); // QueryWise

	// Maps which store precision,recall,f1 values for each URL.
	static Map<Integer, List<Double>> precisionMap = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> recallMap = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> f1Map = new TreeMap<Integer, List<Double>>();
	static Map<Integer, List<Double>> ndcgMap = new TreeMap<Integer, List<Double>>();

	static double rPrecisionValue = 0.0;

	public static void main(String[] args) throws IOException {

		// read the Qrel File
		qrelsList = readQrelsFile("qrelObama");

		resultsList = readResultsFile("resultsObama");

		// calculate Precision Values
		getPrecision();

		// Print Precision Values
		calAvgPrecision();

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
			// System.out.println("Pre For Query:: " + qno);
			// System.out.println("Pre Size:: " + p.size());

			// for (Double li : p) {
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
			// System.out.println("Re For Query:: " + qno);
			// System.out.println("Re Size:: " + p.size());

			for (Double li : p) {
				str = str + "\t" + li;
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
		int count = 0;
		for (double ap : avgPrecMap.values()) {

			// System.out.println("Precision for query :: "+count+"is:: "+ d);
			avg += ap;
			count++;
		}
		avg = avg / 3;
		System.out.println("Total Avg Precision: " + avg);
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
					updateList(tempMap,url,rank);
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
		// System.out.println("Qrels Map Size::: " + maps.size());
		return maps;
	}

	private static void updateList(List<QrelObj> tempMap, String url,
			String rank) {
		
		for(QrelObj q:tempMap){
			if(q.getUrl().equals(url)){
				if(Integer.parseInt(q.getRank())<Integer.parseInt(rank))
					q.setRank(rank);
			}
		}
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

		// System.out.println("Results Map Size::: " + maps.size());
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
				double dcg = getDcgValue(qrelsList.get(queryNum));
				double ndcg = calculateNdcg(dcg, qrelsList.get(queryNum));

				if (precision == recall && precision > 0.0) {
					rPrecision = precision;
					System.out.println("R-Precision @ " + count + " is "
							+ rPrecision);

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

			printQueryResults(queryNum, retrievedDocs, relevantDocs,
					precisionMap.get(queryNum), recallMap.get(queryNum),
					f1Map.get(queryNum), ndcgMap.get(queryNum), rPrecision, tp);

			System.out.println("Average Precision for query " + queryNum
					+ " is :" + (double) avgPrecision / relevantCount);
			System.out
					.println("--------------------------------------------------");
			avgPrecMap.put(queryNum, (double) avgPrecision / relevantCount);

		}

	}

	private static void printQueryResults(int queryNum,
			List<QrelObj> retrievedDocs, List<String> relevantDocs,
			List<Double> list, List<Double> list2, List<Double> list3,
			List<Double> list4, double rPrecision, int tp) {

		System.out.println("Queryid (Num):" + "\t" + queryNum);
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
		/*System.out.println("Average Docs " + list.get(1));
		System.out.println("At 20 Docs " + list.get(2));
		System.out.println("At 50 Docs " + list.get(6));
		System.out.println("At 100 Docs " + list.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average Recall:");
		System.out.println("At 5 Docs " + list2.get(0));
		System.out.println("At 10 Docs " + list2.get(1));
		System.out.println("At 20 Docs " + list2.get(2));
		System.out.println("At 50 Docs " + list2.get(6));
		System.out.println("At 100 Docs " + list2.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average f1:");
		System.out.println("At 5 Docs " + list3.get(0));
		System.out.println("At 10 Docs " + list3.get(1));
		System.out.println("At 20 Docs " + list3.get(2));
		System.out.println("At 50 Docs " + list3.get(6));
		System.out.println("At 100 Docs " + list3.get(9));
		System.out.println("------------------------------------------------");
		System.out.println("Average NDCG:");
		System.out.println("At 5 Docs " + list4.get(0));
		System.out.println("At 10 Docs " + list4.get(1));
		System.out.println("At 20 Docs " + list4.get(2));
		System.out.println("At 50 Docs " + list4.get(6));
		System.out.println("At 100 Docs " + list4.get(9));
		System.out.println("------------------------------------------------");*/

	}

	private static double calculateNdcg(double dcg, List<QrelObj> list) {

		double dcg1 = 0.0;
		List<QrelObj> sortedList = new ArrayList<QrelObj>();
		sortedList = sortListofQrel(list);

		dcg1 = getDcgValue(sortedList);

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

	private static double getDcgValue(List<QrelObj> list) {

		double dcg = 0.0;
		double r1 = 0.0;
		/*
		 * int count = 1; for (QrelObj q : list) { if (count > 1) dcg = dcg +
		 * (double) Double.parseDouble(q.getRank()) / Math.log10(count); else r1
		 * = Double.parseDouble(q.getRank()); count++; }
		 */

		r1 = Double.parseDouble(list.get(0).getRank());
		for (int i = 1; i < list.size(); i++) {
			dcg = dcg
					+ (((double) Double.parseDouble(list.get(i).getRank()) * (double) Math
							.log(2)) / (double) Math.log(i + 1));
		}

		dcg = r1 + dcg;

		return dcg;
	}

	private static List<String> getRelevantDocs(List<QrelObj> map) {

		List<String> relDocs = new ArrayList<String>();
		for (QrelObj d : map) {

			if (d.getRank().equals("1") || d.getRank().equals("2"))
				relDocs.add(d.getUrl());
		}
		// System.out.println("relevant Docs Size:: "+relDocs.size());
		return relDocs;
	}

}
