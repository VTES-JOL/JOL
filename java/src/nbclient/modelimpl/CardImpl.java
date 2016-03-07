/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import javaclient.gen.*;
import nbclient.model.*;
import cards.model.CardEntry;

import nbclient.vtesmodel.JolAdminFactory;

/**
 *
 * @author  administrator
 */
public class CardImpl implements Card {
    
    GameImpl game;
    GameCard gamecard;
    
    /** Creates a new instance of CardImpl */
    public CardImpl(GameImpl game, GameCard gamecard) {
        this.game = game;
        this.gamecard = gamecard;
    }
    
    public String getCardId() {
        return gamecard.getCardid();
    }
    
    public String getId() {
        return gamecard.getId();
    }
    
    public String getName() {
        CardEntry card = JolAdminFactory.INSTANCE.getAllCards().getCardById(getCardId());
        if(card == null) return getCardId();
        return card.getName();
    }
    
    public Note[] getNotes() {
        return NoteImpl.getNotes(gamecard.getNotation());
    }
    
    public void addCard(Card card, boolean first) {
        CardContainer container = game.getCardContainer(this,true);
        container.addCard(card,first);
    }
    
    public Note addNote(String name) {
        return NoteImpl.mkNote(gamecard,name);
    }
    
    public nbclient.model.state.SCard[] getCards() {
        CardContainer container = game.getCardContainer(this,false);
        if (container == null) return new Card[0];
        return (Card[]) container.getCards();
    }
    
    public nbclient.model.state.SCardContainer getParent() {
        return (CardContainer) game.getContainer(this);
    }
    
    public void removeCard(Card card) {
        CardContainer container = game.getCardContainer(this,false);
        container.removeCard(card);
    }
    
    public void removeNote(Note note) {
    }
    
    public String toString() {
        return getName();
    }

	public void setCards(Card[] cards) { 
		for(int i = 0; i < cards.length; i++) {
			GameCard c = game.mkCard(cards[i]);
			Card cd = (Card) game.getCard(c.getId());
			addCard(cd,false);
		}
	}	
	 
}
