/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package net.deckserver.dwr.model;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.game.interfaces.state.*;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.interfaces.turn.TurnRecorder;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.state.RegionType;
import net.deckserver.game.ui.state.CardDetail;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.Deck;

import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author administrator
 */
public class JolGame {

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
    private static final String CONTEST = "contested";
    private static final String DISCIPLINES = "disciplines";
    private static final String TEXT = "text";
    private static final String VOTES = "votes";
    private static final String ACTIVE = "active meth";
    private static final String POOL = "pool";
    private static final String EDGE = "edge";
    private static final String MINION = "minion";
    private static final String TAP = "tapnote";
    private static final String TAPPED = "tap";
    private static final String UNTAPPED = "untap";
    private static final String TIMEOUT = "timeout";
    private static final DecimalFormat format = new DecimalFormat("0.#");
    private static DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");
    @Getter
    private final String id;
    private DsGame state;
    private DsTurnRecorder actions;

    public JolGame(String id, DsGame state, DsTurnRecorder actions) {
        this.id = id;
        this.state = state;
        this.actions = actions;
        format.setRoundingMode(RoundingMode.DOWN);
    }

    public void addPlayer(String name, Deck deck) {
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
        Location crypt = state.getPlayerLocation(name, CRYPT);
        Location library = state.getPlayerLocation(name, LIBRARY);
        List<String> cryptlist = new ArrayList<>();
        List<String> librarylist = new ArrayList<>();
        deck.getCrypt().getCards().forEach(cardCount -> {
            cryptlist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId())));
        });
        deck.getLibrary().getCards().stream()
                .flatMap(libraryCard -> libraryCard.getCards().stream())
                .forEach(cardCount -> librarylist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        crypt.initCards(cryptlist, name);
        library.initCards(librarylist, name);
    }

    public void withdraw(String player) {
        addMessage(player + " withdraws and gains 0.5 VP");
        Notation poolNote = getNote(state, player + POOL, true);
        poolNote.setValue("0");
        Notation vpNote = getNote(state, player + " vp", true);
        String stringValue = Optional.ofNullable(vpNote.getValue()).orElse("0");
        double value = Double.parseDouble(stringValue);
        value += 0.5;
        vpNote.setValue(String.valueOf(value));
    }

    public void updateVP(String targetPlayer, double amount) {
        Notation note = getNote(state, targetPlayer + " vp", true);
        String stringValue = Optional.ofNullable(note.getValue()).orElse("0");
        double value = Double.parseDouble(stringValue);
        value += amount;
        note.setValue(String.valueOf(value));
        addMessage(targetPlayer + " has " + (amount > 0 ? "gained " : "lost ") + format.format(Math.abs(amount)) + " victory points.");
    }

    public double getVictoryPoints(String player) {
        Notation note = getNote(state, player + " vp", true);
        String stringValue = Optional.ofNullable(note.getValue()).orElse("0");
        return Double.parseDouble(stringValue);
    }


    public void timeout() {
        getPlayers().forEach(player -> {
            if (getPool(player) > 0) {
                updateVP(player, 0.5);
                zeroPool(player);
            }
        });
        addMessage("Game has timed out.  Surviving players have been awarded ½ VP.");
    }

    public String requestTimeout(String player, boolean cancel) {
        Notation note = getNote(state, TIMEOUT, true);
        String requester = Optional.ofNullable(note.getValue()).orElse(player);
        if (!requester.equals(player)) {
            timeout();
            addMessage(player + " has confirmed the game time has been reached.");
            return "Confirming game timeout.";
        }
        note.setValue(player);
        addMessage(player + " has requested that the game be timed out.");
        return "Game timeout has been requested";
    }

    public String getName() {
        return state.getName();
    }

    public List<String> getPlayers() {
        return state.getPlayers();
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

    public void shuffle(String player, String region, int num) {
        _shuffle(player, region, num, true);
    }

    public void startGame() {
        List<String> players = state.getPlayers();
        Collections.shuffle(players, new SecureRandom());
        startGame(players);
    }

    public void startGame(List<String> playerSeating) {
        List<String> players = state.getPlayers();
        if (!new HashSet<>(players).containsAll(playerSeating)) {
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
        JolAdmin.INSTANCE.pingPlayer(this.getActivePlayer(), this.getName());
    }

    public Game getState() {
        return state;
    }

    public TurnRecorder getTurnRecorder() {
        return actions;
    }

    public void initGame(String name) {
        state.setName(name);
        setEdge("no one");
    }

    public void sendMsg(String player, String msg, boolean isJudge) {
        msg = truncateMsg(msg);
        msg = ChatParser.sanitizeText(msg);
        msg = ChatParser.parseText(msg);
        // TODO - add some judge styling
        String judgePrefix = isJudge ? "(JUDGE) " : "";
        addMessage("[" + player + "] " + msg);
    }

    public int getCounters(String cardId) {
        Card card = state.getCard(cardId);
        try {
            Notation note = getNote(card, COUNTERS, false);
            return Integer.parseInt(note.getValue());
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public List<String> getDisciplines(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, DISCIPLINES, true);
        return Optional.ofNullable(note)
                .map(Notation::getValue)
                .filter(value -> !Strings.isNullOrEmpty(value))
                .map(value -> value.split(" "))
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream().sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public void changeCounters(String player, String cardId, int incr, boolean quiet) {
        if (incr == 0) return; // no change necessary - PENDING log this though?
        Card card = state.getCard(cardId);
        Notation note = getNote(card, COUNTERS, true);
        String valStr = note.getValue();
        int val = (valStr == null) ? 0 : Integer.parseInt(valStr);
        val += incr;
        note.setValue(val + "");
        if (!quiet) {
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
    }

    public boolean isVisible(String owner, String viewer, RegionType region) {
        return Objects.equals(owner, viewer) ? region.ownerVisibility() : region.otherVisibility();
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

    public int getSize(String player, RegionType region) {
        return state.getPlayerLocation(player, region.xmlLabel()).getCards().length;
    }

    public CardDetail getCard(String id) {
        Card card = state.getCard(id);
        CardDetail cardDetail = new CardDetail(card);
        List<String> cards = Arrays.stream(card.getCards())
                .map(Card::getId)
                .collect(Collectors.toList());
        cardDetail.setCards(cards);
        cardDetail.setDisciplines(getDisciplines(id));
        cardDetail.setCapacity(getCapacity(id));
        cardDetail.setCounters(getCounters(id));
        cardDetail.setLabel(getLabel(id));
        cardDetail.setVotes(getVotes(id));
        cardDetail.setLocked(isTapped(id));
        cardDetail.setContested(getContested(id));
        cardDetail.setMinion(isMinion(id));
        return cardDetail;
    }

    private boolean isMinion(String id) {
        Card card = state.getCard(id);
        Notation note = getNote(card, MINION, true);
        if (note.getValue() == null) {
            CardSummary summary = CardSearch.INSTANCE.get(card.getCardId());
            note.setValue(String.valueOf(summary.isMinion()));
        }
        return Boolean.parseBoolean(note.getValue());
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

    public String getPrivateNotes(String player) {
        Notation note = getNote(state, player + TEXT, true);
        if (note == null) return "";
        return note.getValue();
    }

    public String getLabel(String cardId) {
        Card card = state.getCard(cardId);
        try {
            Notation note = getNote(card, TEXT, false);
            return note.getValue();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    public void setPrivateNotes(String player, String text) {
        Notation note = getNote(state, player + TEXT, true);
        note.setValue(text);
    }

    public void setText(String cardId, String text, boolean quiet) {
        Card card = state.getCard(cardId);
        String oldCardName = getCardName(card);
        Notation note = getNote(card, TEXT, true);
        String cleanText = text.trim();
        note.setValue(cleanText);
        if (!quiet) {
            if (!"".equals(cleanText)) {
                addMessage(String.format("%s now \"%s\"", oldCardName, cleanText));
            } else {
                addMessage("Removed label from " + oldCardName);
            }
        }
    }

    public String getVotes(String cardId) {
        Card card = state.getCard(cardId);
        try {
            Notation note = getNote(card, VOTES, false);
            return note.getValue();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void random(String player, int limit, int result) {
        String msg = player + " rolls from 1-" + limit + " : " + result;
        addMessage(msg);
    }

    public void flip(String player, String result) {
        String msg = player + " flips a coin : " + result;
        addMessage(msg);
    }

    public void setVotes(String cardId, String votes) {
        Card card = state.getCard(cardId);
        Integer voteAmount = 0;
        Notation note = getNote(card, VOTES, true);
        assert note != null;
        try {

            voteAmount = Integer.parseInt(votes);
        } catch (Exception nfe) {
            // do nothing
        }
        if (votes.trim().equalsIgnoreCase("priscus") || votes.trim().equals("P")) {
            note.setValue("P");
            addMessage(getCardName(card) + " is priscus");
        } else if (voteAmount == 0) {
            note.setValue("0");
            addMessage(getCardName(card) + " now has no votes");
        } else if (voteAmount > 0) {
            note.setValue(voteAmount.toString());
            addMessage(getCardName(card) + " now has " + voteAmount + " votes");
        }
    }

    public void contestCard(String player, String cardId, boolean clear) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, CONTEST, true);
        assert note != null;
        if (clear) {
            note.setValue("");
            addMessage(getCardName(card) + " is no longer contested");
        } else {
            note.setValue(CONTEST);
            addMessage(getCardName(card) + " is now contested");
        }
    }

    public boolean getContested(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, CONTEST, true);
        return Optional.ofNullable(note.getValue()).orElse("").equals(CONTEST);
    }

    public boolean isTapped(String cardId) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, TAP, true);
        return Optional.ofNullable(note.getValue()).orElse(UNTAPPED).equals(TAPPED);
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

    public void untapAll(String player) {
        Location[] locs = state.getPlayerLocations(player);
        addCommand(player + " unlocks.", new String[]{"unlock", player});
        for (Location loc : locs) untapAll(loc);
    }

    public String getCurrentTurn() {
        String[] turns = getTurns();
        if (turns.length == 0) return null;
        return turns[turns.length - 1];
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
        String turnString = round + "-" + (index + 1);
        JolAdmin.INSTANCE.writeSnapshot(id, state, actions, turnString);
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
        sendMsg(getActivePlayer(), "START OF " + phase.toUpperCase() + " PHASE.", false);
    }

    public void changeCapacity(String cardId, int capincr, boolean quiet) {
        Card card = state.getCard(cardId);
        Notation cap = getNote(card, "capac", true);
        String val = cap.getValue();
        int amt = 0;
        if (val == null || val.length() == 0) amt += capincr;
        else amt = Integer.parseInt(val) + capincr;
        if (amt < 0) amt = 0;
        cap.setValue(amt + "");
        if (!quiet)
            addCommand("Capacity of " + getCardName(card) + " now " + amt, new String[]{"capacity", cardId, capincr + ""});
    }

    public void setDisciplines(String cardId, List<String> disciplines, boolean quiet) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, DISCIPLINES, true);
        note.setValue(String.join(" ", disciplines));
        if (!quiet && disciplines.size() > 0) {
            String disciplineList = disciplines.stream().map(s -> "[" + s + "]").collect(Collectors.joining(" "));
            String msg = ChatParser.parseText("Disciplines of " + getCardName(card) + " reset back to " + disciplineList);
            addCommand(msg, new String[]{"disc", cardId, disciplines.toString()});
        }
    }

    public void addDiscipline(String cardId, String discipline) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, DISCIPLINES, true);
        String currentDisciplines = Optional.ofNullable(note.getValue()).orElse("");
        List<String> disciplineList = Arrays.stream(currentDisciplines.split(" ")).collect(Collectors.toList());
        // If the discipline is not represented - add it as is
        if (!currentDisciplines.toLowerCase().contains(discipline.toLowerCase())) {
            disciplineList.add(discipline);
        } else {
            int index = disciplineList.indexOf(discipline.toLowerCase());
            discipline = discipline.toUpperCase();
            disciplineList.set(index, discipline);
        }
        note.setValue(String.join(" ", disciplineList));
        String msg = ChatParser.parseText("[" + discipline + "] added to " + getCardName(card));
        addMessage(msg);
    }

    public void removeDiscipline(String cardId, String discipline) {
        Card card = state.getCard(cardId);
        Notation note = getNote(card, DISCIPLINES, true);
        String currentDisciplines = note.getValue();
        List<String> disciplineList = Arrays.stream(currentDisciplines.split(" ")).collect(Collectors.toList());
        // exists, and equals incoming - remove it
        if (currentDisciplines.contains(discipline)) {
            disciplineList.remove(discipline);
        }
        // exists at superior, but incoming is inferior - downgrade it
        else if (currentDisciplines.toLowerCase().contains(discipline)) {
            int index = disciplineList.indexOf(discipline.toUpperCase());
            disciplineList.set(index, discipline.toLowerCase());
        }
        note.setValue(String.join(" ", disciplineList));
        String msg = ChatParser.parseText("[" + discipline + "] removed from " + getCardName(card));
        addMessage(msg);
    }

    public int getCapacity(String cardId) {
        Card card = state.getCard(cardId);
        try {
            Notation cap = getNote(card, "capac", false);
            return Integer.parseInt(cap.getValue());
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public boolean isPinged(String player) {
        return JolAdmin.INSTANCE.isPlayerPinged(player, state.getName());
    }

    public List<String> getPingList() {
        return state.getPlayers().stream()
                .filter(player -> getPool(player) > 0)
                .filter(player -> !JolAdmin.INSTANCE.isPlayerPinged(player, state.getName()))
                .collect(Collectors.toList());
    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        if (getActivePlayer().equals(oldPlayer)) {
            setActivePlayer(newPlayer);
        }
        this.state.replacePlayer(oldPlayer, newPlayer);
        getNote(this.state, oldPlayer + POOL, false).setName(newPlayer + POOL);
        Notation note = getNote(state, TIMEOUT, true);
        note.setValue("");
        addMessage("Player " + newPlayer + " replaced " + oldPlayer);
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

    private void zeroPool(String player) {
        Notation note = getNote(state, player + POOL, true);
        note.setValue(String.valueOf(0));
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
        throw new IllegalArgumentException("Note " + name + " not found");
    }

    private void removeNote(NoteTaker nt, String name) {
        List<Notation> notes = nt.getNotes();
        notes.stream().filter(note -> note.getName().equals(name)).findFirst().ifPresent(notes::remove);
    }

    private boolean isHiddenRegion(String region) {
        return region.equals(INACTIVE_REGION) || region.equals(HAND) || region.equals(LIBRARY) || region.equals(CRYPT) || region.equals(RESEARCH);
    }

    private String getCardName(Card card) {
        return getCardName(card, null, true);
    }

    private String getCardName(Card card, String destRegion) {
        return getCardName(card, destRegion, true);
    }

    private String getCardName(Card card, String destRegion, boolean differentiate) {
        Location loc = (Location) state.getRegionFromCard(card);
        if (loc == null) return card.getName();
        String region = state.getPlayerRegionName(loc);
        if (region == null) return card.getName();

        if (destRegion == null)
            destRegion = region;

        if (isHiddenRegion(region) && isHiddenRegion(destRegion)) {
            int regionIndex = getIndexInRegion(card);
            if (regionIndex > -1) return region + " #" + (regionIndex + 1);
            return "???"; //card is on another card
        }
        String differentiators = "";
        if (differentiate) {
            CardSummary cardEntry = CardSearch.INSTANCE.get(card.getCardId());
            if (!cardEntry.isUnique()) {
                int regionIndex = getIndexInRegion(card);
                String label = getLabel(card.getId());
                differentiators = String.format(
                        "%s%s",
                        regionIndex > -1 ? String.format(" #%s", regionIndex + 1) : "",
                        label.equals("") ? "" : String.format(" \"%s\"", label)
                );
            }
        }
        return getCardLink(card) + differentiators;
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

    private void _setTap(Card card, boolean tapped) {
        Notation note = getNote(card, TAP, true);
        String value = tapped ? TAPPED : UNTAPPED;
        note.setValue(value);
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

    void drawCard(String player, String srcRegion, String destRegion) {
        _drawCard(player, srcRegion, destRegion, true);
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
            CardSummary destEntry = CardSearch.INSTANCE.get(dstCard.getCardId());
            boolean includePlayer = !player.equals(destPlayer) && (destHidden || !destEntry.isUnique());
            String destMessage = String.format(
                    " on %s%s",
                    includePlayer ? String.format("%s's ", destPlayer) : "",
                    getCardName(dstCard)
            );
            message = playCardMessage(player, srcCard, destRegion, modes, destMessage);
        } else {
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

    void burnQuietly(String cardId) {
        Card card = state.getCard(cardId);
        if (card == null) throw new IllegalArgumentException("No such card");

        //Burn attached cards
        for (Card c : card.getCards())
            burnQuietly(c.getId());

        CardContainer source = card.getParent();
        String owner = card.getOwner();
        Location dest = state.getPlayerLocation(owner, ASHHEAP);

        //Move to owner's ash heap
        source.removeCard(card);
        dest.addCard(card, false);

        //Clear label
        setText(cardId, "", true);

        //Clear capacity
        int capacity = getCapacity(cardId);
        if (capacity > 0) //-1 means does not have capacity
            changeCapacity(cardId, -capacity, true);

        //Clear blood/life counters
        int blood = getCounters(cardId);
        if (blood > 0)
            changeCounters(null, cardId, -blood, true);

        //Unlock
        _setTap(card, false);
    }

    void burn(String player, String cardId, String srcPlayer, String srcRegion, boolean top) {
        Card card = state.getCard(cardId);
        if (card == null) throw new IllegalArgumentException("No such card");

        String owner = card.getOwner();
        if (owner == null || owner.isEmpty())
            throw new IllegalArgumentException("Game too old for burn command");

        //Message formats:
        //Target is public: "<player> burns <card> [#<region-index>] from [<player>'s] <region>"
        //Target is private: "<player> burns <card> from [top of] [<player>'s] <region>"

        boolean showRegionOwner = !player.equals(srcPlayer);
        String message = String.format(
                "%s burns %s from %s%s%s",
                player,
                getCardName(card, ASHHEAP, true),
                top ? "top of " : "",
                showRegionOwner ? srcPlayer + "'s " : "",
                srcRegion);

        addCommand(message, new String[]{"move", cardId, owner, ASHHEAP, "bottom"});
        burnQuietly(cardId);
    }

    String playCardMessage(String player, Card card, String destRegion, String[] modes, String destMessage) {
        String modeMessage = "";
        if (modes != null) {
            for (String mode : modes)
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
}
