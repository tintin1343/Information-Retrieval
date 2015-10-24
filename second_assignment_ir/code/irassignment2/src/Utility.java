import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utility {
	private double averageDocLength;
	private long vocabSize;
	private Map<String, DocBean> docCatBean = new HashMap<String, DocBean>();
	private Map<Integer, String> docBean = new HashMap<Integer, String>();
	private Map<String, TokenCatalogBean> tokenCatBean = new HashMap<String, TokenCatalogBean>();
	private String folder="";

	public Utility(String type) throws IOException {
		// TODO Auto-generated constructor stub
		Map<String, DocBean> docCatBean = new HashMap<String, DocBean>();
		Map<String, TokenCatalogBean> tokenCatBean = new HashMap<String, TokenCatalogBean>();
		Map<Integer, String> docBean = new HashMap<Integer, String>();

		
		String folder= "";

		if (type.equals("rawWords")) {
			folder="FilesRaw";
		}
		if (type.equals("woStopWords")) {
			folder="StopWordsRemoved";
		}
		if (type.equals("stemmedWords")) {
			folder="Stemmed";
		}
		if (type.equals("stemAndStop")) {
			folder="stopAndStemmed";
		}
		
		
		docCatBean = generateDocCatBean(docCatBean,folder);
		tokenCatBean = generateTokenCatBean(tokenCatBean,folder);

		docBean = generateDocBean(docCatBean,folder);

		double averageDocLength = getStatsforDoc(docCatBean);
		long vocabSize = tokenCatBean.size();

		this.averageDocLength = averageDocLength;
		this.vocabSize = vocabSize;
		this.docCatBean = docCatBean;
		this.tokenCatBean = tokenCatBean;
		this.docBean = docBean;
		this.folder=folder;

		System.out.println("Vocab Size::: " + vocabSize);

		System.out.println("AverageDocLength:" + averageDocLength);
		System.out.println("Finished Creating Maps @ " + new Date());

	}

	private Map<Integer, String> generateDocBean(
			Map<String, DocBean> docCatBean2, String folder) {
		// TODO Auto-generated method stub
		try {
			Map<Integer, String> docBean = new HashMap<Integer, String>();
			/*File docFile = new File("C:\\Users\\Nitin\\Assign2\\FilesRaw\\DocCatalog.txt");*/
			File docFile = new File("C:\\Users\\Nitin\\Assign2\\"+folder+"\\DocCatalog.txt");
			
			FileReader rd = new FileReader(docFile);
			BufferedReader br = new BufferedReader(rd);
			String line = "";

			while ((line = br.readLine()) != null) {
				String docLine = line;
				// System.out.println("DocLine:: "+docLine);
				if (docLine != null) {
					String docsDetails[] = docLine.split(" ");
					docBean.put(Integer.parseInt(docsDetails[1]),
							docsDetails[0]);
				}

			}

			System.out.println("Doc Map Size::: " + docCatBean.size());
			br.close();
			rd.close();
			return docBean;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public double returnAvgLength() {
		return averageDocLength;

	}

	public long returnVocabSize() {
		return vocabSize;
	}

	public Map<String, DocBean> getDocCat() {
		return docCatBean;
	}

	public Map<String, TokenCatalogBean> getTokenCat() {
		return tokenCatBean;
	}

	public String getDocKey(int docId) {

		return docBean.get(docId);

	}

	private Map<String, TokenCatalogBean> generateTokenCatBean(
			Map<String, TokenCatalogBean> tokenCatBean, String folder) throws IOException {
		// TODO Auto-generated method stub
		try {
			/*File tokenCatFile = new File(
					"C:\\Users\\Nitin\\Assign2\\FilesRaw\\TermsCatalog84.txt");*/
			File tokenCatFile = new File(
					"C:\\Users\\Nitin\\Assign2\\"+folder+"\\TermsCatalog84.txt");
			
			FileReader rd = new FileReader(tokenCatFile);
			BufferedReader br = new BufferedReader(rd);
			String line = "";

			while ((line = br.readLine()) != null) {

				TokenCatalogBean tc = new TokenCatalogBean();
				String termLine = line;

				if (termLine != null) {
					String docsDetails[] = termLine.split(" ");
					tc.setTermId(Integer.parseInt(docsDetails[1]));
					tc.setStartOffset(Integer.parseInt(docsDetails[2]));
					tc.setEndOffset(Integer.parseInt(docsDetails[3]));
					tokenCatBean.put(docsDetails[0], tc);
				}

			}

			System.out.println("ToKen Map Size::: " + tokenCatBean.size());
			br.close();
			rd.close();
			return tokenCatBean;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private Map<String, DocBean> generateDocCatBean(
			Map<String, DocBean> docCatBean, String folder) {
		// TODO Auto-generated method stub
		try {
			/*File docFile = new File("C:\\Users\\Nitin\\Assign2\\DocCatalog.txt");*/
			File docFile = new File("C:\\Users\\Nitin\\Assign2\\"+folder+"\\DocCatalog.txt");
			
			FileReader rd = new FileReader(docFile);
			BufferedReader br = new BufferedReader(rd);
			String line = "";

			while ((line = br.readLine()) != null) {
				DocBean doc = new DocBean();
				String docLine = line;
				// System.out.println("DocLine:: "+docLine);
				if (docLine != null) {
					String docsDetails[] = docLine.split(" ");
					doc.setDocId(Integer.parseInt(docsDetails[1]));
					doc.setDocLength(Integer.parseInt(docsDetails[2]));
					docCatBean.put(docsDetails[0], doc);
				}

			}

			//System.out.println("Doc Map Size::: " + docCatBean.size());
			br.close();
			rd.close();
			return docCatBean;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private double getStatsforDoc(Map<String, DocBean> docCatBean) {
		// TODO Auto-generated method stub
		double averageDocLength = calculateAverageDocLength(docCatBean);
		//System.out.println("Average Doc Length::: " + averageDocLength);
		return averageDocLength;
	}

	private double calculateAverageDocLength(Map<String, DocBean> docMap2) {
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

	public Map<Integer, String> getDocBean() {
		// TODO Auto-generated method stub
		return docBean;
	}

	public String returnFolderName() {
		// TODO Auto-generated method stub
		return folder;
	}

/*	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		new Utility();

	}*/

}
