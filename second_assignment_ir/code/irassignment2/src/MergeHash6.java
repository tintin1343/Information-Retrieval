import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.porterStemmer;

/*This class creates the final Index,Doc Catalog and Term Catalog.
 * Doc Catalog also has docLength in it. Basically a Version2 of MergeHash2.
 * Also Calculated AverageDoc Length and Vocab Size. Also processed the Text a
 * bit more than the previous version.
 * This builds Index using Stemming and By Removing StopWords*/

public class MergeHash6 {

	static TokenMapper tm = new TokenMapper();
	static DocumentMapper dm = new DocumentMapper();

	static int docId = 1;
	static int termId = 1;
	/* static Map<String, Integer> docMap = new HashMap<String, Integer>(); */
	static Map<String, DocBean> docMap = new HashMap<String, DocBean>();
	static Map<String, TokenCatalogBean> tcMap = new HashMap<String, TokenCatalogBean>();

	static Map<Integer, Map<Integer, List<Integer>>> termsHash = new HashMap<Integer, Map<Integer, List<Integer>>>();
	static int count = 0;
	static GetFinalQueries qu = new GetFinalQueries();
	/*static List<String> stop_words_final = qu.getStopWords();*/
	static porterStemmer es =new porterStemmer();
	static String w = null;

	/* Patterns to extract DOC,DOCNO,TEXT and TERM/TOKENS from file */
	final static Pattern DOC_PATTERN = Pattern.compile("<DOC>(.+?)</DOC>");
	final static Pattern DOC_NO_PATTERN = Pattern
			.compile("<DOCNO>(.+?)</DOCNO>");
	final static Pattern TEXT_PATTERN = Pattern.compile("<TEXT>(.+?)</TEXT>");
	final static Pattern TOKEN_PATTERN = Pattern.compile("\\w+(\\.?\\w+)*");

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		/* Read the folder to get the files */
		File folder_name = new File(
				"C:/Users/Nitin/NEU/Summer Sem/IR/Data/Assign 1/AP89_DATA/AP_DATA/ap89_collection/");

		System.out.println("Reading files from folder :::: " + folder_name);

		/* Get the list of files in the folder */
		File[] folderList = folder_name.listFiles();

		/* Start Reading each file */
		for (File file : folderList) {
			// System.out.println("File Name:: " + file.getName());

			if (file.isFile() && file.getName().startsWith("ap89")) {
				readEach(file);
			}

		}

		WriteDocCatalogFile(new SortMap().getSortedDocCat(docMap), "DocCatalog");

		getStats(new SortMap().getSortedDocCat(docMap), tcMap);
	}

	private static void getStats(Map<String, DocBean> docMap2,
			Map<String, TokenCatalogBean> tcMap2) {
		// TODO Auto-generated method stub

		double averageDocLength = calculateAverageDocLength(docMap2);
		long vocabSize = tcMap.size();

		System.out.println("Average Doc Length::: " + averageDocLength);
		System.out.println("Vocab Size::: " + vocabSize);

	}

	private static double calculateAverageDocLength(Map<String, DocBean> docMap2) {
		// TODO Auto-generated method stub
		double averageDocSize = 0.0;
		long docSize = 0;
		for (Map.Entry<String, DocBean> d : docMap2.entrySet()) {
			docSize += d.getValue().getDocLength();
		}

		System.out.println("DocMap Size::: " + docMap2.size());

		averageDocSize = (double) (docSize / docMap2.size());
		return averageDocSize;
	}

	private static void readEach(File file) throws IOException {
		// TODO Auto-generated method stub

		ArticleBean a = new ArticleBean();

		/* Read content from the file and convert it into a string */
		StringBuffer content = null;
		content = new GetStopWords().getContent(file);

		/* Match/ get text between each <DOC> Tags */
		Matcher docMatch = DOC_PATTERN.matcher(content.toString().trim());

		/* Loop Which Iterates through the text till it gets <DOC> tags */
		while (docMatch.find()) {
			DocBean docBean = new DocBean();
			/* Iterate till you reach the limit for 1000 Docs at a time */

			int currentDoc = docId;
			/* Match/ get text between each <DOCNO> Tags */
			Matcher docNumMatch = DOC_NO_PATTERN.matcher(docMatch.group(1));

			/* Match/ get text between each <TEXT> Tags */
			Matcher docText = TEXT_PATTERN.matcher(docMatch.group(1).trim());
			int i = 0;
			/* Iterate till you cover multiple <TEXT> Tags within a <DOC> Tag */
			while (docText.find()) {
				if (i == 0) {
					a.setText(docText.group(1).trim());
				} else {
					a.setText(a.getText().concat(" ")
							.concat(docText.group(1).trim()));
				}
			}

			/* Match/ get terms or tokens in the TEXT */
			/*
			 * Matcher token = TOKEN_PATTERN.matcher(a.getText().replaceAll(
			 * "[\"\"'``]", ""));
			 */
			Matcher token = TOKEN_PATTERN.matcher(a.getText().replace("[,\"()]", "").trim());

			int position = 0;

			/* Iterate till you find terms in the TEXT */
			while (token.find()) {
				List<Integer> termPositions = new ArrayList<Integer>();
				Map<Integer, List<Integer>> docsHash = new HashMap<Integer, List<Integer>>();
				
				es.setCurrent(token.group(0).trim().toLowerCase());
				String w = null;
				
				if(es.stem()){
					w=es.getCurrent().toLowerCase();

				}else{
					w=token.group(0).trim().toLowerCase();
				}

				/* Begin : Code to not insert it again in the terms Catalog */
				if (!(tm.containsKey(w))) {
					/*
					 * termsMap.put(
					 * token.group(0).toString().trim().toLowerCase(), termId);
					 */
					tm.addValues(w, termId);
					termId++;
				}
				/* End : Code to not insert it again in the terms Catalog */

				if (termsHash.containsKey(tm.getId(w))) {

					/*
					 * If it exists, take the value(Map) of the term and update
					 * it
					 */
					Map<Integer, List<Integer>> tempDoc = termsHash.get(tm
							.getId(w));
					List<Integer> tempPos = tempDoc.get(currentDoc);

					if (tempPos != null) {
						tempPos.add(position);
						tempDoc.put(currentDoc, tempPos);
						termsHash.put(
								tm.getId(w), tempDoc);
					} else {
						List<Integer> t = new ArrayList<Integer>();
						t.add(position);
						tempDoc.put(currentDoc, t);
						termsHash.put(
								tm.getId(w),
								tempDoc);
					}

				} else {
					termPositions.add(position);
					docsHash.put(currentDoc, termPositions);
					termsHash.put(
							tm.getId(w),
							docsHash);
				}

				position++;

			}

			if (docNumMatch.find()) {

				if (!(docMap.containsKey(docNumMatch.group(1).trim()))) {
					a.setDocNo(docNumMatch.group(1).trim());
					docBean.setDocId(currentDoc);
					/*
					 * Set the docId and the DocNo in the docMap which is a
					 * catalog file
					 */
					docBean.setDocLength(position - 1);
					docMap.put(docNumMatch.group(1).trim(), docBean);
					/*
					 * Increment the value of the docId once an entry is made
					 * into the Hashmap
					 */
					docId++;
				}
			}

			if (docId == 1000) {
				WriteCatalogFileforTermsHash(
						new SortMap().getSortedHashMap(termsHash),
						new SortMap().getSortedMap(tm.getTermsMap()));
				termsHash = new HashMap<Integer, Map<Integer, List<Integer>>>();
				count++;
			} else if (docId > 1000 && docId % 1000 == 0 || docId == 84679) {

				checkandMerge(termsHash, tcMap, count);
				termsHash = new HashMap<Integer, Map<Integer, List<Integer>>>();
				count++;

			}

		}

	}

	private static void checkandMerge(
			Map<Integer, Map<Integer, List<Integer>>> termsHash2,
			Map<String, TokenCatalogBean> tcM, int count2) {
		// TODO Auto-generated method stub
		try {
			RandomAccessFile mergedFile = new RandomAccessFile(
					"C:\\Users\\Nitin\\Assign2\\Stemmed\\TermsHash" + count2 + ".txt",
					"rw");
			RandomAccessFile oldMergedFile = new RandomAccessFile(
					"C:\\Users\\Nitin\\Assign2\\Stemmed\\TermsHash" + (count2 - 1)
							+ ".txt", "r");
			// System.out.println(tcM.size()+ " new catMap Size:::");
			// System.out.println("Contains::: "+tcM.containsKey(tm.getId("the")));
			Map<String, TokenCatalogBean> tcMapNew = new HashMap<String, TokenCatalogBean>();
			// System.out.println("tcM SIze1:: " + tcM.size());
			for (Map.Entry<Integer, Map<Integer, List<Integer>>> th : termsHash2
					.entrySet()) {

				int tId = th.getKey();
				String currentTerm = tm.getId(tId);
				if (tcM.containsKey(currentTerm)) {
					// System.out.println("Term Repeats......................"+
					// tm.getId(tId));
					long start = tcM.get(tm.getId(tId)).getStartOffset();
					long end = tcM.get(tm.getId(tId)).getEndOffset();
					oldMergedFile.seek(start);

					byte[] line = new byte[(int) (end - start)];
					oldMergedFile.read(line);

					String termLine = new String(line);

					/* if (!(line[0] == tId)) { */
					// System.out.println("termLine Containned term:: "+tm.getId(tId)
					// +" "+termLine.substring(0, termLine.length()-2));
					writeToMergedFile(
							termLine.substring(0, termLine.length() - 2),
							th.getValue(), mergedFile, tcMapNew, tId);
					tcM.remove(currentTerm);
					/* } */
				} else {
					// System.out.println("termLine new for term:: "+tm.getId(tId));
					writeToMergedFile(null, th.getValue(), mergedFile,
							tcMapNew, tId);
				}

			}

			for (Map.Entry<String, TokenCatalogBean> tc : tcM.entrySet()) {

				long start = tc.getValue().getStartOffset();
				long end = tc.getValue().getEndOffset();

				oldMergedFile.seek(start);

				byte[] line = new byte[(int) (end - start)];
				oldMergedFile.read(line);

				String termLine = new String(line);

				// System.out.println("termLine old fetch:: "+termLine.substring(0,
				// termLine.length()-2));
				writeToMergedFile(termLine.substring(0, termLine.length() - 2),
						null, mergedFile, tcMapNew, tm.getId(tc.getKey()));
				// tcM.remove(tc.getKey());

			}
			// System.out.println("After all removal2::: " + tcM.size());
			if(count==84)
			getOffsetAndWriteCatalogFile(
					new SortMap().getSortedTermCat(tcMapNew), count);

			tcMap = tcMapNew;
			
			oldMergedFile.close();
			File oldFile = new File("C:\\Users\\Nitin\\Assign2\\Stemmed\\TermsHash" + (count2-1) + ".txt");
			oldFile.delete();
			
			
			mergedFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeToMergedFile(String termLine,
			Map<Integer, List<Integer>> value, RandomAccessFile mergedFile,
			Map<String, TokenCatalogBean> tcMapNew, int termId2)
			throws IOException {
		// TODO Auto-generated method stub
		long startOffset = mergedFile.getFilePointer();
		long endOffset = startOffset;

		TokenCatalogBean tcb = new TokenCatalogBean();
		// mergedFile.write((termId2 + " ").getBytes());

		tcb.setTermId(termId2);
		tcb.setStartOffset(startOffset);

		if (value == null) {
			mergedFile.write(termLine.getBytes());
			mergedFile.write("\r\n".getBytes());
			endOffset = mergedFile.getFilePointer();
			tcb.setEndOffset(endOffset);
			/* System.out.println("when new map has no value"); */
		} else if (value != null) {
			int dId = 0;
			String s = "";
			List<Integer> posList = new ArrayList<Integer>();

			for (Map.Entry<Integer, List<Integer>> d : value.entrySet()) {
				dId = d.getKey();
				posList = d.getValue();
				StringBuffer pos = new StringBuffer();
				for (Integer p : posList) {
					pos.append(p).append(",");
				}
				s = s
						+ dId
						+ ":"
						+ posList.size()
						+ "-"
						+ pos.toString().substring(0,
								pos.toString().length() - 1) + " ";

			}
			if (termLine == null) {
				/* System.out.println("when new map has value and new doesnt"); */
				mergedFile.write((termId2 + " " + s).getBytes());
				// mergedFile.write(s.getBytes());
				mergedFile.write("\r\n".getBytes());
				endOffset = mergedFile.getFilePointer();
				tcb.setEndOffset(endOffset);
				// tcMapNew.put(tm.getId(termId2), tcb);

			} else {
				/*
				 * System.out.println("when new map has value and new also does")
				 * ;
				 */
				mergedFile.write((termLine + s).getBytes());
				mergedFile.write("\r\n".getBytes());
				endOffset = mergedFile.getFilePointer();
				tcb.setEndOffset(endOffset);
			}

		}

		tcMapNew.put(tm.getId(termId2), tcb);

	}

	private static void WriteCatalogFileforTermsHash(
			Map<Integer, Map<Integer, List<Integer>>> termsHash,
			Map<String, Integer> termsMap) throws IOException {
		// TODO Auto-generated method stub

		RandomAccessFile file = new RandomAccessFile(
				"C:\\Users\\Nitin\\Assign2\\Stemmed\\TermsHash"
						+ /* ((docId / 1000) - 1) */count + ".txt", "rw");

		long startOffset = 0;
		long endOffset = 0;

		for (Map.Entry<Integer, Map<Integer, List<Integer>>> out : termsHash
				.entrySet()) {

			TokenCatalogBean tcb = new TokenCatalogBean();
			int termId = out.getKey();
			int docId = 0;

			startOffset = file.getFilePointer();
			List<Integer> posList = new ArrayList<Integer>();
			Map<Integer, List<Integer>> docHash = out.getValue();

			file.write((termId + " ").getBytes());
			tcb.setTermId(out.getKey());
			tcb.setStartOffset(startOffset);

			for (Map.Entry<Integer, List<Integer>> d : docHash.entrySet()) {
				docId = d.getKey();
				posList = d.getValue();
				StringBuffer pos = new StringBuffer();

				for (Integer p : posList) {
					pos.append(p).append(",");
				}

				file.write((docId
						+ ":"
						+ posList.size()
						+ "-"
						+ pos.toString().substring(0,
								pos.toString().length() - 1) + " ")
						.getBytes());

			}
			// bw.write("/");
			file.write("\r\n".getBytes());
			endOffset = file.getFilePointer();
			tcb.setEndOffset(endOffset);
			tcMap.put(tm.getId(out.getKey()), tcb);

			// file.newLine();
		}

		file.close();
		// System.out.println("TermCatSize" + tcMap.size());
		/*getOffsetAndWriteCatalogFile(new SortMap().getSortedTermCat(tcMap),
				count);
*/
		// System.out.println("TermsHash Size" + termsHash.size());
		System.out.println("Closed TermsHash Catalog File .." + "@ "
				+ new Date());
		// return tcMap;

	}

	private static void getOffsetAndWriteCatalogFile(
			Map<String, TokenCatalogBean> tcMap, int count)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		try {
			RandomAccessFile termsFile = new RandomAccessFile(
					"C:\\Users\\Nitin\\Assign2\\Stemmed\\" + "TermsCatalog" + count
							+ ".txt", "rw");

			for (Map.Entry<String, TokenCatalogBean> tcb : tcMap.entrySet()) {

				termsFile.write((tcb.getKey() + " "
						+ tcb.getValue().getTermId() + " "
						+ tcb.getValue().getStartOffset() + " " + tcb
						.getValue().getEndOffset()).getBytes());
				termsFile.write(System.lineSeparator().getBytes());
			}
			termsFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void WriteCatalogFile(Map<String, Integer> termsMap,
			String filename) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Started " + filename + " Catalog File Creation..."
				+ "@ " + new Date());
		System.out.println(filename + ".Size::: " + termsMap.size());

		File file = new File("C:/Users/Nitin/Assign2/Stemmed/" + filename + ".txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		// System.out.println("Started File write..." + "@ " + new Date());

		for (Map.Entry<String, Integer> out : termsMap.entrySet()) {
			bw.write(out.getKey() + " " + out.getValue());
			bw.newLine();
		}

		bw.flush();
		bw.close();

		System.out.println("Closed " + filename + " Catalog File .." + "@ "
				+ new Date());

	}

	private static void WriteDocCatalogFile(Map<String, DocBean> DocMap,
			String filename) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Started " + filename + " Catalog File Creation..."
				+ "@ " + new Date());
		System.out.println(filename + ".Size::: " + DocMap.size());

		File file = new File("C:/Users/Nitin/Assign2/Stemmed/" + filename + ".txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		// System.out.println("Started File write..." + "@ " + new Date());

		for (Map.Entry<String, DocBean> out : DocMap.entrySet()) {
			bw.write(out.getKey() + " " + out.getValue().getDocId() + " "
					+ out.getValue().getDocLength());
			bw.newLine();
		}

		bw.flush();
		bw.close();

		System.out.println("Closed " + filename + " Catalog File .." + "@ "
				+ new Date());

	}

}

