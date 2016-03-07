package deckserver.dwr.bean;

import nbclient.vtesmodel.BugDescriptor;

public class BugSummaryBean {
	
	private String status;

	public BugSummaryBean(BugDescriptor bd) {
		index = bd.getIndex();
		summary = bd.getSummary();
		status = bd.getStatus();
		filer = bd.getFiler();
	}

	private String index;
	
	private String summary;
	
	private String filer;

	public String getFiler() {
		return filer;
	}

	public String getIndex() {
		return index;
	}

	public String getSummary() {
		return summary;
	}
	
	public String getStatus() {
		return status;
	}
}
