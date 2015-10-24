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

public class AnalyseFeatures {

	static Map<String, Double> model = new LinkedHashMap<String, Double>();

	public static void main(String args[]) throws NumberFormatException,
			IOException {
		// read the model file
		List<Double> scores = readModelFile("modelTrain");
		System.out.println(scores.size());
		List<String> terms = readCatalogFile("trainFeaCatalog");
		System.out.println(terms.size());
		for (int i = 0; i < scores.size(); i++) {
			model.put(terms.get(i), scores.get(i));
		}

		model = getSortedMap(model);

		writeModel("ModelValues.txt", model);

	}

	private static void writeModel(String fileName,
			Map<String, Double> rankedMap) {

		File file = new File("C:/Users/Nitin/Assign7/ranked/" + fileName
				+ ".txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			int in = 1;

			for (Map.Entry<String, Double> m : rankedMap.entrySet()) {

				String finalString = m.getKey() + "\t" + m.getValue();
				in++;
				out.write(finalString);
				out.newLine();

			}

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

	private static List<Double> readModelFile(String fileName)
			throws NumberFormatException, IOException {

		String path = "C:/Users/Nitin/Assign7/output/part2/";
		File file = new File(path + fileName + ".txt");
		List<Double> scores = new ArrayList<Double>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			str = br.readLine(); // disregarding the first six Lines
			str = br.readLine(); // disregarding the first six Lines
			str = br.readLine(); // disregarding the first six Lines
			str = br.readLine(); // disregarding the first six Lines
			str = br.readLine(); // disregarding the first six Lines
			str = br.readLine(); // disregarding the first six Lines

			while ((str = br.readLine()) != null) {
				// String line = str;
				double score = Double.parseDouble(str);
				/*if (score != 0)*/
					scores.add(score);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return scores;
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
				String docId = line[0];
				docIds.add(docId);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return docIds;
	}

}
