/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface NoteTaker {

    Note[] getNotes();

    Note addNote(String name);

    void removeNote(Note note);

}
