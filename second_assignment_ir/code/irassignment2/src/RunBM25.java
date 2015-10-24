import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunBM25 {
	public static void main(String[] args) throws IOException {

		try {
			String type = "rawWords";
			Utility u = new Utility(type);
			String folder = u.returnFolderName();

			Double avg_doc_length = u.returnAvgLength();
			Map<String, DocBean> docCatBean = u.getDocCat();
			Map<String, TokenCatalogBean> tokenCatBean = u.getTokenCat();

			System.out.println("Avg Doc Length:: " + avg_doc_length);
			System.out.println("DocCatBean Size:: " + docCatBean.size());
			System.out.println("Token Cat Bean:: " + tokenCatBean.size());
			System.out.println("Doc Bean:::: " + u.getDocBean().size());

			
			/* Method to read the query file */

			String query_file_path = "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
			/* This will be later replaced by path from the config file */

			GetFinalQueries qu = new GetFinalQueries();
			List<String> queries = qu.readQueryFile(query_file_path);

			/*
			 * Method to get stopwords from the file and append the common words
			 * from query file
			 */

			/* Changed the split regex from space to space and hypen */
			List<String> stop_words_final = qu.getStopWords();

			/*
			 * Method to remove stopwords from query and just get the final
			 * query
			 */

			/*
			 * Iterating the queries one by one. Each Query is a list of String
			 * (Query Words)
			 */
			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = qu.getFinalQueryList(queries, stop_words_final);

			List<List<String>> resultbm25 = new ArrayList<List<String>>();

			for (List<String> query : final_query) {

				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;
				// System.out.println("Query Minus stop words");
				System.out.println("=======================");
				// System.out.println(query.get(0));
				// int y = 0;

				querynum = query.get(0).replace(".", "");
				/*
				 * For every word in a query calculates the okapif value and
				 * sums it up
				 */
				for (int y = 1; y < query.size(); y++) {

					String w = query.get(y).toLowerCase();
					Map<String, Integer> tfMap = new HashMap<String, Integer>();
					// System.out.println("Calculating for Word::: " + q);
					tfMap = OkapiIDF(w.replaceAll("[,\"()]", ""), avg_doc_length,
							tokenCatBean, docCatBean, u,folder);

					System.out.println("Size of TF Results:: " + tfMap.size()
							+ "for :" + w.replaceAll("[,\"()]", ""));

					int idf = tfMap.size();

					for (Map.Entry<String, Integer> term : tfMap.entrySet()) {

						double docLen = getDocLength(term.getKey(), docCatBean);
						if (rankTerm.get(term.getKey()) != null) {
							rankTerm.put(
									term.getKey(),
									(rankTerm.get(term.getKey()) + bm25perTerm(
											term.getValue(), docLen,
											avg_doc_length, idf)));
						} else {
							rankTerm.put(
									term.getKey(),
									bm25perTerm(term.getValue(), docLen,
											avg_doc_length, idf));
						}
					}

				}
				SortMap sm = new SortMap();

				/* Method to Sort Hashmap based on the value */
				LinkedHashMap<String, Double> sortedRanks = (LinkedHashMap<String, Double>) sm
						.getSortedRankMap(rankTerm);

				int j = 1;
				List<String> queryResults = new ArrayList<String>();

				for (Entry<String, Double> term : sortedRanks.entrySet()) {

					if (j <= 100) {

						String toWrite = querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP";

						queryResults.add(toWrite);

					} else {
						break;
					}

					j++;
				}
				resultbm25.add(queryResults);
			}
			WriteFile w = new WriteFile();
			w.writeToFile(resultbm25, "BM25-1.txt",type);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static double getDocLength(String key,
			Map<String, DocBean> docCatBean) {
		// TODO Auto-generated method stub
		double doclength = 0.0;

		if (docCatBean.containsKey(key)) {
			doclength = docCatBean.get(key).getDocLength();
		}

		return doclength;
	}

	public static Map<String, Integer> OkapiIDF(String word,
			Double avg_doc_length, Map<String, TokenCatalogBean> tokenCatBean,
			Map<String, DocBean> docCatBean, Utility u,String folder) throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		if (tokenCatBean.containsKey(word.toLowerCase().trim())) {
			// System.out.println("Calling queryTF..Contains Words");
			results = queryTF(word, tokenCatBean, docCatBean, u,folder);
		}
		return results;

	}

	/* Function for calculating OkapiIDF for individual query terms */
	public static double bm25perTerm(Integer termFreq, double docLen,
			double avgLen, double idf) {
		double k1 = 1.2;
		double k2 = 300;
		double b = 0.75;

		double a_bm25 = (double) Math.log((84678 + 0.5) / (idf + 0.5));
		double b_bm25 = (double) termFreq + (k1 * termFreq);
		double b_d_bm25 = (double) termFreq
				+ (k1 * ((1 - b) + b * (docLen / avgLen)));
		double c_bm25 = 1 /* (double) (1+ k2 * 1)/(1+ k2); */;

		double bm25 = (double) (a_bm25 * (b_bm25 / b_d_bm25) * c_bm25);
		return bm25;

	}

	public static Map<String, Integer> queryTF(String word,
			Map<String, TokenCatalogBean> tokenCatBean,
			Map<String, DocBean> docCatBean, Utility u,String folder) throws IOException {

		Map<String, Integer> results = new HashMap<>();
		long startOffset = 0;
		long endOffset = 0;
		word = word.toLowerCase().trim();

		if (tokenCatBean.containsKey(word)) {
			// System.out.println("tokenCatBeanContains the word "+word);
			startOffset = tokenCatBean.get(word).getStartOffset();
			endOffset = tokenCatBean.get(word).getEndOffset();
			// System.out.println("StartOffset:: "+startOffset);
			// System.out.println("EndOffset:: "+endOffset);

			RandomAccessFile raf = new RandomAccessFile(
					"C:\\Users\\Nitin\\Assign2\\"+folder+"\\TermsHash84.txt", "r");
			raf.seek(startOffset);
			byte[] termLine = new byte[(int) (endOffset - startOffset)];
			raf.read(termLine);
			String term = new String(termLine);
			// System.out.println("Term Fetched:::: "+term);

			String[] termOutput = term.split(" ");
			// System.out.println("last splitTerm"+
			// termOutput[termOutput.length-1]);

			for (int i = 1; i < termOutput.length - 1; i++) {

				String s = termOutput[i];
				// System.out.println("String output:: "+s);
				String[] docDetail = s.split(":");
				int docId = Integer.parseInt(docDetail[0]);
				int endIndex = docDetail[1].indexOf("-");
				int tF = Integer.parseInt(docDetail[1].substring(0, endIndex));
				results.put(u.getDocKey(docId), tF);

			}

			raf.close();

		}

		return results;
	}

}
