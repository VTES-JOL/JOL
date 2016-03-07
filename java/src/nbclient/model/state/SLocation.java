/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model.state;

/**
 *
 * @author  administrator
 */
public interface SLocation extends SCardContainer {
    
    public String getName();
        
    public SCard getCard(int index);
    
    public SCard getLastCard();
    
    public SCard getFirstCard();
    
}
