

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetFinalQueries {
	
	public List<String> readQueryFile(String query_file_path)
			throws IOException {
		// TODO Auto-generated method stub

		String str;
		//StringBuffer query = new StringBuffer();
		List<String> query_list = new ArrayList<String>();

		try {
			File query_file = new File(query_file_path);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(query_file)));

			while ((str = br.readLine()) != null && (str != " ")) {
				// query.append(str.trim());
				int startIndex = 0;
				String q = str.trim();
				 //q = str.replaceAll("[,!?\\()\"]", "");

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
	
	
	public List<List<String>> getFinalQueryList(List<String> queries,
			List<String> stop_words_final) {
		// TODO Auto-generated method stub

		List<List<String>> queries_Final = new ArrayList<List<String>>();

		for (String query : queries) {

			/*String[] query_minus_stop = query.split("\\s|-");*/
			String[] query_minus_stop = query.split("\\s|-");
			/* calculate the query by removing the stop words */
			List<String> final_query = new ArrayList<String>();

			for (int i = 0; i < query_minus_stop.length; i++) {

				if (!(stop_words_final.contains(query_minus_stop[i]))
						&& !(query_minus_stop[i].equals(""))
				/* && !(final_query.contains(query_minus_stop[i])) */
				) {

					/*
					 * final_query.add(query_minus_stop[i].replaceAll("[.]",
					 * ""));
					 */
					final_query.add(query_minus_stop[i].replaceAll("[,]", "").trim().toLowerCase());
				}
			}
			queries_Final.add(final_query);
		}

		return queries_Final;
	}
	
	public List<String> getStopWords() {
		// TODO Auto-generated method stub

		File stop_words = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/stoplist.txt");
		String stop;
		//StringBuffer stop_words_list = new StringBuffer();
		List<String> stop_words_final = new ArrayList<String>();

		/*for (int i = 0; i < stop_words_custom.length; i++) {
			stop_words_final.add(stop_words_custom[i]);
		}*/
		
		try {
			if (stop_words.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					/*stop_words_list.append(stop).append(" ");*/
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
	

}
