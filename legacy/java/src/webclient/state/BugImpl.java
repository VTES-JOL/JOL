package webclient.state;

import java.io.*;

import util.StreamReader;

import nbclient.vtesmodel.BugDescriptor;

public final class BugImpl implements BugDescriptor {
	
	public BugImpl(File dir) {
		this.dir = dir;
	}
	
	private final File dir;
	private String summary, status, filer, description;

	public String[] getComments() {
		File[] files = dir.listFiles(filter);
		String[] comments = new String[files.length];
		for(int i = 0; i < comments.length; i++) {
			try {
				comments[i] = StreamReader.read(new FileInputStream(files[i]));
			} catch (IOException ie) {
				comments[i] = "";
			}
		}
		return comments;
	}

	public String getStatus() {
		if(status == null) 
			try {
				status = StreamReader.read(new FileInputStream(new File(dir,"status")));
			} catch (IOException ie) {
				
			}
		return status;
	}

	public String getSummary() {
		if(summary == null) 
			try {
				summary = StreamReader.read(new FileInputStream(new File(dir,"summary")));
			} catch (IOException ie) {
				
			}
		return summary;	
	}

	public String getFiler() {
		if(filer == null) 
			try {
				filer = StreamReader.read(new FileInputStream(new File(dir,"filer")));
			} catch (IOException ie) {
				
			}
		return filer;	
	}

	public String getIndex() {
		return dir.getName();
	}

	public String getDescription() {
		if(description == null) 
			try {
				description = StreamReader.read(new FileInputStream(new File(dir,"descrip")));
			} catch (IOException ie) {
				
			}
		return description;	
	}

	public void setStatus(String status) {
		writeContents("status",status);
	}

	public void addComment(String comment) {
		File[] comments = dir.listFiles(filter);
		int idx = 0;
		for(int i = 0; i < comments.length; i++) {
			int cur = Integer.parseInt(comments[i].getName().substring(9));
			if(cur > idx) idx = cur;
		}
		idx++;
		writeContents("comment" + idx, comment);
	}

	private void writeContents(String file,String contents) {
		writeContents(dir,file,contents);
	}
	
	static void createBug(File dir, String filer, String summary, String descrip) {
		writeContents(dir,"filer",filer);
		writeContents(dir,"summary",summary);
		writeContents(dir,"descrip",descrip);
		writeContents(dir,"status","open");
	}
	
	static void writeContents(File dir, String file, String contents) {
		Writer out = null;
		try {
			out = new FileWriter(new File(dir,file));
			out.write(contents);
		} catch (IOException ie) {
			
		} finally {
			if(out != null) 
				try {
					out.close();
				} catch (IOException ie) {
					
				}
		}
	}
	
	private static FilenameFilter filter = new CommentFilter();

	private static class CommentFilter implements FilenameFilter {

		public boolean accept(File arg0, String arg1) {
			return arg1.startsWith("comment.");
		}
		
	}
	
}
