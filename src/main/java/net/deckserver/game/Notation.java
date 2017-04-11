package net.deckserver.game;

import java.util.List;

public interface Notation {

    void addNote(String text);

    List<String> getNotes();
}
