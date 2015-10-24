import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class RunOkapi {

	public static void main(String[] args) throws IOException {

		try {
			String type = "woStopWords";
			Utility u = new Utility(type);

			Double avg_doc_length = u.returnAvgLength();
			Map<String, DocBean> docCatBean = u.getDocCat();
			Map<String, TokenCatalogBean> tokenCatBean = u.getTokenCat();
			String folder = u.returnFolderName();

			System.out.println("Avg Doc Length:: " + avg_doc_length);
			System.out.println("DocCatBean Size:: " + docCatBean.size());
			System.out.println("Token Cat Bean:: " + tokenCatBean.size());
			System.out.println("Doc Bean:::: " + u.getDocBean().size());
			System.out.println("Folder:: "+folder);

			// System.out.println("Avg Doc Length:::: " + avg_doc_length);

			/* Method to read the query file */

			String query_file_path = "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
			/* This will be later replaced by path from the config file */

			GetFinalQueries qu = new GetFinalQueries();
			List<String> queries = qu.readQueryFile(query_file_path);

			List<String> stop_words_final = qu.getStopWords();

			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = qu.getFinalQueryList(queries, stop_words_final);

			List<List<String>> okapi = new ArrayList<List<String>>();

			System.out.println("QueryMapisze::: " + final_query.size());

			for (List<String> query : final_query) {

				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;

				System.out.println("=======================");

				querynum = query.get(0).replace(".", "");
				/*
				 * For every word in a query calculates the okapif value and
				 * sums it up
				 */
				for (int y = 1; y < query.size(); y++) {

					String w = query.get(y).toLowerCase();

					Map<String, Integer> tfMap = new HashMap<String, Integer>();

					tfMap = Okapi(w.replaceAll("[,\"()]", ""), avg_doc_length,
							tokenCatBean, docCatBean, u,folder);

					System.out.println("Size of TF Results:: " + tfMap.size()
							+ "for :" + w.replaceAll("[,\"()]", ""));

					/*
					 * try { Thread.sleep(5000); //1000 milliseconds is one
					 * second. } catch(InterruptedException ex) {
					 * Thread.currentThread().interrupt(); }
					 */

					for (Map.Entry<String, Integer> term : tfMap.entrySet()) {

						double docLen = getDocLength(term.getKey(), docCatBean);
						// System.out.println(docLen);

						if (rankTerm.get(term.getKey()) != null) {
							rankTerm.put(
									term.getKey(),
									(rankTerm.get(term.getKey()) + okapiTFPerTerm(
											term.getValue(), docLen,
											avg_doc_length)));
						} else {
							rankTerm.put(
									term.getKey(),
									okapiTFPerTerm(term.getValue(), docLen,
											avg_doc_length));
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
				okapi.add(queryResults);
			}

			WriteFile w = new WriteFile();
			w.writeToFile(okapi, "okapi-1.txt", type);

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

	public static Map<String, Integer> Okapi(String word,
			Double avg_doc_length, Map<String, TokenCatalogBean> tokenCatBean,
			Map<String, DocBean> docCatBean, Utility u,String folder) throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();

		if (tokenCatBean.containsKey(word.toLowerCase().trim())) {
			// System.out.println("Calling queryTF..Contains Words");
			results = queryTF(word, tokenCatBean, docCatBean, u,folder);
		}

		return results;

	}

	/* Function for calculating OkapiTF for individual query terms */
	public static double okapiTFPerTerm(Integer termFreq, double docLen,
			double avgLen) {

		double LenRatio = (double) (docLen / avgLen);
		/*
		 * double oTF = (double) ((double) termFreq /(double) (termFreq + 0.5 +
		 * (1.5 * LenRatio)));
		 */

		return (double) ((double) termFreq / (double) (termFreq + 0.5 + (1.5 * LenRatio)));

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

			/*
			 * RandomAccessFile raf = new RandomAccessFile(
			 * "C:\\Users\\Nitin\\Assign2\\FilesRaw\\TermsHash84.txt", "r");
			 */
			RandomAccessFile raf = new RandomAccessFile(
					"C:\\Users\\Nitin\\Assign2\\"+folder+"\\TermsHash84.txt",
					"r");
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
