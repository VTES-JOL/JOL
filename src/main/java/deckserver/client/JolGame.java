/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package deckserver.client;

import deckserver.game.cards.OldCardSearch;
import deckserver.game.state.SGame;
import deckserver.game.turn.GameAction;

/**
 * @author administrator
 */
public interface JolGame {

    String ACTIVE_REGION = "active region";
    String READY_REGION = "ready region";
    String INACTIVE_REGION = "inactive region";
    String ASHHEAP = "ashheap";
    String HAND = "hand";
    String LIBRARY = "library";
    String CRYPT = "crypt";
    String TORPOR = "torpor";

    String[] TURN_PHASES = new String[]{"Untap", "Master", "Minion", "Influence", "Discard"};

    void initGame(String name);

    SGame getState();

    String getName();

    void addPlayer(OldCardSearch cards, String name, String deck);

    String[] getPlayers();

    void setOrder(String[] players);

    void shuffle(String player, String region, int num);

    void startGame();

    void moveToRegion(String cardId, String destPlayer, String destRegion, boolean bottom);

    void moveToCard(String cardId, String destCard);

    void drawCard(String player, String srcRegion, String destRegion);

    String getEdge();

    void setEdge(String player);

    String getActivePlayer();

    String getCardDescripId(String cardId);

    int getCounters(String cardId);

    void changeCounters(String cardId, int incr);

    boolean isTapped(String cardId);

    void setTapped(String cardId, boolean tapped);

    void untapAll(String player);

    void changePool(String player, int poolincr);

    int getPool(String player);

    void changeCapacity(String cardId, int capincr);

    int getCapacity(String cardId);

    void setText(String cardId, String text);

    String getText(String cardId);

    String getGlobalText();

    void setGlobalText(String text);

    void setPlayerText(String player, String text);

    String getPlayerText(String player);

    void sendMsg(String player, String msg);

    String[] getTurns();

    String getCurrentTurn();

    void newTurn();

    GameAction[] getActions(String turn);

    String getPhase();

    void setPhase(String phase);

    String getPingTag(String player);

    void setPingTag(String player);

}
