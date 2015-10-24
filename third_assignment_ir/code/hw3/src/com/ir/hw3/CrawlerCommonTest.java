package com.ir.hw3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import crawlercommons.robots.SimpleRobotRulesParser;

public class CrawlerCommonTest {
	/*public static void main(String args[]) {
		
		boolean isSearchCrawlable = isCrawlable(
				"https://www.google.com/search", "Googlebot");
		boolean isNewsalertsCrawlable = isCrawlable(
				"https://www.google.com/newsalerts", "Googlebot");
		System.out.println("SEARCH CRAWLBALE " + isSearchCrawlable);
		System.out.println("NEWSALERT CRAWLBALE " + isNewsalertsCrawlable);
		// IN MILLI SECONDS
		long crawlDelay = getCrawlDelay("http://www.seobook.com", "Googlebot");
		System.out.println("CRAWL DELAY " + crawlDelay);
		CrawlerCommonTest test = new CrawlerCommonTest();
		System.out.println(test.isCrawlable("http://en.wikipedia.org/wiki/Barack_Obama", "abc"));
		
	}*/

	public long getCrawlDelay(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			//System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			if (rules.getCrawlDelay() > 0){
				return rules.getCrawlDelay();
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1000;
	}

	public boolean isCrawlable(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			//System.out.println("host_id:::: "+hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			return rules.isAllowed(page_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getContents(String page_url) {
		try {
			URL oracle = new URL(page_url);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					oracle.openStream()));
			String content = new String();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content += inputLine + "\n";
			}
			in.close();
			return content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}