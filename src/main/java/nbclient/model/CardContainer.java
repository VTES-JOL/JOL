/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 * @author administrator
 */
public interface CardContainer extends nbclient.model.state.SCardContainer {

    public void addCard(Card card, boolean first);

    public void removeCard(Card card);

    public void setCards(Card[] cards);

}
