import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.tartarus.snowball.ext.porterStemmer;

/*This class uses the lambda parameter formula and considers only 
 * those docs which has all the query terms.
 * Changed the Formula, and experimenting with all words. Final Proximity*/
public class Proximity5 {

	public static void main(String[] args) throws Exception {

		try {
			String type = "stemmedWords";
			Utility u = new Utility(type);

			Double avg_doc_length = u.returnAvgLength();
			Map<String, DocBean> docCatBean = u.getDocCat();
			Map<String, TokenCatalogBean> tokenCatBean = u.getTokenCat();
			String folder = u.returnFolderName();

			System.out.println("Avg Doc Length:: " + avg_doc_length);
			System.out.println("DocCatBean Size:: " + docCatBean.size());
			System.out.println("Token Cat Bean:: " + tokenCatBean.size());
			System.out.println("Doc Bean:::: " + u.getDocBean().size());
			System.out.println("Folder:: " + folder);

			/* Method to read the query file */

			String query_file_path = "C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
			/* This will be later replaced by path from the config file */

			GetFinalQueries qu = new GetFinalQueries();
			List<String> queries = qu.readQueryFile(query_file_path);

			List<String> stop_words_final = getStopWords();

			List<List<String>> final_query = new ArrayList<List<String>>();

			final_query = getFinalQueryList(queries, stop_words_final);

			List<List<String>> okapi = new ArrayList<List<String>>();

			System.out.println("QueryMapisze::: " + final_query.size());

			for (List<String> query : final_query) {

				Map<String, Double> rankTerm = new HashMap<String, Double>();
				String querynum = null;

				System.out.println("=======================");

				querynum = query.get(0).replace(".", "");
				Map<String, String> minSpanMap = new HashMap<String, String>();
				/*
				 * For every word in a query calculates the proximity min span
				 * if all terms of query exist in the document
				 */
				String q = "";

				for (int i = 1; i < query.size(); i++) {
					q += query.get(i).trim().toLowerCase()
							.replaceAll("[,\"()]", "")
							+ " ";
				}

				// System.out.println("Query:: " + q);
				minSpanMap = getMinSpan(q.trim(), tokenCatBean, u, folder);

				SortMap sm = new SortMap();

				for (Map.Entry<String, String> term : minSpanMap.entrySet()) {
					double docLen = getDocLength(term.getKey(), docCatBean);

					if (rankTerm.get(term.getKey()) != null) {
						rankTerm.put(
								term.getKey(),
								(rankTerm.get(term.getKey()) + proximityPerDoc(
										Integer.parseInt(term.getValue().split(
												" ")[0]),
										Integer.parseInt(term.getValue().split(
												" ")[1]), tokenCatBean.size(),
										docLen)));
					} else {
						rankTerm.put(
								term.getKey(),
								proximityPerDoc(
										Integer.parseInt(term.getValue().split(
												" ")[0]),
										Integer.parseInt(term.getValue().split(
												" ")[1]), tokenCatBean.size(),
										docLen));
					}
				}

				/* Method to Sort Hashmap based on the value */

				LinkedHashMap<String, Double> sortedRanks = (LinkedHashMap<String, Double>) sm
						.getSortedRankMap(rankTerm);

				int j = 1;
				List<String> queryResults = new ArrayList<String>();

				for (Entry<String, Double> term : sortedRanks.entrySet()) {

					if (j <= 1000) {

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
			w.writeToFile(okapi, "proximity-1.txt", type);

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

	public static Map<String, String> getMinSpan(String query,
			Map<String, TokenCatalogBean> tokenCatBean, Utility u, String folder)
			throws Exception {

		Map<String, String> results = new HashMap<String, String>();

		results = calculateMinSpan(query, tokenCatBean, u, folder);

		return results;

	}

	/* Function for calculating proximityPerDoc for individual documents */
	public static double proximityPerDoc(Integer minSpan, double queryLength,
			int vocabSize, double docLen) {

		double lambda = 0.8;
		double c = 1500;
		Double num = (minSpan - queryLength);

		return (double) ((double) ((c - minSpan) * queryLength) / (double) (docLen + vocabSize));

	}

	public static Map<String, String> calculateMinSpan(String query,
			Map<String, TokenCatalogBean> tokenCatBean, Utility u, String folder)
			throws Exception {

		Map<String, String> results = new HashMap<>();

		long startOffset = 0;
		long endOffset = 0;
		query = query.toLowerCase().trim();

		String[] queryWords = query.split("[\\s|-]");

		int q = 0;

		Map<String, List<String[]>> qPositions = new HashMap<String, List<String[]>>();

		for (String qw : queryWords) {

			porterStemmer es = new porterStemmer();
			es.setCurrent(qw.trim().toLowerCase());
			String w = null;

			if (es.stem()) {
				w = es.getCurrent();

			} else {
				w = qw.trim().toLowerCase();
			}

			if (tokenCatBean.containsKey(w)) {
				q++;
				System.out.println("qw:: " + w);
				startOffset = tokenCatBean.get(w).getStartOffset();
				endOffset = tokenCatBean.get(w).getEndOffset();

				RandomAccessFile raf = new RandomAccessFile(
						"C:\\Users\\Nitin\\Assign2\\" + folder
								+ "\\TermsHash84.txt", "r");

				raf.seek(startOffset);

				byte[] termLine = new byte[(int) (endOffset - startOffset)];
				raf.read(termLine);
				String term = new String(termLine);

				String[] termOutput = term.split(" ");

				for (int i = 1; i < termOutput.length - 1; i++) {

					String s = termOutput[i];

					String[] docDetail = s.split(":");

					String docId = docDetail[0];
					int endIndex = docDetail[1].indexOf("-");
					String posns = docDetail[1].substring(endIndex + 1,
							docDetail[1].length());

					if (qPositions.containsKey(docId)) {
						List<String[]> a = qPositions.get(docId);
						a.add(posns.split(","));
						qPositions.put(docId, a);
					} else {
						String[] temp = posns.split(",");
						List<String[]> words = new ArrayList<String[]>();
						words.add(temp);
						qPositions.put(docId, words);

					}

				}

				// Close the RandomAccessFile
				raf.close();
			}

		}

		List<List<String[]>> finalList = new ArrayList<List<String[]>>();

		for (Map.Entry<String, List<String[]>> qDoc : qPositions.entrySet()) {

			int span = getMinSpan(qDoc.getValue());
			results.put(u.getDocKey(Integer.parseInt(qDoc.getKey())), span
					+ " " + qDoc.getValue().size());

		}

		// System.out.println("Docs List Size::: " + qPositions.size());

		// System.out.println("Results Size:: " + results.size());

		return results;
	}

	public static List<String> getStopWords() {
		// TODO Auto-generated method stub

		File stop_words = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt");
		String stop;

		List<String> stop_words_final = new ArrayList<String>();

		try {
			if (stop_words.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					stop_words_final.add(stop.trim().toLowerCase());
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

	public static List<List<String>> getFinalQueryList(List<String> queries,
			List<String> stop_words_final) {
		// TODO Auto-generated method stub

		List<List<String>> queries_Final = new ArrayList<List<String>>();

		for (String query : queries) {

			String[] query_minus_stop = query.split("\\s|-");
			List<String> final_query = new ArrayList<String>();

			for (int i = 0; i < query_minus_stop.length; i++) {

				if (!(stop_words_final.contains(query_minus_stop[i]))
						&& !(query_minus_stop[i].equals(""))) {

					final_query.add(query_minus_stop[i].replaceAll("[,]", "")
							.trim().toLowerCase());
				}
			}
			queries_Final.add(final_query);
		}

		return queries_Final;
	}

	public static int getMinSpan(List<String[]> a) throws Exception {

		List<String[]> wordsList = a; // a list of positions for every query
										// word in a document.
		int span = 0; // set initial span value to 0
		int[] word = new int[wordsList.size()]; // array to hold the length of
												// each positions array
		int[] pos = new int[wordsList.size()]; // array to hold the positions
		int iter = 0; // varaible to count the current iteration
		boolean flag = true; // a flag to indicate if we have reached the end of
								// the arrays.

		for (int i = 0; i < wordsList.size(); i++) {
			word[i] = wordsList.get(i).length; // populate the word array with
												// length of positions array
		}

		/* Iterate till the flag is true. */
		while (flag) {
			
			int count = 0;
			int[] tempPos = new int[wordsList.size()]; // temp array to hold
													// position values

			// populate the temp array
			for (int i = 0; i < wordsList.size(); i++) {
				if (pos[i] == -1) {
					tempPos[i] = Integer.parseInt(wordsList.get(i)[word[i] - 1]);
				} else {
					tempPos[i] = Integer.parseInt(wordsList.get(i)[pos[i]]);
				}
			}

			// get the temporary Span Value.
			int tempSpan = findSpanQueryWords(tempPos);

			// System.out.println("span " + tempSpan);

			// for the first iteration span = tempSpan
			if (iter == 0) {
				span = tempSpan;
				iter++;
			}
			else if (tempSpan < span) {
				span = tempSpan; // after the first iteration , span is the min tempSpan Value
			}

			// get the index of the smallest number in the span
			int smallest = getIndexOfSmallest(tempPos, pos);

			// check if the smallest index is the end of the array for
			// the term
			if (pos[smallest] < word[smallest] - 1) {
				pos[smallest] = pos[smallest] + 1;
			} else {
				pos[smallest] = -1;
			}



			/* Loop to check if we have reached the end of all arrays */
			for (int k = 0; k < wordsList.size(); k++) {
				if (pos[k] == -1) {
					count = count + 1;
				}
			}

			// if we've reached the end of the arrays, change flag
			if (count == wordsList.size()) {
				flag = false;
			}
		}
		return span;
	}

	public static int findSpanQueryWords(int[] tempPos) {

		int smallest = getMin(tempPos); // get minimum element
		int largest = getMax(tempPos); // get Max element

		// get Span
		int span = largest - smallest;
		return span;

	}

	private static int getMin(int[] tempPos) {
		// TODO Auto-generated method stub
		int min = tempPos[0];

		// gets the minimum of all array element
		for (int i = 1; i < tempPos.length; i++) {
			if (tempPos[i] < min)
				min = tempPos[i];
		}

		return min;

	}

	// returns the max number
	public static int getMax(int[] tempPos) {

		// gets the maximum of all array element
		int maxNumber = tempPos[0];

		for (int i = 1; i < tempPos.length; i++) {
			if (tempPos[i] > maxNumber)
				maxNumber = tempPos[i];

		}

		return maxNumber;

	}

	public static int getIndexOfSmallest(int[] tempPos, int[] pos) {
		
		int indexSmallest = 0;		//assign 0 as the smallest index
		int smallest = 1999999999; // assign a very large number to check
									// against
		
		for (int i = 0; i < tempPos.length; i++) {
			if (pos[i] != -1) {
				if (tempPos[i] <= smallest) {
					smallest = tempPos[i];
					indexSmallest = i;
				}
			}
		}
		return indexSmallest;

	}

}
