package deckserver.client;

import deckserver.game.state.Notation;
import deckserver.interfaces.Note;
import deckserver.interfaces.NoteTaker;

import java.util.Collection;
import java.util.LinkedList;

class Notes implements NoteTaker {

    Collection<Note> notes = new LinkedList<Note>();

    public Note[] getNotes() {
        return notes.toArray(new Note[0]);
    }

    public Note addNote(String name) {
        Notation notation = new Notation();
        notation.setName(name);
        Note n = new NoteImpl(notation);
        notes.add(n);
        return n;
    }

    public void removeNote(Note note) {
        notes.remove(note);
    }

}
