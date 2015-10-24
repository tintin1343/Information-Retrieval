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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;

public class UnigramML {
	final static String PATH = "C:/Users/Nitin/Assign7/data/trec07p/";
	static Map<String, String> indexCatalog = new LinkedHashMap<String, String>();
	static Map<String, Integer> termCatalog = new LinkedHashMap<String, Integer>();
	static int count = 0;
	static int docCount = 1;

	static List<String> trainingList = new ArrayList<String>();
	static List<String> testingList = new ArrayList<String>();

	public static void main(String[] args) throws IOException, InvalidInputDataException {
		// read and load the catalog file in memory for spam/ham
		readCatalog("full", "index", indexCatalog);

		Client client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

		getList(client, "train");
		System.out.println("Training List:: " + trainingList.size());
		getList(client, "test");
		System.out.println("Testing List:: " + testingList.size());

		String path = "C:/Users/Nitin/Assign7/output/part2/";

		File file = new File(path + "trainMatrix" + ".txt");
		File file1 = new File(path + "trainCatalog" + ".txt");
		/*File file2 = new File(path + "testMatrix" + ".txt");
		File file3 = new File(path + "testCatalog" + ".txt");*/

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		BufferedWriter out1 = new BufferedWriter(new FileWriter(file1));
		/*BufferedWriter out2 = new BufferedWriter(new FileWriter(file2));
		BufferedWriter out3 = new BufferedWriter(new FileWriter(file3));*/

		for (String doc : trainingList) {
			// get Id of the document from Elasticsearch

			QueryBuilder q = QueryBuilders.matchQuery("docno", doc);

			SearchResponse response = client.prepareSearch("hw7_2")
					.setTypes("document").setQuery(q).get();
			String id = "";
			if (response.getHits().getHits().length > 0)
				id = (String) response.getHits().getHits()[0].getId();

			// System.out.println("id:: " + id);

			// make a termvector response to elastic search
			try {
				TermVectorResponse resp = client.prepareTermVector()
						.setIndex("hw7_2").setType("document").setId(id)
						.setSelectedFields("text").execute().actionGet();
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();
				resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
				builder.endObject();
				// System.out.println(builder.string());

				JSONObject json = new JSONObject(builder.string());
				// System.out.println(json.get("term_vectors"));
				JSONObject j = json.getJSONObject("term_vectors");
				

				List<String> features = new ArrayList<String>();
				if (j.has("text") == true) {
					j = j.getJSONObject("text");

					j = j.getJSONObject("terms");

					Iterator<String> n = j.keys();

					int termId = 0;
					while (n.hasNext()) {
						String key = (String) n.next();
						// System.out.println(key);
						JSONObject t = (JSONObject) j.get(key);
						// System.out.println(t.get("term_freq"));
						// System.out.println(t.getJSONObject("term_freq"));
						int tf = (int) t.get("term_freq");

						if (!termCatalog.containsKey(key)) {
							termCatalog.put(key, count);
							termId = count;
						} else
							termId = termCatalog.get(key);

						count++;
						features.add(termId + ":" + key);

					}
				}

				double label = 0.0;

				if (indexCatalog.get(doc).equals("spam"))
					label = 1.0;

				writeToFile(doc, docCount, label, features, file, file1, out,
						out1);

			} catch (Exception e) {
				e.printStackTrace();
			}

			docCount++;
		}

		out.close();
		out1.close();
		System.out.println("Term Catalog Size::: "+termCatalog.size());
		
		File file2 = new File(path + "testMatrix" + ".txt");
		File file3 = new File(path + "testCatalog" + ".txt");

		
		BufferedWriter out2 = new BufferedWriter(new FileWriter(file2));
		BufferedWriter out3 = new BufferedWriter(new FileWriter(file3));
		System.out.println("Done Processing Training Matrix. Started with test..");
		
		for (String doc : testingList) {
			// get Id of the document from Elasticsearch

			QueryBuilder q = QueryBuilders.matchQuery("docno", doc);

			SearchResponse response = client.prepareSearch("hw7_2")
					.setTypes("document").setQuery(q).get();
			String id = "";
			if (response.getHits().getHits().length > 0)
				id = (String) response.getHits().getHits()[0].getId();

			// System.out.println("id:: " + id);

			// make a termvector response to elastic search
			try {
				TermVectorResponse resp = client.prepareTermVector()
						.setIndex("hw7_2").setType("document").setId(id)
						.setSelectedFields("text").execute().actionGet();
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();
				resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
				builder.endObject();
				// System.out.println(builder.string());

				JSONObject json = new JSONObject(builder.string());
				// System.out.println(json.get("term_vectors"));
				JSONObject j = json.getJSONObject("term_vectors");
				

				List<String> features = new ArrayList<String>();
				if (j.has("text") == true) {
					j = j.getJSONObject("text");

					j = j.getJSONObject("terms");

					Iterator<String> n = j.keys();

					int termId = 0;
					while (n.hasNext()) {
						String key = (String) n.next();
						// System.out.println(key);
						JSONObject t = (JSONObject) j.get(key);
						// System.out.println(t.get("term_freq"));
						// System.out.println(t.getJSONObject("term_freq"));
						int tf = (int) t.get("term_freq");

						if (!termCatalog.containsKey(key)) {
							termCatalog.put(key, count);
							termId = count;
						} else
							termId = termCatalog.get(key);

						count++;
						features.add(termId + ":" + key);

					}
				}

				double label = 0.0;

				if (indexCatalog.get(doc).equals("spam"))
					label = 1.0;

				writeToFile(doc, docCount, label, features, file2, file3, out2,
						out3);

			} catch (Exception e) {
				e.printStackTrace();
			}

			docCount++;
		}
		
		System.out.println("Done Processing Testing Matrix. Started with Training the Model.");
		System.out.println("Term Catalog Size:: "+termCatalog.size());
		
		out2.close();
		out3.close();
		
		
		
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

	private static void writeToFile(String doc, int docCounts, double label,
			List<String> features, File file, File file1, BufferedWriter out,
			BufferedWriter out1) throws IOException {
		String path = "C:/Users/Nitin/Assign7/output/part2/";

		/*
		 * File file = new File(path + "trainMatrix" + ".txt"); File file1 = new
		 * File(path + "trainCatalog" + ".txt");
		 * 
		 * if (!file.exists()) { file.createNewFile(); } if (!file1.exists()) {
		 * file.createNewFile(); }
		 */

		try {
			// BufferedWriter out = new BufferedWriter(new FileWriter(file));
			// BufferedWriter out1 = new BufferedWriter(new FileWriter(file1));

			// int in = 1;
			String fea = "";
			String line = docCount + "\t" + doc;
			out1.write(line);
			out1.newLine();

			for (String m : features) {

				fea += m + "\t";

			}
			String finalString = label + "\t" + fea;

			// in++;

			out.write(finalString);
			out.newLine();

			/*
			 * out.close(); out1.close();
			 */

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void getList(Client client, String split) {
		// Build Query
		QueryBuilder qb = QueryBuilders.matchQuery("split", split);

		// Search Response
		SearchResponse scrollResp = client.prepareSearch("hw7_2")
				.setSearchType(SearchType.SCAN).setScroll(new TimeValue(60000))
				.setQuery(qb).setSize(1000).execute().actionGet();

		// System.out.println("Size:: " + scrollResp.getHits().getTotalHits());
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
				indexCatalog2.put(doc, type);
			}
			br.close();

			System.out.println("Map Size::: " + indexCatalog2.size());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
