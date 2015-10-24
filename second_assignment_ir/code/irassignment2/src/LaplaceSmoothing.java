
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LaplaceSmoothing {
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

			long vocabSize = tokenCatBean.size();
			System.out.println("Vocab Size ::" + vocabSize);

			System.out.println("Avg Doc Length:::: " + avg_doc_length);

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
			 * List<String> stop_words_final = new ArrayList<String>();
			 * 
			 * for (int i = 0; i < stop_words_custom.length; i++) {
			 * stop_words_final.add(stop_words_custom[i]); }
			 */

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

			/*
			 * for (List<String> query : final_query) {
			 * System.out.println("___________________________"); for (String q
			 * : query) { System.out.println(q); }
			 * System.out.println("___________________________"); }
			 */

			List<List<String>> resultOkapi = new ArrayList<List<String>>();
			/* for (String query : queries) { */

			for (List<String> query : final_query) {
				Map<String, String> queryTFList = new HashMap<String, String>();
				Map<String, Double> rankTerm = new HashMap<String, Double>();

				String querynum = null;

				System.out.println("Query Minus stop words");
				System.out.println("=======================");
				// System.out.println(query.get(0));

				/*
				 * For every word in a query calculates the okapif value and
				 * sums it up
				 */
				querynum = query.get(0).replace(".", "");

				// System.out.println("Query Numm::: "+ querynum);

				System.out.println("Query Size::" + query.size());
				for (int i = 1; i < query.size(); i++) {
					/* Method to calculate tfs for each term in query */
					String w = query.get(i).toLowerCase();

					Map<String, Integer> tfMap = new HashMap<String, Integer>();
					// System.out.println("Calculating for Word::: " + q);
					tfMap = laplaceSmoothing(w.replaceAll("[,\"()]", ""),
							avg_doc_length, tokenCatBean, docCatBean, u,
							vocabSize,folder);

					System.out.println("Size of TF Results:: " + tfMap.size()
							+ "for :" + w.replaceAll("[,\"()]", ""));

					for (Map.Entry<String, Integer> term : tfMap.entrySet()) {

						if (queryTFList.get(term.getKey()) == null) {
							queryTFList.put(term.getKey(), term.getValue()
									.toString());
						} else {
							queryTFList.put(term.getKey(),
									queryTFList.get(term.getKey()) + " "
											+ term.getValue().toString());

							// System.out.println("TF:: "+queryTFList.get(term.getKey()));
						}
					}

				}

				System.out.println("Final DOc List Size::: "
						+ queryTFList.size());
				System.out
						.println("Calculating Laplace Smoothing Score for each ::::::: ");
				for (Map.Entry<String, String> d : queryTFList.entrySet()) {

					double docLen = getDocLength(d.getKey(), docCatBean);

					rankTerm.put(
							d.getKey(),
							laplacePerTerm(d.getValue(), docLen,
									avg_doc_length, vocabSize, query.size()));

				}
				/* Method to Sort Hashmap based on the value */
				SortMap sm = new SortMap();

				LinkedHashMap<String, Double> sortedRanks = (LinkedHashMap<String, Double>) sm.getSortedRankMap(rankTerm);

				int j = 1;
				List<String> queryResults = new ArrayList<String>();

				for (Entry<String, Double> term : sortedRanks.entrySet()) {

					if (j <= 1000) {

						String toWrite = querynum + " " + "Q0" + " "
								+ term.getKey() + " " + j + " "
								+ term.getValue() + " " + "EXP";

						// System.out.println(toWrite);

						queryResults.add(toWrite);

					} else {
						// bw.newLine();
						break;
					}

					j++;
				}

				resultOkapi.add(queryResults);

			}

			WriteFile w = new WriteFile();
			w.writeToFile(resultOkapi, "Laplace-1.txt",type);
			// node.close();

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

	
	public static Map<String, Integer> laplaceSmoothing(String word,
			Double avg_doc_length, Map<String, TokenCatalogBean> tokenCatBean,
			Map<String, DocBean> docCatBean, Utility u, long vocabSize, String folder)
			throws IOException {

		Map<String, Integer> results = new HashMap<String, Integer>();
		

		if (tokenCatBean.containsKey(word.toLowerCase().trim())) {
			//System.out.println("Calling queryTF..Contains Words");
			results = queryTF(word, tokenCatBean,docCatBean,u,folder);
		}

		return results;

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

	/*
	 * @SuppressWarnings("unchecked") private static LinkedHashMap<String,
	 * Double> getSortedMap(Map<String, Double> rankTerm) { // TODO
	 * Auto-generated method stub System.out.println("Started Sorting.."); List
	 * mapKeys = new ArrayList(rankTerm.keySet()); List mapValues = new
	 * ArrayList(rankTerm.values()); Collections.sort(mapValues,
	 * Collections.reverseOrder()); Collections.sort(mapKeys);
	 * 
	 * @SuppressWarnings("rawtypes") LinkedHashMap sortedMap = new
	 * LinkedHashMap();
	 * 
	 * Iterator valueIt = mapValues.iterator(); while (valueIt.hasNext()) {
	 * Object val = valueIt.next(); Iterator keyIt = mapKeys.iterator();
	 * 
	 * while (keyIt.hasNext()) { Object key = keyIt.next(); String comp1 =
	 * rankTerm.get(key).toString(); String comp2 = val.toString();
	 * 
	 * if (comp1.equals(comp2)){ rankTerm.remove(key); mapKeys.remove(key);
	 * sortedMap.put((String)key, (Double)val); break; }
	 * 
	 * }
	 * 
	 * } System.out.println("Finished Sorting.."); return sortedMap;
	 * 
	 * }
	 */

	private static List<String> getStopWords() {
		// TODO Auto-generated method stub

		File stop_words = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt");
		String stop;
		// StringBuffer stop_words_list = new StringBuffer();
		List<String> stop_words_final = new ArrayList<String>();

		/*
		 * for (int i = 0; i < stop_words_custom.length; i++) {
		 * stop_words_final.add(stop_words_custom[i]); }
		 */

		try {
			if (stop_words.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					/* stop_words_list.append(stop).append(" "); */
					stop_words_final.add(stop.trim());

				}
				stop_words_final.add("discuss");
				stop_words_final.add("identify");
				stop_words_final.add("report");
				stop_words_final.add("include");
				stop_words_final.add("predict");
				stop_words_final.add("cite");
				stop_words_final.add("describe");
				stop_words_final.add("Document");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stop_words_final;
	}

	private static List<String> readQueryFile(String query_file_path)
			throws IOException {
		// TODO Auto-generated method stub

		String str;
		// StringBuffer query = new StringBuffer();
		List<String> query_list = new ArrayList<String>();

		try {
			File query_file = new File(query_file_path);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(query_file)));

			while ((str = br.readLine()) != null) {
				// query.append(str.trim());
				int startIndex = 0;
				String q = str.trim();
				// q = str.replaceAll("[,!?\\()\"]", "");

				if (q.length() > 0) {

					int endIndexofStop = q.lastIndexOf(".");

					query_list.add(q.substring(startIndex, endIndexofStop)
							.trim());
				}

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query_list;
	}

	/* Function for calculating OkapiTF for individual query terms */
	public static double okapiTFPerTerm(Integer termFreq, double docLen,
			double avgLen) {

		double LenRatio = (docLen / avgLen);
		double oTF = (double) (termFreq / (termFreq + 0.5 + (1.5 * LenRatio)));

		return oTF;

	}

	/* Function for calculating laplacePerTerm for individual query terms */
	public static double laplacePerTerm(String termFreq, double docLen,
			double avgLen, long vocabSize, int queryWords) {

		// System.out.println("Query Words:::");

		String[] termFs = termFreq.split(" ");

		// System.out.println("TermFs::: "+ termFreq);

		double log_p_lap = 0.0;

		/*
		 * if(termFs.length <= queryWords){
		 * 
		 * for(int j=0;j<(queryWords-termFs.length); j++){ log_p_lap +=
		 * Math.log(((0 + 1) / (docLen + vocabSize)); } log_p_lap+= (Math.log((0
		 * + 1) / (docLen + vocabSize))) * (queryWords - termFs.length);
		 * 
		 * }
		 */
		if (termFs.length < queryWords) {
			log_p_lap += (Math.log((0 + 1) / (docLen + vocabSize)))
					* (queryWords - termFs.length);
		}

		for (int i = 0; i < termFs.length; i++) {

			log_p_lap += Math.log(((Integer.parseInt(termFs[i])) + 1)
					/ (docLen + vocabSize));
		}

		return log_p_lap;

	}


	public static Map<String, Integer> queryTF(String word,
			Map<String, TokenCatalogBean> tokenCatBean,
			Map<String, DocBean> docCatBean, Utility u, String folder) throws IOException {

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
