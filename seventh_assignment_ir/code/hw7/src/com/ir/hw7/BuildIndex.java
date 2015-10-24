package com.ir.hw7;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BuildIndex {
	final static String PATH = "C:/Users/Nitin/Assign7/data/trec07p/";
	final static Pattern HTML_PATTERN = Pattern
			.compile("(<html.*?>)(.+?)(</html>)");
	static int textType = 0;
	static int htmlType = 0;

	static Map<String, String> indexCatalog = new LinkedHashMap<String, String>();
	static Map<String, String> trainCat = new LinkedHashMap<String, String>();
	static Map<String, String> testCat = new LinkedHashMap<String, String>();

	public static void main(String[] args) {
		// create a client

		Client client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

		// read and load the catalog file in memory for spam/ham
		readCatalog("full", "index", indexCatalog);

		// read the folder to get files
		File folder_name = new File(PATH + "/data");
		System.out.println("folder_name:: " + folder_name);
		// get list of files
		File[] listOfFiles = folder_name.listFiles();

		// iterate through the folder to get the files
		int id = 0;
		System.out.println("Reading File..");
		System.out.println("Started Indexing @ " + new Date());
		for (File file : listOfFiles) {

			if (file.isFile() && file.getName().startsWith("inmail")) {
				List<XContentBuilder> builders = getBuilders(file);
				// ITERATE THROUGH THE LIST OF DOCUMENTS AND INDEX EACH ONE
				for (XContentBuilder builder : builders) {
					client.prepareIndex("hw7", "document", "" + id)
							.setSource(builder).execute().actionGet();
					++id;
				}
			}
		}

		System.out.println("Number of files Indexed::: " + id);

		System.out.println("Text types:: " + textType);
		System.out.println("HTML types:: " + htmlType);

		System.out.println("trainMap Size:: " + trainCat.size());
		System.out.println("Train Map %:: " + (trainCat.size() * 100) / id);
		System.out.println("testMap Size:: " + testCat.size());
		System.out.println("Test Map %:: " + (testCat.size() * 100) / id);

		System.out.println("Stopped Indexing @ " + new Date());

	}

	private static List<XContentBuilder> getBuilders(File file) {
		List<XContentBuilder> builds = new ArrayList<XContentBuilder>();
		try {

			String str;
			StringBuffer content = new StringBuffer();
			String split = trainOrTest();
			

			if (split.equals("train")) {
				trainCat.put(file.getName(), split);
			} else {
				testCat.put(file.getName(), split);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			while ((str = br.readLine()) != null) {
				content.append(" ").append(str);
			}

			String con = "";

			if (content.toString().contains("Lines: ")) {
				String lines[] = content.toString().split("Lines: \\d+");
				if (lines.length > 0)
					con = lines[1].trim().toLowerCase();
			}

			String htmlContent = "";
			String fileType = "";
			String finalText = "";
			Document resp = null;
			Matcher matcher = null;

			matcher = HTML_PATTERN.matcher(content.toString().toLowerCase());

			if (matcher.find()) {
				htmlContent = matcher.group(0).trim();
				resp = Jsoup.parse(htmlContent, "UTF-8");
				String text = resp.body().text();
				//System.out.println("HTML:: " + text);
				finalText = text;
				htmlType++;

			} else if (!matcher.find()) {
				// htmlContent=con.trim();
				String text = con;
				finalText = text;
				//System.out.println("Text:: " + text);
				textType++;
			}

			// System.out.println("text::: "+text);
			fileType = indexCatalog.get(file.getName());

			XContentBuilder builder = jsonBuilder().startObject()
					.field("docno", file.getName()).field("text", finalText)
					.field("label", fileType).field("split", split)
					.field("postDate", new Date()).endObject();

			builds.add(builder);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return builds;
	}

	private static String trainOrTest() {
		Random randomGenerator = new Random();
		int random = randomGenerator.nextInt(11);
		String split = "";
		if (random < 9)
			split = "train";
		else
			split = "test";

		return split;
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

}
