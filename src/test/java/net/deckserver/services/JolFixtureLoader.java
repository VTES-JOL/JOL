package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jpa.entity.*;
import net.deckserver.jpa.repository.GameActivityRepository;
import net.deckserver.jpa.repository.GameHistoryRepository;
import net.deckserver.jpa.repository.TournamentRepository;
import net.deckserver.storage.json.game.GameTimestampEntry;
import net.deckserver.storage.json.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Populates the H2 test database from fixture JSON files in src/test/resources/data/.
 * Called once per JVM by JolServiceExtension before any service singleton is first accessed.
 */
class JolFixtureLoader {

    private static final Logger logger = LoggerFactory.getLogger(JolFixtureLoader.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final EntityManager em;
    private final Path dataDir;

    private final Map<String, String> playerNameToId = new HashMap<>();
    private final Map<String, String> gameNameToId = new HashMap<>();
    private final Set<String> insertedGameIds = new HashSet<>();

    JolFixtureLoader(EntityManager em) {
        this.em = em;
        this.dataDir = Path.of("src/test/resources/data");
    }

    void load() throws IOException {
        loadPlayers();
        em.flush();
        loadGames();
        em.flush();
        loadGameStatesAndChat();
        em.flush();
        loadDeckInfo();
        em.flush();
        loadDeckContent();
        em.flush();
        loadRegistrations();
        em.flush();
        loadPlayerActivity();
        em.flush();
        loadGameActivity();
        em.flush();
        loadTournaments();
        em.flush();
        loadGameHistory();
    }

    private void loadPlayers() throws IOException {
        File file = dataDir.resolve("players.json").toFile();
        if (!file.exists()) return;
        Map<String, PlayerInfo> players = objectMapper.readValue(file, new TypeReference<>() {});
        players.values().forEach(info -> {
            em.persist(PlayerEntity.from(info));
            playerNameToId.put(info.getName(), info.getId());
        });
        logger.info("Fixture: {} players", players.size());
    }

    private void loadGames() throws IOException {
        File file = dataDir.resolve("games.json").toFile();
        if (!file.exists()) return;
        Map<String, GameInfo> games = objectMapper.readValue(file, new TypeReference<>() {});
        games.values().forEach(info -> {
            String ownerId = info.getOwner() != null ? playerNameToId.get(info.getOwner()) : null;
            em.persist(GameInfoEntity.from(info, ownerId));
            gameNameToId.put(info.getName(), info.getId());
            insertedGameIds.add(info.getId());
        });
        logger.info("Fixture: {} games", games.size());
    }

    private void loadGameStatesAndChat() throws IOException {
        Path gamesDir = dataDir.resolve("games");
        if (!Files.isDirectory(gamesDir)) return;
        try (var dirs = Files.list(gamesDir)) {
            dirs.filter(Files::isDirectory).forEach(gameDir -> {
                String gameId = gameDir.getFileName().toString();
                // Insert a synthetic jol_game entry for test-only game directories
                if (!insertedGameIds.contains(gameId)) {
                    GameInfoEntity synthetic = buildSyntheticGame(gameId);
                    em.persist(synthetic);
                    insertedGameIds.add(gameId);
                }
                loadGameState(gameDir, gameId);
            });
        }
        // Chat FK references game_state, so flush states first then insert chats
        em.flush();
        try (var dirs = Files.list(gamesDir)) {
            dirs.filter(Files::isDirectory).forEach(gameDir -> {
                String gameId = gameDir.getFileName().toString();
                loadGameChat(gameDir, gameId);
            });
        }
    }

    private void loadGameState(Path gameDir, String gameId) {
        File stateFile = gameDir.resolve("game.json").toFile();
        if (!stateFile.exists()) return;
        try {
            GameStateEntity entity = new GameStateEntity();
            entity.setGameId(gameId);
            entity.setState(Files.readString(stateFile.toPath()));
            entity.setUpdatedAt(OffsetDateTime.now());
            em.persist(entity);
        } catch (IOException e) {
            logger.warn("Fixture: could not load game state {}", gameId, e);
        }
    }

    private void loadGameChat(Path gameDir, String gameId) {
        File histFile = gameDir.resolve("history.json").toFile();
        if (!histFile.exists()) return;
        try {
            GameChatEntity entity = new GameChatEntity();
            entity.setGameId(gameId);
            entity.setHistory(Files.readString(histFile.toPath()));
            entity.setUpdatedAt(OffsetDateTime.now());
            em.persist(entity);
        } catch (IOException e) {
            logger.warn("Fixture: could not load game chat {}", gameId, e);
        }
    }

    private GameInfoEntity buildSyntheticGame(String gameId) {
        GameInfo info = new GameInfo();
        info.setName(gameId);
        info.setId(gameId);
        info.setVisibility(Visibility.PUBLIC);
        info.setStatus(GameStatus.ACTIVE);
        info.setGameFormat(GameFormat.STANDARD);
        info.setCreated(OffsetDateTime.now());
        info.setVersion(GameInfo.Version.DATA_FIX);
        return GameInfoEntity.from(info, null);
    }

    private void loadDeckInfo() throws IOException {
        File file = dataDir.resolve("decks.json").toFile();
        if (!file.exists()) return;
        Map<String, Map<String, DeckInfo>> allDecks = objectMapper.readValue(file, new TypeReference<>() {});
        Set<String> seenDeckIds = new HashSet<>();
        allDecks.forEach((playerName, deckMap) -> {
            String playerId = playerNameToId.get(playerName);
            if (playerId == null) return;
            deckMap.forEach((deckName, deckInfo) -> {
                if (seenDeckIds.add(deckInfo.getDeckId())) {
                    em.persist(DeckInfoEntity.from(playerId, deckName, deckInfo));
                }
            });
        });
        logger.info("Fixture: {} deck infos", seenDeckIds.size());
    }

    private void loadDeckContent() throws IOException {
        Path decksDir = dataDir.resolve("decks");
        if (!Files.isDirectory(decksDir)) return;
        try (var files = Files.list(decksDir)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(deckFile -> {
                try {
                    String deckId = deckFile.getFileName().toString().replace(".json", "");
                    em.persist(new DeckContentEntity(deckId, Files.readString(deckFile)));
                } catch (IOException e) {
                    logger.warn("Fixture: could not load deck content {}", deckFile.getFileName(), e);
                }
            });
        }
    }

    private void loadRegistrations() throws IOException {
        File file = dataDir.resolve("registrations.json").toFile();
        if (!file.exists()) return;
        Map<String, Map<String, RegistrationStatus>> allRegs = objectMapper.readValue(file, new TypeReference<>() {});
        allRegs.forEach((gameName, playerMap) -> {
            String gameId = gameNameToId.get(gameName);
            if (gameId == null) return;
            playerMap.forEach((playerName, status) -> {
                String playerId = playerNameToId.get(playerName);
                if (playerId == null) return;
                if (status.getDeckContent() == null && status.getDeckId() != null) {
                    tryLoadRegistrationDeckContent(gameId, status);
                }
                em.persist(RegistrationEntity.from(gameId, playerId, status));
            });
        });
    }

    private void tryLoadRegistrationDeckContent(String gameId, RegistrationStatus status) {
        Path deckFile = dataDir.resolve("games").resolve(gameId).resolve(status.getDeckId() + ".json");
        if (deckFile.toFile().exists()) {
            try {
                status.setDeckContent(Files.readString(deckFile));
            } catch (IOException e) {
                logger.warn("Fixture: could not load registration deck content {} {}", gameId, status.getDeckId(), e);
            }
        }
    }

    private void loadPlayerActivity() throws IOException {
        File file = dataDir.resolve("player-timestamps.json").toFile();
        if (!file.exists()) return;
        Map<String, OffsetDateTime> timestamps = objectMapper.readValue(file, new TypeReference<>() {});
        timestamps.forEach((playerName, lastSeen) -> {
            String playerId = playerNameToId.get(playerName);
            if (playerId == null) return;
            PlayerActivityEntity entity = new PlayerActivityEntity();
            entity.setPlayerId(playerId);
            entity.setLastSeen(lastSeen);
            em.persist(entity);
        });
    }

    private void loadGameActivity() throws IOException {
        File file = dataDir.resolve("game-timestamps.json").toFile();
        if (!file.exists()) return;
        Map<String, GameTimestampEntry> entries = objectMapper.readValue(file, new TypeReference<>() {});
        // Reuse repository pattern: save() looks up GameInfoEntity by game_name
        GameActivityRepository repo = new GameActivityRepository();
        entries.forEach((gameName, entry) -> repo.save(em, gameName, entry));
    }

    private void loadTournaments() throws IOException {
        File file = dataDir.resolve("tournaments.json").toFile();
        if (!file.exists()) return;
        List<TournamentDefinition> tournaments = objectMapper.readValue(file, new TypeReference<>() {});
        TournamentRepository repo = new TournamentRepository();
        tournaments.forEach(t -> repo.save(em, t));
        logger.info("Fixture: {} tournaments", tournaments.size());
    }

    private void loadGameHistory() throws IOException {
        File file = dataDir.resolve("pastGames.json").toFile();
        if (!file.exists()) return;
        Map<OffsetDateTime, GameHistory> histories = objectMapper.readValue(file, new TypeReference<>() {});
        GameHistoryRepository repo = new GameHistoryRepository();
        histories.forEach((recordedAt, history) -> repo.save(em, recordedAt, history));
        logger.info("Fixture: {} game histories", histories.size());
    }
}
