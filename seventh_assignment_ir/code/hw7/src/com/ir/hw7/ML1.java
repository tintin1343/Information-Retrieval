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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;

public class ML1 {

	final static String PATH = "C:/Users/Nitin/Assign7/data/trec07p/";
	static Map<String, String> indexCatalog = new LinkedHashMap<String, String>();

	static Map<String, Double[]> trainFeatureMatrix = new LinkedHashMap<String, Double[]>();
	static Map<String, Double[]> testFeatureMatrix = new LinkedHashMap<String, Double[]>();

	static List<String> trainingList = new ArrayList<String>();
	static List<String> testingList = new ArrayList<String>();
	
	static int hamcount=0;
	static int spamcount=0;

	public static void main(String[] args) throws IOException,
			InvalidInputDataException {
		// read and load the catalog file in memory for spam/ham
		readCatalog("full", "index", indexCatalog);
		System.out.println("Index Catalog SIze::: " + indexCatalog.size());

		// Build QueryList
		List<String> queryWords = new ArrayList<String>();
		queryWords= createQueryList(queryWords);
		

		System.out.println("Query Size:: " + queryWords.size());
		// create List of Training Docs from Elastic.
		Client client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

		getList(client, "train");
		System.out.println("Training List:: " + trainingList.size());
		getList(client, "test");
		System.out.println("Testing List:: " + testingList.size());
		
		System.out.println("Training List %:: " + trainingList.size()*100/indexCatalog.size());
		
		System.out.println("Testing List %:: " + testingList.size()*100/indexCatalog.size());

		int i = 0;

		for (String q : queryWords) {

			//System.out.println("Processing query Word:: " + q);
			// Build Query
			QueryBuilder qb = QueryBuilders.matchPhraseQuery("text", q);

			// Search Response
			SearchResponse scrollResp = client.prepareSearch("hw7_1")
					.setTypes("document")
					.setScroll(new TimeValue(60000)).setQuery(qb).setSize(1000)
					.execute().actionGet();

			System.out.println("Total Results for word " + q + " :::"
					+ scrollResp.getHits().getTotalHits());
			
			while (true) {
				for (SearchHit hit : scrollResp.getHits().getHits()) {

					String split = (String) hit.getSource().get("split");
					String docno = (String) hit.getSource().get("docno");
					String type = (String) hit.getSource().get("label");

					double score = (double) hit.getScore();
					double label = -1.0;

					// System.out.println("Score"+score1);
					// System.out.println("Score3:: "+ score);
					 

					if (type.equals("spam"))
						label = 1.0;
					else
						label = 0.0;

					if (trainingList.contains(docno)) {
						if (trainFeatureMatrix.containsKey(docno)) {
							Double[] query = trainFeatureMatrix.get(docno);
							query[i] = score;
							trainFeatureMatrix.put(docno, query);
						} else {
							Double[] queryScore = new Double[queryWords.size() + 1];
							initArray(queryScore);
							queryScore[i] = score;
							queryScore[queryWords.size()] = label;
							trainFeatureMatrix.put(docno, queryScore);
						}
					}

					if (testingList.contains(docno)) {
						if (testFeatureMatrix.containsKey(docno)) {
							Double[] query = testFeatureMatrix.get(docno);
							query[i] = score;
							testFeatureMatrix.put(docno, query);
						} else {
							Double[] queryScore = new Double[queryWords.size() + 1];
							initArray(queryScore);
							queryScore[i] = score;
							queryScore[queryWords.size()] = label;

							testFeatureMatrix.put(docno, queryScore);
						}
					}

				}

				scrollResp = client
						.prepareSearchScroll(scrollResp.getScrollId())
						.setScroll(new TimeValue(600000)).execute().actionGet();
				// Break condition: No hits are returned
				if (scrollResp.getHits().getHits().length == 0) {
					break;
				}
			}

			i++;

		}

		System.out.println("Training Matrix Size::: "
				+ trainFeatureMatrix.size());
		System.out.println("Test Matrix Size::: " + testFeatureMatrix.size());

		writeMatrixToFile(trainFeatureMatrix, "trainingMatrix", "trainCatalog");
		writeMatrixToFile(testFeatureMatrix, "testMatrix", "testCatalog");

		// Train the model
		String[] args1 = { "-s", "0",
				"C:/Users/Nitin/Assign7/output/trainingMatrix.txt",
				"C:/Users/Nitin/Assign7/output/modelTrain.txt" };
		Train.main(args1);

		System.out.println("Done with training. Started Predicting..");

		// predict for Testing
		predictLabels("testMatrix.txt", "modelTrain.txt", "outputTest.txt");

		generateRankedLists("outputTest", "testCatalog", "testRankedList");

		// Predict for Training
		predictLabels("trainingMatrix.txt", "modelTrain.txt", "outputTrain.txt");

		// generate Ranked List for Training
		generateRankedLists("outputTrain", "trainCatalog", "trainRankedList");

		// create List of Testing Docs from Elastic.

	}
	
	private static List<String> createQueryList(List<String> queryWords) {
		queryWords.add("porn");
		queryWords.add("sex");
		queryWords.add("free");
		queryWords.add("Buy");
		queryWords.add("click here");
		queryWords.add("love");
		queryWords.add("business");
	    queryWords.add("Legal");
		queryWords.add("best");
		queryWords.add("sale");
		queryWords.add("discount");
		queryWords.add("win");
		queryWords.add("Make $");
		queryWords.add("Wife");
		queryWords.add("Viagra");
		queryWords.add("Deal");
		queryWords.add("degree");
		queryWords.add("Friend");
		return queryWords;
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
		writeRankedFile(rankedFile,rankedMap);

	}

	private static List<Double> readModelFile(String fileName)
			throws NumberFormatException, IOException {

		String path = "C:/Users/Nitin/Assign7/output/";
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
	
	private static List<String> readCatalog(String fileName)
			throws NumberFormatException, IOException {
		String path = "C:/Users/Nitin/Assign7/output/";
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

	private static void predictLabels(String testFile, String model,
			String output) throws IOException {
		String path = "C:/Users/Nitin/Assign7/output/";
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

	private static void initArray(Double[] queryScore) {
		// TODO Auto-generated method stub

		for (int i = 0; i < queryScore.length - 1; i++)
			queryScore[i] = -1.0;

	}

	private static void getList(Client client, String split) {
		// Build Query
		QueryBuilder qb = QueryBuilders.matchQuery("split", split);

		// Search Response
		SearchResponse scrollResp = client.prepareSearch("hw7_1")
				.setSearchType(SearchType.SCAN).setScroll(new TimeValue(60000))
				.setQuery(qb).setSize(1000).execute().actionGet();

		System.out.println("Size:: " + scrollResp.getHits().getTotalHits());
		// System.out.println(scrollResp.getHits().getHits().length);
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {

				String type = (String) hit.getSource().get("split");
				String docno = (String) hit.getSource().get("docno");

				if (type.equals("train"))
					trainingList.add(docno);
				else
					testingList.add(docno);

			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(600000)).execute().actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

	}

	private static void writeMatrixToFile(Map<String, Double[]> matrix,
			String fileName, String fileName1) {

		String path = "C:/Users/Nitin/Assign7/output/";
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

				String finalString = features[features.length - 1] + "\t" + fea;

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

	private static void readCatalog(String folder, String fileName,
			Map<String, String> indexCatalog2) {
		File file = new File(PATH + folder + "/" + fileName + "/");

		try {
			System.out.println("Reading Catalog..");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str = "";

			while ((str = br.readLine()) != null) {
				String[] line = str.split(" ");
				String type = line[0];
				String docPath = line[1];
				int startIndex = docPath.lastIndexOf("/") + 1;
				int endIndex = docPath.length();
				String doc = docPath.substring(startIndex, endIndex);
				// System.out.println(doc);
				indexCatalog2.put(doc, type);
			}
			br.close();

			System.out.println("Map Size::: " + indexCatalog2.size());

		} catch (Exception e) {
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
	
	
	private static void writeRankedFile(String fileName,
			Map<String, Double> rankedMap) {

		File file = new File("C:/Users/Nitin/Assign7/" + fileName + ".txt");
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
			
			System.out.println("% spam:: "+(spam*100)/rankedMap.size());
			System.out.println("% ham:: "+(ham*100)/rankedMap.size());

			out.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}



}
