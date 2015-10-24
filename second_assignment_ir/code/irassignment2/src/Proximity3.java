import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.tartarus.snowball.ext.porterStemmer;
/*proximity with lambda formula and considering docs which has all the query words*/
public class Proximity3 {

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

			// System.out.println("Avg Doc Length:::: " + avg_doc_length);

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
				Map<String, Integer> minSpanMap = new HashMap<String, Integer>();
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

				System.out.println("Query:: " + q);
				minSpanMap = getMinSpan(q.trim(), tokenCatBean, u, folder);

				SortMap sm = new SortMap();

				for (Map.Entry<String, Integer> term : minSpanMap.entrySet()) {

					if (rankTerm.get(term.getKey()) != null) {
						rankTerm.put(
								term.getKey(),
								(rankTerm.get(term.getKey()) + proximityPerDoc(
										term.getValue(), query.size())));
					} else {
						rankTerm.put(term.getKey(),
								proximityPerDoc(term.getValue(), query.size()));
					}
				}

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
			w.writeToFile(okapi, "proximity-1.txt", type);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * private static double getDocLength(String key, Map<String, DocBean>
	 * docCatBean) { // TODO Auto-generated method stub double doclength = 0.0;
	 * 
	 * if (docCatBean.containsKey(key)) { doclength =
	 * docCatBean.get(key).getDocLength(); }
	 * 
	 * return doclength; }
	 */

	public static Map<String, Integer> getMinSpan(String query,
			Map<String, TokenCatalogBean> tokenCatBean, Utility u, String folder)
			throws Exception {

		Map<String, Integer> results = new HashMap<String, Integer>();

		/*
		 * if (tokenCatBean.containsKey(query.toLowerCase().trim())) {
		 * System.out.println("in calcluating minSpan..");
		 */
		results = calculateMinSpan(query, tokenCatBean, u, folder);
		/* } */

		return results;

	}

	/* Function for calculating proximityPerDoc for individual documents */
	public static double proximityPerDoc(Integer minSpan, double queryLength) {

		double lambda = 0.8;
		Double num = (minSpan - queryLength);

		return (double) (Math.pow(lambda, (double) (num / queryLength)));

	}

	public static Map<String, Integer> calculateMinSpan(String query,
			Map<String, TokenCatalogBean> tokenCatBean, Utility u, String folder)
			throws Exception {

		Map<String, Integer> results = new HashMap<>();

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
					// System.out.println("Positions:: "+posns);

					if (qPositions.containsKey(docId)) {
						List<String[]> a = qPositions.get(docId);
						a.add(posns.split(","));
						qPositions.put(docId, a);
					} else {
						String[] temp = posns.split(",");
						// WordPositions.add(posns.split(","));
						List<String[]> words = new ArrayList<String[]>();
						words.add(temp);
						qPositions.put(docId, words);

					}

				}

				// Close the RandomAccessFile
				raf.close();
			}
			// System.out.println("QueryCOunt:: "+ q);

		}

		List<List<String[]>> finalList = new ArrayList<List<String[]>>();

		for (Map.Entry<String, List<String[]>> qDoc : qPositions.entrySet()) {

			// System.out.println("Size for the DOc:: "+
			// qDoc.getValue().size());
			if (qDoc.getValue().size() == q) {
				/* finalList.add(qDoc.getValue()); */
				int span = getMinSpan(qDoc.getValue());
				results.put(u.getDocKey(Integer.parseInt(qDoc.getKey())), span);
			}

		}

		System.out.println("Docs List Size::: " + qPositions.size());
		//System.out.println("Final List Size::: " + finalList.size());

		// calculateSpanValues(finalList);
		System.out.println("Results Size:: "+ results.size());

		return results;
	}

	public static List<String> getStopWords() {
		// TODO Auto-generated method stub

		File stop_words = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt");
		String stop;
		// StringBuffer stop_words_list = new StringBuffer();
		List<String> stop_words_final = new ArrayList<String>();

		try {
			if (stop_words.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					/* stop_words_list.append(stop).append(" "); */
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
				System.out.println("Added to further process queries");
				stop_words_final.add("allegations,");
				stop_words_final.add("against,");
				stop_words_final.add("taken");
				stop_words_final.add("forces");
				stop_words_final.add("public");
				stop_words_final.add("jurisdiction");
				stop_words_final.add("worldwide");
				stop_words_final.add("type");
				stop_words_final.add("directly");
				stop_words_final.add("fatality");
				stop_words_final.add("caused");
				stop_words_final.add("least");
				stop_words_final.add("location");
				stop_words_final.add("actual");
				stop_words_final.add("prediction");
				/* stop_words_final.add("prime"); */
				stop_words_final.add("rate,");
				// stop_words_final.add("guerrilla");
				stop_words_final.add("border");
				stop_words_final.add("move");
				stop_words_final.add("land,");
				stop_words_final.add("air,");
				stop_words_final.add("water");
				stop_words_final.add("military");
				stop_words_final.add("area");
				stop_words_final.add("force");
				stop_words_final.add("second");
				stop_words_final.add("group");
				stop_words_final.add("based");
				stop_words_final.add("nra");
				/*
				 * stop_words_final.add("hostage");
				 */
				stop_words_final.add("communist,");
				stop_words_final.add("regulate");
				stop_words_final.add("\"dual");
				stop_words_final.add("use\"");
				stop_words_final.add("reservation,");
				stop_words_final.add("signing");
				stop_words_final.add("taking");
				stop_words_final.add("d'");
				stop_words_final.add("etat");
				stop_words_final.add("d'etat");
				stop_words_final.add("politically");
				stop_words_final.add("motivated");
				stop_words_final.add("result");
				stop_words_final.add("attempted");
				stop_words_final.add("supporters");
				stop_words_final.add("successful,");
				stop_words_final.add("country");
				stop_words_final.add("nra");
				/* stop_words_final.add("National"); */
				/*
				 * stop_words_final.add("Rifle");
				 * stop_words_final.add("Association");
				 */
				stop_words_final.add("assets");
				stop_words_final.add("development");
				stop_words_final.add("anticipate");
				stop_words_final.add("ongoing");
				stop_words_final.add("method");
				// stop_words_final.add("type");
				/* stop_words_final.add("System"); */
				stop_words_final.add("MCI");
				stop_words_final.add("U.S.");
				/* stop_words_final.add("officers"); */
				stop_words_final.add("actions");
				stop_words_final.add("Affair");
				stop_words_final.add("aid");
				stop_words_final.add("agreement,");
				stop_words_final.add("transfer");
				stop_words_final.add("contract");
				stop_words_final.add("investment");
				stop_words_final.add("role");
				stop_words_final.add("\"downstream\"");
				stop_words_final.add("preliminary");
				stop_words_final.add("making");
				stop_words_final.add("tentative");
				stop_words_final.add("reservation");
				stop_words_final.add("current");
				stop_words_final.add("failed");
				stop_words_final.add("financial");
				/* stop_words_final.add("institution"); */
				stop_words_final.add("perpetrated");
				stop_words_final.add("efforts");
				stop_words_final.add("use");
				/* stop_words_final.add("system"); */
				stop_words_final.add("systems");
				stop_words_final.add("specified");
				stop_words_final.add("platform");
				stop_words_final.add("individuals");
				stop_words_final.add("organizations");
				stop_words_final.add("actually");
				/* stop_words_final.add("affair"); */
				/* stop_words_final.add("Iran"); */
				stop_words_final.add("state");
				stop_words_final.add("technology");
				stop_words_final.add("equipment");
				stop_words_final.add("produce");
				stop_words_final.add("solving");
				stop_words_final.add("instances");
				stop_words_final.add("application");
				stop_words_final.add("breakup");
				stop_words_final.add("existing");
				stop_words_final.add("pending");
				stop_words_final.add("state");
				stop_words_final.add("operation");
				stop_words_final.add("non");
				stop_words_final.add("communist");
				stop_words_final.add("industrialized");
				stop_words_final.add("states");
				stop_words_final.add("undesirable");
				stop_words_final.add("nations");
				stop_words_final.add("goods");
				stop_words_final.add("actual");
				stop_words_final.add("studies,");
				stop_words_final.add("fine");
				stop_words_final.add("diameter");
				stop_words_final.add("fibers");
				/* stop_words_final.add("workers"); */
				stop_words_final.add("unsubstantiated");
				stop_words_final.add("concerns");
				stop_words_final.add("installation");
				stop_words_final.add("insulation");
				stop_words_final.add("products");
				stop_words_final.add("standards");
				stop_words_final.add("contrasted");
				stop_words_final.add("determine");
				stop_words_final.add("determining");
				stop_words_final.add("levels");
				/* stop_words_final.add("pay"); */
				stop_words_final.add("solely");
				stop_words_final.add("basis");
				stop_words_final.add("longevity");
				stop_words_final.add("manufacturing");
				stop_words_final.add("acquisition");
				stop_words_final.add("advanced");
				stop_words_final.add("d'etat");
				stop_words_final.add("measures");
				stop_words_final.add("governmental");
				stop_words_final.add("sides");
				stop_words_final.add("controversy");
				stop_words_final.add("incentive");
				stop_words_final.add("seniority");
				stop_words_final.add("translation");
				stop_words_final.add("performance");

				/*
				 * stop_words_final.add(""); stop_words_final.add("");
				 * stop_words_final.add(""); stop_words_final.add("");
				 * stop_words_final.add("");
				 */

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

			/* String[] query_minus_stop = query.split("\\s|-"); */
			String[] query_minus_stop = query.split("\\s|-");
			/* calculate the query by removing the stop words */
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

		List<String[]> wordsList = a;
		int span = 0;
		int[] word = new int[wordsList.size()];
		int[] pos = new int[wordsList.size()];

		for (int i = 0; i < wordsList.size(); i++) {
			word[i] = wordsList.get(i).length;
		}

		boolean flag = true;
		int cycle = 0;
		while (flag) {

			int[] temp = new int[wordsList.size()]; 

			for (int i = 0; i < wordsList.size(); i++) {
				if (pos[i] == -1) {
					temp[i] = Integer.parseInt(wordsList.get(i)[word[i] - 1]);
				} else {
					temp[i] = Integer.parseInt(wordsList.get(i)[pos[i]]);
				}
			}

			int tempSpan = findSpanQueryWords(temp);

			// System.out.println("span " + tempSpan);

			if (cycle == 0) {
				span = tempSpan;
				cycle++;
			}
			if (tempSpan < span) {
				span = tempSpan;
			}

			int smallest = getSmallest(temp, pos);

			if (pos[smallest] < word[smallest] - 1) {
				pos[smallest] = pos[smallest] + 1;
			} else {
				pos[smallest] = -1;
			}

			int count = 0;

			for (int k = 0; k < wordsList.size(); k++) {
				if (pos[k] == -1) {
					count = count + 1;
				}
			}

			if (count == wordsList.size()) {
				flag = false;
			}
		}
		return span;
	}

	public static int findSpanQueryWords(int[] temp) {

		int smallest = temp[0];
		int largest = temp[0];

		for (int i = 1; i < temp.length; i++) {
			if (temp[i] > largest)
				largest = temp[i];
			else if (temp[i] < smallest) {
				smallest = temp[i];
			}
		}
		int span = largest - smallest;
		return span;

	}

	public static int getSmallest(int[] temp, int[] wordPos) {
		int smallest = getMax(temp);
		int indexSmallest = 0;

		for (int i = 0; i < temp.length; i++) {
			if (wordPos[i] != -1) {
				if (temp[i] <= smallest) {
					smallest = temp[i];
					indexSmallest = i;
				}
			}
		}
		return indexSmallest;

	}

	public static int getMax(int[] temp) {

		int maxNumber = temp[0];

		for (int i = 1; i < temp.length; i++) {
			if (temp[i] > maxNumber)
				maxNumber = temp[i];

		}

		return maxNumber;

	}
}
