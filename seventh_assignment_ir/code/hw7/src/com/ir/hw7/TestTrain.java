package com.ir.hw7;

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

public class TestTrain {

	static Map<String, String> indexCatalog = new LinkedHashMap<String, String>();
	static Map<String, Integer> termCatalog = new LinkedHashMap<String, Integer>();
	static int count = 0;
	static int docCount = 1;

	static List<String> trainingList = new ArrayList<String>();
	static List<String> testingList = new ArrayList<String>();
	
	public static void main(String args[]) throws IOException, InvalidInputDataException {
		// Train the model
		String[] args1 = { "-s", "0",
				"C:/Users/Nitin/Assign7/output/part2/trainMatrix.txt",
				"C:/Users/Nitin/Assign7/output/part2/modelTrain.txt" };
		
		Train.main(args1);

		System.out.println("Done with training. Started Predicting..");
		
		// predict for Testing
		predictLabels("testMatrix.txt", "modelTrain.txt", "outputTest.txt");

		generateRankedLists("outputTest", "testCatalog", "testRankedList");

		// Predict for Training
		predictLabels("trainMatrix.txt", "modelTrain.txt", "outputTrain.txt");

		// generate Ranked List for Training
		generateRankedLists("outputTrain", "trainCatalog", "trainRankedList");

		
		System.out.println("Term Catalog Size:: "+termCatalog.size());
		
		System.out.println("Closed @" + new Date());
	}
	
	private static void predictLabels(String testFile, String model,
			String output) throws IOException {
		String path = "C:/Users/Nitin/Assign7/output/part2/";
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
	
	
	
	private static void generateRankedLists(String outputFile,
			String catalogFile, String rankedFile)
			throws NumberFormatException, IOException {
		// Generate a Ranked List file.
		// Step 1: load the ModelOutput.
		List<Double> scores = readModelFile(outputFile);
		// Step2: Load the Catalog file
		List<String> docIds = readCatalogFile(catalogFile);

		System.out.println("Model Size::" + scores.size());
		System.out.println("Catalog Size::" + docIds.size());
		// create a Map

		Map<String, Double> rankedMap = new LinkedHashMap<String, Double>();

		for (int i = 0; i < scores.size(); i++) {
			rankedMap.put(docIds.get(i), scores.get(i));
		}

		rankedMap = getSortedMap(rankedMap);
		// Print a ranked List.
		writeRankedFile(rankedFile,rankedMap);

	}
	
	private static List<String> readCatalogFile(String fileName)
			throws NumberFormatException, IOException {
		String path = "C:/Users/Nitin/Assign7/output/part2/";
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

		String path = "C:/Users/Nitin/Assign7/output/part2/";
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
	
	private static void writeRankedFile(String fileName,
			Map<String, Double> rankedMap) {

		File file = new File("C:/Users/Nitin/Assign7/ranked/" + fileName + ".txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			int in = 1;
			int spam=0;
			int ham=0;

			for (Map.Entry<String, Double> m : rankedMap.entrySet()) {
				String ids = m.getKey();
				/*String qId = ids[0];
				String docId = ids[1];*/
				
				String type=indexCatalog.get(ids);
				if(type.equals("spam"))
					spam++;
				else
					ham++;

				String finalString = type + " " + "Q0" + " " + ids + " " + in
						+ " " + m.getValue() + " " + "EXP";
				in++;
				out.write(finalString);
				out.newLine();

			}
			System.out.println("Spam Count:: "+spam);
			System.out.println("Ham Count:: "+ham);
			System.out.println("Total Results:: "+ rankedMap.size());
			System.out.println("Spam + Ham:: "+ (spam+ham) );
			
			System.out.println("% spam:: "+(double) (spam*100)/rankedMap.size());
			System.out.println("% ham:: "+(double) (ham*100)/rankedMap.size());

			out.close();

		} catch (IOException e) {

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
	
	public static <K, V extends Comparable<? super V>> Map<K, V> getSortedKeys(
			Map<K, V> rankTerm) {
		//System.out.println("Started Sorting..." + "@ " + new Date());

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				rankTerm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());
				return Integer.parseInt(o1.getKey().toString()) < Integer.parseInt(o2.getKey().toString()) ? -1 : Integer.parseInt(o1.getKey().toString()) == Integer.parseInt(o2.getKey().toString()) ? 0 : 1;

			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		//System.out.println("Stopped Sorting..." + "@ " + new Date());
		return result;

}
	
}	
	
