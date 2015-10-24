package com.ir.hw3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestJsoup {
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//final Pattern TITLE_PATTERN = Pattern.compile("<title>(.+?)</title>");
		String url = "http://en.wikipedia.org/wiki/Barack_Obama";
		//System.out.println(url);

		// Making a get request and parsing the page.
		Document response = Jsoup.connect(url).get();

		String title = response.title();
		//Matcher titleMatch = TITLE_PATTERN.matcher(pageTitle);
		
		System.out.println("title:: "+title);
		
		//System.out.println("title filtered:: "+titleMatch.group(0).toString());
		String cleantext = response.body().text();
		String rawHtml = response.html();
		List<Element> outLinks = new ArrayList<Element>();
		outLinks = response.select("a[href]");
	/*	
		System.out.println("Outlinks Size::"+ outLinks.size());

		System.out.println("Title :::::::::::: " + title.toString());
		System.out.println("::::::::::::::::::::::::::::::::::::::::");

		System.out.println("Clean text :::::::::::: " + cleantext.toString());

		System.out.println("Raw HTML :::::::::::: " + rawHtml.toString());*/
		/*try {
			Thread.sleep(1000); // 1000 milliseconds is one second.
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}*/


		File file = new File("C:/Users/Nitin/Assign3/files/test.txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		String out = "";
		
		for(Element element:outLinks){
			out = out+ element.toString().trim() + "|";
		}
		
		bw.write("<DOC>");
		bw.newLine();
		bw.write("<DOCNO>"+url+"</DOCNO>");
		bw.newLine();
		bw.write("<TITLE>"+title+"</TITLE>");
		bw.newLine();
		bw.write("<OCOUNT>"+outLinks.size()+"</OCOUNT>");
		bw.newLine();
		bw.write("<ICOUNT>"+1+"</ICOUNT>");
		bw.newLine();
		bw.write("<TEXT>"+cleantext+"</TEXT>");
		bw.newLine();
		bw.write("<HTMLRESPONSE>"+rawHtml+"</HTMLRESPONSE>");
		bw.newLine();
		bw.write("<OUTLINKS>"+out.substring(0, out.length()-1)+"</OUTLINKS>");
		bw.newLine();
		bw.write("</DOC>");
		
		fw.flush();
		bw.close();

	}

}
