package com.ccs.ir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class ReadIndexAndBuild1 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Map<String, String> linkMap = new HashMap<String, String>();
		int count = 0;
		Node node = nodeBuilder().client(true).clusterName("IR").node();

		try {

			Client client = node.client();

			QueryBuilder qb = QueryBuilders.matchAllQuery();

			SearchResponse scrollResp = client.prepareSearch("spider")
					.setSearchType(SearchType.SCAN).setTypes("document")
					.setQuery(qb).setScroll(new TimeValue(60000)).setSize(1000)
					.execute().actionGet(); // 1000 hits per shard will be
											// returned for each
											// scroll

			File file = new File("C:/Users/Nitin/Assign4/inLinksCatalog1.txt");

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			// Scroll until no hits are returned
			while (true) {
				System.out.println("In While");

				for (SearchHit hit : scrollResp.getHits().getHits()) {
					Map<String, Integer> outLinkPerPage = new HashMap<String, Integer>();
					// System.out.println("In hit");
					// Handle the hit...
					String doc_no = (String) hit.getSource().get("docno");
					String inLinks = (String) hit.getSource().get("in_links");

					if (!linkMap.containsKey(doc_no)) {
						// linkMap.put(doc_no, inLinks);
						String[] in = inLinks.split("\\|\\|");
						String outLinksCount = "";

						for (int i = 0; i < in.length; i++) {

							String outLinks1 = "";
							int length = 0;

							if (!outLinkPerPage.containsKey(in[i])) {
								
								QueryBuilder qb1 = QueryBuilders.matchQuery(
										"docno", in[i]);
								SearchResponse response = client
										.prepareSearch("spider")
										.setTypes("document").setQuery(qb1)
										.get();
								
								if (response.getHits().getHits().length > 0) {

									outLinks1 = (String) response.getHits()
											.getHits()[0].getSource().get(
											"out_links");
									String[] out1 = outLinks1.split("\\|\\|");
									length = out1.length;
									outLinkPerPage.put(in[i], length);
								}

								

							}
							else {
								length = outLinkPerPage.get(in[i]);
							}
							outLinksCount += in[i] + " " + length + "||";

						}

						bw.write(doc_no + "||" + outLinksCount);
						linkMap.put(doc_no, outLinksCount);

						bw.newLine();

						count++;

						System.out.println("Count :: " + count);
						System.out.println("LinkMap Size:: " + linkMap.size());

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

			System.out.println("LinkMap Size:: " + linkMap.size());
			bw.flush();
			bw.close();
			fw.close();

			if (node != null) {
				node.close();
				node = null;
			}

		} catch (Exception e) {
			System.out.println("In Error...");
			e.printStackTrace();
			node.close();
		}

	}

}
