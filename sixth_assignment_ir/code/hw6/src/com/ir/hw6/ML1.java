package com.ir.hw6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*Creating the feature Matrix as Map of Map*/
public class ML1 {

	static Map<Integer, Map<String, Integer>> qRelMap = new LinkedHashMap<Integer, Map<String, Integer>>();
	static Map<Integer, List<Integer>> setsMap = new LinkedHashMap<Integer, List<Integer>>();
	static Map<Integer, Map<String, Double>> okapiMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> okapiIdfMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> bm25Map = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> laplaceMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> jmMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<String, Double[]> docFeatureMatrix = new LinkedHashMap<String, Double[]>();
	static Map<String, Double[]> testFeatureMatrix = new LinkedHashMap<String, Double[]>();

	public static void main(String[] args) {

		try {
			// read the qrelFile and Create the data Map.
			readQrelFile("qrels.adhoc.51-100.AP89");
			System.out.println("Qrel Map Size::: " + qRelMap.size());

			// create training and test query sets
			readQueryFiles("query_desc.51-100.short");

			// read the result files and create a matrix
			okapiMap = readResultsMap(okapiMap, "okapi-FINAL-1");
			okapiIdfMap = readResultsMap(okapiIdfMap, "okapi-idf-3");
			bm25Map = readResultsMap(bm25Map, "BM-FINAL-1");
			laplaceMap = readResultsMap(laplaceMap, "Laplace-Final-1");
			jmMap = readResultsMap(jmMap, "JM-Final-1");

			System.out.println("Okpai Map Size:: " + okapiMap.size());
			System.out.println("Okpai IDF Map Size:: " + okapiIdfMap.size());
			System.out.println("BM25 Map Size:: " + bm25Map.size());
			System.out.println("Laplace Map Size:: " + laplaceMap.size());
			System.out.println("JM Map Size:: " + jmMap.size());

			System.out.println("Sets Size:: " + setsMap.size());

			// int count = 0;
			//
			// for (Map.Entry<Integer, List< Integer>> q : setsMap
			// .entrySet()) {
			// count += q.getValue().size();
			// }
			//

			System.out.println("Training Set:: " + setsMap.get(0).size());
			System.out.println("Test Set:: " + setsMap.get(1).size());
			// System.out.println("Query Count:: " + count);

			// Add features to the matrix
			createMatrix(okapiMap);
			updateMatrix(okapiIdfMap, "tf-idf");
			updateMatrix(bm25Map, "bm25");
			updateMatrix(laplaceMap, "laplace");
			updateMatrix(jmMap, "jm");

			System.out.println("Training Matrix Size:: "
					+ docFeatureMatrix.size());
			System.out
					.println("Test Matrix Size:: " + testFeatureMatrix.size());

			writeMatrixToFile(docFeatureMatrix, "trainingMatrix");
			writeMatrixToFile(testFeatureMatrix, "testMatrix");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void updateMatrix(Map<Integer, Map<String, Double>> map,
			String type) {
		// create the doc-feature Matrix for training set

		int i = 0;

		if (type.equals("tf-idf")) {

			i = 1;
		}
		if (type.equals("bm25")) {

			i = 2;
		}
		if (type.equals("laplace")) {

			i = 3;
		}
		if (type.equals("jm")) {

			i = 4;
		}

		for (Map.Entry<Integer, Map<String, Double>> q : map.entrySet()) {
			int qid = q.getKey();

			Map<String, Double> docsmap = q.getValue();

			if (setsMap.get(0).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					if (docFeatureMatrix.containsKey(qid + "-" + docId)) {
						Double[] features = docFeatureMatrix.get(qid + "-"
								+ docId);
						features[i] = d.getValue();
						docFeatureMatrix.put(qid + "-" + docId, features);

					} else {
						Double[] features = new Double[6];
						for (int j = 0; j < 5; j++) {

							if (j != i) {
								features[j] = 0.0;
							} else
								features[i] = d.getValue();
						}
						if (qRelMap.get(qid).get(docId) != null) {
							features[5] = (double) qRelMap.get(qid).get(docId);
						} else {
							features[5] = 0.0;
						}
						docFeatureMatrix.put(qid + "-" + docId, features);
					}

				}
			} else if (setsMap.get(1).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					if (testFeatureMatrix.containsKey(qid + "-" + docId)) {
						Double[] features = testFeatureMatrix.get(qid + "-"
								+ docId);
						features[i] = d.getValue();
						testFeatureMatrix.put(qid + "-" + docId, features);

					} else {
						Double[] features = new Double[6];

						for (int j = 0; j < 5; j++) {

							if (j != i) {
								features[j] = 0.0;
							} else
								features[i] = d.getValue();
						}
						if (qRelMap.get(qid).get(docId) != null) {
							features[5] = (double) qRelMap.get(qid).get(docId);
						} else {
							features[5] = 0.0;
						}

						testFeatureMatrix.put(qid + "-" + docId, features);
					}

				}

			}

		}

	}

	private static void writeMatrixToFile(Map<String, Double[]> matrix,
			String fileName) {

		File file = new File("C:/Users/Nitin/Assign6/Final/" + fileName
				+ ".txt");

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (Map.Entry<String, Double[]> m : matrix.entrySet()) {
				String qid = m.getKey();
				Double[] features = m.getValue();
				String fea = "";

				for (int i = 0; i < features.length; i++) {
					fea += features[i] + "\t";
				}

				String finalString = qid + "\t" + fea;
				out.write(finalString);
				out.newLine();
			}
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createMatrix(Map<Integer, Map<String, Double>> map) {

		// create the doc-feature Matrix for training set
		for (Map.Entry<Integer, Map<String, Double>> q : map.entrySet()) {
			int qid = q.getKey();
			Map<String, Double> docsmap = q.getValue();

			if (setsMap.get(0).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					Double[] features = new Double[6];
					features[0] = d.getValue();
					features[1] = 0.0;
					features[2] = 0.0;
					features[3] = 0.0;
					features[4] = 0.0;
					if (qRelMap.get(qid).get(docId) != null) {
						features[5] = (double) qRelMap.get(qid).get(docId);
					} else {
						features[5] = 0.0;
					}

					docFeatureMatrix.put(qid + "-" + docId, features);
				}
			} else if (setsMap.get(1).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					Double[] features = new Double[6];
					features[0] = d.getValue();
					features[1] = 0.0;
					features[2] = 0.0;
					features[3] = 0.0;
					features[4] = 0.0;
					if (qRelMap.get(qid).get(docId) != null) {
						features[5] = (double) qRelMap.get(qid).get(docId);
					} else {
						features[5] = 0.0;
					}
					testFeatureMatrix.put(qid + "-" + docId, features);

				}

			}

		}

	}

	private static void readQueryFiles(String fileName)
			throws NumberFormatException, IOException {
		File file = new File("C:/Users/Nitin/Assign6/" + fileName + ".txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";
			int count = 0;
			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				String queryNo = line[0];

				int docno = Integer.parseInt(queryNo.substring(0,
						queryNo.length() - 1));

				if (count < 20) {
					List<Integer> tempMap = new ArrayList<Integer>();
					if (setsMap.get(0) != null) {
						tempMap = setsMap.get(0);
						tempMap.add(docno);
						setsMap.put(0, tempMap);
					} else {
						tempMap.add(docno);
						setsMap.put(0, tempMap);
					}

				} else {
					List<Integer> tempMap = new ArrayList<Integer>();
					if (setsMap.get(1) != null) {
						tempMap = setsMap.get(1);
						tempMap.add(docno);
						setsMap.put(1, tempMap);
					} else {
						tempMap.add(docno);
						setsMap.put(1, tempMap);
					}
				}

				count++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static Map<Integer, Map<String, Double>> readResultsMap(
			Map<Integer, Map<String, Double>> map, String fileName)
			throws NumberFormatException, IOException {
		File file = new File("C:/Users/Nitin/Assign6/" + fileName + ".txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");

				int queryNo = Integer.parseInt(line[0]);
				String docno = line[2];
				double score = Double.parseDouble(line[4]);

				if (map.containsKey(queryNo)) {
					Map<String, Double> tempMap = new LinkedHashMap<String, Double>();
					tempMap = map.get(queryNo);
					tempMap.put(docno, score);
					map.put(queryNo, tempMap);
				} else {
					Map<String, Double> tempMap = new LinkedHashMap<String, Double>();
					tempMap.put(docno, score);
					map.put(queryNo, tempMap);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return map;
	}

	private static void readQrelFile(String fileName) throws IOException {
		File file = new File("C:/Users/Nitin/Assign6/" + fileName + ".txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");

				int queryNo = Integer.parseInt(line[0]);
				String docno = line[2];
				int rel = Integer.parseInt(line[3]);

				if (qRelMap.containsKey(queryNo)) {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap = qRelMap.get(queryNo);
					tempMap.put(docno, rel);
					qRelMap.put(queryNo, tempMap);
				} else {
					Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
					tempMap.put(docno, rel);
					qRelMap.put(queryNo, tempMap);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
