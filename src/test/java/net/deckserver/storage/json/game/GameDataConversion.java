package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.jaxb.XmlFileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.jaxb.state.Region;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.cards.CardType;
import net.deckserver.game.storage.state.RegionType;
import net.deckserver.storage.json.cards.CardSummary;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GameDataConversion {

    static CardSearch SEARCH = CardSearch.INSTANCE;

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    public void writeGameData() throws IOException {

        Path gamesPath = Paths.get("/Users/shannon/data/games");
        try (Stream<Path> stream = Files.list(gamesPath)) {
            stream
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(this::hasGame)
                    .forEach(this::convertGame);
        }
    }

    private boolean hasGame(String gameId) {
        return Files.exists(Paths.get("/Users/shannon/data/games", gameId, "game.xml"));
    }

    private void save(String gameId, String turnId, GameData gameData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path gamePath;
            if (turnId != null) {
                gamePath = Paths.get("/Users/shannon/data/games", gameId, "game" + "-" + turnId + ".json");
            } else {
                gamePath = Paths.get("/Users/shannon/data/games", gameId, "game.json");
            }
            mapper.writeValue(gamePath.toFile(), gameData);
        } catch (IOException e) {
            System.err.println("Something went wrong " + e);
        }
    }

    private void save(String gameId, String turnId, TurnData turnData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path gamePath = Paths.get("/Users/shannon/data/games", gameId, "actions-" + turnId + ".json");
            mapper.writeValue(gamePath.toFile(), turnData);
        } catch (IOException e) {
            System.err.println("Something went wrong " + e);
        }
    }

    private void convertGame(String gameId) {
        GameData data = convertGameFile(Paths.get("/Users/shannon/data/games", gameId, "game.xml"), gameId);
        List<TurnData> turns = convertActionsFile(Paths.get("/Users/shannon/data/games", gameId, "actions.xml"), data.getPlayers().keySet());
        data.setTurn(turns.getLast().getTurnId());
        save(gameId, null, data);
        turns.forEach(turn -> {
            String fileTurnId = turn.getTurnId().replaceAll("\\.", "-");
            System.out.println("Converting " + gameId + "/game-" + fileTurnId + ".xml");
            GameData gameTurn = convertGame(gameId, fileTurnId);
            if (gameTurn != null) {
                save(gameId, fileTurnId, gameTurn);
            }
            save(gameId, fileTurnId, turn);
        });
    }

    private GameData convertGame(String gameId, String turnId) {
        try {
            GameData gameData = convertGameFile(Paths.get("/Users/shannon/data/games", gameId, "game-" + turnId + ".xml"), gameId);
            gameData.setTurn(turnId);
            return gameData;
        } catch (Exception e) {
            System.err.println("Something went wrong " + e);
            return null;
        }
    }

    private List<TurnData> convertActionsFile(Path filePath, Set<String> players) {
        GameActions gameActions = XmlFileUtils.loadGameActions(filePath);
        List<TurnData> turnData = generateTurnEntries(gameActions, players);
        return turnData;
    }

    private GameData convertGameFile(Path filePath, String gameId) {
        GameData data = new GameData();
        data.setId(gameId);
        GameState gameState = XmlFileUtils.loadGameState(filePath);
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
                    playerData.setNotes(getNotes(gameState, name));
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
                        parentCard.add(card);
                        cardData.put(card.getId(), card);
                    });
                });

        data.setCurrentPlayer(getActivePlayer(gameState, players));

        PlayerData edge = getEdge(gameState, players);
        data.setEdge(edge);
        data.setNotes(getNotes(gameState));
        data.setPhase(getPhase(gameState));
        data.addCards(cardData.values());
        return data;
    }

    private List<TurnData> generateTurnEntries(GameActions gameActions, Set<String> players) {
        final Map<String, Integer> playerCounter = new HashMap<>();
        AtomicInteger phaseIndex = new AtomicInteger(0);
        AtomicInteger turnIndex = new AtomicInteger(0);
        players.forEach(player -> playerCounter.put(player, 0));
        return gameActions.getTurn().stream().map(turn -> {
            TurnData turnData = new TurnData();
            phaseIndex.getAndIncrement();
            String player = turn.getName();
            playerCounter.computeIfAbsent(player, (k) -> 0);
            playerCounter.compute(player, (k, v) -> v + 1);
            int maxTurn = playerCounter.values().stream().max(Comparator.naturalOrder()).get();
            if (maxTurn > turnIndex.get()) {
                turnIndex.incrementAndGet();
                phaseIndex.set(1);
            }
            turn.getAction().stream().map(action -> {
                String text = action.getText();
                text = text.replaceAll("\\n", "").replace("\\r", "").replaceAll("\\s{2,}", " ").trim();
                String dateString = text.replaceAll("^(\\d{1,2}-[a-zA-Z]{3} \\d{2}:\\d{2}) .*", "$1");
                String messageString = text.replaceAll(dateString, "").trim();
                boolean sourcePresent = messageString.matches("^\\[.*?\\].*");
                String source = null;
                if (sourcePresent) {
                    source = messageString.replaceAll("^(\\[.*?\\]).*", "$1");
                    messageString = messageString.substring(source.length()).trim();
                    source = source.replaceAll("^\\[(.*?)\\]", "$1");
                }
                messageString = messageString.replaceAll("<a(.*?)<\\/a>", "<span$1</span>");
                String command = String.join(" ", action.getCommand());
                return new ChatData(dateString, messageString, source, command);
            }).forEach(turnData::addChat);
            turnData.setTurn(turnIndex.get(), phaseIndex.get());
            turnData.setPlayer(player);
            return turnData;
        }).collect(Collectors.toList());
    }

    String getNotation(List<Notation> notations, String name, String defaultValue) {
        return notations
                .stream()
                .filter(notation -> notation.getName().equals(name))
                .findFirst()
                .map(Notation::getValue)
                .orElse(defaultValue);
    }

    int getNotationAsInt(List<Notation> notations, String name, int defaultValue) {
        return Integer.parseInt(getNotation(notations, name, String.valueOf(defaultValue)));
    }

    float getNotationAsFloat(List<Notation> notations, String name, double defaultValue) {
        return Float.parseFloat(getNotation(notations, name, String.valueOf(defaultValue)));
    }

    boolean getNotationAsBoolean(List<Notation> notations, String name, String check) {
        return getNotation(notations, name, "").equals(check);
    }

    int getPool(GameState state, String player) {
        return getNotationAsInt(state.getNotation(), player + "pool", 0);
    }

    float getVictoryPoints(GameState state, String player) {
        return getNotationAsFloat(state.getNotation(), player + " vp", 0.0f);
    }

    String getNotes(GameState state, String player) {
        return state.getNotation()
                .stream()
                .filter(notation -> notation.getName().equals(player + "text"))
                .findFirst()
                .map(Notation::getValue)
                .orElse(null);
    }


    PlayerData getActivePlayer(GameState state, Map<String, PlayerData> players) {
        String playerName = state.getNotation()
                .stream()
                .filter(notation -> notation.getName().equals("active meth"))
                .findFirst()
                .map(Notation::getValue)
                .orElseThrow(() -> new RuntimeException("Unable to find active player"));
        return players.get(playerName);
    }

    PlayerData getEdge(GameState state, Map<String, PlayerData> players) {
        String playerName = getNotation(state.getNotation(), "edge", null);
        return players.get(playerName);
    }

    String getNotes(GameState state) {
        return getNotation(state.getNotation(), "text", null);
    }

    String getPhase(GameState state) {
        return getNotation(state.getNotation(), "phase", "Unlock");
    }

    RegionType getType(String regionName) {
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

    void getRegions(GameState state, PlayerData player, Map<String, CardData> cards) {
        state.getRegion()
                .stream()
                .filter(region -> region.getName().startsWith(player.getName()))
                .forEach(region -> {
                    RegionType type = getType(region.getName());
                    RegionData regionData = player.getRegion(type);
                    List<CardData> cardData = getCardData(state, region.getName());
                    cardData.forEach(card -> card.setRegion(regionData));
                    regionData.setCards(cardData);
                    cardData.forEach(cData -> {
                        cards.put(cData.getId(), cData);
                    });
                });
    }

    List<CardData> getCardData(GameState state, String name) {
        return state.getRegion()
                .stream().filter(region -> region.getName().equals(name))
                .flatMap(region -> region.getGameCard().stream().map(gameCard -> mapCard(gameCard, region)))
                .collect(Collectors.toList());
    }

    CardData mapCard(GameCard gameCard, Region region) {
        CardData cardData = new CardData();
        cardData.setId(gameCard.getId());
        cardData.setCardId(gameCard.getCardid());
        CardSummary summary = SEARCH.get(gameCard.getCardid());
        cardData.setName(summary.getDisplayName());
        if (CardType.permanentTypes().contains(summary.getCardType())) {
            cardData.setType(summary.getCardType());
        } else {
            cardData.setType(CardType.NONE);
        }
        if (CardType.clanTypes().contains(summary.getCardType())) {
            cardData.setClan(String.join("", summary.getClans()));
        }
        cardData.setCounters(getNotationAsInt(gameCard.getNotation(), "counters", 0));
        cardData.setCapacity(getNotationAsInt(gameCard.getNotation(), "capac", 0));
        cardData.setLocked(getNotationAsBoolean(gameCard.getNotation(), "tapnote", "tap"));
        cardData.setContested(getNotationAsBoolean(gameCard.getNotation(), "contested", "contested"));
        cardData.setNotes(getNotation(gameCard.getNotation(), "text", null));
        cardData.setVotes(summary.getVotes());
        cardData.setTitle(summary.getTitle());
        cardData.setAdvanced(summary.isAdvanced());
        String disciplines = getNotation(gameCard.getNotation(), "disciplines", null);
        if (disciplines != null) {
            Arrays.asList(disciplines.split(" ")).forEach(cardData::addDiscipline);
        }
        return cardData;
    }

}