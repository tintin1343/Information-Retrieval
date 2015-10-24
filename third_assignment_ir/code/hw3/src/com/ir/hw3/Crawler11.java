package com.ir.hw3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/*This is an upgrade over crawler 9 with change in sleep*/
public class Crawler11 {/*
	static URLCanonicalizer uc = new URLCanonicalizer();
	static Map<String, List<String>> domainList = new TreeMap<String, List<String>>();

	public static void main(String[] args) throws IOException {
		 This will be the frontier 
		try {
			List<String> seedsList = new ArrayList<String>();
			 This will be the frontier 
			List<WebPageBean> frontier = new ArrayList<WebPageBean>();

			// add the Seed URL's to the initial List to be crawled first.
			seedsList.add("http://en.wikipedia.org/wiki/Barack_Obama");

			seedsList
					.add("http://en.wikipedia.org/wiki/Barack_Obama_presidential_campaign,_2008");
			seedsList
					.add("http://en.wikipedia.org/wiki/United_States_presidential_election,_2008");
			Seeds Added by Me
			seedsList
			.add("http://www.biography.com/people/barack-obama-12782369");
			seedsList
			.add("https://www.whitehouse.gov/1600/presidents/barackobama");
			seedsList
			.add("http://self.gutenberg.org/articles/barack_obama_presidential_campaign,_2008");
			
			
			 * Start Crawling each of the SeedURL from the List and process
			 * accordingly
			 
			for (String sl : seedsList) {
				// for each element in the seedsList , Pick up the first element and
				// process it.
				WebPageBean wb= new WebPageBean();
				wb.setDocno(sl);
				wb.setInLinks(new ArrayList<String>());
				frontier.add(wb);
			}

			System.out.println("Queue Size:: " + frontier.size());

			startCrawl(frontier);
			System.out.println("End time:: " + new Date());
		} catch (Exception e) {
			System.out.println("Error in main...");
		}

	}

	private static void startCrawl(List<WebPageBean> frontier)
			throws IOException {
		 Will hold the Visited URL's in the Map with their inLinks 
		Map<String, List<String>> visitedLinks = new HashMap<String, List<String>>();
		 This will hold the Outlinks List for a given Iteration 
		//Map<String, List<String>> outLinksList = new HashMap<String, List<String>>();
		List<String> outLinksList= new ArrayList<String>();
		 This list holds the temp outlinks for a given URL 
		List<String> out = new ArrayList<String>();
		 This is a list of links which don't cater to the subject 
		List<String> discardedLinks = new ArrayList<String>();
		List<String> linksToAvoid = new ArrayList<String>();
		
		linksToAvoid = addLinksToAvoid(linksToAvoid);

		WebPageBean currentLink = new WebPageBean();
		 Check if the first element in the frontier queue is empty or not 
		int count = 0;

		while (visitedLinks.size() <= 20000) {
			// System.out.println("Count Value::: " + count);
			try {

				currentLink = getURLtoCrawl(frontier);
				String link =currentLink.getDocno();
				
				System.out.println("First Link::: "+link);
				
				if (link != null
						&& !(linksToAvoid.contains(link))) {

					if (!(visitedLinks.containsKey(link))
							&& !(discardedLinks.contains(link))) {

						
						 * System.out.println("ContentIsHTML::: " +
						 * contentIsHtml(currentLink));
						 

						ReadRobots rr = new ReadRobots();
						String domain = getDomain(link);
						
						if(domain.contains("wikipedia.org")){
							if(!domain.contains("en.wikipedia.org")){
								continue;
							}
						}

						List<String> disAllowedLinks = new ArrayList<String>();

						if (domainList.containsKey(domain)) {
							// System.out.println("Domain Already Present");
							disAllowedLinks = domainList.get(domain);

						} else {
							List<String> robotsDisAllowedLinks = rr
									.getRobotsList(domain);
							domainList.put(domain, robotsDisAllowedLinks);

						}

						if (disAllowedLinks.size() > 1) {

							if (!(disAllowedLinks.contains(link))
									&& contentIsHtml(link)) {
								String url = "";
								String title = "";
								String cleantext = "";
								String rawHtml = "";

								url = link;

								long a = System.currentTimeMillis();
								Document response = Jsoup.connect(url)
										.timeout(100000000)
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

										Thread.sleep(1000 - (b - a)); // wait
																		// for 1
																		// second
																		// before
										// a
										// GET
									} catch (InterruptedException ex) {
										Thread.currentThread().interrupt();
									}
								}

								// filter as per the topic given
								if (CheckContent(rawHtml, cleantext, title)) {
									// filter duplicate links & add them to
									// frontier
									System.out.println("Link::: " + url);
									out = getFinalOutLink(outLinks);
									// System.out.println("Out Size:: "+
									// out.size());

									 Add Outlinks 
									for (String l : out) {
										if (!(visitedLinks.containsKey(l))
												&& !(discardedLinks.contains(l))) {

											
											 * Check in Frontier as well and
											 * update Inlinks
											 
											if (frontier.containsKey(l)) {
												List<String> o = frontier
														.get(l);
												o.add(url);
												outLinksList.put(l, o);
											} else if (outLinksList
													.containsKey(l)) {
												List<String> o = outLinksList
														.get(l);
												o.add(url);
												outLinksList.put(l, o);

											} else {
												List<String> o = new ArrayList<String>();
												o.add(url);
												outLinksList.put(l, o);
											}
											if (visitedLinks.containsKey(l)) {
												List<String> o = visitedLinks
														.get(l);
												o.add(url);
												visitedLinks.put(l, o);
											}

										}

										 InLinks Check ends 

									}

									 Add Outlinks End 

									if (frontier.get(url) != null) {
																	 * is this
																	 * required
																	 
										visitedLinks
												.put(url, frontier.get(url));
									}
									// write to file
									writeToFile(url, out, title,
											cleantext, rawHtml, count);

								} else {
									discardedLinks.add(url);

								}

							}
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
			e.printStackTrace();
		}

		return hostId;

	}

	private static List<String> addLinksToAvoid(List<String> linksToAvoid) {
		// TODO Auto-generated method stub

		linksToAvoid.add("https://en.wikipedia.org/wiki/Special:Random");
		linksToAvoid.add("https://en.wikipedia.org/wiki/Main_Page");
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

		return linksToAvoid;
	}

	private static String getURLtoCrawlNonSorted(
			Map<String, List<String>> frontier) {

		if (frontier.size() > 0) {
			return uc.getCanonicalURL(frontier.entrySet().iterator().next()
					.getKey());
			
			return uc.getCanonicalURL(frontier.keySet().toArray()[0].toString());

			
			 * int i = 0; for (Map.Entry<String, List<String>> a :
			 * frontier.entrySet()) {
			 * 
			 * if (i < 1){ i++; return a.getKey(); }else{ break; } }
			 

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static WebPageBean getURLtoCrawl(List<WebPageBean> frontier) {
		// TODO Auto-generated method stub

		if (frontier.size() > 0) {	
			
			WebPageBean w= new WebPageBean();		
			return getSortedFrontier(frontier).get(0);		
			
			return uc.getCanonicalURL(getSortedFrontier(frontier).get(0).getDocno());	
			
		}
		
		return null;

	}

	private static ArrayList<WebPageBean> getSortedFrontier(
			List<WebPageBean> frontier) {
		// TODO Auto-generated method stub

		Comparator comparator = new Comparator<WebPageBean>() {

			@Override
			public int compare(WebPageBean w1, WebPageBean w2) {
				// TODO Auto-generated method stub
				return  w1.getInLinks().size() > w2.getInLinks().size() ? -1
						: w1.getInLinks().size() == w2.getInLinks().size() ? 0
								: 1;
			}
		};
		
		frontier.sort(comparator);
		
		return frontier;

		
	}

	private static List<String> getFinalOutLink(List<Element> outLinks) {
		// TODO Auto-generated method stub
		List<String> finalOutLinkList = new ArrayList<String>();

		for (Element l : outLinks) {

			String currentLink = uc.getCanonicalURL(l.attr("abs:href"));

			if (!(finalOutLinkList.contains(currentLink))) {
				finalOutLinkList.add(currentLink);
			}
		}
		return finalOutLinkList;
	}

	private static boolean CheckContent(String rawHtml, String plainText,
			String title) {

		
		 * String[] filterTexts = new String[] { "Barack Obama", "Obama",
		 * "President Obama" };
		 * 
		 * int i = 0;
		 * 
		 * while (i < filterTexts.length) { if
		 * (plainText.trim().contains(filterTexts[i]) ||
		 * title.trim().contains(filterTexts[i])) { return true; } i++; }
		 
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
			bw.write("<OUTLINKS>" + out.substring(0, out.length() - 2)
					+ "</OUTLINKS>");
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
		 Check if the page/domain is crawlable? 

		
		 * try {
		 * 
		 * Thread.sleep(1000); // wait for 1 second before a GET } catch
		 * (InterruptedException ex) { Thread.currentThread().interrupt(); }
		 
		boolean robot = cw.isCrawlable(currentLink, "Mozilla/5.0");

		// System.out.println("robots allowed::: " + robot);

		return robot;
	}

	private static boolean contentIsHtml(String currentLink) throws IOException {
		
		 * Make a HEAD Request to get the page type and return true if its
		 * text/html
		 
		try {
			URL url = new URL(currentLink);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			
			 * try {
			 * 
			 * Thread.sleep(1000); // wait for 1 second before a GET } catch
			 * (InterruptedException ex) { Thread.currentThread().interrupt(); }
			 
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(10000);
			connection.connect();

			if (isRedirect(connection.getResponseCode())) {
				String newUrl = connection.getHeaderField("Location");
				 get Redirected URL from location header field 
				
				 * System.out.println("Original request URL: " + currentLink +
				 * "redirected to: " + newUrl);
				 
				return contentIsHtml(newUrl);
			}
			
			 * String content = connection.getContentType().split(" ")[0];
			 * String contentType = content.substring(0, content.length() - 1);
			 

			// System.out.println("content Type:: " + contentType);

			if (connection.getContentType().contains("text/html")) {
				return true;
			} else
				return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	protected static boolean isRedirect(int statusCode) {

		if (statusCode != HttpURLConnection.HTTP_OK) {
			if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
					|| statusCode == HttpURLConnection.HTTP_MOVED_PERM
					|| statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
				return true;
			}
		}
		return false;
	}

*/}

