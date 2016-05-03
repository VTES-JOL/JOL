package dsclient.modelimpl;

import nbclient.model.Note;

class NoteImpl implements Note {
	
	NoteImpl(String name) {
		this.name = name;
	}
	
	private final String name;
	private String value;

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
