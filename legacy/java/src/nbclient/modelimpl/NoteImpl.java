/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import javaclient.gen.*;
import nbclient.model.*;

/**
 *
 * @author  administrator
 */
public class NoteImpl implements Note {
    
    Notation note;
    
    /** Creates a new instance of CardImpl */
    public NoteImpl(Notation note) {
        this.note = note;
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
    
    public static NoteImpl mkNote(Region region, String name) {
        Notation note = new Notation();
        note.setName(name);
        region.addNotation(note);
        return new NoteImpl(note);
    }
    
    public static NoteImpl mkNote(GameCard card, String name) {
        Notation note = new Notation();
        note.setName(name);
        card.addNotation(note);
        return new NoteImpl(note);
    }
    
    public static NoteImpl mkNote(GameState state, String name) {
        Notation note = new Notation();
        note.setName(name);
        state.addNotation(note);
        return new NoteImpl(note);
    }
    
    public static NoteImpl[] getNotes(Notation[] notes) {
        NoteImpl[] ret = new NoteImpl[notes.length];
        for(int i = 0; i < ret.length; i++) 
            ret[i] = new NoteImpl(notes[i]);
        return ret;
    }
}
