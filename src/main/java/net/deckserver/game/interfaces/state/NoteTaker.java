/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.state;

import net.deckserver.game.jaxb.state.Notation;

import java.util.List;

/**
 * @author administrator
 */
public interface NoteTaker {

    List<Notation> getNotes();

    Notation addNote(String name);

}
