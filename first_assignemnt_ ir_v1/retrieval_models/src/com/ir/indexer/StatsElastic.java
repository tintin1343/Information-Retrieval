package com.ir.indexer;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;

public class StatsElastic {

	/**
	 * get statistical facet by given docno or whole documents INFO including
	 * following: "facets": { "text": { "_type": "statistical", "count": 84678,
	 * "total": 18682561, "min": 0, "max": 802, "mean": 220.63063605659084,
	 * "sum_of_squares": 4940491417, "variance": 9666.573376838636,
	 * "std_deviation": 98.31873360066552 } }
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param matchedField
	 * @param matchedValue
	 * @return
	 * @throws IOException
	 */
	public StatisticalFacet getStatsOnTextTerms(Client client,
			String index, String type, String matchedField, String matchedValue)
			throws IOException {
		XContentBuilder facetsBuilder;
		if (matchedField == null && matchedValue == null) { // match_all docs
			facetsBuilder = getStatsTermsBuilder();
		} else {
			facetsBuilder = getStatsTermsByMatchFieldBuilder(matchedField,
					matchedValue);
		}
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSource(facetsBuilder).execute().actionGet();
		StatisticalFacet f = (StatisticalFacet) response.getFacets()
				.facetsAsMap().get("text");
		return f;
	}

	/**
	 * builder for facets statistical terms length by given matched field, like
	 * docno. In Sense:
	 *
	 * POST ap_dataset/document/_search { "query": { "match": { "docno":
	 * "AP891216-0142" } }, "facets": { "text": { "statistical": { "script":
	 * "doc['text'].values.size()" } } } }
	 * 
	 * @param matchField
	 * @param matchValue
	 * @return
	 * @throws IOException
	 */
	public XContentBuilder getStatsTermsByMatchFieldBuilder(
			String matchField, String matchValue) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().startObject("query").startObject("match")
				.field(matchField, matchValue).endObject().endObject()
				.startObject("facets").startObject("text")
				.startObject("statistical")
				.field("script", "doc['text'].values.size()").endObject()
				.endObject().endObject().endObject();
		return builder;
	}

	

	/**
	 * builder for the facets statistical terms length by whole documents. In
	 * Sense: POST /ap_dataset/document/_search { "query": {"match_all": {}},
	 * "facets": { "text": { "statistical": { "script":
	 * "doc['text'].values.size()" } } } }
	 * 
	 * @return
	 * @throws IOException
	 */
	private static XContentBuilder getStatsTermsBuilder() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject().startObject("query").startObject("match_all")
				.endObject().endObject().startObject("facets")
				.startObject("text").startObject("statistical")
				.field("script", "doc['text'].values.size()").endObject()
				.endObject().endObject().endObject();
		return builder;
	}
	
}
