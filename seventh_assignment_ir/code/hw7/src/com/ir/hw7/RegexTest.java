package com.ir.hw7;

import groovy.json.JsonBuilder;
import groovy.json.internal.JsonFastParser;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.index.Fields;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvector.TermVectorRequest;
import org.elasticsearch.action.termvector.TermVectorRequest.Flag;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.metrics.percentiles.InternalPercentileRanks.Iter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class RegexTest {

	final static Pattern HTML_PATTERN = Pattern
			.compile("(<html.*?>)(.+?)(</html>)");

	public static void main(String[] args) throws IOException, JSONException {
		String text = "[R] Confidence-Intervals.... $$$$ help... | hi...  i have to use r to find out the 90% confidence-interval for the sensitivity and specificity of the following diagnostic test:  a particular diagnostic test for multiple sclerosis was conducted on 20 ms patients and 20 healthy subjects, 6 ms patients were classified as healthy and 8 healthy subjects were classified as suffering from the ms.  furthermore, i need to find the number of ms patients required for a sensitivity of 1%...  is there a simple r-command which can do that for me?  i am completely new to r...  help please!  jochen --  view this message in context: http://www.nabble.com/confidence-intervals....-help...-tf3544217.html#a9894014 sent from the r help mailing list archive at nabble.com.  ______________________________________________ r-help@stat.math.ethz.ch mailing list https://stat.ethz.ch/mailman/listinfo/r-help please do read the posting guide http://www.r-project.org/posting-guide.html and provide commented, minimal, self-contained, reproducible code."
				+ "and 8 healthy subjects were classified as suffering from the MS.";
		System.out.println(text);
		/*
		 * text= text.replaceAll("\\p{Punct}+", "");
		 */
		text = text.replaceAll("[!#%&'()*+,-./:;<=>?@[\\/]^_`{|}~\"]", "");

		System.out.println(text);

		Double x = 1.0;
		int i = (int) (double) x;
		System.out.println(i);

		Client client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

		QueryBuilder q = QueryBuilders.matchQuery("docno", "inmail.10");

		SearchResponse response = client.prepareSearch("hw7_2")
				.setTypes("document").setQuery(q).get();
		String id = "";
		if (response.getHits().getHits().length > 0)
			id = (String) response.getHits().getHits()[0].getId();

		System.out.println("id:: " + id);

		String fields[] = new String[1];
		fields[0] = "text";
		// Search Response
		// TermVectorResponse resp = client
		// .prepareTermVector("hw7_2", "document", id)
		// .setSelectedFields("text").setTermStatistics(true).get();

		TermVectorResponse resp = client.prepareTermVector().setIndex("hw7_2")
				.setType("document").setId(id).setSelectedFields("text")
				.execute().actionGet();
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
		builder.endObject();
		System.out.println(builder.string());
		
		JSONObject json = new JSONObject(builder.string());
		System.out.println(json.get("term_vectors"));
		JSONObject j = json.getJSONObject("term_vectors");
		j= j.getJSONObject("text");
		j= j.getJSONObject("terms");
		
		Iterator<String> n = j.keys();
		
		while(n.hasNext()){
			String key= (String) n.next();
			System.out.println(key);
			JSONObject t = (JSONObject) j.get(key);
			System.out.println(t.get("term_freq"));
			//System.out.println(t.getJSONObject("term_freq"));
		}
		
		//System.out.println(j);
		
		
		

		

		

	}

}
