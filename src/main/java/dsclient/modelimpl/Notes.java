package dsclient.modelimpl;

import nbclient.model.Note;
import nbclient.model.NoteTaker;

import java.util.Collection;
import java.util.LinkedList;

class Notes implements NoteTaker {
	
	Collection<Note> notes = new LinkedList<Note>();

	public Note[] getNotes() {
		return notes.toArray(new Note[0]);
	}

	public Note addNote(String name) {
		Note n = new NoteImpl(name);
		notes.add(n);
		return n;
	}

	public void removeNote(Note note) {
		notes.remove(note);
	}

}
