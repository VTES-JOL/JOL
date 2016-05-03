package dsclient.modelimpl;

import java.util.*;

import nbclient.model.Note;
import nbclient.model.NoteTaker;

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
