/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package nbclient.vtesmodel;

import java.text.*;
import java.util.*;

import nbclient.model.*;
import cards.local.*;
import cards.model.*;
import nbclient.model.state.SLocation;

/**
 *
 * @author  administrator
 */
public class JolGameImpl implements JolGame {
    
    Game state = null;
    TurnRecorder actions = null;
    
    static final String COUNTERS = "counters";
    static final String BLOOD = "blood";
    static final String TEXT = "text";
    static final String ACTIVE = "active meth";
    static final String POOL = "pool";
    static final String EDGE = "edge";
    static final String TAP = "tapnote";
    static final String TAPPED = "tap";
    static final String UNTAPPED = "untap";
    static final String PING = "ping";
    
    public JolGameImpl(Game state, TurnRecorder actions) {
        this.state = state;
        this.actions = actions;
    }
    
    public void addPlayer(CardSearch cardset, String name, String deckStr) {
        Deck deck = NormalizeDeckFactory.getNormalizer(cardset, deckStr);
        boolean reregister = false;
        String[] players = state.getPlayers();
        for(int i = 0; i < players.length; i++)
            if(name.equals(players[i])) reregister = true;
        if(!reregister) {
            state.addPlayer(name);
            state.addLocation(name,READY_REGION);
            changePool(name, 30);
            state.addLocation(name,TORPOR);
            state.addLocation(name,INACTIVE_REGION);
            state.addLocation(name,HAND);
            state.addLocation(name,ASHHEAP);
            state.addLocation(name,LIBRARY);
            state.addLocation(name,CRYPT);
        }
        CardEntry[] cards = deck.getCards();
        Location crypt = (Location) state.getPlayerLocation(name, CRYPT);
        Location library = (Location) state.getPlayerLocation(name, LIBRARY);
        Collection<String> cryptlist = new Vector<String>();
        Collection<String> librarylist = new Vector<String>();
        for(int i = 0; i < cards.length; i++) {
            CardEntry card = cards[i];
            Collection<String> dest;
            if(card.isCrypt()) {
                dest = cryptlist;
            } else {
                dest = librarylist;
            }
            int q = deck.getQuantity(card);
            for(int j = 0; j < q; j++) {
                dest.add(card.getCardId());
            }
        }
        String[] cryptarr = new String[cryptlist.size()];
        cryptlist.toArray(cryptarr);
        crypt.initCards(cryptarr);
        String[] libraryarr = new String[librarylist.size()];
        librarylist.toArray(libraryarr);
        library.initCards(libraryarr);
        
    }
    
    public void replacePlayer(String oldPlayer, String newPlayer) {
        // 1.  change the player order to reflect the new player
        // 2.  add all the regions
        // 3.  move the cards fromt he old player regions to the new player regions
        // 4.  set the pool and notes for the new player
        // 5.  if the old player is active, then create a new turn for him.
        // 5 1/2.  If the old player has the edge, move it to the new player
        // 6.  ping the new player
        String[] p = getPlayers();
        for (int i = 0; i < p.length; i++) {
            if (oldPlayer.equals(p[i])) {
                p[i] = newPlayer;
            }
        }
        state.addPlayer(newPlayer);
        state.addLocation(newPlayer, READY_REGION);
        Note note = getNote(state, newPlayer+POOL,true);
        note.setValue("" + getPool(oldPlayer));
        setPlayerText(newPlayer, getPlayerText(oldPlayer));
        state.addLocation(newPlayer, TORPOR);
        state.addLocation(newPlayer, INACTIVE_REGION);
        state.addLocation(newPlayer, HAND);
        state.addLocation(newPlayer, ASHHEAP);
        state.addLocation(newPlayer, LIBRARY);
        state.addLocation(newPlayer, CRYPT);
        _moveall(oldPlayer,READY_REGION,newPlayer,READY_REGION);
        _moveall(oldPlayer,TORPOR,newPlayer,TORPOR);
        _moveall(oldPlayer,INACTIVE_REGION,newPlayer,INACTIVE_REGION);
        _moveall(oldPlayer,HAND,newPlayer,HAND);
        _moveall(oldPlayer,ASHHEAP,newPlayer,ASHHEAP);
        _moveall(oldPlayer,LIBRARY,newPlayer,LIBRARY);
        _moveall(oldPlayer,CRYPT,newPlayer,CRYPT);
        if(oldPlayer.equals(getEdge())) setEdge(newPlayer);
        if(oldPlayer.equals(getActivePlayer())) {
            String turn = getCurrentTurn();
            int space = turn.lastIndexOf(" ");
            actions.addTurn(newPlayer, turn.substring(space + 1));
            setActivePlayer(newPlayer);
        }
        state.orderPlayers(p);
    }
    
    public void drawCard(String player, String srcRegion, String destRegion) {
        _drawCard(player,srcRegion,destRegion,true);
    }
    
    public void _drawCard(String player, String srcRegion, String destRegion,boolean log) {
        Location source = (Location) state.getPlayerLocation(player, srcRegion);
        Location dest = (Location) state.getPlayerLocation(player, destRegion);
        if(source == null || dest == null) throw new IllegalArgumentException("Not a valid region");
        Card card = (Card) source.getFirstCard();
        if(card == null) throw new IllegalArgumentException("No card available");
        source.removeCard(card);
        dest.addCard(card, false);
        if(log) {
            addCommand(player + " draws from " + srcRegion,
            new String[] { "draw", srcRegion, destRegion });
        }
    }
    
    public String getName() {
        return state.getName();
    }
    
    public String[] getPlayers() {
        return state.getPlayers();
    }
    
    public void moveToCard(String cardId, String destCard) {
        if(cardId.equals(destCard)) throw new IllegalArgumentException("Can't move a card to itself");
        Card srcCard = (Card) state.getCard(cardId);
        Card dstCard = (Card) state.getCard(destCard);
        if(srcCard == null || dstCard == null) throw new IllegalArgumentException("No such card");
        CardContainer source = (CardContainer) srcCard.getParent();
        addCommand("Put " + srcCard.getName() + " on " + getCardName(dstCard),
        new String[] { "puton", cardId, destCard });
        source.removeCard(srcCard);
        dstCard.addCard(srcCard,false);
    }
    
    public void moveToRegion(String cardId, String destPlayer, String destRegion, boolean bottom) {
        Card card = (Card) state.getCard(cardId);
        if(card == null) throw new IllegalArgumentException("No such card");
        CardContainer source = (CardContainer) card.getParent();
        Location dest = (Location) state.getPlayerLocation(destPlayer,destRegion);
        if(dest == null) throw new IllegalStateException("No such region");
        addCommand("Move " + getCardName(card, destRegion) + " to " + destPlayer + "'s " + destRegion,
        new String[] {"move", cardId, destPlayer, destRegion, bottom ? "bottom" : "top"});
        source.removeCard(card);
        dest.addCard(card,false);
        // PENDING flatten when moving to ashheap
    }
    
    public void setOrder(String[] players) {
        state.orderPlayers(players);
        String order = "";
        for(int i = 0; i < players.length; i++) order = order + " " + players[i];
        addCommand("Player order" + order,
        new String[] {"order", order});
    }
    
    public void shuffle(String player, String region, int num) {
        _shuffle(player, region, num, true);
    }
    
    private void _shuffle(String player, String region, int num, boolean log) {
        Location location = (Location) state.getPlayerLocation(player, region);
        location.shuffle(num);
        if(log) {
            String add = (num == 0) ? "" : num + " of ";
            addCommand("Shuffle " + add + player + "'s " + region,
            new String[] { "shuffle", player, region, num+""});
        }
    }
    
    private void _moveall(String player, String srcLoc,String destPlayer, String destLoc) {
        if(destPlayer == null) destPlayer = player;
        Location src = (Location) state.getPlayerLocation(player, srcLoc);
        Location dest = (Location) state.getPlayerLocation(destPlayer, destLoc);
        Card[] cards = (Card[]) src.getCards();
        for(int i = 0; i < cards.length; i++) {
            src.removeCard(cards[i]);
            dest.addCard(cards[i],false);
        }
    }
    
    public void startGame() {
        String[] players = state.getPlayers();
        players = (String[]) util.Shuffle.shuffle(players);
        state.orderPlayers(players);
        addCommand("Start game",new String[] {"start"});
        newTurn();
        for(int i = 0; i < players.length; i++) {
            Note note = getNote(state, players[i]+POOL,true);
            note.setValue("30");
            _moveall(players[i], INACTIVE_REGION,null, CRYPT);
            _moveall(players[i], HAND,null, LIBRARY);
            _shuffle(players[i], CRYPT, 0, false);
            _shuffle(players[i], LIBRARY, 0, false);
            for(int j = 0; j < 4 ; j++)
                _drawCard(players[i], CRYPT, INACTIVE_REGION,false);
            for(int j = 0; j < 7 ; j++)
                _drawCard(players[i], LIBRARY, HAND,false);
        }
        // PENDING should record the shuffle seed, so needs to be formatted differently
    }
    
    public nbclient.model.state.SGame getState() {
        return state;
    }
    
    public void initGame(String name) {
        state.setName(name);
        setEdge("no one");
    }
    
    public void sendMsg(String player, String msg) {
        msg = truncateMsg(msg);
        addMessage("[" + player + "] " + msg);
    }
    
    private String truncateMsg(String msg) {
        if(msg.length() < 120) return msg;
        return msg.substring(0,120);
    }
    
    private Note getNote(NoteTaker nt, String name, boolean create) {
        Note[] notes = nt.getNotes();
        for(int i = 0; i < notes.length; i++)
            if(notes[i].getName().equals(name)) return notes[i];
        if (create) {
            Note note = nt.addNote(name);
            return note;
        }
        return null;
    }
    
    public String getCardDescripId(String cardId) {
        Card card = (Card) state.getCard(cardId);
    	return card.getCardId();
    }
    
    public int getCounters(String cardId) {
    //	System.out.println("Getting counters for " + cardId);
        Card card = (Card) state.getCard(cardId);
     //   System.out.println("Card is " + card);
        Note note = getNote(card, COUNTERS, false);
        if(note != null)
            return Integer.parseInt(note.getValue());
        return 0;
    }
    
    public void changeCounters(String cardId, int incr) {
        if(incr == 0) return; // no change necessary - PENDING log this though?
        Card card = (Card) state.getCard(cardId);
        Note note = getNote(card, COUNTERS, true);
        String valStr = note.getValue();
        int val = (valStr == null) ? 0 : Integer.parseInt(valStr);
        val += incr;
        note.setValue(val + "");
        String logText;
        if(incr < 0) logText = "Removed " + Math.abs(incr) + " blood from ";
        else logText = "Added " + incr + " blood to ";
        logText = logText + getCardName(card) + ", now " + val + ".";
        addCommand(logText, new String[] { "counter", cardId, incr + "" });
    }
    
    private boolean isHiddenRegion(String region) {
        return region.equals(INACTIVE_REGION) || region.equals(HAND) || region.equals(LIBRARY) || region.equals(CRYPT);
    }
    
    /*
    private String getCardName(Card card, Card destCard) {
        Location loc = (Location) state.getRegionFromCard(destCard);
        if(loc == null) return card.getName();
        String region = state.getPlayerRegionName(loc);
        if(region == null) return card.getName();
        return getCardName(card,region);
    } */
    
    private String getCardName(Card card, String destRegion) {
        if(!isHiddenRegion(destRegion)) return card.getName();
        return getCardName(card);
    }
    
    private String getCardName(Card card) {
        Location loc = (Location) state.getRegionFromCard(card);
        if(loc == null) return card.getName();
        String region = state.getPlayerRegionName(loc);
        if(region == null) return card.getName();
        if(isHiddenRegion(region)) {
            CardContainer container = (CardContainer) card.getParent();
            if(container instanceof Location) {
                loc = (Location) container;
                Card[] cards = (Card[])loc.getCards();
                for(int j = 0; j < cards.length; j++)
                    if(card.getId().equals(cards[j].getId()))
                        return region + " #" + (j + 1);
            }
        }
        return card.getName();
    }
    
    public void setEdge(String player) {
        String edge = (player == null) ? "no one" : player;
        Note note = getNote(state, EDGE, true);
        String old = note.getValue();
        if(old == null) old = ""; else old = " from " + old;
        note.setValue(edge);
        if(player == null)
            addCommand("Edge burned" + old + ".", new String[]{ "edge", "burn" });
        else
            addCommand(player + " grabs edge" + old + ".", new String[] { "edge", player });
    }
    
    private void setActivePlayer(String player) {
        Note note = getNote(state, ACTIVE, true);
        note.setValue(player);
    }
    
    public String getActivePlayer() {
        Note note = getNote(state, ACTIVE, false);
        if(note == null) return "";
        return note.getValue();
    }
    
    public String getEdge() {
        Note note = getNote(state, EDGE, false);
        if(note == null) return "";
        return note.getValue();
    }
    
    public int getPool(String player) {
        Note note = getNote(state, player+POOL,false);
        return Integer.parseInt(note.getValue());
    }
    
    public void changePool(String player, int pool) {
        if(pool == 0) return; // PENDING report this in status?
        Note note = getNote(state, player+POOL, true);
        String sval = note.getValue();
        int ival = (sval == null) ? 0 : Integer.parseInt(sval);
        int val = ival + pool;
        note.setValue(val+"");
        addCommand(player + "'s pool was " + ival + ", now is " + val + ".", new String[] { "pool", player, val + ""});
    }
    
    public String getGlobalText() {
        Note note = getNote(state, TEXT, true);
        if(note.getValue() == null) note.setValue("");
        return note.getValue();
    }
    
    public String getPlayerText(String player) {
        Note note = getNote(state, player+TEXT, false);
        if(note == null) return "";
        return note.getValue();
    }
    
    public String getText(String cardId) {
        Card card = (Card) state.getCard(cardId);
        Note note = getNote(card, TEXT, false);
        if (note != null) return note.getValue();
        return "";
    }
    
    public void setGlobalText(String text) {
        Note note = getNote(state, TEXT, true);
        note.setValue(text);
    }
    
    public void setPlayerText(String player, String text) {
        Note note = getNote(state, player+TEXT, true);
        note.setValue(text);
    }
    
    public void setText(String cardId, String text) {
        Card card = (Card) state.getCard(cardId);
        Note note = getNote(card, TEXT, true);
        note.setValue(text);
        addMessage(getCardName(card) + " now " + text);
    }
    
    public boolean isTapped(String cardId) {
        Card card = (Card) state.getCard(cardId);
        Note note = getNote(card, TAP, false);
        return note != null && note.getValue().equals(TAPPED);
    }
    
    public void setTapped(String cardId, boolean tapped) {
        Card card = (Card) state.getCard(cardId);
        String logtext = (tapped ? "Tap " : "Untap ") + getCardName(card);
        addCommand(logtext,new String[] { "tap", cardId });
        _setTap(card,tapped);
    }
    
    private void _setTap(Card card, boolean tapped) {
        Note note = getNote(card, TAP, tapped);
        if(note == null) return; // defaults to untapped, no need to create a note
        // in this circumstance.
        String value = tapped ? TAPPED : UNTAPPED;
        note.setValue(value);
    }
    
    public void untapAll(String player) {
        Location[] locs = (Location[]) state.getPlayerLocations(player);
        addCommand(player + " untaps.",new String[] {"untap", player });
        for(int i = 0; i < locs.length; i++)
            untapAll(locs[i]);
    }
    
    private void untapAll(CardContainer loc) {
        Card[] cards = (Card[]) loc.getCards();
        for(int i = 0; i < cards.length; i++) {
            _setTap(cards[i],false);
            untapAll(cards[i]);
        }
    }
    
    private String getTurn() {
        return getCurrentTurn();
    }
    
    public String getCurrentTurn() {
        String[] turns = getTurns();
        if(turns.length == 0) return null;
        return turns[turns.length - 1 ];
    }
    
    private void addCommand(String arg1, String[] arg2) {
        String turn = getTurn();
        if(turn != null)
            actions.addCommand(turn,getDate() + arg1,arg2);
    }
    
    private void addMessage(String arg1) {
        String turn = getTurn();
        if(turn != null)
            actions.addMessage(getTurn(),getDate() + arg1);
    }
    
    private static DateFormat format = new SimpleDateFormat("HH:mm M/d ");
    private String getDate() {
        return format.format(new Date());
    }
    
    public GameAction[] getActions(String turn) {
        return actions.getActions(turn);
    }
    
    public String[] getTurns() {
        return actions.getTurns();
    }
    
    public void newTurn() {
        String turn = getCurrentTurn();
        String[] players = getPlayers();
        int round = 1;
        int index = 0;
        if(turn != null) {
            String meth = actions.getMethTurn(turn);
            int length = turn.length();
            int space = turn.lastIndexOf(" ");
            round = Integer.parseInt(turn.substring(space + 1,length - 2));
            for(int i = 0; i < players.length; i++)
                if(meth.equals(players[i])) index = i + 1;
            if(index == players.length) {
                index = 0; round++;
            }
            while (!meth.equals(players[index]) && getPool(players[index]) < 1) {
                index++;
                if(index == players.length) {
                    index = 0;
                    round++;
                }
            }
        }
        actions.addTurn(players[index], players[index] + " " + round + "." + (index+1));
        setActivePlayer(players[index]);
        setPhase(TURN_PHASES[0]);
    }
    
    public String getPhase() {
        Note n = getNote(state, "phase", true);
        String val = n.getValue();
        if(val == null || val.length() == 0) val = TURN_PHASES[0];
        return val;
    }
    
    public void setPhase(String phase) {
        Note n = getNote(state, "phase", true);
        n.setValue(phase);
        sendMsg(getActivePlayer(),"START OF " + phase.toUpperCase() + " PHASE.");
    }
    
    public int getGameCounter() {
        return actions.getCounter();
    }
    
    public void changeCapacity(String cardId, int capincr) {
        Card card = (Card) state.getCard(cardId);
        Note cap = getNote(card,"capac", true);
        String val = cap.getValue();
        int amt = 0;
        if(val == null || val.length() == 0) amt += capincr;
        else amt = Integer.parseInt(val) + capincr;
        if (amt < 0) amt = 0;
        cap.setValue(amt + "");
        addCommand("Capacity of " + getCardName(card) + " now " + amt,
        new String[] { "capacity", cardId, capincr + "" });
    }
    
    public int getCapacity(String cardId) {
        Card card = (Card) state.getCard(cardId);
        Note cap = getNote(card,"capac", false);
        if(cap == null)
            return -1;
        return Integer.parseInt(cap.getValue());
    }
    
    public String getPingTag(String player) {
       Note note = getNote(state, player+PING, false);
       if(note == null) return "";
       return note.getValue();
    }
    
    public void setPingTag(String player) {
        Note note = getNote(state, player+PING, true);
        note.setValue(getDate());
    }

}
