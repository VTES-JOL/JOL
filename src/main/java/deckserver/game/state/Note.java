/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package deckserver.game.state;

import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.jaxb.state.Region;

import java.util.List;
import java.util.stream.Collectors;

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
        region.getNotation().add(note);
        return new Note(note);
    }

    public static Note mkNote(GameCard card, String name) {
        Notation note = new Notation();
        note.setName(name);
        card.getNotation().add(note);
        return new Note(note);
    }

    public static Note mkNote(GameState state, String name) {
        Notation note = new Notation();
        note.setName(name);
        state.getNotation().add(note);
        return new Note(note);
    }

    public static Note[] getNotes(List<Notation> notes) {
        return notes.stream()
                .map(Note::new)
                .collect(Collectors.toList())
                .toArray(new Note[notes.size()]);
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
