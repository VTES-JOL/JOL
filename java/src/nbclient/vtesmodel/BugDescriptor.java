package nbclient.vtesmodel;

public interface BugDescriptor {

	public String getFiler();

	public String getStatus();
	
	public void setStatus(String status);

	public String getSummary();
	
	public String getDescription();
	
	public String[] getComments();
	
	public void addComment(String comment);

	public String getIndex();

}
