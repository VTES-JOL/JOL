/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 *
 * @author  administrator
 */
public interface NoteTaker {
    
    public Note[] getNotes();
    
    public Note addNote(String name);
    
    public void removeNote(Note note);
    
}
