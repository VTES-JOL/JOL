package deckserver.dwr.bean;

import nbclient.vtesmodel.BugDescriptor;

public class BugDetailBean {
	
	public BugDetailBean(BugDescriptor bd) {
		index = bd.getIndex();
		details = bd.getDescription();
		comments = bd.getComments();
	}
	
	public String index;
	
	public String details;
	
	public String[] comments;

	public String[] getComments() {
		return comments;
	}

	public String getDetails() {
		return details;
	}

	public String getIndex() {
		return index;
	}

}
