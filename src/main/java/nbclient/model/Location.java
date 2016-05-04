/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 * @author administrator
 */
public interface Location extends NoteTaker, nbclient.model.state.SLocation, CardContainer {

    public void initCards(String[] cardIds);

    public void shuffle(int num);

}
