package cards.local;

import cards.model.CardSet;
import cards.model.Deck;

public interface NormalizeDeck extends Deck {

	public abstract String getDeckString();

	public abstract String getFilteredDeck();

	public abstract CardSet findCardName(String text);

	public abstract String[] getErrorLines();
	
	public abstract int getCryptSize();
	
	public abstract int getLibSize();

	public abstract String getGroups();

}