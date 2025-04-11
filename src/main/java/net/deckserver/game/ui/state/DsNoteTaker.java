package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.NoteTaker;
import net.deckserver.game.jaxb.state.Notation;

import java.util.ArrayList;
import java.util.List;

public class DsNoteTaker implements NoteTaker {

    private final List<Notation> notes = new ArrayList<>();

    public List<Notation> getNotes() {
        return notes;
    }

    public Notation addNote(String name) {
        Notation notation = new Notation();
        notation.setName(name);
        notes.add(notation);
        return notation;
    }

}
