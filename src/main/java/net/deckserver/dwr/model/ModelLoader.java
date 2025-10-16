package net.deckserver.dwr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.services.CardService;
import net.deckserver.game.enums.*;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.game.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ModelLoader {

    public static synchronized GameData convertGameState(GameState gameState, String gameId) {
        GameData data = new GameData(gameId);
        data.setId(gameId);
        data.setName(gameState.getName());
        Map<String, CardData> cardData = new HashMap<>();
        Map<String, PlayerData> players = gameState.getPlayer().stream()
                .map(name -> {
                    PlayerData playerData = new PlayerData(name);
                    data.addPlayer(playerData);
                    int pool = getPool(gameState, name);
                    float victoryPoints = getVictoryPoints(gameState, name);
                    playerData.setPool(pool);
                    playerData.setVictoryPoints(victoryPoints);
                    playerData.setOusted(pool <= 0);
                    String currentNotes = getNotes(gameState, name);
                    if (!StringUtils.isEmpty(currentNotes)) {
                        playerData.setNotes(currentNotes);
                    }
                    playerData.setChoice(getChoice(gameState, name));
                    getRegions(gameState, playerData, cardData);
                    return playerData;
                })
                .collect(Collectors.toMap(PlayerData::getName, Function.identity()));

        gameState.getRegion().stream().filter(region -> region.getName().startsWith("ZZZ"))
                .forEach(cardRegion -> {
                    String id = cardRegion.getName().replaceAll("ZZZ", "").replaceAll(" container", "");
                    List<CardData> attachedCards = getCardData(gameState, cardRegion.getName());
                    CardData parentCard = cardData.get(id);
                    attachedCards.forEach(card -> {
                        card.setParent(parentCard);
                        card.setRegion(parentCard.getRegion());
                        parentCard.add(card, false);
                        cardData.put(card.getId(), card);
                    });
                });

        data.updatePredatorMapping();

        Map<String, String> oldCards = getOwners(gameState);
        mapOwners(oldCards, players, cardData);

        data.setCurrentPlayer(getActivePlayer(gameState, players));

        PlayerData edge = getEdge(gameState, players);
        data.setEdge(edge);
        data.setNotes(getNotes(gameState));
        data.setPhase(getPhase(gameState));
        data.setCards(cardData);
        return data;
    }

    private static Map<String, String> getOwners(GameState gameState) {
        Map<String, String> cards = new HashMap<>();
        gameState.getRegion().stream()
                .flatMap(region -> region.getGameCard().stream())
                .forEach(card -> cards.put(card.getId(), card.getOwner()));
        return cards;
    }

    private static void mapOwners(Map<String, String> oldCards, Map<String, PlayerData> players, Map<String, CardData> cardData) {
        cardData.forEach((id, card) -> {
            String owner = oldCards.get(id);
            PlayerData player = players.get(owner);
            card.setOwner(player);
        });
    }

    static String getNotation(List<Notation> notations, String name, String defaultValue) {
        return notations
                .stream()
                .filter(notation -> notation.getName().equals(name))
                .findFirst()
                .map(Notation::getValue)
                .orElse(defaultValue);
    }

    static int getNotationAsInt(List<Notation> notations, String name) {
        return Integer.parseInt(getNotation(notations, name, String.valueOf(0)));
    }

    static float getNotationAsFloat(List<Notation> notations, String name) {
        return Float.parseFloat(getNotation(notations, name, String.valueOf(0.0)));
    }

    static boolean getNotationAsBoolean(List<Notation> notations, String name, String check) {
        return getNotation(notations, name, "").equals(check);
    }

    static int getPool(GameState state, String player) {
        return getNotationAsInt(state.getNotation(), player + "pool");
    }

    static float getVictoryPoints(GameState state, String player) {
        return getNotationAsFloat(state.getNotation(), player + " vp");
    }

    static String getChoice(GameState state, String player) {
        return state.getNotation()
                .stream()
                .filter(notation -> notation.getName().equals(player + "-choice"))
                .findFirst()
                .map(Notation::getValue)
                .orElse(null);
    }

    static String getNotes(GameState state, String player) {
        return state.getNotation()
                .stream()
                .filter(notation -> notation.getName().equals(player + "text"))
                .findFirst()
                .map(Notation::getValue)
                .orElse(null);
    }

    static PlayerData getActivePlayer(GameState state, Map<String, PlayerData> players) {
        String playerName = state.getNotation()
                .stream()
                .filter(notation -> notation.getName().equals("active meth"))
                .findFirst()
                .map(Notation::getValue)
                .orElseThrow(() -> new RuntimeException("Unable to find active player"));
        return players.get(playerName);
    }

    static PlayerData getEdge(GameState state, Map<String, PlayerData> players) {
        String playerName = getNotation(state.getNotation(), "edge", null);
        return players.get(playerName);
    }

    static String getNotes(GameState state) {
        return getNotation(state.getNotation(), "text", null);
    }

    static Phase getPhase(GameState state) {
        return Phase.of(getNotation(state.getNotation(), "phase", "Unlock"));
    }

    static RegionType getType(String regionName) {
        if (regionName.endsWith("ready region")) {
            return RegionType.READY;
        } else if (regionName.endsWith("torpor")) {
            return RegionType.TORPOR;
        } else if (regionName.endsWith("inactive region")) {
            return RegionType.UNCONTROLLED;
        } else if (regionName.endsWith("hand")) {
            return RegionType.HAND;
        } else if (regionName.endsWith("ashheap")) {
            return RegionType.ASH_HEAP;
        } else if (regionName.endsWith("library")) {
            return RegionType.LIBRARY;
        } else if (regionName.endsWith("crypt")) {
            return RegionType.CRYPT;
        } else if (regionName.endsWith("rfg")) {
            return RegionType.REMOVED_FROM_GAME;
        } else if (regionName.endsWith("research")) {
            return RegionType.RESEARCH;
        } else throw new RuntimeException("Unknown region type");
    }

    static void getRegions(GameState state, PlayerData player, Map<String, CardData> cards) {
        state.getRegion()
                .stream()
                .filter(region -> region.getName().startsWith(player.getName()))
                .forEach(region -> {
                    RegionType type = getType(region.getName());
                    RegionData regionData = player.getRegion(type);
                    LinkedList<CardData> cardData = getCardData(state, region.getName());
                    cardData.forEach(card -> card.setRegion(regionData));
                    regionData.setCards(cardData);
                    cardData.forEach(cData -> cards.put(cData.getId(), cData));
                });
    }

    static LinkedList<CardData> getCardData(GameState state, String name) {
        return state.getRegion()
                .stream().filter(region -> region.getName().equals(name))
                .flatMap(region -> region.getGameCard().stream().map(ModelLoader::mapCard))
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    static CardData mapCard(GameCard gameCard) {
        CardData cardData = new CardData();
        cardData.setId(gameCard.getId());
        cardData.setCardId(gameCard.getCardid());
        CardSummary summary = CardService.get(gameCard.getCardid());
        cardData.setName(summary.getDisplayName());
        if (CardType.permanentTypes().contains(summary.getCardType())) {
            cardData.setType(summary.getCardType());
        } else {
            cardData.setType(CardType.NONE);
        }
        cardData.setCounters(getNotationAsInt(gameCard.getNotation(), "counters"));
        cardData.setCapacity(getNotationAsInt(gameCard.getNotation(), "capac"));
        cardData.setLocked(getNotationAsBoolean(gameCard.getNotation(), "tapnote", "tap"));
        cardData.setContested(getNotationAsBoolean(gameCard.getNotation(), "contested", "contested"));
        if (summary.isMinion()) {
            if (!summary.getClans().isEmpty()) {
                cardData.setClan(Clan.of(summary.getClans().getFirst()));
            }
            cardData.setPath(net.deckserver.game.enums.Path.of(summary.getPath()));
            cardData.setSect(Sect.of(summary.getSect()));
            cardData.setMinion(summary.isMinion());
            cardData.setPlaytest(summary.isPlayTest());
            cardData.setInfernal(summary.isInfernal());
            if (summary.getVotes() != null) {
                String currentVotes = getNotation(gameCard.getNotation(), "votes", null);
                if (currentVotes != null) {
                    cardData.setVotes(currentVotes);
                } else {
                    cardData.setVotes(summary.getVotes());
                }
            }
        }
        String currentNotes = getNotation(gameCard.getNotation(), "notes", null);
        if (!StringUtils.isEmpty(currentNotes)) {
            cardData.setNotes(currentNotes);
        }
        cardData.setUnique(summary.isUnique());
        cardData.setTitle(summary.getTitle());
        cardData.setAdvanced(summary.isAdvanced());
        String disciplines = getNotation(gameCard.getNotation(), "disciplines", null);
        if (disciplines != null) {
            Arrays.asList(disciplines.split(" ")).forEach(cardData::addDiscipline);
        }
        return cardData;
    }

    public static TurnHistory convertHistory(GameActions gameActions) {
        Pattern ACTION_PATTERN = Pattern.compile("^(\\d{1,2}-\\w{3} \\d{2}:\\d{2})(?:\\s+\\[\\s*<\\s*([^>]+)\\s*>\\s*]|\\s+\\[([^\\]]+)])?\\s+(.*)$");
        TurnHistory history = new TurnHistory();
        Set<String> turnLabels = new HashSet<>();
        gameActions.getTurn().forEach(turn -> {
            TurnData turnData = new TurnData();
            String label = turn.getLabel();
            // Not sure how we get duplicates but keep the oldest one
            if (turnLabels.contains(label)) {
                return;
            }
            turnLabels.add(label);
            String player = turn.getName();
            String turnId = label.replaceAll(player, "").trim();
            turnData.setPlayer(player);
            turnData.setTurnId(turnId);
            turn.getAction().forEach(action -> {
                ChatData chatData = new ChatData();
                String text = action.getText();
                Matcher matcher = ACTION_PATTERN.matcher(text.trim());
                if (!matcher.matches()) {
                    chatData.setMessage(text.trim());
                    return;
                }
                chatData.setTimestamp(matcher.group(1));
                String judgeName = matcher.group(2);
                String regularName = matcher.group(3);
                String playerName = judgeName != null ? "Judge - " + judgeName.trim() : regularName;
                chatData.setSource(playerName);
                chatData.setMessage(matcher.group(4));
                if (!action.getCommand().isEmpty()) {
                    chatData.setCommand(String.join(" ", action.getCommand()));
                }
                turnData.addChat(chatData);
            });
            history.addTurn(turnData);
        });
        return history;
    }
}
