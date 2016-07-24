package deckserver.game.state;

import deckserver.game.state.model.Notation;

import java.util.Collection;
import java.util.LinkedList;

class Notes implements NoteTaker {

    private Collection<Note> notes = new LinkedList<>();

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

}
