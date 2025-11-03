/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package net.deckserver.dwr.model;

import com.google.common.base.Strings;
import net.deckserver.services.CardService;
import net.deckserver.game.enums.*;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.ParserService;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import net.deckserver.storage.json.game.RegionData;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record JolGame(String id, GameData data) {

    private static final Comparator<String> DISC_COMPARATOR = Comparator.comparing(s -> Character.isLowerCase(s.charAt(0)) ? 0 : 1);

    public void addPlayer(String name, Deck deck) {
        // Common code that doesn't rely on state
        List<String> cryptlist = new ArrayList<>();
        List<String> librarylist = new ArrayList<>();
        deck.getCrypt().getCards().forEach(cardCount -> cryptlist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        deck.getLibrary().getCards().stream()
                .flatMap(libraryCard -> libraryCard.getCards().stream())
                .forEach(cardCount -> librarylist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        PlayerData playerData = new PlayerData(name);
        RegionData crypt = playerData.getRegion(RegionType.CRYPT);
        RegionData library = playerData.getRegion(RegionType.LIBRARY);
        List<CardData> cryptCards = cryptlist.stream().map(cardId -> new CardData(cardId, playerData)).map(this::hydrateCard).toList();
        List<CardData> libraryCards = librarylist.stream().map(cardId -> new CardData(cardId, playerData)).map(this::hydrateCard).toList();
        data.addPlayer(playerData);
        data.initRegion(crypt, cryptCards);
        data.initRegion(library, libraryCards);
    }

    public void withdraw(String player) {
        data.getPlayer(player).setPool(0);
        data.getPlayer(player).addVictoryPoints(0.5f);
        ChatService.sendCommand(id, player, player + " withdraws and gains 0.5 victory points.", "withdraw");
    }

    public void updateVP(String targetPlayer, float amount) {
        data.getPlayer(targetPlayer).addVictoryPoints(amount);
        ChatService.sendCommand(id, targetPlayer, targetPlayer + " has " + (amount > 0 ? "gained " : "lost ") + DecimalFormat.getCompactNumberInstance().format(Math.abs(amount)) + " victory points.", "vp", String.valueOf(amount));
    }

    public double getVictoryPoints(String player) {
        return data.getPlayer(player).getVictoryPoints();
    }

    public void timeout() {
        data.getCurrentPlayers().forEach(playerData -> {
            playerData.addVictoryPoints(0.5f);
            playerData.setPool(0);
        });
        ChatService.sendSystemMessage(id, "Game has timed out.  Surviving players have been awarded ½ VP.");
    }

    public void requestTimeout(String player) {
        boolean isTimedOut = false;
        String requestor = data.getTimeoutRequestor();
        if (requestor != null && !requestor.equals(player)) {
            isTimedOut = true;
        } else {
            data.setTimeoutRequestor(player);
        }
        if (isTimedOut) {
            ChatService.sendCommand(id, player, player + " has confirmed the game time has been reached.", "timeout", "confirmed");
            timeout();
        } else {
            ChatService.sendCommand(id, player, player + " has requested that the game be timed out.", "timeout", "requested");
        }
    }

    public String getName() {
        return data.getName();
    }

    public List<String> getPlayers() {
        return data.getPlayerNames();
    }

    public void discard(String player, String cardId, boolean random) {
        String cardLink;
        CardData card = data.getCard(cardId);
        RegionData destination = data.getPlayer(player).getRegion(RegionType.ASH_HEAP);
        destination.addCard(card, false);
        cardLink = getCardLink(card.getCardId(), card.getName(), card.isPlaytest());
        String message = String.format("%s discards %s%s", player, cardLink, random ? " (picked randomly)" : "");
        ChatService.sendCommand(id, player, message, "discard", cardId, player, RegionType.ASH_HEAP.xmlLabel());
    }

    public void playCard(String player, String cardId, String destinationPlayer, RegionType destinationRegion, String targetId, String[] modes) {
        // Common
        StringBuilder modeMessage = new StringBuilder();
        String sourceMessage;
        String destinationMessage;
        String playerTitle = destinationPlayer.equals(player) ? "their" : destinationPlayer + "'s";
        String cardLink;

        if (modes != null) {
            for (String mode : modes)
                modeMessage.append(ParserService.generateDisciplineLink(mode));
            modeMessage.insert(0, " at ");
        }
        CardData card = data.getCard(cardId);
        CardData target = data.getCard(targetId);
        RegionData source = card.getRegion();
        RegionData destination = data.getPlayerRegion(destinationPlayer, destinationRegion);

        cardLink = getCardLink(card.getCardId(), card.getName(), card.isPlaytest());
        sourceMessage = RegionType.HAND.equals(source.getType()) ? "" : " from their " + source.getType().xmlLabel();
        if (target == null) {
            destinationMessage = RegionType.ASH_HEAP.equals(destinationRegion) ? "" : String.format(" to %s %s", playerTitle, destinationRegion.xmlLabel());
            destination.addCard(card, false);
        } else {
            destinationMessage = String.format(" on %s", getTargetCardName(target, player));
            target.add(card, false);
        }

        if (destinationRegion.equals(RegionType.READY)) {
            List<CardData> cards = data.getUniqueCards(card);
            if (cards.size() > 1) {
                cards.forEach(c -> c.setContested(true));
            }
        }


        String message = String.format("%s plays %s%s%s%s.", player, cardLink, sourceMessage, modeMessage, destinationMessage);
        ChatService.sendCommand(id, player, message, "play", cardId, destinationPlayer, destinationRegion.xmlLabel());

    }

    public void influenceCard(String player, String cardId) {
        String votesText = "";
        CardData card = data.getCard(cardId);
        RegionData destination = data.getPlayerRegion(player, RegionType.READY);
        destination.addCard(card, true);
        if (!Strings.isNullOrEmpty(card.getVotes())) {
            votesText = ", votes: " + card.getVotes();
        }
        ChatService.sendCommand(id, player, String.format("%s influences out %s%s.", player, getCardLink(card), votesText), "influence", card.getId(), player, RegionType.READY.xmlLabel());
        List<CardData> cards = data.getUniqueCards(card);
        if (cards.size() > 1) {
            cards.forEach(c -> c.setContested(true));
        }
    }

    public void setSect(String cardId, Sect sect) {
        CardData card = data.getCard(cardId);
        card.setSect(sect);
    }

    public void setPath(String cardId, Path path) {
        CardData card = data.getCard(cardId);
        card.setPath(path);
    }

    public void setClan(String cardId, Clan clan) {
        CardData card = data.getCard(cardId);
        card.setClan(clan);
    }

    public void shuffle(String player, RegionType type, int num) {
        RegionData region = data.getPlayerRegion(player, type);
        int size = region.getCards().size();
        region.shuffle(num);
        String add = (num == 0 || num >= size) ? "their" : "the first " + num + " cards of their";
        ChatService.sendCommand(id, player, String.format("%s shuffles %s %s.", player, add, type.xmlLabel()), "shuffle", player, type.xmlLabel(), String.valueOf(num));
    }

    public void startGame(List<String> playerSeating) {
        List<String> players = data.getPlayerNames();
        if (playerSeating.size() != players.size() || !new HashSet<>(players).containsAll(playerSeating)) {
            throw new IllegalArgumentException("Player ordering not valid, does not contain current players");
        }
        data.orderPlayers(playerSeating);
        for (String player : players) {
            PlayerData playerData = data.getPlayer(player);
            playerData.getRegion(RegionType.CRYPT).shuffle(0);
            playerData.getRegion(RegionType.LIBRARY).shuffle(0);
            for (int j = 0; j < 4; j++) {
                _drawCard(player, RegionType.CRYPT, RegionType.UNCONTROLLED, false);
            }
            for (int j = 0; j < 7; j++) {
                _drawCard(player, RegionType.LIBRARY, RegionType.HAND, false);
            }
        }
        data.updatePredatorMapping();
        newTurn();
    }

    public void sendMsg(String player, String msg, boolean isJudge) {
        msg = ParserService.sanitizeText(msg);
        // TODO : look at this
        msg = ParserService.parseGameChat(msg);
        if (isJudge) {
            ChatService.sendJudgeMessage(id, player, msg);
        } else {
            ChatService.sendMessage(id, player, msg);
        }
    }

    public int getCounters(CardData card) {
        return card.getCounters();
    }

    public List<String> getDisciplines(CardData card) {
        return card.getDisciplines();
    }

    public void transfer(String player, String cardId, int amount) {
        CardData card = data.getCard(cardId);
        PlayerData playerData = data.getPlayer(player);
        int counters = card.getCounters();
        int pool = playerData.getPool();
        int newCounters = counters + amount;
        int newPool = pool - amount;
        playerData.setPool(newPool);
        card.setCounters(newCounters);
        String direction = amount > 0 ? "onto" : "off";
        String message = String.format("%s transferred %d blood %s %s. Currently: %d, Pool: %d", player, Math.abs(amount), direction, getCardName(card), newCounters, newPool);
        ChatService.sendCommand(id, player, message, "transfer", card.getId(), String.valueOf(amount));
    }

    public void changeCounters(String player, String cardId, int incr, boolean quiet) {
        if (incr == 0) return;
        {
            CardData card = data.getCard(cardId);
            int current = card.getCounters();
            current += incr;
            card.setCounters(current);
            if (!quiet) {
                String logText = String.format("%s %s %s blood %s %s, now %s. ", player, incr < 0 ? "removes" : "adds", Math.abs(incr), incr < 0 ? "from" : "to", getCardName(card), current);
                ChatService.sendCommand(id, player, logText, "counter", card.getId(), String.valueOf(incr));
            }
        }
    }

    public String getActivePlayer() {
        return data.getCurrentPlayerName();
    }

    public String getTurnLabel() {
        return data.getTurnLabel();
    }

    public CardData getCard(String id) {
        return data.getCard(id);
    }

    public CardData hydrateCard(CardData card) {
        CardSummary summary = CardService.get(card.getCardId());
        card.setName(summary.getName());
        card.setPlaytest(summary.isPlayTest());
        card.setUnique(summary.isUnique());
        card.setType(summary.getCardType());
        card.setMinion(summary.isMinion());
        if (card.isMinion()) {
            if (!summary.getClans().isEmpty()) {
                card.setClan(Clan.of(summary.getClans().getFirst()));
            }
            card.setSect(Sect.of(summary.getSect()));
            card.setPath(Path.of(summary.getPath()));
            card.setDisciplines(summary.getDisciplines());
            card.setCapacity(Optional.ofNullable(summary.getCapacity()).orElse(0));
            card.setVotes(summary.getVotes());
            card.setTitle(summary.getTitle());
            card.setInfernal(summary.isInfernal());
            card.setAdvanced(summary.isAdvanced());
        }
        return card;
    }

    public String getPredatorOf(String player) {
        PlayerData playerData = data.getPlayer(player);
        return Optional.ofNullable(playerData.getPredator()).map(PlayerData::getName).orElse(null);
    }

    public String getPreyOf(String player) {
        PlayerData playerData = data.getPlayer(player);
        return Optional.ofNullable(playerData.getPrey()).map(PlayerData::getName).orElse(null);
    }

    public int getSize(String player, RegionType region) {
        return data.getPlayerRegion(player, region).getCards().size();
    }

    public String getEdge() {
        return data.getEdgePlayer();
    }

    public void setEdge(String source, String player) {
        ChatService.sendCommand(id, source, String.format("%s gains the edge from %s.", player, getEdge()), "edge", player);
        PlayerData playerData = data.getPlayer(player);
        data.setEdge(playerData);
    }

    public void burnEdge(String player) {
        data.setEdge(null);
        ChatService.sendCommand(id, player, String.format("%s burns the edge.", player), "edge", "burn");
    }

    public int getPool(String player) {
        return data.getPlayer(player).getPool();
    }

    public void changePool(String source, String player, int amount) {
        if (amount == 0) return; // PENDING report this in status?
        PlayerData playerData = data.getPlayer(player);
        int starting = playerData.getPool();
        int ending = starting + amount;
        playerData.setPool(ending);
        if (ending <= 0) {
            playerData.setOusted(true);
            data.updatePredatorMapping();
        }
        ChatService.sendCommand(id, source, player + "'s pool was " + starting + ", now is " + ending + ".", "pool", player, String.valueOf(amount));
    }

    public String getGlobalText() {
        return data.getNotes();
    }

    public void setGlobalText(String text) {
        data.setNotes(text);
    }

    public String getPrivateNotes(String player) {
        return Optional.ofNullable(data.getPlayer(player)).map(PlayerData::getNotes).orElse("");
    }

    public void setPrivateNotes(String player, String text) {
        data.getPlayer(player).setNotes(text);
    }

    public void setLabel(String player, String cardId, String text, boolean quiet) {
        CardData card = data.getCard(cardId);
        String cardName = getCardName(card);
        String cleanText = text.trim();
        card.setNotes(cleanText);
        if (!quiet) {
            if (!cleanText.isEmpty()) {
                ChatService.sendCommand(id, player, String.format("%s labels %s: \"%s\"", player, cardName, cleanText), "label", card.getId(), cleanText);
            } else {
                ChatService.sendCommand(id, player, String.format("%s removes label from %s ", player, cardName), "label", card.getId(), "remove");
            }
        }
    }

    public String getVotes(CardData card) {
        return Optional.ofNullable(card.getVotes()).orElse("");
    }

    public void random(String player, int limit, int result) {
        ChatService.sendCommand(id, player, player + " rolls from 1-" + limit + " : " + result, "random", String.valueOf(limit));
    }

    public void flip(String player, String result) {
        ChatService.sendCommand(id, player, player + " flips a coin : " + result, "flip");
    }

    public void setVotes(String source, String cardId, String votes, boolean quiet) {
        int voteAmount = 0;
        try {
            voteAmount = Integer.parseInt(votes);
        } catch (Exception nfe) {
            // do nothing
        }
        String value;
        String message;
        if (votes.trim().equalsIgnoreCase("priscus") || votes.trim().equals("P")) {
            value = "P";
            message = " is priscus.";
        } else if (voteAmount == 0) {
            value = "0";
            message = " now has no votes.";
        } else {
            value = String.valueOf(voteAmount);
            message = " now has " + voteAmount + " votes.";
        }
        CardData card = data.getCard(cardId);
        card.setVotes(value);
        message = getCardName(card) + message;
        if (!quiet) {
            ChatService.sendCommand(id, source, message, "votes", value);
        }
    }

    public void contestCard(String source, String cardId, boolean clear) {
        CardData card = data.getCard(cardId);
        card.setContested(!clear);
        String message = clear ? "no longer contested." : "now contested.";
        ChatService.sendCommand(id, source, String.format("%s's %s is %s", card.getOwnerName(), getCardName(card), message), "contest", card.getId(), String.valueOf(clear));
    }

    public boolean getContested(CardData card) {
        return card.isContested();
    }

    public void setLocked(String player, String cardId, boolean locked) {
        CardData card = data.getCard(cardId);
        card.setLocked(locked);
        String message = String.format("%s %s %s.", player, locked ? "locks" : "unlocks", getCardName(card));
        ChatService.sendCommand(id, player, message, "lock", card.getId(), String.valueOf(locked));
    }

    public void unlockAll(String player) {
        StringBuilder notUnlockedString = new StringBuilder();
        for (RegionType regionType : RegionType.values()) {
            RegionData regionData = data.getPlayerRegion(player, regionType);
            String regionResults = unlockAll(regionData).stream().map(this::getCardLink).collect(Collectors.joining(" "));
            if (!regionResults.isEmpty()) {
                notUnlockedString.append(regionResults).append(" ");
            }
        }
        String message = String.format("%s unlocks.", player);
        ChatService.sendCommand(id, player, message, "untap", player);
        if (!notUnlockedString.toString().isEmpty()) {
            ChatService.sendSystemMessage(id, "The following cards do not unlock as normal: " + notUnlockedString);
        }
    }

    public String getCurrentTurn() {
        return data.getTurn();
    }

    public void newTurn() {
        String turn = data.getTurn();
        int round = Integer.parseInt(turn.split("\\.")[0]);
        int index = Integer.parseInt(turn.split("\\.")[1]);
        List<String> players = getValidPlayers();
        PlayerData currentPlayer = data.getCurrentPlayer();
        PlayerData nextPlayer;
        if (currentPlayer == null) {
            nextPlayer = data.getPlayer(players.getFirst());
            data.setCurrentPlayer(nextPlayer);
        } else {
            // Increment Round & Index
            if (++index > players.size()) {
                index = 1;
                round++;
            }
            nextPlayer = data.isOrderOfPlayReversed() ? data.getCurrentPlayer().getPredator() : data.getCurrentPlayer().getPrey();
            data.setCurrentPlayer(nextPlayer);
        }
        String turnId = String.format("%d.%d", round, index);
        // If we are reversed, choose predator, otherwise choose prey
        ChatService.addTurn(id, nextPlayer.getName(), turnId);
        setPhase(Phase.UNLOCK);
        data.setTurn(turnId);
        GameService.saveGame(this, turnId);
    }

    public Phase getPhase() {
        return Optional.ofNullable(data.getPhase()).orElse(Phase.UNLOCK);
    }

    public void setPhase(Phase phase) {
        data.setPhase(phase);
        sendMsg(getActivePlayer(), "START OF " + phase.toString() + " PHASE.", false);
    }

    public void changeCapacity(String source, String cardId, int change, boolean quiet) {
        {
            CardData card = data.getCard(cardId);
            int currentCapacity = card.getCapacity();
            int newCapacity = currentCapacity + change;
            if (newCapacity < 0) newCapacity = 0;
            card.setCapacity(newCapacity);
            if (!quiet)
                ChatService.sendCommand(id, source, "Capacity of " + getCardName(card) + " now " + newCapacity, "capacity", card.getId(), change + "");
        }
    }

    public void setDisciplines(String player, String cardId, List<String> disciplines, boolean quiet) {
        {
            CardData card = data.getCard(cardId);
            card.setDisciplines(disciplines);
            if (!quiet && !disciplines.isEmpty()) {
                String disciplineList = disciplines.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" "));
                String msg = ParserService.parseGameChat(player + " reset " + getCardName(card) + " back to " + disciplineList);
                ChatService.sendCommand(id, player, msg, "disc", card.getId(), disciplines.toString());
            }
        }
    }

    public void setDisciplines(String player, String cardId, Set<String> additions, Set<String> removals) throws CommandException {
        List<String> currentDisciplines = data.getCard(cardId).getDisciplines();
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
        CardData card = data.getCard(cardId);
        if (!discAdded.isEmpty() || !discRemoved.isEmpty()) {
            card.setDisciplines(newDisciplines);
        } else {
            throw new CommandException("No valid disciplines chosen.");
        }
        String additionString = discAdded.isEmpty() ? "" : "added " + ParserService.parseGlobalChat(discAdded.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" ")));
        String removalsString = discRemoved.isEmpty() ? "" : "removed " + ParserService.parseGlobalChat(discRemoved.stream().map(d -> "[" + d + "]").collect(Collectors.joining(" ")));
        ChatService.sendCommand(id, player, String.format("%s %s%s to %s.", player, additionString, removalsString, getCardName(data.getCard(cardId))), "disc", cardId, additionString, removalsString);

    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        data.replacePlayer(oldPlayer, newPlayer);
        data.setTimeoutRequestor(null);
        ChatService.sendSystemMessage(id, "Player " + newPlayer + " replaced " + oldPlayer);
    }

    public void setChoice(String player, String choice) {
        data.getPlayer(player).setChoice(choice);
        ChatService.sendCommand(id, player, player + " has made their choice.", "choice", choice);
    }

    public void getChoices() {
        ChatService.sendSystemMessage(id, "The choices have been revealed:");
        data.getPlayers().values().forEach(player -> {
            String choice = player.getChoice();
            if (!Strings.isNullOrEmpty(choice)) {
                ChatService.sendSystemMessage(id, player + " chose " + choice);
                player.setChoice(null);
            }
        });
    }

    public void setOrder(String source, List<String> players) {
        data.orderPlayers(players);
        StringBuilder order = new StringBuilder();
        for (String player : players) order.append(" ").append(player);
        ChatService.sendCommand(id, source, "Player order" + order, "order", order.toString());
    }

    public List<String> getValidPlayers() {
        return data.getCurrentPlayers()
                .stream()
                .map(PlayerData::getName)
                .collect(Collectors.toList());
    }

    public void show(String player, RegionType targetRegion, int amount, List<String> recipients) {
        List<CardData> regionData = data.getPlayerRegion(player, targetRegion).getCards();
        int max = Math.min(regionData.size(), amount);
        List<CardData> cards = regionData.stream().limit(max).toList();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%d cards of %s's %s\n", max, player, targetRegion.description()));
        for (int i = 0; i < cards.size(); i++) {
            builder.append(String.format("%d %s\n", i + 1, cards.get(i).getName()));
        }
        String notes = builder.toString();
        for (String recipient: recipients) {
            PlayerData recipientData = data.getPlayer(recipient);
            String privateNotes = recipientData.getNotes();
            privateNotes += notes;
            recipientData.setNotes(privateNotes);
        }
        String msg;
        boolean self = recipients.size() == 1 && recipients.contains(player);
        boolean all = recipients.size() == getValidPlayers().size();
        if (self) {
            msg = "%s looks at %d cards of their %s.";
        } else if (all) {
            msg = "%s shows everyone %d cards of their %s.";
        } else {
            msg = "%1$s shows %4$s %2$d cards of their %3$s.";
        }
        msg = String.format(msg, player, max, targetRegion.description(), String.join(", ", recipients));
        ChatService.sendCommand(id, player, msg, "show", targetRegion.xmlLabel(), String.valueOf(max), String.join(" ", recipients));
    }

    private void _drawCard(String player, RegionType srcRegion, RegionType destRegion, boolean log) {
        {
            RegionData source = data.getPlayerRegion(player, srcRegion);
            RegionData dest = data.getPlayerRegion(player, destRegion);
            CardData card = source.getFirstCard();
            dest.addCard(card, false);
        }
        if (log) {
            ChatService.sendCommand(id, player, String.format("%s draws from their %s.", player, srcRegion.xmlLabel()), "draw", srcRegion.xmlLabel(), destRegion.xmlLabel());
        }
    }

    private String getTargetCardName(CardData card, String player) {
        RegionData cardLocation = card.getRegion();
        RegionType cardRegion = cardLocation.getType();

        boolean sameOwner = card.getOwnerName().equals(player);
        String cardName;
        if (RegionType.OTHER_HIDDEN_REGIONS.contains(cardRegion)) {
            String coordinates = getIndexCoordinates(card);
            cardName = "Card #" + coordinates;
        } else {
            cardName = getCardName(card);
        }
        String playerName = sameOwner ? "their" : card.getOwnerName() + "'s";
        return String.format("%s in %s %s", cardName, playerName, cardRegion.xmlLabel());
    }

    private String getCardName(CardData card) {
        return getCardName(card, card.getRegion());
    }

    private String getCardName(CardData card, RegionData destination) {
        RegionData source = card.getRegion();
        RegionType sourceType = source.getType();
        RegionType destinationType = destination.getType();

        String cardOwner = card.getOwnerName();
        String sourceOwner = source.getOwner();
        String destinationOwner = destination.getOwner();

        boolean sameOwner = Stream.of(sourceOwner, destinationOwner).allMatch(c -> c.equals(cardOwner));

        if (RegionType.OTHER_HIDDEN_REGIONS.containsAll(List.of(sourceType, destinationType)) && sameOwner) {
            String coordinates = getIndexCoordinates(card);
            return String.format("card #%s in their %s", coordinates, sourceType.xmlLabel());
        }

        // if the card is not unique, then add some flavour to the name to help identify it better
        String differentiators = "";
        if (!card.isUnique()) {
            differentiators = getDifferentiators(card);
        }
        return getCardLink(card) + differentiators;
    }

    private String getDifferentiators(CardData card) {
        String coordinates = getIndexCoordinates(card);
        String label = card.getNotes();
        label = Strings.isNullOrEmpty(label) ? "" : String.format(" \"%s\"", label);
        return String.format(" %s%s", coordinates, label);
    }

    private String getIndexCoordinates(CardData card) {
        List<String> coordinates = new ArrayList<>();
        String id = card.getId();
        CardData parent = card.getParent();
        // No parent card, just a region
        if (parent == null) {
            coordinates.add(String.valueOf(card.getRegion().getCards().indexOf(card) + 1));
        } else {
            boolean looking = true;
            while (looking) {
                List<String> idList = parent.getCards().stream().map(CardData::getId).toList();
                int index = idList.indexOf(id);
                coordinates.add(String.valueOf(index + 1));
                if (parent.getParent() == null) {
                    looking = false;
                    coordinates.add(String.valueOf(card.getRegion().getCards().indexOf(parent) + 1));
                } else {
                    id = parent.getId();
                    parent = parent.getParent();
                }
            }
        }

        return String.join(".", coordinates.reversed());
    }

    private String getCardLink(String cardId, String name, boolean playTest) {
        return String.format("<a class='card-name' data-card-id='%s' data-secured='%s'>%s</a>", cardId, playTest, name);
    }

    private String getCardLink(CardData card) {
        return String.format("<a class='card-name' data-card-id='%s' data-secured='%s'>%s</a>", card.getCardId(), card.isPlaytest(), card.getName());
    }

    private List<CardData> unlockAll(RegionData regionData) {
        List<CardData> notUnlocked = new ArrayList<>();
        for (CardData card : regionData.getCards()) {
            // Don't unlock infernal cards in play regions
            if (card.isInfernal() && RegionType.IN_PLAY_REGIONS.contains(regionData.getType())) {
                notUnlocked.add(card);
            } else {
                card.setLocked(false);
            }
            unlockAll(card);
        }
        return notUnlocked;
    }

    private void unlockAll(CardData cardData) {
        for (CardData card : cardData.getCards()) {
            if (!card.isInfernal()) {
                card.setLocked(false);
            }
            unlockAll(card);
        }
    }

    private void burnQuietly(CardData card) {
        if (card == null) throw new IllegalArgumentException("No such card");
        for (CardData c : card.getCards()) {
            burnQuietly(c);
        }

        String owner = card.getOwnerName();
        RegionData region = data.getPlayerRegion(owner, RegionType.ASH_HEAP);
        card.setNotes(null);
        region.addCard(card, false);
        int counters = card.getCounters();
        if (counters > 0) {
            changeCounters(null, card.getId(), -counters, true);
        }
        card.setLocked(false);
    }

    void drawCard(String player, RegionType srcRegion, RegionType destRegion) {
        _drawCard(player, srcRegion, destRegion, true);
    }

    public void moveToCard(String player, String srcCardId, String dstCardId) throws CommandException {
        if (srcCardId.equals(dstCardId)) throw new CommandException("Can't move a card to itself");
        {
            CardData srcCard = data.getCard(srcCardId);
            CardData dstCard = data.getCard(dstCardId);
            CardData parentCard = dstCard.getParent();
            while (parentCard != null) {
                // No more parents, parent is a region
                if (parentCard.getId().equals(srcCardId)) {
                    throw new CommandException("Can't create card loop");
                }
                parentCard = parentCard.getParent();
            }
            RegionData dstRegion = dstCard.getRegion();

            String message = String.format("%s puts %s on %s.", player, getCardName(srcCard, dstRegion), getTargetCardName(dstCard, player));
            ChatService.sendCommand(id, player, message, "move", srcCard.getId(), dstCard.getId());
            dstCard.add(srcCard, false);
        }
    }

    void moveToRegion(String player, String cardId, String destPlayer, RegionType destRegion, boolean top) {
        {
            CardData card = data.getCard(cardId);
            RegionData sourceRegion = card.getRegion();
            RegionData destinationRegion = data.getPlayerRegion(destPlayer, destRegion);
            boolean sameOwner = Stream.of(sourceRegion.getOwner(), destinationRegion.getOwner()).allMatch(c -> c.equals(player));
            String topMessage = top ? "the top of " : "";
            String playerName = sameOwner ? "their" : destinationRegion.getOwner() + "'s";
            String message = String.format("%s moves %s to %s%s %s.", player, getCardName(card, destinationRegion), topMessage, playerName, destRegion.xmlLabel());
            ChatService.sendCommand(id, player, message, "move", card.getId(), destPlayer, destRegion.xmlLabel(), top ? "top" : "bottom");
            destinationRegion.addCard(card, top);
        }
    }

    void burn(String player, String cardId, String srcPlayer, RegionType srcRegion, boolean random) {
        {
            CardData card = data.getCard(cardId);
            String owner = card.getOwnerName();
            RegionData destination = data.getPlayerRegion(player, RegionType.ASH_HEAP);
            boolean showRegionOwner = !player.equals(srcPlayer);
            String message = String.format(
                    "%s burns %s%s from %s %s.",
                    player,
                    getCardName(card, destination),
                    random ? " (picked randomly)" : "",
                    showRegionOwner ? srcPlayer + "'s" : "their",
                    srcRegion.xmlLabel());

            ChatService.sendCommand(id, player, message, "burn", card.getId(), owner, RegionType.ASH_HEAP.xmlLabel());
            burnQuietly(card);
        }
    }

    void rfg(String player, String cardId, String srcPlayer, RegionType srcRegion, boolean random) {
        {
            CardData card = data.getCard(cardId);
            String owner = card.getOwnerName();
            RegionData destination = data.getPlayerRegion(owner, RegionType.REMOVED_FROM_GAME);
            destination.addCard(card, false);
            boolean showRegionOwner = !player.equals(srcPlayer);
            String message = String.format(
                    "%s removes %s%s in %s %s from the game.",
                    player,
                    getCardName(card),
                    random ? " (picked randomly)" : "",
                    showRegionOwner ? srcPlayer + "'s" : "their",
                    srcRegion.xmlLabel());
            ChatService.sendCommand(id, player, message, "rfg", card.getId(), owner, RegionType.REMOVED_FROM_GAME.xmlLabel());
        }
    }
}
