package com.ir.hw5;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QrelBean {
	
	public QrelBean() {
		super();
		
	}
	
	public QrelBean(String queryId,String accessor, String docId, String rank, String fileName) {
		super();
		this.queryId = queryId;
		this.accessor = accessor;
		this.docId = docId;
		this.rank = rank;
		this.fileName = fileName;
	}
	
	public String queryId;
	public String accessor;
	public String docId;
	public String rank;
	public String fileName;
	
	public String getAccessor() {
		return accessor;
	}
	public void setAccessor(String accessor) {
		this.accessor = accessor;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
