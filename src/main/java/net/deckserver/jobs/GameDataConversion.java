package net.deckserver.jobs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.dwr.model.ModelLoader;
import net.deckserver.game.jaxb.XmlFileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.services.CardService;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import net.deckserver.storage.json.game.TurnHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class GameDataConversion {

    private static final String BASE_PATH = System.getenv().getOrDefault("JOL_DATA", "/data/games");
    private static final Logger LOGGER = LoggerFactory.getLogger(GameDataConversion.class);

    public void convertGame(String gameId) {
        GameData data = convertGameData(gameId);
        verify(data);
    }

    public void checkCards(String gameName, String id) {
        GameData data = load(id);
        assert data != null;
        data.getCards().values().forEach(card -> {
            CardSummary summary = CardService.get(card.getCardId());
            if (summary.hasBlood()) {
                if (card.getDisciplines().isEmpty() && !summary.getDisciplines().isEmpty()) {
                    LOGGER.info("Restoring missing disciplines on {} - {} ({})", gameName, card.getName(), card.getId());
                    card.setDisciplines(summary.getDisciplines());
                }
                if (card.getCapacity() <= 0 && summary.getCapacity() > 0) {
                    LOGGER.info("Restoring missing capacity on {} - {} ({})", gameName, card.getName(), card.getId());
                    card.setCapacity(summary.getCapacity());
                }
            }
        });
        save(id, data);
    }

    private void verify(GameData gameData) {
        GameData source = load(gameData.getId());
        assert source != null : "Unable to load game data for " + gameData.getId();
        // Compare only stable, serialized content instead of relying on Lombok equals
        assertObjectsEqual(gameData, source);
    }

    private void assertObjectsEqual(GameData a, GameData b) {
        String gid = a != null ? a.getId() : "UNKNOWN";

        // Basic scalars
        assert safeEquals(a.getId(), b.getId()) : String.format("game[%s] id mismatch: a=%s, b=%s", gid, a.getId(), b.getId());
        assert safeEquals(a.getName(), b.getName()) : String.format("game[%s] name mismatch: a=%s, b=%s", gid, a.getName(), b.getName());
        assert a.isOrderOfPlayReversed() == b.isOrderOfPlayReversed() : String.format("game[%s] orderOfPlayReversed mismatch: a=%s, b=%s", gid, a.isOrderOfPlayReversed(), b.isOrderOfPlayReversed());
        assert safeEquals(a.getTurn(), b.getTurn()) : String.format("game[%s] turn mismatch: a=%s, b=%s", gid, a.getTurn(), b.getTurn());
        assert safeEquals(a.getPhase(), b.getPhase()) : String.format("game[%s] phase mismatch: a=%s, b=%s", gid, a.getPhase(), b.getPhase());
        assert safeEquals(a.getNotes(), b.getNotes()) : String.format("game[%s] notes mismatch: a=%s, b=%s", gid, a.getNotes(), b.getNotes());

        // Players (by name and core fields)
        assert a.getPlayerOrder().equals(b.getPlayerOrder()) : String.format("game[%s] playerOrder mismatch: a=%s, b=%s", gid, a.getPlayerOrder(), b.getPlayerOrder());
        assert a.getPlayers().keySet().equals(b.getPlayers().keySet()) : String.format("game[%s] players keys mismatch: a=%s, b=%s", gid, a.getPlayers().keySet(), b.getPlayers().keySet());
        a.getPlayers().forEach((name, pa) -> {
            PlayerData pb = b.getPlayers().get(name);
            assert pb != null : String.format("game[%s] missing player '%s' in deserialized data", gid, name);
            assert pa.getPool() == pb.getPool() : String.format("game[%s] player '%s' pool mismatch: a=%s, b=%s", gid, name, pa.getPool(), pb.getPool());
            assert pa.getVictoryPoints() == pb.getVictoryPoints() : String.format("game[%s] player '%s' vp mismatch: a=%s, b=%s", gid, name, pa.getVictoryPoints(), pb.getVictoryPoints());
            assert pa.isOusted() == pb.isOusted() : String.format("game[%s] player '%s' ousted mismatch: a=%s, b=%s", gid, name, pa.isOusted(), pb.isOusted());
            assert safeEquals(pa.getNotes(), pb.getNotes()) : String.format("game[%s] player '%s' notes mismatch: a=%s, b=%s", gid, name, pa.getNotes(), pb.getNotes());
        });

        // Current/edge player by name (avoid object identity)
        String aCurrent = a.getCurrentPlayer() != null ? a.getCurrentPlayer().getName() : null;
        String bCurrent = b.getCurrentPlayer() != null ? b.getCurrentPlayer().getName() : null;
        assert safeEquals(aCurrent, bCurrent) : String.format("game[%s] currentPlayer mismatch: a=%s, b=%s", gid, aCurrent, bCurrent);
        String aEdge = a.getEdge() != null ? a.getEdge().getName() : null;
        String bEdge = b.getEdge() != null ? b.getEdge().getName() : null;
        assert safeEquals(aEdge, bEdge) : String.format("game[%s] edge mismatch: a=%s, b=%s", gid, aEdge, bEdge);

        // Cards by id with stable fields
        assert a.getCards().keySet().equals(b.getCards().keySet()) : String.format("game[%s] card ids mismatch: a=%s, b=%s", gid, a.getCards().keySet(), b.getCards().keySet());
        a.getCards().forEach((id, ca) -> {
            CardData cb = b.getCards().get(id);
            assert cb != null : String.format("game[%s] missing card '%s' in deserialized data", gid, id);
            assert safeEquals(ca.getCardId(), cb.getCardId()) : String.format("game[%s] card[%s] cardId mismatch: a=%s, b=%s", gid, id, ca.getCardId(), cb.getCardId());
            assert safeEquals(ca.getName(), cb.getName()) : String.format("game[%s] card[%s] name mismatch: a=%s, b=%s", gid, id, ca.getName(), cb.getName());
            assert ca.isLocked() == cb.isLocked() : String.format("game[%s] card[%s] locked mismatch: a=%s, b=%s", gid, id, ca.isLocked(), cb.isLocked());
            assert ca.isContested() == cb.isContested() : String.format("game[%s] card[%s] contested mismatch: a=%s, b=%s", gid, id, ca.isContested(), cb.isContested());
            assert ca.getType() == cb.getType() : String.format("game[%s] card[%s] type mismatch: a=%s, b=%s", gid, id, ca.getType(), cb.getType());
            assert ca.getCapacity() == cb.getCapacity() : String.format("game[%s] card[%s] capacity mismatch: a=%s, b=%s", gid, id, ca.getCapacity(), cb.getCapacity());
            assert ca.getCounters() == cb.getCounters() : String.format("game[%s] card[%s] counters mismatch: a=%s, b=%s", gid, id, ca.getCounters(), cb.getCounters());
            assert safeEquals(ca.getVotes(), cb.getVotes()) : String.format("game[%s] card[%s] votes mismatch: a=%s, b=%s", gid, id, ca.getVotes(), cb.getVotes());
            assert safeEquals(ca.getNotes(), cb.getNotes()) : String.format("game[%s] card[%s] notes mismatch: a=%s, b=%s", gid, id, ca.getNotes(), cb.getNotes());
            assert safeEquals(ca.getTitle(), cb.getTitle()) : String.format("game[%s] card[%s] title mismatch: a=%s, b=%s", gid, id, ca.getTitle(), cb.getTitle());
            assert ca.isAdvanced() == cb.isAdvanced() : String.format("game[%s] card[%s] advanced mismatch: a=%s, b=%s", gid, id, ca.isAdvanced(), cb.isAdvanced());
            assert safeEquals(ca.getClan(), cb.getClan()) : String.format("game[%s] card[%s] clan mismatch: a=%s, b=%s", gid, id, ca.getClan(), cb.getClan());
            assert safeEquals(ca.getSect(), cb.getSect()) : String.format("game[%s] card[%s] sect mismatch: a=%s, b=%s", gid, id, ca.getSect(), cb.getSect());
            assert safeEquals(ca.getPath(), cb.getPath()) : String.format("game[%s] card[%s] path mismatch: a=%s, b=%s", gid, id, ca.getPath(), cb.getPath());
            assert ca.isMinion() == cb.isMinion() : String.format("game[%s] card[%s] minion mismatch: a=%s, b=%s", gid, id, ca.isMinion(), cb.isMinion());
            assert ca.isPlaytest() == cb.isPlaytest() : String.format("game[%s] card[%s] playtest mismatch: a=%s, b=%s", gid, id, ca.isPlaytest(), cb.isPlaytest());
            assert ca.isInfernal() == cb.isInfernal() : String.format("game[%s] card[%s] infernal mismatch: a=%s, b=%s", gid, id, ca.isInfernal(), cb.isInfernal());
            // Disciplines as sorted list
            assert ca.getDisciplinesSorted().equals(cb.getDisciplinesSorted()) : String.format("game[%s] card[%s] disciplines mismatch: a=%s, b=%s", gid, id, ca.getDisciplinesSorted(), cb.getDisciplinesSorted());
        });
    }

    private boolean safeEquals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    private boolean hasGame(String gameId) {
        return Files.exists(Paths.get(BASE_PATH, "games", gameId, "game.xml"));
    }

    private GameData load(String gameId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path gamePath;
            gamePath = Paths.get(BASE_PATH, "games", gameId, "game.json");
            return mapper.readValue(gamePath.toFile(), GameData.class);
        } catch (IOException e) {
            System.err.println("Something went wrong " + e);
        }
        return null;
    }

    private void save(String gameId, GameData gameData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path gamePath = Paths.get(BASE_PATH, "games", gameId, "game.json");
            mapper.writeValue(gamePath.toFile(), gameData);
        } catch (IOException e) {
            System.err.println("Something went wrong " + e);
        }
    }

    private GameData convertGameData(String gameId) {
        GameState gameState = XmlFileUtils.loadGameState(Paths.get(BASE_PATH, "games", gameId, "game.xml"));
        GameData data = ModelLoader.convertGameState(gameState, gameId);
        GameActions gameActions = XmlFileUtils.loadGameActions(Paths.get(BASE_PATH, "games", gameId, "actions.xml"));
        TurnHistory history = ModelLoader.convertHistory(gameActions);
        String turn = history.getCurrentTurn();
        data.setTurn(turn);
        save(gameId, data);
        save(gameId, history);
        return data;
    }

    private void save(String gameId, TurnHistory history) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path historyPath = Paths.get(BASE_PATH, "games", gameId, "history.json");
            mapper.writeValue(historyPath.toFile(), history.getTurns());
        } catch (IOException e) {
            System.err.println("Something went wrong " + e);
        }
    }

}