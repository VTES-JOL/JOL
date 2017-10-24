package net.deckserver.dwr.bean;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Game;

import java.util.ArrayList;
import java.util.List;

public class GameCardBean {

    private String cardId;
    private String name;
    private boolean visible;
    private boolean locked;
    private int capacity;
    private int counters;
    private boolean isCrypt;
    private boolean isAlly;
    private String label;
    private List<GameCardBean> cards = new ArrayList<>();

    public GameCardBean(Game gameState, String id) {
        Card card = gameState.getCard(id);
        this.cardId = card.getCardId();
    }

    public String getCardId() {
        return cardId;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCounters() {
        return counters;
    }

    public boolean isCrypt() {
        return isCrypt;
    }

    public boolean isAlly() {
        return isAlly;
    }

    public String getLabel() {
        return label;
    }

    public List<GameCardBean> getCards() {
        return cards;
    }
}
