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
import net.deckserver.game.storage.cards.Clan;
import net.deckserver.game.storage.cards.Path;
import net.deckserver.game.storage.cards.Sect;
import net.deckserver.game.storage.state.RegionType;
import net.deckserver.game.ui.state.CardDetail;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.Deck;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JolGame {

    public static final String[] TURN_PHASES = new String[]{"Unlock", "Master", "Minion", "Influence", "Discard"};

    private static final String COUNTERS = "counters";
    private static final String CONTEST = "contested";
    private static final String DISCIPLINES = "disciplines";
    private static final String TEXT = "text";
    private static final String VOTES = "votes";
    private static final String SECT = "sect";
    private static final String PATH = "path";
    private static final String CLAN = "clan";
    private static final String ACTIVE = "active meth";
    private static final String POOL = "pool";
    private static final String EDGE = "edge";
    private static final String MINION = "minion";
    private static final String MERGED = "merged";
    private static final String TAP = "tapnote";
    private static final String TAPPED = "tap";
    private static final String PHASE = "phase";
    private static final String CAPACITY = "capac";
    private static final String CHOICE = "-choice";
    private static final String UNTAPPED = "untap";
    private static final String TIMEOUT = "timeout";
    private static final String VP = " vp";
    private static final DecimalFormat format = new DecimalFormat("0.#");
    private static final DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");
    private static final Comparator<String> DISC_COMPARATOR = Comparator.comparing(s -> Character.isLowerCase(s.charAt(0)) ? 0 : 1);
    @Getter
    private final String id;
    private final DsGame state;
    private final DsTurnRecorder actions;

    public JolGame(String id, DsGame state, DsTurnRecorder actions) {
        this.id = id;
        this.state = state;
        this.actions = actions;
        format.setRoundingMode(RoundingMode.DOWN);
    }

    public void addPlayer(String name, Deck deck) {
        state.addPlayer(name);
        state.addLocation(name, RegionType.READY);
        state.addLocation(name, RegionType.TORPOR);
        state.addLocation(name, RegionType.UNCONTROLLED);
        state.addLocation(name, RegionType.HAND);
        state.addLocation(name, RegionType.ASH_HEAP);
        state.addLocation(name, RegionType.LIBRARY);
        state.addLocation(name, RegionType.CRYPT);
        state.addLocation(name, RegionType.REMOVED_FROM_GAME);
        state.addLocation(name, RegionType.RESEARCH);
        changePool(name, 30);
        Location crypt = state.getPlayerLocation(name, RegionType.CRYPT);
        Location library = state.getPlayerLocation(name, RegionType.LIBRARY);
        List<String> cryptlist = new ArrayList<>();
        List<String> librarylist = new ArrayList<>();
        deck.getCrypt().getCards().forEach(cardCount -> cryptlist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        deck.getLibrary().getCards().stream()
                .flatMap(libraryCard -> libraryCard.getCards().stream())
                .forEach(cardCount -> librarylist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        crypt.initCards(cryptlist, name);
        library.initCards(librarylist, name);
    }

    public void withdraw(String player) {
        setNotation(state, player + POOL, "0");
        updateVP(player, 0.5);
        addMessage(player + " withdraws.");
    }

    public void updateVP(String targetPlayer, double amount) {
        double value = getNotationAsDouble(state, targetPlayer + VP);
        value += amount;
        setNotation(state, targetPlayer + VP, String.valueOf(value));
        addMessage(targetPlayer + " has " + (amount > 0 ? "gained " : "lost ") + format.format(Math.abs(amount)) + " victory points.");
    }

    public double getVictoryPoints(String player) {
        return getNotationAsDouble(state, player + VP);
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

    public void requestTimeout(String player) {
        String requester = getNotation(state, TIMEOUT, player);
        if (!requester.equals(player)) {
            addMessage(player + " has confirmed the game time has been reached.");
            timeout();
        } else {
            addMessage(player + " has requested that the game be timed out.");
            setNotation(state, TIMEOUT, player);
        }
    }

    public String getName() {
        return state.getName();
    }

    public List<String> getPlayers() {
        return state.getPlayers();
    }

    public void discard(String player, Card card, boolean random) {
        CardContainer source = card.getParent();
        Location dest = state.getPlayerLocation(player, RegionType.ASH_HEAP);
        String message = String.format(
                "%s discards %s%s",
                player,
                getCardLink(card),
                random ? " (picked randomly)" : "");
        addCommand(message, new String[]{"discard", card.getId(), player, RegionType.ASH_HEAP.xmlLabel()});
        source.removeCard(card);
        dest.addCard(card, false);
    }

    public void playCard(String player, Card card, String destinationPlayer, RegionType destinationRegion, Card target, String[] modes) {
        CardContainer parent = card.getParent();
        Location source = (Location) state.getRegionFromCard(card);
        CardContainer destination = state.getPlayerLocation(destinationPlayer, destinationRegion);

        // Message structure is as follows:
        // [Player] plays [card] [sourceRegion] [mode] [target]
        String cardName = getCardLink(card);
        String sourceMessage = RegionType.HAND.equals(RegionType.of(source.getName())) ? "" : " from their " + source.getName();

        // Build mode details
        StringBuilder modeMessage = new StringBuilder();
        if (modes != null) {
            for (String mode : modes)
                modeMessage.append(ChatParser.generateDisciplineLink(mode));
            modeMessage.insert(0, " at ");
        }

        // Build a destination string
        // to [targetCard] in [destinationPlayer | their] [destinationRegion]
        String destinationMessage;
        if (target == null) {
            String playerTitle = destinationPlayer.equals(player) ? "their" : destinationPlayer + "'s";
            destinationMessage = RegionType.ASH_HEAP.equals(destinationRegion) ? "" : String.format(" to %s %s", playerTitle, destinationRegion.xmlLabel());
        } else {
            destination = target;
            destinationMessage = String.format(" on %s", getTargetCardName(target, player));
        }

        String message = String.format("%s plays %s%s%s%s.", player, cardName, sourceMessage, modeMessage, destinationMessage);

        addCommand(message, new String[]{"play", card.getId(), destinationPlayer, destinationRegion.xmlLabel()});
        parent.removeCard(card);
        destination.addCard(card, false);
    }

    public void influenceCard(String player, Card card, String destPlayer, RegionType destRegion) {
        CardContainer source = card.getParent();
        Location dest = state.getPlayerLocation(destPlayer, destRegion);

        Location loc = (Location) state.getRegionFromCard(card);

        CardDetail detail = getDetail(card);
        CardSummary cardSummary = CardSearch.INSTANCE.get(detail.getCardId());
        Integer capacity = cardSummary.getCapacity();
        String capacityText = "";
        if (capacity != null && getCapacity(card) <= 0) {
            changeCapacity(card, capacity, true);
            capacityText = ", capacity: " + capacity;
        }
        // Do disciplines
        List<String> disciplines = cardSummary.getDisciplines();
        setDisciplines(player, card, disciplines, true);
        // Do votes
        String votes = cardSummary.getVotes();
        String votesText = "";
        if (!Strings.isNullOrEmpty(votes)) {
            setVotes(card, votes, true);
            votesText = ", votes: " + votes;
        }
        // Do sect
        Sect sect = Sect.of(cardSummary.getSect());
        setSect(player, card, sect, true);
        // Do path
        Path path = Path.of(cardSummary.getPath());
        if (path != null) {
            setPath(player, card, path, true);
        }
        // Do clan
        Clan clan = Clan.of(cardSummary.getClans().getFirst());
        setClan(player, card, clan, true);
        source.removeCard(card);
        dest.addCard(card, true);
        addCommand(String.format("%s influences out %s%s%s.", player, getCardLink(card), capacityText, votesText), new String[]{"influence", card.getId(), destPlayer, destRegion.xmlLabel()});
    }

    public void setSect(String player, Card card, Sect sect, boolean quiet) {
        setNotation(card, SECT, sect.toString());
        if (!quiet) {
            addCommand(String.format("%s changes sect of %s to %s", player, getCardLink(card), sect.getDescription()), new String[]{"sect", card.getId(), sect.toString()});
        }
    }

    public void setPath(String player, Card card, Path path, boolean quiet) {
        setNotation(card, PATH, path.toString());
        if (!quiet) {
            addCommand(String.format("%s changes path of %s to %s", player, getCardLink(card), path.getDescription()), new String[]{"path", card.getId(), path.getDescription()});
        }
    }

    public void setClan(String player, Card targetCard, Clan clan, boolean quiet) {
        setNotation(targetCard, CLAN, clan.getDescription());
        if (!quiet) {
            addCommand(String.format("%s changes clan of %s to %s", player, getCardLink(targetCard), clan.getDescription()), new String[]{"path", targetCard.getId(), clan.getDescription()});
        }
    }

    public void clearPath(String player, Card card) {
        setNotation(card, PATH, "");
        addCommand(String.format("%s clears path from %s", player, getCardLink(card)), new String[]{"path", card.getId()});
    }

    public void clearSect(String player, Card card) {
        setNotation(card, SECT, "");
        addCommand(String.format("%s clears sect from %s", player, getCardLink(card)), new String[]{"sect", card.getId()});
    }

    public void shuffle(String player, RegionType region, int num) {
        _shuffle(player, region, num, true);
    }

    public void startGame(List<String> playerSeating) {
        List<String> players = state.getPlayers();
        if (!new HashSet<>(players).containsAll(playerSeating)) {
            throw new IllegalArgumentException("Player ordering not valid, does not contain current players");
        }
        state.orderPlayers(playerSeating);
        addCommand("Start game", new String[]{"start"});
        for (String player : players) {
            setNotation(state, player + POOL, "30");
            moveAll(player, RegionType.UNCONTROLLED, RegionType.CRYPT);
            moveAll(player, RegionType.HAND, RegionType.LIBRARY);
            _shuffle(player, RegionType.CRYPT, 0, false);
            _shuffle(player, RegionType.LIBRARY, 0, false);
            for (int j = 0; j < 4; j++)
                _drawCard(player, RegionType.CRYPT, RegionType.UNCONTROLLED, false);
            for (int j = 0; j < 7; j++)
                _drawCard(player, RegionType.LIBRARY, RegionType.HAND, false);
        }
        newTurn();
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
        setNotation(state, EDGE, "no one");
    }

    public void sendMsg(String player, String msg, boolean isJudge) {
        msg = ChatParser.sanitizeText(msg);
        msg = ChatParser.parseText(msg);
        String playerName = player;
        if (isJudge) {
            playerName = "< " + playerName + " >";
        }
        addMessage("[" + playerName + "] " + msg);
    }

    public int getCounters(Card card) {
        try {
            return getNotationAsInt(card, COUNTERS);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public List<String> getDisciplines(Card card) {
        String discString = getNotation(card, DISCIPLINES, "");
        return Strings.isNullOrEmpty(discString) ? Collections.emptyList() : Arrays.asList(discString.split(" "));
    }

    public void transfer(String player, Card card, int amount) {
        int counters = getCounters(card);
        int pool = getPool(player);
        int newCounters = counters + amount;
        int newPool = pool - amount;
        setNotation(state, player + POOL, String.valueOf(newPool));
        setNotation(card, COUNTERS, String.valueOf(newCounters));
        String direction = amount > 0 ? "onto" : "off";
        String message = String.format("%s transferred %d blood %s %s. Currently: %d, Pool: %d", player, Math.abs(amount), direction, getCardName(card), newCounters, newPool);
        addCommand(message, new String[]{"transfer", card.getId(), String.valueOf(amount)});
    }

    public void changeCounters(String player, Card card, int incr, boolean quiet) {
        if (incr == 0) return; // no change necessary - PENDING log this though?
        int current = getCounters(card);
        current += incr;
        setNotation(card, COUNTERS, String.valueOf(current));
        if (!quiet) {
            String logText = String.format(
                    "%s %s %s blood %s %s, now %s. ",
                    player,
                    incr < 0 ? "removes" : "adds",
                    Math.abs(incr),
                    incr < 0 ? "from" : "to",
                    getCardName(card),
                    current);
            addCommand(logText, new String[]{"counter", card.getId(), incr + ""});
        }
    }

    public boolean isVisible(String owner, String viewer, RegionType region) {
        return Objects.equals(owner, viewer) ? region.ownerVisibility() : region.otherVisibility();
    }

    public String getActivePlayer() {
        return getNotation(state, ACTIVE, "");
    }



    private void setActivePlayer(String player) {
        setNotation(state, ACTIVE, player);
    }

    public void show(String player, RegionType targetRegion, int amount, List<String> recipients) {
        Location location = getState().getPlayerLocation(player, targetRegion);
        Card[] cards = location.getCards();
        int size = Math.min(cards.length, amount);
        StringBuilder notes = new StringBuilder();
        String message = String.format("%s shows %d cards in their %s to %s.", player, size, targetRegion.xmlLabel(), recipients);
        notes.append(message).append("<br/>");
        notes.append("<ol class='list-group list-group-flush list-group-numbered'>");
        for (int i = 0; i < size; i++) {
            Card card = cards[i];
            if (card != null) {
                String entry = String.format("<li class='list-group-item'>%s</li><br/>", getCardLink(card));
                notes.append(entry);
            }
        }
        notes.append("</ol>");
        for (String recipient : recipients) {
            setRegionNotes(location, recipient, notes.toString());
        }
        addMessage(message);
    }

    public String getPredatorOf(String player) {
        List<String> validPlayers = getValidPlayers();
        int playerPosition = validPlayers.indexOf(player);
        if (playerPosition == -1) return "";
        int predatorIndex = playerPosition - 1;
        if (predatorIndex < 0) predatorIndex = validPlayers.size() - 1;
        return validPlayers.get(predatorIndex);
    }

    public String getPreyOf(String player) {
        List<String> validPlayers = getValidPlayers();
        int playerPosition = validPlayers.indexOf(player);
        if (playerPosition == -1) return "";
        int predatorIndex = playerPosition + 1;
        if (predatorIndex > validPlayers.size() - 1) predatorIndex = 0;
        return validPlayers.get(predatorIndex);
    }

    public int getSize(String player, RegionType region) {
        return state.getPlayerLocation(player, region).getCards().length;
    }

    public CardDetail getDetail(Card card) {
        CardDetail cardDetail = new CardDetail(card);
        List<String> cards = Arrays.stream(card.getCards())
                .map(Card::getId)
                .collect(Collectors.toList());
        cardDetail.setCards(cards);
        cardDetail.setDisciplines(getDisciplines(card));
        cardDetail.setCapacity(getCapacity(card));
        cardDetail.setCounters(getCounters(card));
        cardDetail.setLabel(getLabel(card));
        cardDetail.setVotes(getVotes(card));
        cardDetail.setLocked(isTapped(card));
        cardDetail.setContested(getContested(card));
        cardDetail.setMinion(isMinion(card));
        cardDetail.setMerged(isMerged(card));
        cardDetail.setPath(getPath(card));
        cardDetail.setSect(getSect(card));
        cardDetail.setClan(getClan(card));
        return cardDetail;
    }

    public String getEdge() {
        return getNotation(state, EDGE, "");
    }

    public void setEdge(String player) {
        addCommand(String.format("%s gains the edge from %s.", player, getEdge()), new String[]{"edge", player});
        setNotation(state, EDGE, player);
    }

    public void burnEdge(String player) {
        setNotation(state, EDGE, "no one");
        addCommand(String.format("%s burns the edge.", player), new String[]{"edge", "burn"});
    }

    public int getPool(String player) {
        return getNotationAsInt(state, player + POOL);
    }

    public void changePool(String player, int amount) {
        if (amount == 0) return; // PENDING report this in status?
        int starting = getPool(player);
        int ending = starting + amount;
        setNotation(state, player + POOL, String.valueOf(ending));
        addCommand(player + "'s pool was " + starting + ", now is " + ending + ".", new String[]{"pool", player, amount + ""});
    }

    public String getGlobalText() {
        return getNotation(state, TEXT, "");
    }

    public void setGlobalText(String text) {
        setNotation(state, TEXT, text);
    }

    public String getPrivateNotes(String player) {
        return getNotation(state, player + TEXT, "");
    }

    public void setPrivateNotes(String player, String text) {
        setNotation(state, player + TEXT, text);
    }

    public String getRegionNotes(String player, RegionType region, String viewer) {
        Location location = state.getPlayerLocation(player, region);
        return getNotation(location, viewer + TEXT, "");
    }

    public void setRegionNotes(Location location, String player, String text) {
        setNotation(location, player + TEXT, text);
    }

    public String getLabel(Card card) {
        return getNotation(card, TEXT, "");
    }

    public String getPath(Card card) {
        return getNotation(card, PATH, "");
    }

    public String getSect(Card card) {
        return getNotation(card, SECT, "");
    }

    public String getClan(Card card) {
        return getNotation(card, CLAN, "");
    }

    public void setLabel(Card card, String text, boolean quiet) {
        String cardName = getCardName(card);
        String cleanText = text.trim();
        setNotation(card, TEXT, cleanText);
        if (!quiet) {
            if (!cleanText.isEmpty()) {
                addMessage(String.format("%s now \"%s\"", cardName, cleanText));
            } else {
                addMessage("Removed label from " + cardName);
            }
        }
    }

    public String getVotes(Card card) {
        return getNotation(card, VOTES, "");
    }

    public void random(String player, int limit, int result) {
        addMessage(player + " rolls from 1-" + limit + " : " + result);
    }

    public void flip(String player, String result) {
        addMessage(player + " flips a coin : " + result);
    }

    public void setVotes(Card card, String votes, boolean quiet) {
        int voteAmount = 0;
        try {
            voteAmount = Integer.parseInt(votes);
        } catch (Exception nfe) {
            // do nothing
        }
        String message;
        if (votes.trim().equalsIgnoreCase("priscus") || votes.trim().equals("P")) {
            setNotation(card, VOTES, "P");
            message = getCardName(card) + " is priscus.";
        } else if (voteAmount == 0) {
            setNotation(card, VOTES, "0");
            message = getCardName(card) + " now has no votes.";
        } else {
            setNotation(card, VOTES, String.valueOf(voteAmount));
            message = getCardName(card) + " now has " + voteAmount + " votes.";
        }
        if (!quiet) {
            addMessage(message);
        }
    }

    public void contestCard(Card card, boolean clear) {
        if (clear) {
            setNotation(card, CONTEST, "");
            addMessage(getCardName(card) + " is no longer contested.");
        } else {
            setNotation(card, CONTEST, CONTEST);
            addMessage(getCardName(card) + " is now contested.");
        }
    }

    public boolean getContested(Card card) {
        return getNotation(card, CONTEST, "").equals(CONTEST);
    }

    public boolean isTapped(Card card) {
        return getNotation(card, TAP, UNTAPPED).equals(TAPPED);
    }

    public void setLocked(String player, Card card, boolean locked) {
        String message = String.format("%s %s %s.", player, locked ? "locks" : "unlocks", getCardName(card));
        addCommand(message, new String[]{"tap", card.getId()});
        setNotation(card, TAP, locked ? TAPPED : UNTAPPED);
    }

    public void unlockAll(String player) {
        Location[] locs = state.getPlayerLocations(player);
        addCommand(player + " unlocks.", new String[]{"untap", player});
        for (Location loc : locs) unlockAll(loc);
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
        JolAdmin.INSTANCE.pingPlayer(getActivePlayer(), getName());
    }

    public String getPhase() {
        String current = getNotation(state, PHASE, "");
        return Strings.isNullOrEmpty(current) ? TURN_PHASES[0] : current;
    }

    public void setPhase(String phase) {
        setNotation(state, PHASE, phase);
        sendMsg(getActivePlayer(), "START OF " + phase.toUpperCase() + " PHASE.", false);
    }

    public void changeCapacity(Card card, int change, boolean quiet) {
        int currentCapacity = getNotationAsInt(card, CAPACITY);
        int newCapacity = currentCapacity + change;
        if (newCapacity < 0) newCapacity = 0;
        setNotation(card, CAPACITY, String.valueOf(newCapacity));
        if (!quiet)
            addCommand("Capacity of " + getCardName(card) + " now " + newCapacity, new String[]{"capacity", card.getId(), change + ""});
    }

    public void setDisciplines(String player, Card card, List<String> disciplines, boolean quiet) {
        setNotation(card, DISCIPLINES, String.join(" ", disciplines));
        if (!quiet && !disciplines.isEmpty()) {
            String disciplineList = disciplines.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" "));
            String msg = ChatParser.parseText(player + " reset " + getCardName(card) + " back to " + disciplineList);
            addCommand(msg, new String[]{"disc", card.getId(), disciplines.toString()});
        }
    }

    public void setDisciplines(String player, Card card, Set<String> additions, Set<String> removals) throws CommandException {
        List<String> currentDisciplines = getDisciplines(card);
        List<String> newDisciplines = new ArrayList<>(currentDisciplines);
        List<String> discAdded = new ArrayList<>();
        List<String> discRemoved = new ArrayList<>();
        additions.forEach(disc -> {
            String disciplineString = String.join(" ", newDisciplines);
            if (!disciplineString.toLowerCase().contains(disc.toLowerCase())) {
                newDisciplines.add(disc);
            } else {
                int index = newDisciplines.indexOf(disc.toLowerCase());
                disc = disc.toUpperCase();
                newDisciplines.set(index, disc);
            }
            discAdded.add(disc);
        });
        removals.forEach(disc -> {
            String disciplineString = String.join(" ", newDisciplines);
            if (newDisciplines.contains(disc)) {
                newDisciplines.remove(disc);
                discRemoved.add(disc);
            } else if (disciplineString.toLowerCase().contains(disc)) {
                int index = newDisciplines.indexOf(disc.toUpperCase());
                newDisciplines.set(index, disc.toLowerCase());
                discRemoved.add(disc);
            }
        });
        newDisciplines.sort(DISC_COMPARATOR.thenComparing(Comparator.naturalOrder()));
        discAdded.sort(DISC_COMPARATOR.thenComparing(Comparator.naturalOrder()));
        discRemoved.sort(DISC_COMPARATOR.thenComparing(Comparator.naturalOrder()));
        if (!discAdded.isEmpty() || !discRemoved.isEmpty()) {
            String additionString = discAdded.isEmpty() ? "" : "added " + ChatParser.parseText(discAdded.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" ")));
            String removalsString = discRemoved.isEmpty() ? "" : "removed " + ChatParser.parseText(discRemoved.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" ")));
            addMessage(String.format("%s %s%s to %s.", player, additionString, removalsString, getCardName(card)));
            setNotation(card, DISCIPLINES, String.join(" ", newDisciplines));
        } else {
            throw new CommandException("No valid disciplines chosen.");
        }
    }

    public int getCapacity(Card card) {
        return getNotationAsInt(card, CAPACITY);
    }

    public boolean isPinged(String player) {
        return JolAdmin.INSTANCE.isPlayerPinged(player, state.getName());
    }

    public List<String> getPingList() {
        return getValidPlayers()
                .stream()
                .filter(player -> !JolAdmin.INSTANCE.isPlayerPinged(player, state.getName()))
                .collect(Collectors.toList());
    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        if (getActivePlayer().equals(oldPlayer)) {
            setActivePlayer(newPlayer);
        }
        this.state.replacePlayer(oldPlayer, newPlayer);
        getNote(this.state, oldPlayer + POOL).setName(newPlayer + POOL);
        getNote(this.state, oldPlayer + VP).setName(newPlayer + VP);
        getNote(this.state, oldPlayer + TEXT).setName(newPlayer + TEXT);
        setNotation(state, TIMEOUT, "");
        addMessage("Player " + newPlayer + " replaced " + oldPlayer);
    }

    public void setChoice(String player, String choice) {
        setNotation(state, player + CHOICE, choice);
        addMessage(player + " has made their choice.");
    }

    public void getChoices() {
        addMessage("The choices have been revealed:");
        state.getPlayers().forEach(player -> {
            String choice = getNotation(state, player + CHOICE, "");
            if (!Strings.isNullOrEmpty(choice)) {
                addMessage(player + " chose " + choice);
                setNotation(state, player + CHOICE, "");
            }
        });
    }

    public void setOrder(List<String> players) {
        state.orderPlayers(players);
        StringBuilder order = new StringBuilder();
        for (String player : players) order.append(" ").append(player);
        addCommand("Player order" + order, new String[]{"order", order.toString()});
    }

    public Card getCard(String id) {
        return state.getCard(id);
    }

    private List<String> getValidPlayers() {
        return state.getPlayers().stream()
                .filter(player -> getPool(player) > 0)
                .collect(Collectors.toList());
    }

    private boolean isMinion(Card card) {
        String current = getNotation(card, MINION, null);
        if (current == null) {
            CardSummary summary = CardSearch.INSTANCE.get(card.getCardId());
            current = String.valueOf(summary.isMinion());
            setNotation(card, MINION, current);
        }
        return Boolean.parseBoolean(current);
    }

    private boolean isMerged(Card card) {
        return getNotation(card, MERGED, "false").equals("true");
    }

    private void zeroPool(String player) {
        setNotation(state, player + POOL, "0");
    }

    private void _drawCard(String player, RegionType srcRegion, RegionType destRegion, boolean log) {
        Location source = state.getPlayerLocation(player, srcRegion);
        Location dest = state.getPlayerLocation(player, destRegion);
        Card card = source.getFirstCard();
        source.removeCard(card);
        dest.addCard(card, false);
        if (log) {
            addCommand(String.format("%s draws from their %s.", player, srcRegion.xmlLabel()), new String[]{"draw", srcRegion.xmlLabel(), destRegion.xmlLabel()});
        }
    }

    private void _shuffle(String player, RegionType region, int num, boolean log) {
        Location location = state.getPlayerLocation(player, region);
        int size = location.getCards().length;
        location.shuffle(num);
        if (log) {
            String add = (num == 0 || num >= size) ? "their" : "the first " + num + " cards of their";
            addCommand(String.format("%s shuffles %s %s.", player, add, region.xmlLabel()), new String[]{"shuffle", player, region.xmlLabel(), num + ""});
        }
    }

    private void moveAll(String player, RegionType srcLoc, RegionType destLoc) {
        Location src = state.getPlayerLocation(player, srcLoc);
        Location dest = state.getPlayerLocation(player, destLoc);
        Card[] cards = src.getCards();
        for (Card card : cards) {
            src.removeCard(card);
            dest.addCard(card, false);
        }
    }

    private Notation getNote(NoteTaker nt, String name) {
        List<Notation> notes = nt.getNotes();
        for (Notation note1 : notes) if (note1.getName().equals(name)) return note1;
        return nt.addNote(name);
    }

    private void setNotation(NoteTaker nt, String name, String value) {
        Notation note = getNote(nt, name);
        note.setValue(value);
    }

    private String getNotation(NoteTaker nt, String name, String defaultValue) {
        return nt.getNotes()
                .stream()
                .filter(notation -> notation.getName().equals(name))
                .findFirst()
                .map(Notation::getValue)
                .orElse(defaultValue);
    }

    private int getNotationAsInt(NoteTaker nt, String name) {
        return Integer.parseInt(getNotation(nt, name, "0"));
    }

    private double getNotationAsDouble(NoteTaker nt, String name) {
        return Double.parseDouble(getNotation(nt, name, "0"));
    }

    /*
        Used for getting a card name in a region, from the view of the player
     */
    private String getTargetCardName(Card card, String player) {
        Location cardLocation = (Location) state.getRegionFromCard(card);
        RegionType cardRegion = RegionType.of(cardLocation.getName());

        boolean sameOwner = card.getOwner().equals(player);
        String cardName;
        if (RegionType.OTHER_HIDDEN_REGIONS.contains(cardRegion)) {
            String coordinates = getIndexCoordinates(card);
            cardName = "card #" + coordinates;
        } else {
            cardName = getCardName(card);
        }
        String playerName = sameOwner ? "their" : card.getOwner() + "'s";
        return String.format("%s in %s %s", cardName, playerName, cardLocation.getName());
    }

    private String getCardName(Card card) {
        return getCardName(card, (Location) state.getRegionFromCard(card));
    }

    /*
    Build up a card name for the card, if the card is going into a different location, check that location to see if the card name can be exposed first.
    Card name should be hidden if both regions are hidden, and the card is owned by the same person.
     */
    private String getCardName(Card card, Location destinationLocation) {
        Location sourceLocation = (Location) state.getRegionFromCard(card);
        RegionType sourceRegion = RegionType.of(sourceLocation.getName());
        RegionType destinationRegion = RegionType.of(destinationLocation.getName());

        // sourceRegion should always be valid
        assert sourceRegion != null : "Source region is null";

        if (destinationRegion == null)
            destinationRegion = sourceRegion;

        String cardOwner = card.getOwner();
        String sourceOwner = sourceLocation.getOwner();
        String destinationOwner = destinationLocation.getOwner();

        boolean sameOwner = Stream.of(sourceOwner, destinationOwner).allMatch(c -> c.equals(cardOwner));

        // If the card is moving between hidden regions, and the card and regions are owned by the same player, print the index #
        if (RegionType.OTHER_HIDDEN_REGIONS.containsAll(List.of(sourceRegion, destinationRegion)) && sameOwner) {
            String coordinates = getIndexCoordinates(card);
            return String.format("card #%s in their %s", coordinates, sourceLocation.getName());
        }

        // if the card is not unique, then add some flavor to the name to help identify it better
        String differentiators = "";
        CardSummary cardEntry = CardSearch.INSTANCE.get(card.getCardId());
        if (!cardEntry.isUnique()) {
            differentiators = getDifferentiators(card);
        }
        return getCardLink(card) + differentiators;
    }

    private String getDifferentiators(Card card) {
        String coordinates = getIndexCoordinates(card);
        String label = getLabel(card);
        label = label.isEmpty() ? "" : String.format(" \"%s\"", label);
        return String.format(" %s%s", coordinates, label);
    }

    private String getIndexCoordinates(Card card) {
        List<String> coordinates = new ArrayList<>();
        String id = card.getId();
        CardContainer parent = card.getParent();
        boolean looking = true;
        while (looking) {
            Card[] cards = parent.getCards();
            for (int i = 0; i < cards.length; i++) {
                if (id.equals(cards[i].getId())) {
                    coordinates.add(String.valueOf(i + 1));
                    break;
                }
            }
            if (parent instanceof Card c) {
                id = c.getId();
                parent = c.getParent();
            } else {
                looking = false;
            }
        }
        return String.join(".", coordinates.reversed());
    }

    private String getCardLink(Card card) {
        return "<a class='card-name' data-card-id='" + card.getCardId() + "'>" + card.getName() + "</a>";
    }

    private void unlockAll(CardContainer loc) {
        Card[] cards = loc.getCards();
        for (Card card : cards) {
            setNotation(card, TAP, "false");
            unlockAll(card);
        }
    }

    private String getTurn() {
        return getCurrentTurn();
    }

    private void addCommand(String arg1, String[] arg2) {
        String turn = getTurn();
        if (turn != null) actions.addCommand(turn, getDate() + arg1, arg2);
    }

    private String getDate() {
        return OffsetDateTime.now().format(SIMPLE_FORMAT);
    }

    void addMessage(String message) {
        String turn = getTurn();
        if (turn != null) actions.addMessage(getTurn(), getDate() + message);
    }

    void drawCard(String player, RegionType srcRegion, RegionType destRegion) {
        _drawCard(player, srcRegion, destRegion, true);
    }

    void moveToCard(String player, Card srcCard, Card dstCard) throws CommandException {
        if (srcCard.equals(dstCard)) throw new CommandException("Can't move a card to itself");
        CardContainer parentContainer = dstCard.getParent();
        while (true) {
            if (parentContainer instanceof Card parentCard) {
                if (parentCard.getId().equals(srcCard.getId()))
                    throw new CommandException("Can't create card loop");
                parentContainer = parentCard.getParent();
            } else {
                break;
            }
        }
        CardContainer source = srcCard.getParent();
        Location destinationRegion = (Location) state.getRegionFromCard(dstCard);

        String message = String.format("%s puts %s on %s.", player, getCardName(srcCard, destinationRegion), getTargetCardName(dstCard, player));
        addCommand(message, new String[]{"move", srcCard.getId(), dstCard.getId()});

        source.removeCard(srcCard);
        dstCard.addCard(srcCard, false);
    }

    void moveToRegion(String player, Card card, String destPlayer, RegionType destRegion, boolean top) {
        CardContainer source = card.getParent();
        Location sourceRegion = (Location) state.getRegionFromCard(card);
        Location destinationRegion = state.getPlayerLocation(destPlayer, destRegion);

        boolean sameOwner = Stream.of(sourceRegion.getOwner(), destinationRegion.getOwner()).allMatch(c -> c.equals(player));
        String topMessage = top ? "the top of " : "";
        String playerName = sameOwner ? "their" : destinationRegion.getOwner() + "'s";

        String message = String.format("%s moves %s to %s%s %s.", player, getCardName(card, destinationRegion), topMessage, playerName, destRegion.xmlLabel());
        addCommand(message, new String[]{"move", card.getId(), destPlayer, destRegion.xmlLabel(), top ? "top" : "bottom"});
        source.removeCard(card);
        destinationRegion.addCard(card, top);
    }

    void burnQuietly(Card card) {
        if (card == null) throw new IllegalArgumentException("No such card");

        //Burn attached cards
        for (Card c : card.getCards())
            burnQuietly(c);

        CardContainer source = card.getParent();
        String owner = card.getOwner();
        Location dest = state.getPlayerLocation(owner, RegionType.ASH_HEAP);

        //Move to owner's ash heap
        source.removeCard(card);
        dest.addCard(card, false);

        //Clear label
        setLabel(card, "", true);

        //Clear capacity
        int capacity = getCapacity(card);
        if (capacity > 0) //-1 means does not have capacity
            changeCapacity(card, -capacity, true);

        //Clear blood/life counters
        int blood = getCounters(card);
        if (blood > 0)
            changeCounters(null, card, -blood, true);

        //Unlock
        setNotation(card, TAP, "false");
    }

    void burn(String player, Card card, String srcPlayer, RegionType srcRegion, boolean random) {
        String owner = card.getOwner();
        Location destination = state.getPlayerLocation(card.getOwner(), RegionType.ASH_HEAP);

        //Message formats:
        //Target is public: "<player> burns <card> [#<region-index>] from [<player>'s] <region>"
        //Target is private: "<player> burns <card> from [top of] [<player>'s] <region>"

        boolean showRegionOwner = !player.equals(srcPlayer);
        String message = String.format(
                "%s burns %s%s from %s %s.",
                player,
                getCardName(card, destination),
                random ? " (picked randomly)" : "",
                showRegionOwner ? srcPlayer + "'s" : "their",
                srcRegion.xmlLabel());

        addCommand(message, new String[]{"burn", card.getId(), owner, RegionType.ASH_HEAP.xmlLabel()});
        burnQuietly(card);
    }

    void rfg(String player, Card card, String srcPlayer, RegionType srcRegion, boolean random) {
        CardContainer source = card.getParent();
        String owner = card.getOwner();
        Location destinationRegion = state.getPlayerLocation(owner, RegionType.REMOVED_FROM_GAME);

        boolean showRegionOwner = !player.equals(srcPlayer);
        source.removeCard(card);
        destinationRegion.addCard(card, false);
        String message = String.format(
                "%s removes %s%s in %s %s from the game.",
                player,
                getCardName(card),
                random ? " (picked randomly)" : "",
                showRegionOwner ? srcPlayer + "'s" : "their",
                srcRegion.xmlLabel());

        addCommand(message, new String[]{"rfg", card.getId(), owner, RegionType.REMOVED_FROM_GAME.xmlLabel()});
    }
}
