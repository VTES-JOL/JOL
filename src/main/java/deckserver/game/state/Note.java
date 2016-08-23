/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package deckserver.game.state;

import deckserver.game.state.model.GameCard;
import deckserver.game.state.model.GameState;
import deckserver.game.state.model.Notation;
import deckserver.game.state.model.Region;

/**
 * @author administrator
 */
public class Note {

    Notation note;

    /**
     * Creates a new instance of CardImpl
     */
    public Note(Notation note) {
        this.note = note;
    }

    public static Note mkNote(Region region, String name) {
        Notation note = new Notation();
        note.setName(name);
        region.addNotation(note);
        return new Note(note);
    }

    public static Note mkNote(GameCard card, String name) {
        Notation note = new Notation();
        note.setName(name);
        card.addNotation(note);
        return new Note(note);
    }

    public static Note mkNote(GameState state, String name) {
        Notation note = new Notation();
        note.setName(name);
        state.addNotation(note);
        return new Note(note);
    }

    public static Note[] getNotes(Notation[] notes) {
        Note[] ret = new Note[notes.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new Note(notes[i]);
        return ret;
    }

    public String getName() {
        return note.getName();
    }

    public String getValue() {
        return note.getValue();
    }

    public void setValue(String value) {
        note.setValue(value);
    }
}
