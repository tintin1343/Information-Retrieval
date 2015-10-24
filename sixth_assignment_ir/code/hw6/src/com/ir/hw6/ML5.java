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

/*Creating the feature Matrix as Map of Map . Generated Model for training ran the same 
 * for testing and training*/
public class ML5 {

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

			// read the result files and create a matrix
			okapiMap = readResultsMap(okapiMap, "okapi-FINAL-1");
			okapiIdfMap = readResultsMap(okapiIdfMap, "okapi-idf-3");
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

			System.out.println("Sets Size:: " + setsMap.size());

			System.out.println("Training Set:: " + setsMap.get(0).size());
			System.out.println("Test Set:: " + setsMap.get(1).size());

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

			writeMatrixToFile(docFeatureMatrix, "trainingMatrix3",
					"trainCatalog3");
			writeMatrixToFile(testFeatureMatrix, "testMatrix3", "testCatalog3");

			// Train the model

			String[] args1 = { "-s", "0",
					"C:/Users/Nitin/Assign6/Final/trainingMatrix3.txt",
					"C:/Users/Nitin/Assign6/Final/modelTrain3.txt" };
			Train.main(args1);

			System.out.println("Done with training. Started Predicting..");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// load Model file
			String[] args2 = {"-b", "1", "C:/Users/Nitin/Assign6/Final/testMatrix3.txt",
					"C:/Users/Nitin/Assign6/Final/modelTrain3.txt",
					"C:/Users/Nitin/Assign6/Final/modelTest3.txt" };
			// model = Model.load(modelFile);
			Predict.main(args2);

			System.out.println("Started Predicting for training..");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] args3 = {
					"-b", "1",
					"C:/Users/Nitin/Assign6/Final/trainingMatrix3.txt",
					"C:/Users/Nitin/Assign6/Final/modelTrain3.txt",
					"C:/Users/Nitin/Assign6/Final/modelTraining3.txt" };
			// model = Model.load(modelFile);
			Predict.main(args3);
			
			
			//Generate a Ranked List file.
			//Step 1: load the ModelOutput.
			List<Double> scores = readModelFile("modelTest3");
			//Step2: Load the Catalog file
			List<String> docIds = readCatalog("testCatalog3");
			
			System.out.println("Model Size::"+ scores.size());
			System.out.println("Catalog Size::"+ docIds.size());
			//create a Map
			
			Map<String,Double> rankedMap =  new LinkedHashMap<String, Double>();
			
			for(int i=0;i<scores.size();i++){
				rankedMap.put(docIds.get(i),scores.get(i));
			}
			
			rankedMap = getSortedMap(rankedMap);
			//Print a ranked List.
			writeRankedFile("rankedTestFile",rankedMap);
			
			//Generate a Ranked List file.
			//Step 1: load the ModelOutput.
			List<Double> scores1 = readModelFile("modelTraining3");
			//Step2: Load the Catalog file
			List<String> docIds1 = readCatalog("trainCatalog3");
			
			System.out.println("Model Size::"+ scores1.size());
			System.out.println("Catalog Size::"+ docIds1.size());
			//create a Map
			
			Map<String,Double> rankedMap1 =  new LinkedHashMap<String, Double>();
			
			for(int i=0;i<scores1.size();i++){
				rankedMap1.put(docIds1.get(i),scores1.get(i));
			}
			
			rankedMap1 = getSortedMap(rankedMap1);
			//Print a ranked List.
			writeRankedFile("rankedTrainFile",rankedMap1);


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortQids(
			Map<K, V> rankTerm) {
		System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Integer.parseInt(o1.getKey().toString()) < Integer
						.parseInt(o2.getKey().toString()) ? -1 : Integer
						.parseInt(o1.getKey().toString()) == Integer
						.parseInt(o2.getKey().toString()) ? 0 : 1;

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
		
		File file = new File("C:/Users/Nitin/Assign6/Final/" + fileName
				+ ".txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			int in = 1;

			for (Map.Entry<String, Double> m : rankedMap.entrySet()) {
				String ids[] = m.getKey().split(":");
				String qId=ids[0];
				String docId=ids[1];

				String finalString =qId  + " " +"Q0" +" " + docId+" "+in+" "+m.getValue()+" "+ "EXP";
				in++;
				out.write(finalString);
				out.newLine();
				
			}
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static List<String> readCatalog(String fileName) throws NumberFormatException, IOException {
		File file = new File("C:/Users/Nitin/Assign6/Final/" + fileName + ".txt");
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

	private static List<Double> readModelFile(String fileName) throws NumberFormatException, IOException {
		
		File file = new File("C:/Users/Nitin/Assign6/Final/" + fileName + ".txt");
		List<Double> scores = new ArrayList<Double>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			str = br.readLine(); //disregarding the first line
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

		File file = new File("C:/Users/Nitin/Assign6/Final/" + fileName
				+ ".txt");
		File file1 = new File("C:/Users/Nitin/Assign6/Final/" + fileName1
				+ ".txt");

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
