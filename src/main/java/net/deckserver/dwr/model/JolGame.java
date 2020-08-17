/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package net.deckserver.dwr.model;

import com.google.common.base.Strings;
import net.deckserver.Utils;
import net.deckserver.dwr.model.ChatParser;
import net.deckserver.game.interfaces.state.*;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.json.deck.CardSummary;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.cards.Deck;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.rest.ApiResource;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author administrator
 */
public class JolGame {

    public static final String ACTIVE_REGION = "active region";
    public static final String READY_REGION = "ready region";
    public static final String INACTIVE_REGION = "inactive region";
    public static final String ASHHEAP = "ashheap";
    public static final String HAND = "hand";
    public static final String LIBRARY = "library";
    public static final String CRYPT = "crypt";
    public static final String TORPOR = "torpor";
    public static final String RFG = "rfg";
    public static final String RESEARCH = "research";
    public static final String[] TURN_PHASES = new String[]{"Unlock", "Master", "Minion", "Influence", "Discard"};

    private static final String COUNTERS = "counters";
    private static final String TEXT = "text";
    private static final String VOTES = "votes";
    private static final String ACTIVE = "active meth";
    private static final String POOL = "pool";
    private static final String EDGE = "edge";
    private static final String TAP = "tapnote";
    private static final String TAPPED = "tap";
    private static final String UNTAPPED = "untap";
    private static DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");
    private DsGame state;
    private DsTurnRecorder actions;
    private final String id;

    private Set<String> uniques = new HashSet<>();

    public JolGame(String id, DsGame state, DsTurnRecorder actions) {
        this.id = id;
        this.state = state;
        this.actions = actions;
    }

    public void addPlayer(CardSearch cardset, String name, String deckStr) {
        Deck deck = new Deck(cardset, deckStr);
        boolean reregister = false;
        List<String> players = state.getPlayers();
        for (String player : players) if (name.equals(player)) reregister = true;
        if (!reregister) {
            state.addPlayer(name);
            state.addLocation(name, READY_REGION);
            state.addLocation(name, TORPOR);
            state.addLocation(name, INACTIVE_REGION);
            state.addLocation(name, HAND);
            state.addLocation(name, ASHHEAP);
            state.addLocation(name, LIBRARY);
            state.addLocation(name, CRYPT);
            state.addLocation(name, RFG);
            state.addLocation(name, RESEARCH);
            changePool(name, 30);
        }
        CardEntry[] cards = deck.getCards();
        Location crypt = state.getPlayerLocation(name, CRYPT);
        Location library = state.getPlayerLocation(name, LIBRARY);
        Collection<String> cryptlist = new Vector<>();
        Collection<String> librarylist = new Vector<>();
        for (CardEntry card : cards) {
            Collection<String> dest;
            if (card.isCrypt()) {
                dest = cryptlist;
            } else {
                dest = librarylist;
            }
            int q = deck.getQuantity(card);
            for (int j = 0; j < q; j++) {
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

    void drawCard(String player, String srcRegion, String destRegion) {
        _drawCard(player, srcRegion, destRegion, true);
    }

    private void _drawCard(String player, String srcRegion, String destRegion, boolean log) {
        Location source = state.getPlayerLocation(player, srcRegion);
        Location dest = state.getPlayerLocation(player, destRegion);
        if (source == null || dest == null) throw new IllegalArgumentException("Not a valid region");
        Card card = source.getFirstCard();
        if (card == null) throw new IllegalArgumentException("No card available");
        source.removeCard(card);
        dest.addCard(card, false);
        if (log) {
            addCommand(player + " draws from " + srcRegion, new String[]{"draw", srcRegion, destRegion});
        }
    }

    public String getName() {
        return state.getName();
    }

    public List<String> getPlayers() {
        return state.getPlayers();
    }

    void moveToCard(
            boolean play, String player, String cardId, String destPlayer,
            String destRegion, String destCard, String[] modes) {
        if (cardId.equals(destCard)) throw new IllegalArgumentException("Can't move a card to itself");
        Card srcCard = state.getCard(cardId);
        Card dstCard = state.getCard(destCard);
        CardContainer parentContainer = dstCard.getParent();
        while (parentContainer != null) {
            if (parentContainer instanceof Card) {
                Card parentCard = (Card) parentContainer;
                if (parentCard.getId().equals(srcCard.getId()))
                    throw new IllegalArgumentException("Can't create card loop");
                parentContainer = parentCard.getParent();
            } else {
                break;
            }
        }
        if (srcCard == null) throw new IllegalArgumentException("No such card");
        CardContainer source = srcCard.getParent();
        Location loc = (Location) state.getRegionFromCard(dstCard);

        String message = null;
        if (play) {
            //Destination messages:
            //Target is unique and public: "on <target>"
            //Target is non-unique and public: "on [<player>'s] <target> [#<region-index>] ["<label>"]"
            //Target is in secret region: "on [<player>'s] <secret region #>"

            boolean destHidden = isHiddenRegion(destRegion);
            CardEntry destEntry = CardSearch.INSTANCE.getCardById(dstCard.getCardId());

            boolean includePlayer = !player.equals(destPlayer) && (destHidden || !destEntry.isUnique());
            String differentiators = "";
            if (!(destHidden || destEntry.isUnique())) {
                int regionIndex = getIndexInRegion(dstCard);
                String label = getText(destCard);
                differentiators = String.format(
                    "%s%s",
                    regionIndex > -1 ? String.format(" #%s", regionIndex + 1) : "",
                    label.equals("") ? "" : String.format(" \"%s\"", label)
                );
            }
            String destMessage = String.format(
                " on %s%s%s",
                includePlayer ? String.format("%s's ", destPlayer) : "",
                getCardName(dstCard),
                differentiators
            );
            message = playCardMessage(player, srcCard, destRegion, modes, destMessage);
        }
        else {
            message = String.format(
                "%s puts %s on %s",
                player,
                getCardName(srcCard, loc.getName()),
                getCardName(dstCard));
        }
        addCommand(message, new String[]{"puton", cardId, destCard});

        source.removeCard(srcCard);
        dstCard.addCard(srcCard, false);
    }

    void moveToCard(String player, String cardId, String destCard) {
        moveToCard(false, player, cardId, null, null, destCard, null);
    }

    void moveToRegion(String player, String cardId, String destPlayer, String destRegion, boolean bottom) {
        Card card = state.getCard(cardId);
        if (card == null) throw new IllegalArgumentException("No such card");
        CardContainer source = card.getParent();
        Location dest = state.getPlayerLocation(destPlayer, destRegion);
        if (dest == null) throw new IllegalStateException("No such region");

        String message = String.format(
            "%s moves %s to %s%s's %s",
            player,
            getCardName(card, destRegion),
            bottom ? "" : "top of ",
            destPlayer,
            destRegion);
        addCommand(message, new String[]{"move", cardId, destPlayer, destRegion, bottom ? "bottom" : "top"});
        source.removeCard(card);
        dest.addCard(card, !bottom);
    }

    public void discard(String player, String cardId, boolean random) {
        Card card = state.getCard(cardId);
        if (card == null) throw new IllegalArgumentException("No such card");
        CardContainer source = card.getParent();
        Location dest = state.getPlayerLocation(player, ASHHEAP);
        if (dest == null) throw new IllegalStateException("No such region");
        String message = String.format(
                "%s discards %s%s",
                player,
                getCardLink(card),
                random ? " (picked randomly)" : "");
        addCommand(message, new String[]{"move", cardId, player, JolGame.ASHHEAP, "top"});
        source.removeCard(card);
        dest.addCard(card, false);
    }

    public void playCard(String player, String cardId, String destPlayer, String destRegion, String[] modes) {
        Card card = state.getCard(cardId);
        if (card == null) throw new IllegalArgumentException("No such card");
        CardContainer source = card.getParent();
        Location dest = state.getPlayerLocation(destPlayer, destRegion);
        if (dest == null) throw new IllegalStateException("No such region");
        String destMessage = "";
        if (!destPlayer.equals(player))
            destMessage = String.format(" to %s's %s", destPlayer, destRegion);
        else if (!destRegion.equals(JolGame.ASHHEAP))
            destMessage = String.format(" to %s", destRegion);

        String message = playCardMessage(player, card, destRegion, modes, destMessage);
        addCommand(message, new String[]{"move", cardId, destPlayer, destRegion, "bottom"});
        source.removeCard(card);
        dest.addCard(card, false);
    }

    String playCardMessage(String player, Card card, String destRegion, String[] modes, String destMessage) {
        String modeMessage = "";
        if (modes != null) {
            for (String mode: modes)
                modeMessage += ChatParser.generateDisciplineLink(mode);
            modeMessage = " at " + modeMessage;
        }
        return String.format(
            "%s plays %s%s%s",
            player,
            getCardLink(card),
            modeMessage,
            destMessage);
    }

    public void setOrder(List<String> players) {
        state.orderPlayers(players);
        String order = "";
        for (String player : players) order = order + " " + player;
        addCommand("Player order" + order, new String[]{"order", order});
    }

    public void shuffle(String player, String region, int num) {
        _shuffle(player, region, num, true);
    }

    private void _shuffle(String player, String region, int num, boolean log) {
        Location location = state.getPlayerLocation(player, region);
        location.shuffle(num);
        if (log) {
            String add = (num == 0) ? "" : num + " of ";
            addCommand("Shuffle " + add + player + "'s " + region, new String[]{"shuffle", player, region, num + ""});
        }
    }

    private void _moveall(String player, String srcLoc, String destPlayer, String destLoc) {
        if (destPlayer == null) destPlayer = player;
        Location src = state.getPlayerLocation(player, srcLoc);
        Location dest = state.getPlayerLocation(destPlayer, destLoc);
        Card[] cards = src.getCards();
        for (Card card : cards) {
            src.removeCard(card);
            dest.addCard(card, false);
        }
    }

    public void startGame() {
        List<String> players = state.getPlayers();
        Utils.shuffle(players);
        startGame(players);
    }

    public void startGame(List<String> playerSeating) {
        List<String> players = state.getPlayers();
        if (!players.containsAll(playerSeating)) {
            throw new IllegalArgumentException("Player ordering not valid, does not contain current players");
        }
        state.orderPlayers(playerSeating);
        addCommand("Start game", new String[]{"start"});
        newTurn();
        for (String player : players) {
            Notation note = getNote(state, player + POOL, true);
            note.setValue("30");
            _moveall(player, INACTIVE_REGION, null, CRYPT);
            _moveall(player, HAND, null, LIBRARY);
            _shuffle(player, CRYPT, 0, false);
            _shuffle(player, LIBRARY, 0, false);
            for (int j = 0; j < 4; j++)
                _drawCard(player, CRYPT, INACTIVE_REGION, false);
            for (int j = 0; j < 7; j++)
                _drawCard(player, LIBRARY, HAND, false);
        }
    }

    public Game getState() {
        return state;
    }

    public void initGame(String name) {
        state.setName(name);
        setEdge("no one");
    }

    public void sendMsg(String player, String msg) {
        msg = truncateMsg(msg);
        msg = ChatParser.sanitizeText(msg);
        msg = ChatParser.parseText(msg);
        addMessage("[" + player + "] " + msg);
    }

    private String truncateMsg(String msg) {
        if (msg.length() < 120) return msg;
        return msg.substring(0, 120);
    }

    private Notation getNote(NoteTaker nt, String name, boolean create) {
        List<Notation> notes = nt.getNotes();
        for (Notation note1 : notes) if (note1.getName().equals(name)) return note1;
        if (create) {
            return nt.addNote(name);
        }
        return null;
    }

    private void removeNote(NoteTaker nt, String name) {
        List<Notation> notes = nt.getNotes();
        notes.stream().filter(note -> note.getName().equals(name)).findFirst().ifPresent(notes::remove);
    }

    public String getCardDescripId(String cardId) {
        Card card = state.getCard(cardId);
        return card.getCardId();
    }

    public int getCounters(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, COUNTERS, false);
        if (note != null) return Integer.parseInt(note.getValue());
        return 0;
    }

    public void changeCounters(String player, String cardId, int incr) {
        if (incr == 0) return; // no change necessary - PENDING log this though?
        Card card = state.getCard(cardId);
        Notation note = getNote(card, COUNTERS, true);
        String valStr = note.getValue();
        int val = (valStr == null) ? 0 : Integer.parseInt(valStr);
        val += incr;
        note.setValue(val + "");
        String logText = String.format(
            "%s %s %s blood %s %s, now %s",
            player,
            incr < 0 ? "removes" : "adds",
            Math.abs(incr),
            incr < 0 ? "from" : "to",
            getCardName(card),
            val);
        addCommand(logText, new String[]{"counter", cardId, incr + ""});
    }

    private boolean isHiddenRegion(String region) {
        return region.equals(INACTIVE_REGION) || region.equals(HAND) || region.equals(LIBRARY) || region.equals(CRYPT) || region.equals(RESEARCH);
    }

    private String getCardName(Card card, String destRegion) {
        if (!isHiddenRegion(destRegion))
            return getCardLink(card);
        return getCardName(card);
    }

    private String getCardName(Card card) {
        Location loc = (Location) state.getRegionFromCard(card);
        if (loc == null) return card.getName();
        String region = state.getPlayerRegionName(loc);
        if (region == null) return card.getName();
        if (isHiddenRegion(region)) {
            int regionIndex = getIndexInRegion(card);
            if (regionIndex > -1) return region + " #" + (regionIndex + 1);
        }
        return getCardLink(card);
    }

    /**
     * Zero-based.
     */
    private int getIndexInRegion(Card card) {
        CardContainer container = card.getParent();
        if (container instanceof Location) {
            Location loc = (Location) container;
            Card[] cards = loc.getCards();
            for (int j = 0; j < cards.length; j++)
                if (card.getId().equals(cards[j].getId()))
                    return j;
        }
        return -1;
    }

    private String getCardLink(Card card) {
        return "<a class='card-name' data-card-id='" + card.getCardId() + "'>" + card.getName() + "</a>";
    }

    public String getActivePlayer() {
        Notation note = getNote(state, ACTIVE, false);
        if (note == null) return "";
        return note.getValue();
    }

    private void setActivePlayer(String player) {
        Notation note = getNote(state, ACTIVE, true);
        note.setValue(player);
    }

    public String getEdge() {
        Notation note = getNote(state, EDGE, false);
        if (note == null) return "";
        return note.getValue();
    }

    public void setEdge(String player) {
        String edge = (player == null) ? "no one" : player;
        Notation note = getNote(state, EDGE, true);
        String old = note.getValue();
        if (old == null) old = "";
        else old = " from " + old;
        note.setValue(edge);
        if (player == null) addCommand("Edge burned" + old + ".", new String[]{"edge", "burn"});
        else addCommand(player + " grabs edge" + old + ".", new String[]{"edge", player});
    }

    public int getPool(String player) {
        Notation note = getNote(state, player + POOL, false);
        return Integer.parseInt(note.getValue());
    }

    public void changePool(String player, int pool) {
        if (pool == 0) return; // PENDING report this in status?
        Notation note = getNote(state, player + POOL, true);
        String sval = note.getValue();
        int ival = (sval == null) ? 0 : Integer.parseInt(sval);
        int val = ival + pool;
        note.setValue(val + "");
        addCommand(player + "'s pool was " + ival + ", now is " + val + ".", new String[]{"pool", player, val + ""});
    }

    public String getGlobalText() {
        Notation note = getNote(state, TEXT, true);
        if (note.getValue() == null) note.setValue("");
        return note.getValue();
    }

    public void setGlobalText(String text) {
        Notation note = getNote(state, TEXT, true);
        note.setValue(text);
    }

    public String getPlayerText(String player) {
        Notation note = getNote(state, player + TEXT, false);
        if (note == null) return "";
        return note.getValue();
    }

    public Integer getPlayerVotes(String player) {
        Notation note = getNote(state, player + VOTES, false);
        if (note != null) {
            return Integer.valueOf(note.getValue());
        } else {
            return 0;
        }
    }

    public void setPlayerVotes(String player, Integer votes) {
        Notation note = getNote(state, player + VOTES, true);
        note.setValue(votes.toString());
    }

    public void changePlayerVotes(String player, Integer votes) {
        Notation note = getNote(state, player + VOTES, true);
        Integer current = Integer.valueOf(note.getValue());
        Integer newVotes = current + votes;
        if (newVotes < 0) {
            newVotes = 0;
        }
        setPlayerVotes(player, newVotes);
    }

    public String getText(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, TEXT, false);
        if (note != null) return note.getValue();
        return "";
    }

    public void setPlayerText(String player, String text) {
        Notation note = getNote(state, player + TEXT, true);
        note.setValue(text);
    }

    public void setText(String cardId, String text) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, TEXT, true);
        String cleanText = text.trim();
        note.setValue(cleanText);
        if (!"".equals(cleanText)) {
            addMessage(getCardName(card) + " now " + text);
        } else {
            addMessage("Removed label from " + getCardName(card));
        }
    }

    public String getVotes(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, VOTES, false);
        if (note != null) {
            return note.getValue();
        } else {
            return "0";
        }
    }

    public void setVotes(String cardId, String votes) {
        Card card = state.getCard(cardId);
        Integer voteAmount = 0;
        Notation note = getNote(card, VOTES, true);
        try {
            voteAmount = Integer.parseInt(votes);
        } catch (Exception nfe) {
            // do nothing
        }
        if (votes.trim().toLowerCase().equals("priscus")) {
            note.setValue("P");
            addMessage(getCardName(card) + " is priscus");
        } else if (voteAmount == 0) {
            removeNote(card, VOTES);
            addMessage(getCardName(card) + " now has no votes");
        } else if (voteAmount > 0) {
            note.setValue(voteAmount.toString());
            addMessage(getCardName(card) + " now has " + voteAmount + " votes");
        }
    }

    public boolean isTapped(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, TAP, false);
        return note != null && note.getValue().equals(TAPPED);
    }

    public void setTapped(String player, String cardId, boolean tapped) {
        Card card = state.getCard(cardId);
        String logtext = String.format(
            "%s %s %s",
            player,
            tapped ? "locks" : "unlocks",
            getCardName(card));
        addCommand(logtext, new String[]{"tap", cardId});
        _setTap(card, tapped);
    }

    private void _setTap(Card card, boolean tapped) {
        Notation note = getNote(card, TAP, tapped);
        if (note == null) return; // defaults to unlocked, no need to create a note
        // in this circumstance.
        String value = tapped ? TAPPED : UNTAPPED;
        note.setValue(value);
    }

    public void untapAll(String player) {
        Location[] locs = state.getPlayerLocations(player);
        addCommand(player + " unlocks.", new String[]{"unlock", player});
        for (Location loc : locs) untapAll(loc);
    }

    private void untapAll(CardContainer loc) {
        Card[] cards = loc.getCards();
        for (Card card : cards) {
            _setTap(card, false);
            untapAll(card);
        }
    }

    private String getTurn() {
        return getCurrentTurn();
    }

    public String getCurrentTurn() {
        String[] turns = getTurns();
        if (turns.length == 0) return null;
        return turns[turns.length - 1];
    }

    private void addCommand(String arg1, String[] arg2) {
        String turn = getTurn();
        if (turn != null) actions.addCommand(turn, getDate() + arg1, arg2);
    }

    private void addMessage(String arg1) {
        String turn = getTurn();
        if (turn != null) actions.addMessage(getTurn(), getDate() + arg1);
    }

    private String getDate() {
        return OffsetDateTime.now().format(SIMPLE_FORMAT);
    }

    /**
    * Return actions for the given turn.
    */
    public GameAction[] getActions(String turn) {
        return actions.getActions(turn);
    }

    /**
    * Return actions for the current turn.
    */
    public GameAction[] getActions() {
        return getActions(getCurrentTurn());
    }

    public String[] getTurns() {
        return actions.getTurns();
    }

    public void newTurn() {
        String turn = getCurrentTurn();
        List<String> players = getPlayers();
        int round = 1;
        int index = 0;
        if (turn != null) {
            String meth = actions.getMethTurn(turn);
            int length = turn.length();
            int space = turn.lastIndexOf(" ");
            round = Integer.parseInt(turn.substring(space + 1, length - 2));
            for (int i = 0; i < players.size(); i++)
                if (meth.equals(players.get(i))) index = i + 1;
            if (index == players.size()) {
                index = 0;
                round++;
            }
            while (!meth.equals(players.get(index)) && getPool(players.get(index)) < 1) {
                index++;
                if (index == players.size()) {
                    index = 0;
                    round++;
                }
            }
        }
        actions.addTurn(players.get(index), players.get(index) + " " + round + "." + (index + 1));
        setActivePlayer(players.get(index));
        setPhase(TURN_PHASES[0]);
        String turnString = round + "-" + (index +1);
        JolAdmin.getInstance().writeSnapshot(id, state, actions, turnString);
    }

    public String getPhase() {
        Notation n = getNote(state, "phase", true);
        String val = n.getValue();
        if (val == null || val.length() == 0) val = TURN_PHASES[0];
        return val;
    }

    public void setPhase(String phase) {
        Notation n = getNote(state, "phase", true);
        n.setValue(phase);
        sendMsg(getActivePlayer(), "START OF " + phase.toUpperCase() + " PHASE.");
    }

    public void changeCapacity(String cardId, int capincr) {
        Card card = state.getCard(cardId);
        Notation cap = getNote(card, "capac", true);
        String val = cap.getValue();
        int amt = 0;
        if (val == null || val.length() == 0) amt += capincr;
        else amt = Integer.parseInt(val) + capincr;
        if (amt < 0) amt = 0;
        cap.setValue(amt + "");
        addCommand("Capacity of " + getCardName(card) + " now " + amt, new String[]{"capacity", cardId, capincr + ""});
    }

    public int getCapacity(String cardId) {
        Card card = state.getCard(cardId);
        Notation cap = getNote(card, "capac", false);
        if (cap == null) return -1;
        return Integer.parseInt(cap.getValue());
    }

    public List<String> getPingedList() {
        return state.getPlayers().stream()
                .filter(player -> getPool(player) > 0)
                .filter(player -> JolAdmin.getInstance().isPlayerPinged(player, state.getName()))
                .collect(Collectors.toList());
    }

    public List<String> getPingList() {
        return state.getPlayers().stream()
                .filter(player -> getPool(player) > 0)
                .collect(Collectors.toList());
    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        if (getActivePlayer().equals(oldPlayer)) {

            setActivePlayer(newPlayer);
        }
        this.state.replacePlayer(oldPlayer, newPlayer);
        getNote(this.state, oldPlayer + POOL, false).setName(newPlayer + POOL);
    }

    public void setChoice(String player, String choice) {
        getNote(this.state, player + "-choice", true).setValue(choice);
        addMessage(player + " has made their choice");
    }

    public void getChoices() {
        addMessage("The choices have been revealed:");
        state.getPlayers().forEach(player -> {
            Notation choiceNotation = getNote(this.state, player + "-choice", true);
            if (!Strings.isNullOrEmpty(choiceNotation.getValue())) {
                addMessage(player + " chose " + choiceNotation.getValue());
                choiceNotation.setValue("");
            }
        });
    }
}
