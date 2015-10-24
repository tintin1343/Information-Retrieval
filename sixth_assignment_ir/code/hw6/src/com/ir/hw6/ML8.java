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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;

/* Removing Coding redundancies etc.
 * Changed Read/write and output folders. Complete Codes*/

public class ML8 {

	static Map<Integer, Map<String, Integer>> qRelMap = new LinkedHashMap<Integer, Map<String, Integer>>();
	static Map<Integer, List<Integer>> setsMap = new LinkedHashMap<Integer, List<Integer>>();

	static Map<Integer, Map<String, Double>> okapiMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> okapiIdfMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> bm25Map = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> laplaceMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> jmMap = new LinkedHashMap<Integer, Map<String, Double>>();
	static Map<Integer, Map<String, Double>> proxMap = new LinkedHashMap<Integer, Map<String, Double>>();

	static Map<String, Double[]> docFeatureMatrix = new LinkedHashMap<String, Double[]>();
	static Map<String, Double[]> testFeatureMatrix = new LinkedHashMap<String, Double[]>();

	public static void main(String[] args) throws InvalidInputDataException {

		try {
			// read the qrelFile and Create the data Map.
			readQrelFile("qrels.adhoc.51-100.AP89");
			System.out.println("Qrel Map Size::: " + qRelMap.size());

			// create training and test query sets
			readQueryFiles("query_desc.51-100.short");
			System.out.println("Sets Size:: " + setsMap.size());
			System.out.println("Training Set:: " + setsMap.get(0).size());
			System.out.println("Test Set:: " + setsMap.get(1).size());

			// read the result files and create a matrix
			okapiMap = readResultsMap(okapiMap, "okapi-FINAL-1");
			okapiIdfMap = readResultsMap(okapiIdfMap, "okapiIDF-Final-1");
			bm25Map = readResultsMap(bm25Map, "BM-FINAL-1");
			laplaceMap = readResultsMap(laplaceMap, "Laplace-Final-1");
			jmMap = readResultsMap(jmMap, "JM-Final-1");
			proxMap = readResultsMap(proxMap, "proximity-1");

			System.out.println("Okpai Map Size:: " + okapiMap.size());
			System.out.println("Okpai IDF Map Size:: " + okapiIdfMap.size());
			System.out.println("BM25 Map Size:: " + bm25Map.size());
			System.out.println("Laplace Map Size:: " + laplaceMap.size());
			System.out.println("JM Map Size:: " + jmMap.size());
			System.out.println("Prox Map Size:: " + proxMap.size());

			// Add features to the matrix
			createMatrix(okapiMap);
			updateMatrix(okapiIdfMap, "tf-idf");
			updateMatrix(bm25Map, "bm25");
			updateMatrix(laplaceMap, "laplace");
			updateMatrix(jmMap, "jm");
			updateMatrix(proxMap, "prox");

			System.out.println("Training Matrix Size:: "
					+ docFeatureMatrix.size());
			System.out
					.println("Test Matrix Size:: " + testFeatureMatrix.size());

			writeMatrixToFile(docFeatureMatrix, "trainingMatrix",
					"trainCatalog");
			writeMatrixToFile(testFeatureMatrix, "testMatrix", "testCatalog");

			// Train the model
			String[] args1 = { "-s", "0",
					"C:/Users/Nitin/Assign6/output/trainingMatrix.txt",
					"C:/Users/Nitin/Assign6/output/modelTrain.txt" };
			Train.main(args1);

			System.out.println("Done with training. Started Predicting..");

			// predict for Testing
			predictLabels("testMatrix.txt", "modelTrain.txt", "outputTest.txt");
			// generate Ranked List for Testing
			generateRankedLists("outputTest", "testCatalog",
					"testingPerformance");
			// Predict for Training
			predictLabels("trainingMatrix.txt", "modelTrain.txt",
					"outputTrain.txt");
			// generate Ranked List for Training
			generateRankedLists("outputTrain", "trainCatalog",
					"trainingPerformance");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void generateRankedLists(String outputFile,
			String catalogFile, String rankedFile)
			throws NumberFormatException, IOException {
		// Generate a Ranked List file.
		// Step 1: load the ModelOutput.
		List<Double> scores = readModelFile(outputFile);
		// Step2: Load the Catalog file
		List<String> docIds = readCatalog(catalogFile);

		System.out.println("Model Size::" + scores.size());
		System.out.println("Catalog Size::" + docIds.size());
		// create a Map

		Map<String, Double> rankedMap = new LinkedHashMap<String, Double>();

		for (int i = 0; i < scores.size(); i++) {
			rankedMap.put(docIds.get(i), scores.get(i));
		}

		rankedMap = getSortedMap(rankedMap);
		// Print a ranked List.
		writeRankedFile(rankedFile, sortQids(rankedMap));

	}

	private static void predictLabels(String testFile, String model,
			String output) throws IOException {
		String path = "C:/Users/Nitin/Assign6/output/";
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Predict
		String[] args2 = { "-b", "1", path + testFile, path + model,
				path + output };
		Predict.main(args2);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortQids(
			Map<K, V> rankTerm) {
		System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Integer.parseInt(o1.getKey().toString().split(":")[0]) < Integer
						.parseInt(o2.getKey().toString().split(":")[0]) ? -1 : Integer
						.parseInt(o1.getKey().toString().split(":")[0]) == Integer
						.parseInt(o2.getKey().toString().split(":")[0]) ? 0 : 1;

			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;
	}

	private static void writeRankedFile(String fileName,
			Map<String, Double> rankedMap) {

		File file = new File("C:/Users/Nitin/Assign6/" + fileName + ".txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			int in = 1;

			for (Map.Entry<String, Double> m : rankedMap.entrySet()) {
				String ids[] = m.getKey().split(":");
				String qId = ids[0];
				String docId = ids[1];

				String finalString = qId + " " + "Q0" + " " + docId + " " + in
						+ " " + m.getValue() + " " + "EXP";
				in++;
				out.write(finalString);
				out.newLine();

			}
			out.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private static List<String> readCatalog(String fileName)
			throws NumberFormatException, IOException {
		String path = "C:/Users/Nitin/Assign6/output/";
		File file = new File(path + fileName + ".txt");
		List<String> docIds = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split("\t");
				String docId = line[1];
				docIds.add(docId);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return docIds;
	}

	private static List<Double> readModelFile(String fileName)
			throws NumberFormatException, IOException {

		String path = "C:/Users/Nitin/Assign6/output/";
		File file = new File(path + fileName + ".txt");
		List<Double> scores = new ArrayList<Double>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			str = br.readLine(); // disregarding the first line
			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				double score = Double.parseDouble(line[1]);
				scores.add(score);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return scores;
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
		if (type.equals("prox")) {

			i = 5;
		}

		for (Map.Entry<Integer, Map<String, Double>> q : map.entrySet()) {
			int qid = q.getKey();

			Map<String, Double> docsmap = q.getValue();

			if (setsMap.get(0).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					if (docFeatureMatrix.containsKey(qid + ":" + docId)) {
						Double[] features = docFeatureMatrix.get(qid + ":"
								+ docId);
						features[i] = d.getValue();
						docFeatureMatrix.put(qid + ":" + docId, features);

					} else {
						Double[] features = new Double[7];
						for (int j = 0; j < 6; j++) {

							if (j != i) {
								features[j] = -1.0;
							} else
								features[i] = d.getValue();
						}
						if (qRelMap.get(qid).get(docId) != null) {
							features[6] = (double) qRelMap.get(qid).get(docId);
							docFeatureMatrix.put(qid + ":" + docId, features);
						}

					}

				}
			} else if (setsMap.get(1).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					if (testFeatureMatrix.containsKey(qid + ":" + docId)) {
						Double[] features = testFeatureMatrix.get(qid + ":"
								+ docId);
						features[i] = d.getValue();
						testFeatureMatrix.put(qid + ":" + docId, features);

					} else {
						Double[] features = new Double[7];

						for (int j = 0; j < 6; j++) {

							if (j != i) {
								features[j] = -1.0;
							} else
								features[i] = d.getValue();
						}
						if (qRelMap.get(qid).get(docId) != null) {
							features[6] = (double) qRelMap.get(qid).get(docId);
							testFeatureMatrix.put(qid + ":" + docId, features);
						}

					}

				}

			}

		}

	}

	private static void writeMatrixToFile(Map<String, Double[]> matrix,
			String fileName, String fileName1) {

		String path = "C:/Users/Nitin/Assign6/output/";
		File file = new File(path + fileName + ".txt");
		File file1 = new File(path + fileName1 + ".txt");

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			BufferedWriter out1 = new BufferedWriter(new FileWriter(file1));

			int in = 1;

			for (Map.Entry<String, Double[]> m : matrix.entrySet()) {
				String qid = m.getKey();

				String line = in + "\t" + qid;
				out1.write(line);
				out1.newLine();

				Double[] features = m.getValue();
				String fea = "";

				for (int i = 0; i < features.length - 1; i++) {
					if (features[i] != -1.0)
						fea += (i + 1) + ":" + features[i] + "\t";
				}

				String finalString = features[6] + "\t" + fea;

				in++;

				out.write(finalString);
				out.newLine();

			}

			out.close();
			out1.close();

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

					Double[] features = new Double[7];
					features[0] = d.getValue();
					features[1] = -1.0;
					features[2] = -1.0;
					features[3] = -1.0;
					features[4] = -1.0;
					features[5] = -1.0;
					if (qRelMap.get(qid).get(docId) != null) {
						features[6] = (double) qRelMap.get(qid).get(docId);
						docFeatureMatrix.put(qid + ":" + docId, features);
					}

				}
			} else if (setsMap.get(1).contains(qid)) {

				for (Map.Entry<String, Double> d : docsmap.entrySet()) {
					String docId = d.getKey();

					Double[] features = new Double[7];
					features[0] = d.getValue();
					features[1] = -1.0;
					features[2] = -1.0;
					features[3] = -1.0;
					features[4] = -1.0;
					features[5] = -1.0;
					if (qRelMap.get(qid).get(docId) != null) {
						features[6] = (double) qRelMap.get(qid).get(docId);
						testFeatureMatrix.put(qid + ":" + docId, features);

					}

				}

			}

		}

	}

	private static void readQueryFiles(String fileName)
			throws NumberFormatException, IOException {
		String path = "C:/Users/Nitin/Assign6/data/";
		File file = new File(path + fileName + ".txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			List<Integer> trainMap = new ArrayList<Integer>();

			List<Integer> testMap = new ArrayList<Integer>();

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				String queryNo = line[0];

				int docno = Integer.parseInt(queryNo.substring(0,
						queryNo.length() - 1));

				if (docno == 58 || docno == 93 || docno == 85 || docno == 61
						|| docno == 100) {
					if (setsMap.get(1) != null) {
						testMap = setsMap.get(1);
						testMap.add(docno);
						setsMap.put(1, testMap);
					} else {
						testMap.add(docno);
						setsMap.put(1, testMap);
					}
				}

				/*
				 * textMap.add(58); textMap.add(93); textMap.add(85);
				 * textMap.add(99); textMap.add(100); setsMap.put(1,textMap);
				 */

				/*
				 * if (docno == 56 || docno == 71 || docno == 91 || docno == 97
				 * || docno == 59 || docno == 64 || docno == 62 || docno == 77
				 * || docno == 54 || docno == 87 || docno == 94 || docno == 89
				 * || docno == 61 || docno == 95 || docno == 68 || docno == 57
				 * || docno == 98 || docno == 60 || docno == 80 || docno == 63)
				 */
				if (!testMap.contains(docno)) {
					if (setsMap.get(0) != null) {
						trainMap = setsMap.get(0);
						trainMap.add(docno);
						setsMap.put(0, trainMap);
					} else {
						trainMap.add(docno);
						setsMap.put(0, trainMap);
					}
				}
				// trainMap.add(docno);
				/*
				 * trainMap.add(56); trainMap.add(71); trainMap.add(97);
				 * trainMap.add(59); trainMap.add(64); trainMap.add(62);
				 * trainMap.add(77); trainMap.add(54); trainMap.add(87);
				 * trainMap.add(94); trainMap.add(89); trainMap.add(61);
				 * trainMap.add(95); trainMap.add(68); trainMap.add(57);
				 * trainMap.add(98); trainMap.add(60); trainMap.add(80);
				 * trainMap.add(63); trainMap.add(91); setsMap.put(0,trainMap);
				 */

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static Map<Integer, Map<String, Double>> readResultsMap(
			Map<Integer, Map<String, Double>> map, String fileName)
			throws NumberFormatException, IOException {
		String path = "C:/Users/Nitin/Assign6/data/";
		File file = new File(path + fileName + ".txt");
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
		String path = "C:/Users/Nitin/Assign6/data/";
		File file = new File(path + fileName + ".txt");
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

}
