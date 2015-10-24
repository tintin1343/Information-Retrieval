package com.ir.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
 * Class : GetStopWords.java
 * Purpose: Is a Helper class to BuildIndex.java. Has Two Methods
 * Method1: Reads each ap-data article and creates a content which can be parsed later.
 * Method 2: Returns a list of Stop Words after reading and processing them from the StopWords.txt file
 * Author: Nitin Shetty
 * Last Updated Date: 06/01/2015.
 * */

public class GetStopWords {

	public StringBuffer getContent(File file) throws IOException {
		StringBuffer content = new StringBuffer();
		String str;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		try {
			
			while ((str = br.readLine()) != null) {
				content.append(" ").append(str);
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in Reading file");
			e.printStackTrace();
		}
		return content;

	}

	
	
	
	/*public List<String> getStopWordsList(File stop_words) throws IOException {
		List<String> stop_words_list = new ArrayList<String>();
		String stop;
		try {
			if (stop_words.isFile()) {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stop_words)));

				while ((stop = br.readLine()) != null) {
					if (stop.length() > 0)
						stop_words_list.add(stop);
				}
				// System.out.println("Stop Words String "+ stop_words_list);

				br.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stop_words_list;

	}*/


}
