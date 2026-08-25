package net.deckserver.jobs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.services.CardService;
import net.deckserver.services.DataPaths;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.game.GameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameDataConversion {

    private static final String BASE_PATH = DataPaths.baseDir().toString();
    private static final Logger LOGGER = LoggerFactory.getLogger(GameDataConversion.class);

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

    private GameData load(String gameId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            Path gamePath = Paths.get(BASE_PATH, "games", gameId, "game.json");
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

}