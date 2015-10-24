import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class WriteFile {

	public void writeToFile(List<List<String>> resultOkapi, String filename,
			String type) {
		// TODO Auto-generated method stub
		try {

			System.out.println("Started File Creation..." + "@ " + new Date());
			String folder = "";

			if (type.equals("rawWords")) {
				folder = "FilesRaw";
			}
			if (type.equals("woStopWords")) {
				folder = "StopWordsRemoved";
			}
			if (type.equals("stemmedWords")) {
				folder = "Stemmed";
			}
			if (type.equals("stemAndStop")) {
				folder = "stopAndStemmed";
			}
			/*
			 * File file = new
			 * File("C:/Users/Nitin/Assign2/FilesRaw/Results/"+filename);
			 */
			File file = new File("C:/Users/Nitin/Assign2/" + folder
					+ "/Results/" + filename);

			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			// System.out.println("Started File write..." + "@ " + new Date());
			for (List<String> queryRes : resultOkapi) {
				for (String out : queryRes) {
					bw.write(out);
					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
			System.out.println("Closed File .." + "@ " + new Date());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
