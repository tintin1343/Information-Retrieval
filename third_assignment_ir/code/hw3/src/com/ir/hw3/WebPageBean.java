package com.ir.hw3;

import java.util.List;

public class WebPageBean {

	private String docno; // Stores the canonicalized URL
	private String head; // Stores the title of the Page
	private String text; // Stores the cleaned Text of the page.
	private String rawhtml; // Stores the rawHTML response of the page
	private int inLinkCount; // Stores the in Links Count for a particular page
	private int outLinksCount; // Stores the out Links Count for a particular
								// page.
	private List<String> inLinks;
	private List<String> outLinks;

	// list of all Inlinks and outlinks

	/*
	 * private List<String> inLinks; //Stores the List of all in links to the
	 * page private List<String> outLinks; // Stores the List of all out links
	 * 										// to the page
	 */

	/* Getter & Setters for the above bean variables */
	public String getDocno() {
		return docno;
	}

	public void setDocno(String docno) {
		this.docno = docno;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRawhtml() {
		return rawhtml;
	}

	public void setRawhtml(String rawhtml) {
		this.rawhtml = rawhtml;
	}

	public int getInLinkCount() {
		return inLinkCount;
	}

	public void setInLinkCount(int inLinkCount) {
		this.inLinkCount = inLinkCount;
	}

	public int getOutLinksCount() {
		return outLinksCount;
	}

	public void setOutLinksCount(int outLinksCount) {
		this.outLinksCount = outLinksCount;
	}

	public List<String> getInLinks() {
		return inLinks;
	}

	public void setInLinks(List<String> inLinks) {
		this.inLinks = inLinks;
	}

	public List<String> getOutLinks() {
		return outLinks;
	}

	public void setOutLinks(List<String> outLinks) {
		this.outLinks = outLinks;
	}

}
