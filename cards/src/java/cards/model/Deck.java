/*
 * Deck.java
 *
 * Created on September 25, 2003, 8:45 PM
 */

package cards.model;

/**
 *
 * @author  administrator
 */
public interface Deck {
    
    public CardEntry[] getCards();
    
    public void addCard(CardEntry card);
    
    public int getQuantity(CardEntry card);
    
    public void setQuantity(CardEntry card, int quantity);
    
    public void removeCard(CardEntry card);
    
    public void removeAllCards();
}
