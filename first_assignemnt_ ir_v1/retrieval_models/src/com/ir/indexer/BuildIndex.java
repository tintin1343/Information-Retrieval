package com.ir.indexer;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildIndex {
	public static void main(String[] args) throws Exception {

		
		  Client client = new TransportClient().addTransportAddress(new
		  InetSocketTransportAddress( "localhost", 9300));
		 

		/*Node node = nodeBuilder().client(true).clusterName("elasticsearch").node();
		Client client = node.client();*/

		/* Setting and Analysis done in Marvel Sense */

		/* Reading and parsing file starts */
		
		try {
			File folder_name = new File("C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/ap89_collection/");

			/*
			 * File stop_words = new File(
			 * "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt"
			 * );
			 */
			
			System.out.println("folder_name:: " + folder_name);
			/* System.out.println("Stop Words file name:: " + stop_words); */

			File[] listOfFiles = folder_name.listFiles();

			/*
			 * List<String> stop_words_list = new ArrayList<String>();
			 * 
			 * GetStopWords gstp = new GetStopWords(); stop_words_list =
			 * gstp.getStopWordsList(stop_words);
			 * 
			 * System.out.println("Length of stop Words File::" +
			 * stop_words_list.size());
			 */
			// index, starting from 0
			int id = 1;
			System.out.println("Call for Indexing Starts @" + new Date());

			for (File file : listOfFiles) {

				if (file.isFile() && file.getName().startsWith("ap89")) {

					/*
					 * List<XContentBuilder> builders = getBuilders(file,
					 * stop_words_list);
					 */

					List<XContentBuilder> builders = getBuilders(file);

					for (XContentBuilder builder : builders) {
						client.prepareIndex("ap_dataset_4", "document", "" + id)
								.setSource(builder).execute().actionGet();
						++id;
					}
				}
			}

			System.out.println("Total Files Indexed: " + id + "@ " + new Date());
			
		} catch (Exception e) {
			e.printStackTrace();
			//node.close();
		}
		

		//node.close();

	}

	public static List<XContentBuilder> getBuilders(File file)
			throws FileNotFoundException {
		/*
		 * This function parses the input file and returns a list of json
		 * document
		 */

		List<XContentBuilder> builds = new ArrayList<XContentBuilder>();
		try {

			final Pattern DOC_PATTERN = Pattern.compile("<DOC>(.+?)</DOC>");
			final Pattern DOC_NO_PATTERN = Pattern
					.compile("<DOCNO>(.+?)</DOCNO>");
			final Pattern TXT_PATTERN = Pattern.compile("<TEXT>(.+?)</TEXT>");

			/*
			 * BufferedReader br = new BufferedReader(new InputStreamReader( new
			 * FileInputStream(file)));
			 * 
			 * String str;
			 */

			StringBuffer content = new StringBuffer();

			content = new GetStopWords().getContent(file);

			Matcher matcher0 = DOC_PATTERN.matcher(content.toString());

			/* int doc_Count = 0; */
			while (matcher0.find()) {

				ArticleBean e = new ArticleBean();
				Matcher matcher = DOC_NO_PATTERN.matcher(matcher0.group(1));

				if (matcher.find()) {
					e.setDocNo(matcher.group(1).trim());
				}

				Matcher matcher1 = TXT_PATTERN.matcher(matcher0.group(1));

				int k = 0;

				while (matcher1.find()) {
					String text = matcher1.group(1);
					if (k == 0) {
						e.setText(text);
					} else {
						e.setText(e.getText().concat(text));
					}
					k++;
				}

				// String[] words = e.getText().split(" ");

				/* Snippet to calculate each doc length without stopwords starts */
				/*
				 * int docLength = words.length; for (int i = 0; i <
				 * words.length; i++) { if (stop_words.contains(words[i])) {
				 * docLength = docLength - 1; } }
				 */

				XContentBuilder builder = jsonBuilder().startObject()
						.field("docno", e.getDocNo())
						.field("text", e.getText())
						// .field("doc_length", e.getText().length())
						.field("postDate", new Date()).endObject();
				builds.add(builder);

				/* doc_Count++; */
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return builds;
	}

}
