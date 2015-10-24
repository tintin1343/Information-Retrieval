package com.ir.hw3;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildAndMergeIndex {
	public static void main(String[] args) throws Exception {

		/*
		 * Client client = new TransportClient().addTransportAddress(new
		 * InetSocketTransportAddress( "localhost", 9300));
		 */

		Node node = nodeBuilder().client(true).clusterName("rnh").node();
		Client client = node.client();

		/* Setting and Analysis done in Marvel Sense */

		Map<String, String> inLinksMap = readInLinks();
		//System.out.println("Inlinks Map Size:: " + inLinksMap.size());

		/* Reading and parsing file starts */

		try {
			File folder_name = new File("C:/Users/Nitin/Assign3/files/test/");

			System.out.println("folder_name:: " + folder_name);

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
			int id = 1001;
			System.out.println("Call for Indexing Starts @" + new Date());

			for (File file : listOfFiles) {

				if (file.isFile() && file.getName().startsWith("obama")) {

					/*
					 * List<XContentBuilder> builders = getBuilders(file,
					 * stop_words_list);
					 */

					/* List<XContentBuilder> builders = */getBuilders(file,
							inLinksMap, client);

					/*
					 * for (XContentBuilder builder : builders) {
					 * client.prepareIndex("crawler", "document", "" +
					 * builder.field("docno"))
					 * .setSource(builder).execute().actionGet(); ++id; }
					 */
				}
			}

			System.out
					.println("Total Files Indexed: " + id + "@ " + new Date());

		} catch (Exception e) {
			e.printStackTrace();
			// node.close();
		}

		node.close();

	}

	public static List<XContentBuilder> getBuilders(File file,
			Map<String, String> inLinksMap, Client client)
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
			final Pattern HEAD_PATTERN = Pattern.compile("<HEAD>(.+?)</HEAD>");
			final Pattern HTML_PATTERN = Pattern
					.compile("<HTMLRESPONSE>(.+?)</HTMLRESPONSE>");
			final Pattern OUTLINKS_PATTERN = Pattern
					.compile("<OUTLINKS>(.+?)</OUTLINKS>");

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
				Matcher matcher1 = TXT_PATTERN.matcher(matcher0.group(1));
				Matcher matcherhead = HEAD_PATTERN.matcher(matcher0.group(1));
				Matcher matcherhtml = HTML_PATTERN.matcher(matcher0.group(1));
				Matcher matcheroutlinks = OUTLINKS_PATTERN.matcher(matcher0
						.group(1));

				if (matcher.find()) {
					e.setDocNo(matcher.group(1).trim());
				}

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

				if (matcherhtml.find()) {
					String htmlresponse = matcherhtml.group(1).toString();
					e.setHtmlResponse(htmlresponse);
				}

				if (matcheroutlinks.find()) {
					String outlinks = matcheroutlinks.group(1);
					e.setOutlinks(outlinks);
				}

				if (matcherhead.find()) {
					String title = matcherhead.group(1);
					e.setTitle(title);
				}

				String inLinks = "";

				inLinks = inLinksMap.get(e.getDocNo().trim());

				TestIndex li = new TestIndex();
				String oldInLinks = li.checkURL(inLinks, client);
				
				XContentBuilder builder=null;
				
				if(oldInLinks.length() > 0 || oldInLinks != ""){
					
					oldInLinks=getFinalString(oldInLinks,inLinks);
					
					builder = jsonBuilder()
							.startObject()
							.field("docno", e.getDocNo())
							.field("title", e.getTitle())
							.field("text",
									e.getText().trim().replaceAll("[\"\"'``]", ""))
							.field("html_Source", e.getHtmlResponse())
							.field("out_links", e.getOutlinks())
							.field("in_links", oldInLinks)
							.field("postDate", new Date()).endObject();
					
					//System.out.println("After concat.....");
				}else{
					builder = jsonBuilder()
							.startObject()
							.field("docno", e.getDocNo())
							.field("title", e.getTitle())
							.field("text",
									e.getText().trim().replaceAll("[\"\"'``]", ""))
							.field("html_Source", e.getHtmlResponse())
							.field("out_links", e.getOutlinks())
							.field("in_links",inLinks)
							.field("postDate", new Date()).endObject();
					//System.out.println("No concat.....");
				}
				

				client.prepareIndex("crawler", "document", "" + e.getDocNo())
						.setSource(builder).execute().actionGet();
				// builds.add(builder);

				/* doc_Count++; */
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return builds;
	}

	private static String getFinalString(String oldInLinks, String inLinks) {
		// TODO Auto-generated method stub
		
		String[] old = null;
		
		System.out.println("InlInks::::: "+ inLinks);
		System.out.println("OldLinnks::::: "+ oldInLinks);

		String[] newLink = null;
		List<String> finalInLinks = new ArrayList<String>();
		
		if(oldInLinks.contains("||")){
			old = oldInLinks.split("\\|\\|");
			for(int i=0 ;i<old.length;i++){
				finalInLinks.add(old[i]);
			}
		}
		else{
			finalInLinks.add(oldInLinks);
		}
		
		if(inLinks.contains("||")){
			newLink = inLinks.split("\\|\\|");
			for(int i=0 ;i<newLink.length;i++){
				if(!finalInLinks.contains(newLink[i]))
					finalInLinks.add(newLink[i]);
			}
		}
		else{
			if(!finalInLinks.contains(inLinks))
				finalInLinks.add(inLinks);
		}
	
	
		String finalList= "";
		
		for(String l:finalInLinks){
			finalList = finalList+ "||"+ l;
			
		}
		//System.out.println("FinalList::::");
		//System.out.println(finalList);
		return finalList;
		
	}

	private static Map<String, String> readInLinks() throws IOException {

		try {

			File tokenCatFile = new File(
					"C:\\Users\\Nitin\\Assign3\\files\\test\\inLinksCatalog.txt");

			FileReader rd = new FileReader(tokenCatFile);
			BufferedReader br = new BufferedReader(rd);
			String line = "";
			Map<String, String> inLinksMap = new HashMap<String, String>();

			while ((line = br.readLine()) != null) {

				ArticleBean ab = new ArticleBean();
				String termLine = line;

				if (termLine != null) {
					int indexOfPipe = termLine.indexOf("||");
					String urlKey = termLine.substring(0, indexOfPipe);
					String urlValues = termLine.substring(indexOfPipe + 2);

					inLinksMap.put(urlKey, urlValues);
				}

			}

			// System.out.println("ToKen Map Size::: " + inLinksMap.size());

			br.close();
			rd.close();
			return inLinksMap;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
