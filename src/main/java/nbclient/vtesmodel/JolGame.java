/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package nbclient.vtesmodel;

import cards.model.CardSearch;
import nbclient.model.GameAction;
import nbclient.model.state.SGame;

/**
 *
 * @author  administrator
 */
public interface JolGame {
    
    public static String ACTIVE_REGION = "active region";
    public static String READY_REGION = "ready region";
    public static String INACTIVE_REGION = "inactive region";
    public static String ASHHEAP = "ashheap";
    public static String HAND = "hand";
    public static String LIBRARY = "library";
    public static String CRYPT = "crypt";
    public static String TORPOR = "torpor";
    
    public static String[] TURN_PHASES = new String[] { "Untap", "Master", "Minion", "Influence", "Discard" };
    
    public void initGame(String name);
    
    public int getGameCounter();
    
    public SGame getState();
    
    public String getName();
    
    public void addPlayer(CardSearch cards, String name, String deck);
    
    public String[] getPlayers();

    public void replacePlayer(String oldPlayer, String newPlayer);
    
    public void setOrder(String[] players);
    
    /** if num == 0 shuffle the whole region 
     */
    public void shuffle(String player, String region, int num);
    
    public void startGame();
    
    public void moveToRegion(String cardId, String destPlayer, String destRegion, boolean bottom);
    
    public void moveToCard(String cardId, String destCard);
    
    public void drawCard(String player, String srcRegion, String destRegion);
    
    public void setEdge(String player);
    
    public String getEdge();
    
    public String getActivePlayer();
    
    public String getCardDescripId(String cardId);
    
    public int getCounters(String cardId);
    
    public void changeCounters(String cardId, int incr);
    
    public boolean isTapped(String cardId);
    
    public void setTapped(String cardId, boolean tapped);
    
    public void untapAll(String player);
    
    public void changePool(String player, int poolincr);
    
    public int getPool(String player);
    
    public void changeCapacity(String cardId, int capincr);
    
    public int getCapacity(String cardId);
    
    public void setText(String cardId, String text);
    
    public String getText(String cardId);
    
    public void setGlobalText(String text);
    
    public String getGlobalText();
    
    public void setPlayerText(String player, String text);
    
    public String getPlayerText(String player);
    
    public void sendMsg(String player, String msg);
    
    public String[] getTurns();
    
    public String getCurrentTurn();
    
    public void newTurn();
    
    public GameAction[] getActions(String turn);
    
    public String getPhase();
    
    public void setPhase(String phase);
    
    public String getPingTag(String player);
    
    public void setPingTag(String player);
   
}
