package com.ir.hw3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/*This is an upgrade over crawler 9,10 with change in sleep, 
 * filter removed and other processing to inlinks and outlinks map*/
public class Crawler12 {
	static URLCanonicalizer uc = new URLCanonicalizer();
	static Map<String, List<String>> domainList = new TreeMap<String, List<String>>();

	public static void main(String[] args) throws IOException {
		/* This will be the frontier */
		try {
			Map<String, List<String>> frontier = new LinkedHashMap<String, List<String>>();

			frontier.put(
					uc.getCanonicalURL("http://catalogue.bnf.fr/ark:/12148/cb15591663c"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("https://en.wikipedia.org/wiki/Barack_Obama"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("https://en.wikipedia.org/wiki/Barack_Obama_presidential_campaign,_2008"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("https://en.wikipedia.org/wiki/United_States_presidential_election,_2008"),
					new ArrayList<String>());
			// seeds added by me
			frontier.put(
					uc.getCanonicalURL("http://www.biography.com/people/barack-obama-12782369"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("https://www.whitehouse.gov/1600/presidents/barackobama"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("https://www.whitehouse.gov/administration/president-obama"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("http://millercenter.org/president/obama"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("http://millercenter.org/president/obama/essays/biography/2"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("http://uspolitics.about.com/od/senators/a/barack_obama.htm"),
					new ArrayList<String>());
			frontier.put(
					uc.getCanonicalURL("http://keywiki.org/Barack_Obama_-_Political_Career"),
					new ArrayList<String>());

			startCrawl(frontier);
			System.out.println("End time:: " + new Date());
		} catch (Exception e) {
			System.out.println("Error in main...");
			e.printStackTrace();
		}

	}

	private static void startCrawl(Map<String, List<String>> frontier)
			throws IOException {
		/* Will hold the Visited URL's in the Map with their inLinks */
		Map<String, List<String>> visitedLinks = new LinkedHashMap<String, List<String>>();
		/* This will hold the Outlinks List for a given Iteration */
		Map<String, List<String>> outLinksList = new LinkedHashMap<String, List<String>>();
		/* This list holds the temp outlinks for a given URL */
		List<String> out = new ArrayList<String>();
		/* This is a list of links which don't cater to the subject */
		List<String> discardedLinks = new ArrayList<String>();

		List<String> linksToAvoid = new ArrayList<String>();
		linksToAvoid = addLinksToAvoid(linksToAvoid);

		String currentLink = "";
		/* Check if the first element in the frontier queue is empty or not */
		int count = 0;

		while (visitedLinks.size() <= 20000) {
			// System.out.println("Count Value::: " + count);
			try {

				if (count < 10) {
					currentLink = getURLtoCrawlNonSorted(frontier);
				} else {
					currentLink = getURLtoCrawl(frontier);
				}

				currentLink = getURLtoCrawl(frontier);

				if (currentLink.contains(".pdf")
						|| currentLink.contains(".jpg")
						|| currentLink.contains(".svg")
						|| currentLink.contains(".ogg")) {
					count++;
					frontier.remove(currentLink);
					discardedLinks.add(currentLink);
					continue;

				}

				System.out.println("First Link::: " + currentLink);

				if (currentLink != null
						&& !(linksToAvoid.contains(currentLink))) {

					if (!(visitedLinks.containsKey(currentLink))
							|| !(discardedLinks.contains(currentLink))) {

						/*
						 * System.out.println("ContentIsHTML::: " +
						 * contentIsHtml(currentLink));
						 */

						ReadRobots rr = new ReadRobots();
						String domain = getDomain(currentLink);

						// check only for english wikipedia pages
						if (domain.contains("wikipedia.org")) {
							if (!domain.contains("en.wikipedia.org")) {
								count++;
								frontier.remove(currentLink);
								discardedLinks.add(currentLink);
								continue;
							}
							
						}
						if(domain.endsWith(".fr")){
							count++;
							frontier.remove(currentLink);
							discardedLinks.add(currentLink);
							continue;
						}

						List<String> disAllowedLinks = new ArrayList<String>();

						if (domainList.containsKey(domain)) {
							// System.out.println("Domain Already Present");
							disAllowedLinks = domainList.get(domain);

						} else {
							List<String> robotsDisAllowedLinks = rr
									.getRobotsList(domain);
							domainList.put(domain, robotsDisAllowedLinks);
							System.out.println("Domain Added");

						}

						if (!(disAllowedLinks.contains(currentLink))
								&& contentIsHtml(currentLink)) {
							String url = "";
							String title = "";
							String cleantext = "";
							String rawHtml = "";

							url = currentLink;

							long a = System.currentTimeMillis();
							Document response = Jsoup.connect(url)
									.ignoreHttpErrors(true).get();
							
							

							if (title != null || title != "") {
								title = response.select("title").text();
							}

							cleantext = response.body().text();
							rawHtml = response.html();
							List<Element> outLinks = new ArrayList<Element>();
							outLinks = response.select("a[href]");

							long b = System.currentTimeMillis();
							if ((b - a) <= 1000) {
								try {

									Thread.sleep(1000 - (b - a));
								} catch (InterruptedException ex) {
									Thread.currentThread().interrupt();
								}
							}

							System.out.println("Link::: " + url);
							out = getFinalOutLink(outLinks);
							// System.out.println("Out Size:: "+
							// out.size());

							/* Add Outlinks */
							for (String l : out) {
								if (!(visitedLinks.containsKey(l))
										|| !(discardedLinks
												.contains(currentLink))) {

									/*
									 * Check in Frontier as well and update
									 * Inlinks
									 */
									if (frontier.containsKey(l)) {
										List<String> o = frontier.get(l);
										o.add(url);
										outLinksList.put(l, o);
									} else if (outLinksList.containsKey(l)) {
										List<String> o = outLinksList.get(l);
										if (!o.contains(url)) {
											o.add(url);
											outLinksList.put(l, o);
										}

									} else {
										List<String> o = new ArrayList<String>();
										o.add(url);
										outLinksList.put(l, o);
									}
									if (visitedLinks.containsKey(l)) {
										List<String> o = visitedLinks.get(l);
										if (!o.contains(url)) {
											o.add(url);
											visitedLinks.put(l, o);
										}
									}

								}

								/* InLinks Check ends */

							}

							/* Add Outlinks End */

							if (frontier.get(url) != null) {/*
															 * is this required
															 */
								visitedLinks.put(url, frontier.get(url));
							}
							// write to file
							writeToFile(url, out, title, cleantext, rawHtml,
									count);

							/*
							 * } else { discardedLinks.add(url);
							 * 
							 * }
							 */

						}else{
							count++;
							frontier.remove(currentLink);
							discardedLinks.add(currentLink);
							continue;
						}

					}

				}
				count++;
				frontier.remove(currentLink);

				if (frontier.size() == 0) {
					System.out.println("In Frontier Switch........");
					java.awt.Toolkit.getDefaultToolkit().beep();
					SortMap sm = new SortMap();
					frontier = sm.getSortedMap(outLinksList);
					outLinksList = new HashMap<String, List<String>>();
				}

			} catch (Exception e) {
				e.printStackTrace();
				count++;
				frontier.remove(currentLink);
				discardedLinks.add(currentLink);
				continue;

			}

		}

		System.out.println("Visited Links::: " + visitedLinks.size());
		System.out.println("Discarded Links::: " + discardedLinks.size());
		System.out.println("Frontier Links::: " + frontier.size());
		System.out.println("Domains Visited:: " + domainList.size());
		writeInlinksToFile(visitedLinks);

	}

	private static String getDomain(String currentLink) {

		String hostId = "";

		try {
			URL urlObj = new URL(currentLink);
			hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			// System.out.println("host_id:::: " + hostId);
		} catch (Exception e) {
			System.out.println("Error while getting domain..");
			e.printStackTrace();
		}

		return hostId;

	}

	private static List<String> addLinksToAvoid(List<String> linksToAvoid) {
		// TODO Auto-generated method stub

		linksToAvoid.add("http://keywiki.org/Main_Page");
		linksToAvoid.add("http://keywiki.org/GoogleSearch");
		linksToAvoid.add("http://keywiki.org/Special:RecentChanges");
		linksToAvoid.add("http://keywiki.org/GoogleSearch");
		linksToAvoid.add("http://keywiki.org/Special:Random");
		linksToAvoid.add("http://keywiki.org/Help:Contents");
		linksToAvoid.add("http://keywiki.org/Contact");
		linksToAvoid.add("http://keywiki.org/KeyWiki:About");
		linksToAvoid
				.add("http://keywiki.org/Special:WhatLinksHere/KeyWiki:About");
		linksToAvoid
				.add("http://keywiki.org/Special:RecentChangesLinked/KeyWiki:About");
		linksToAvoid.add("http://keywiki.org/Special:SpecialPages");
		linksToAvoid
				.add("http://keywiki.org/index.php?title=KeyWiki:About&printable=yes");
		linksToAvoid
				.add("http://keywiki.org/index.php?title=KeyWiki:About&oldid=204784");
		linksToAvoid
				.add("http://keywiki.org/index.php?title=KeyWiki:About&oldid=204784");
		linksToAvoid
				.add("http://keywiki.org/index.php?title=KeyWiki:About&action=info");
		linksToAvoid.add("http://keywiki.org/KeyWiki_talk:About");
		linksToAvoid
				.add("http://keywiki.org/index.php?title=KeyWiki_talk:About&action=edit");
		linksToAvoid
				.add("http://keywiki.org/Category:Council_for_a_Livable_World");

		linksToAvoid.add("https://en.wikipedia.org/wiki/Special:Random");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:Wikipedia_articles_with_LCCN_identifiers");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:Commons_category_with_local_link_same_as_on_Wikidata");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Main_Page");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Wikipedia:Link_rot");
		linksToAvoid
				.add("https://donate.wikimedia.org/wiki/Special:FundraiserRedirector?uselang=en&utm_campaign=C13_en.wikipedia.org&utm_medium=sidebar&utm_source=donate");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Wikipedia:Contact_us");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Portal:Contents");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Wikipedia:About");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Special:SpecialPages");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Wikipedia:File_Upload_Wizard");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Help:Category");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Help:Contents");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Special:RecentChanges");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Portal:Current_events");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Wikipedia:Text_of_Creative_Commons_Attribution-ShareAlike_3.0_Unported_License");
		linksToAvoid.add("https://creativecommons.org/licenses/by-sa/3.0/");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Portal:Featured_content");
		linksToAvoid.add("https://wikimediafoundation.org/wiki/Terms_of_Use");
		linksToAvoid.add("https://wikimediafoundation.org/wiki/Privacy_policy");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Wikipedia:Community_portal");
		linksToAvoid.add("https://www.wikimediafoundation.org/");
		linksToAvoid.add("https://www.mediawiki.org/");
		linksToAvoid
				.add("https://www.mediawiki.org/wiki/Special:MyLanguage/How_to_contribute");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Wikipedia:General_disclaimer");
		linksToAvoid.add("https://wikimediafoundation.org/");
		linksToAvoid.add("https://wikimediafoundation.org/wiki/Privacy_policy");
		linksToAvoid.add("https://shop.wikimedia.org/");
		linksToAvoid.add("https://wikimediafoundation.org/wiki/Terms_of_Use");
		linksToAvoid.add("https://shop.wikimedia.org/");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Wikipedia:Link_rot");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:All_articles_with_dead_external_links");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Help:Authority_control");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/International_Standard_Book_Number");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:Wikipedia_articles_with_VIAF_identifiers");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Help:IPA_for_English");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Alma_mater");
		linksToAvoid
				.add("https://www.mediawiki.org/wiki/Special:MyLanguage/Extension:TimedMediaHandler/Client_download");
		linksToAvoid.add("https://en.wikipedia.org/wiki/MusicBrainz");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:Articles_containing_video_clips");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Wikipedia:Featured_articles");
		linksToAvoid
				.add("Link::: http://www.cnn.com/2008/POLITICS/03/05/mccain.bush/index.html");
		linksToAvoid
				.add(" https://en.wikipedia.org/wiki/Category:Living_people");
		linksToAvoid
				.add(" https://en.wikipedia.org/wiki/Category:Articles_with_hAudio_microformats");
		linksToAvoid
				.add(" https://en.wikipedia.org/wiki/Category:Commons_category_template_with_no_category_set");
		linksToAvoid.add(" https://en.wikipedia.org/wiki/Wikipedia:Media_help");
		linksToAvoid
				.add(" https://en.wikipedia.org/wiki/Category:Pages_using_citations_with_accessdate_and_no_URL");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/Category:Articles_with_dead_external_links_from_November_2012");
		linksToAvoid
				.add("https://en.wikipedia.org/wiki/File:Tommy_Thompson_1.jpg");

		return linksToAvoid;
	}

	private static String getURLtoCrawlNonSorted(
			Map<String, List<String>> frontier) {

		try {
			if (frontier.size() > 0) {

				return uc.getCanonicalURL(frontier.keySet().toArray()[0]
						.toString());

			}

		} catch (Exception e) {
			System.out.println("Error in getting first URL...");
			e.printStackTrace();
		}

		return null;

	}

	private static void writeInlinksToFile(
			Map<String, List<String>> visitedLinks) {
		// TODO Auto-generated method stub

		try {
			File file = new File(
					"C:/Users/Nitin/Assign3/files/test/inLinksCatalog.txt");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(fw);

			// System.out.println("Visit Size:: "+visitedLinks.size());
			for (Map.Entry<String, List<String>> e : visitedLinks.entrySet()) {
				String url = e.getKey();
				String inLinks = "";
				List<String> inLinksList = e.getValue();
				// System.out.println("inlInks.size:: "+inLinksList.size());

				if (inLinksList.size() > 0) {
					for (String in : inLinksList) {
						inLinks = inLinks + in + "||";
					}
				}

				bw.write(url + "||");

				if (inLinksList.size() > 0) {
					bw.write(inLinks.substring(0, inLinks.length() - 2));
				} else {
					bw.write(inLinks);
				}
				bw.newLine();

			}

			fw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println("Error in writing links to file..");
			e.printStackTrace();
		}

	}

	private static String getURLtoCrawl(Map<String, List<String>> frontier) {
		// TODO Auto-generated method stub
		try {
			if (frontier.size() > 0) {
				return uc.getCanonicalURL(getSortedFrontier(frontier)
						.entrySet().iterator().next().getKey());
			}
		} catch (Exception e) {
			System.out.println("Error in Getting First url to crawl...");
			e.printStackTrace();
		}

		return null;

	}

	private static Map<String, List<String>> getSortedFrontier(
			Map<String, List<String>> frontier) {
		// TODO Auto-generated method stub
		SortMap sm = new SortMap();
		Map<String, List<String>> sf = new LinkedHashMap<String, List<String>>();
		try {
			sf = sm.getSortedMap(frontier);

		} catch (Exception e) {
			System.out.println("Error in getting Frontier sorted...");
			e.printStackTrace();
		}
		return sf;

	}

	private static List<String> getFinalOutLink(List<Element> outLinks) {
		// TODO Auto-generated method stub
		List<String> finalOutLinkList = new ArrayList<String>();
		try {

			for (Element l : outLinks) {

				String currentLink = uc.getCanonicalURL(l.attr("abs:href"));

				if (!(finalOutLinkList.contains(currentLink))) {
					finalOutLinkList.add(currentLink);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in outlinks..");
		}

		return finalOutLinkList;
	}

	private static boolean CheckContent(String rawHtml, String plainText,
			String title) {

		if (plainText.trim().contains("Barack Obama")
				|| plainText.trim().contains("President Obama")
				|| plainText.trim().contains("Obama")
				|| title.trim().contains("Obama")
				|| title.trim().contains("Barack Obama")
				|| title.trim().contains("President Obama")) {
			return true;
		} else {
			return false;
		}

	}

	private static void writeToFile(String url, List<String> out2,
			String title, String cleantext, String rawHtml, int i) {
		// TODO Auto-generated method stub
		try {
			File file = new File("C:/Users/Nitin/Assign3/files/test/obama" + i
					+ ".txt");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			String out = "";

			for (String element : out2) {
				out = out + element.toString().trim() + "||";
			}

			bw.write("<DOC>");
			bw.newLine();
			bw.write("<DOCNO>" + url + "</DOCNO>");
			bw.newLine();
			bw.write("<HEAD>" + title + "</HEAD>");
			bw.newLine();
			bw.write("<TEXT>" + cleantext + "</TEXT>");
			bw.newLine();
			bw.write("<HTMLRESPONSE>" + rawHtml + "</HTMLRESPONSE>");
			bw.newLine();
			if (out2.contains("||")) {
				bw.write("<OUTLINKS>" + out.substring(0, out.length() - 2)
						+ "</OUTLINKS>");
			} else {
				bw.write("<OUTLINKS>" + out2 + "</OUTLINKS>");
			}

			bw.newLine();
			bw.write("</DOC>");

			fw.flush();
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean robotsAllowed(String currentLink) {
		CrawlerCommonTest cw = new CrawlerCommonTest();
		/* Check if the page/domain is crawlable? */
		boolean robot = false;
		try {
			robot = cw.isCrawlable(currentLink, "Mozilla/5.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("robots allowed::: " + robot);

		return robot;
	}

	private static boolean contentIsHtml(String currentLink) throws IOException {
		/*
		 * Make a HEAD Request to get the page type and return true if its
		 * text/html
		 */
		try {
			URL url = new URL(currentLink);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("HEAD");
			// connection.setConnectTimeout(10000);
			connection.connect();

			if (isRedirect(connection.getResponseCode())) {
				String newUrl = connection.getHeaderField("Location");

				return contentIsHtml(newUrl);
			}

			if (connection.getContentType().contains("text/html")) {
				return true;
			} else
				return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("InException check content");
			e.printStackTrace();
		}
		return false;

	}

	protected static boolean isRedirect(int statusCode) {

		try {
			if (statusCode != HttpURLConnection.HTTP_OK) {
				if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
						|| statusCode == HttpURLConnection.HTTP_MOVED_PERM
						|| statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
					return true;
				}
			}

		} catch (Exception e) {
			System.out.println("Error in Redirection..");
			e.printStackTrace();
		}
		return false;
	}

}
