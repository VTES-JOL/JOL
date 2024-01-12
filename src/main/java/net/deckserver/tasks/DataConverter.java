package net.deckserver.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import io.azam.ulidj.ULID;
import net.deckserver.DeckParser;
import net.deckserver.storage.json.deck.DeckStats;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.Timestamps;
import net.deckserver.storage.json.system.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DataConverter {

    private static final Logger logger = LoggerFactory.getLogger(DataConverter.class);

    private final Path BASE_PATH;
    private static final Predicate<String> PLAYER_PREDICATE = s -> s.matches("^player\\d{1,5}");
    private static final Predicate<String> GAME_PREDICATE = s -> s.matches("^game\\d{1,5}");
    private static final Predicate<String> DECK_PREDICATE = s -> s.matches("^deck\\d{1,5}");

    // PlayerName, Deck Name, Info
    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();
    // Game Name, Player Name, Status
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    // Player Name, Info
    private final Map<String, PlayerInfo> players = new HashMap<>();
    // GameName, Info
    private final Map<String, GameInfo> games = new HashMap<>();
    private Timestamps timestamps;
    // Id, Name
    private final BiMap<String, String> nameLookup = HashBiMap.create();
    // PlayerId, DeckId, DeckInfo
    private final Table<String, String, DeckInfo> deckLookup = HashBasedTable.create();
    // Game DeckID, Stats
    private final Map<String, DeckStats> deckStats = new HashMap<>();
    private final Set<String> closedGames = new HashSet<>();
    private Properties systemProperties = new Properties();
    // GameId, Properties
    private final Map<String, Properties> gameProperties = new HashMap<>();
    // PlayerId, Properties
    private final Map<String, Properties> playerProperties = new HashMap<>();
    private Set<String> gameDirectoryNames;
    private Set<String> playerDirectoryNames;
    private Path gamesDirectory;
    private Path decksDirectory;
    private final ObjectMapper mapper = new ObjectMapper();

    private int deckCount, convertedDeckCount, emptyDeckCount, unreadableDeckCount = 0;

    public static void main(String[] args) {
        Path base = Paths.get(System.getenv("JOL_DATA"));
        try {
            if (Files.exists(base.resolve("system.properties"))) {
                logger.debug("Legacy system data exists - converting");
                DataConverter dataConverter = new DataConverter(base);
                dataConverter.convertAll();
            }
        } catch (IOException e) {
            logger.error("Unable to convert data", e);
        }
    }

    public DataConverter(Path basePath) {
        this.BASE_PATH = basePath;
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void convertAll() throws IOException {
        preChecks();
        loadTimestamps();
        loadSystem();
        loadPlayers();
        loadPlayerDecks();
        loadGames();
        populateRegistrations();
        cleanUp();
        saveIds();
        deleteOldFolders();
        postChecks();
    }

    private void preChecks() throws IOException {
        File baseFile = BASE_PATH.toFile();
        File[] allDirectories = baseFile.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        assert allDirectories != null;
        Set<String> allDirectoryNames = Arrays.stream(allDirectories).map(File::getName).collect(Collectors.toSet());
        gameDirectoryNames = allDirectoryNames.stream().filter(name -> name.startsWith("game")).collect(Collectors.toSet());
        playerDirectoryNames = allDirectoryNames.stream().filter(name -> name.startsWith("player")).collect(Collectors.toSet());
        gamesDirectory = BASE_PATH.resolve("games");
        if (!Files.exists(gamesDirectory)) {
            Files.createDirectory(gamesDirectory);
        }
        decksDirectory = BASE_PATH.resolve("decks");
        if (!Files.exists(decksDirectory)) {
            Files.createDirectory(decksDirectory);
        }
    }

    private void loadTimestamps() throws IOException {
        Path timestampsPath = BASE_PATH.resolve("timestamps.json");
        timestamps = mapper.readValue(timestampsPath.toFile(), Timestamps.class);
    }

    private void loadSystem() throws IOException {
        systemProperties = loadSystemFile(BASE_PATH);
    }

    private void loadPlayers() {
        systemProperties.stringPropertyNames().stream().filter(PLAYER_PREDICATE)
                .filter(Objects::nonNull)
                .filter(name -> !name.trim().isEmpty())
                .sorted()
                .forEach(playerId -> {
                    try {
                        Properties playerProperty = loadPlayerFile(BASE_PATH, playerId);
                        String playerName = playerProperty.getProperty("name");
                        String email = playerProperty.getProperty("email");
                        String hash = playerProperty.getProperty("hash");
                        String discordId = playerProperty.getProperty("discordID");
                        Set<PlayerRole> roles = parseRoles(playerProperty);
                        if (playerName != null) {
                            playerProperties.put(playerId, playerProperty);
                            nameLookup.forcePut(playerId, playerName);
                            players.put(playerName, new PlayerInfo(playerName, playerId, email, hash, discordId, roles));
                        }
                    } catch (IOException e) {
                        logger.error("Unable to load player properties for {}", playerId, e);
                    }
                });
        logger.debug("Loaded {} players", playerProperties.size());
    }

    private void loadPlayerDeck(String playerId, String deckId, String deckName) {
        deckCount++;
        try {
            String newId = ULID.random();
            Path deckPath = BASE_PATH.resolve(playerId).resolve(deckId + ".txt");
            long deckSize = Files.size(deckPath);
            String contents = getFileContents(deckPath);
            if (contents == null) {
                emptyDeckCount++;
                logger.debug("{} for {} is empty - skipping", deckId, playerId);
            } else {
                logger.debug("{} for {} has size {}", deckId, playerId, deckSize);
                String playerName = nameLookup.get(playerId);
                ExtendedDeck deck = DeckParser.parseDeck(contents);
                DeckFormat format = deck.getDeck().getComments().isEmpty() ? DeckFormat.MODERN : DeckFormat.LEGACY;
                DeckInfo deckInfo = new DeckInfo(newId, deckName, format);
                if (playerName != null) {
                    logger.debug("{} {} {}", playerName, deckName, deckInfo);
                    decks.put(playerName, deckName, deckInfo);
                    deckLookup.put(playerId, deckId, deckInfo);
                    if (format == DeckFormat.MODERN) {
                        convertedDeckCount++;
                        saveModernDeck(deck, deckInfo);
                    } else {
                        copyLegacyDeck(deckPath, deckInfo);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error reading {}.txt for {}", deckId, playerId, e);
        }
    }

    private void loadPlayerDecks() {
        playerProperties.forEach((playerId, playerProperty) -> {
            playerProperty.stringPropertyNames().stream().filter(DECK_PREDICATE).forEach(deckId -> {
                String deckName = playerProperty.getProperty(deckId);
                loadPlayerDeck(playerId, deckId, deckName);
            });
        });
        logger.info("Loaded {} decks, converted {}, {} unreadable, and {} were empty", deckCount, convertedDeckCount, unreadableDeckCount, emptyDeckCount);
    }

    private void loadGames() {
        systemProperties.stringPropertyNames().stream().filter(GAME_PREDICATE).forEach(gameId -> {
            try {
                Properties gameProperty = loadGameFile(BASE_PATH, gameId);
                String gameName = systemProperties.getProperty(gameId);
                boolean isPublic = gameProperty.getProperty("private", "true").equals("false");
                String owner = gameProperty.getProperty("owner");
                if (owner != null) {
                    owner = nameLookup.inverse().get(owner);
                }
                GameStatus status = GameStatus.CLOSED;
                if (gameProperty.getProperty("state", "closed").equals("open")) {
                    status = GameStatus.STARTING;
                } else if (gameProperty.getProperty("state", "closed").equals("closed")) {
                    status = GameStatus.ACTIVE;
                } else {
                    closedGames.add(gameId);
                }
                if (gameName != null && status != GameStatus.CLOSED) {
                    gameProperties.put(gameId, gameProperty);
                    nameLookup.put(gameId, gameName);
                    games.put(gameName, new GameInfo(gameName, gameId, owner, Visibility.fromBoolean(isPublic), status, OffsetDateTime.now()));
                }
            } catch (IOException e) {
                logger.error("Unable to load game properties for {}", gameId, e);
            }
        });
        logger.debug("Loaded {} games", gameProperties.size());
    }

    private void populateRegistrations() {
        games.values().forEach(gameInfo -> {
            String gameId = gameInfo.getId();
            Properties properties = gameProperties.get(gameId);
            properties.stringPropertyNames().stream().filter(PLAYER_PREDICATE).forEach(playerId -> {
                String playerName = nameLookup.get(playerId);
                String gameName = nameLookup.get(gameId);
                String deckId = loadGameDeck(gameId, playerId);
                DeckStats stats = deckStats.get(deckId);
                RegistrationStatus status = new RegistrationStatus(deckId);
                status.setDeckId(deckId);
                status.setDeckName("");
                status.setValid(stats.isValid());
                status.setSummary(stats.getSummary());
                registrations.put(gameName, playerName, status);
            });
        });

        players.values().forEach(playerInfo -> {
            String playerId = playerInfo.getId();
            Properties properties = playerProperties.get(playerId);
            properties.stringPropertyNames().stream().filter(GAME_PREDICATE).forEach(gameId -> {
                boolean invited = properties.getProperty(gameId).equals("invited");
                if (invited && games.containsKey(gameId)) {
                    String playerName = nameLookup.get(playerId);
                    String gameName = nameLookup.get(gameId);
                    RegistrationStatus status = new RegistrationStatus(OffsetDateTime.now());
                    registrations.put(gameName, playerName, status);
                }
            });
        });
    }

    private String loadGameDeck(String gameId, String playerId) {
        try {
            Path deckPath = BASE_PATH.resolve(gameId).resolve(playerId + ".deck");
            String contents = getFileContents(deckPath);
            if (contents == null) {
                logger.error("deck {} for {} is empty - skipping", playerId, gameId);
            } else {
                String newId = ULID.random();
                ExtendedDeck deck = DeckParser.parseDeck(contents);
                DeckFormat format = deck.getDeck().getComments().isEmpty() ? DeckFormat.MODERN : DeckFormat.LEGACY;
                Path gamePath = gamesDirectory.resolve(gameId);
                if (!Files.exists(gamePath)) {
                    Files.createDirectory(gamePath);
                }
                deckStats.put(newId, deck.getStats());
                if (format == DeckFormat.MODERN) {
                    saveModernGameDeck(deck, newId, gameId);
                } else {
                    copyLegacyGameDeck(deckPath, newId, gameId);
                }
                return newId;
            }
        } catch (IOException e) {
            logger.error("Error reading {}.deck for {}", playerId, gameId, e);
        }
        return null;
    }

    private void copyGameData(String gameId) throws IOException {
        String[] extensions = new String[]{"xml", "txt", "json"};
        Path gameDirectory = BASE_PATH.resolve(gameId);
        Path newGameDirectory = gamesDirectory.resolve(gameId);
        Collection<File> gameFiles = FileUtils.listFiles(gameDirectory.toFile(), extensions, false);
        for (File gameFile : gameFiles) {
            logger.debug("Moving {} to {}", gameFile, newGameDirectory);
            FileUtils.moveFileToDirectory(gameFile, newGameDirectory.toFile(), true);
        }
    }

    private void cleanUp() throws IOException {
        // Move active games
        Set<String> activeGames = games.values().stream()
                .map(GameInfo::getId)
                .collect(Collectors.toSet());

        for (String gameId : activeGames) {
            copyGameData(gameId);
        }

        // Fix timestamps
        timestamps.getGameTimestamps().entrySet().removeIf(entry -> closedGames.contains(entry.getKey()));
    }

    private void saveIds() throws IOException {
        mapper.writeValue(BASE_PATH.resolve("registrations.json").toFile(), registrations);
        mapper.writeValue(BASE_PATH.resolve("games.json").toFile(), games);
        mapper.writeValue(BASE_PATH.resolve("players.json").toFile(), players);
        mapper.writeValue(BASE_PATH.resolve("decks.json").toFile(), decks);
        mapper.writeValue(BASE_PATH.resolve("timestamps.json").toFile(), timestamps);
    }

    private void postChecks() {
        games.values().forEach(gameInfo -> {
            String gameId = gameInfo.getId();
            String gameName = gameInfo.getName();
            String playerId = gameInfo.getOwner();
            String playerName = nameLookup.get(playerId);
            Path gamePath = gamesDirectory.resolve(gameId);
            Path gameDataPath = gamePath.resolve("game.xml");
            Path gameActionsPath = gamePath.resolve("actions.xml");
            assert Files.exists(gamePath);
            assert Files.exists(gameDataPath);
            assert Files.exists(gameActionsPath);
            assert players.containsKey(playerName);
            registrations.row(gameName).values().stream()
                    .filter(status -> status.getDeckId() != null)
                    .forEach(status -> {
                                String deckId = status.getDeckId();
                                Path gameDeckPath = gamePath.resolve(deckId + ".json");
                                assert Files.exists(gameDeckPath);
                            }
                    );
        });
        decks.values().forEach(deckInfo -> {
            DeckFormat format = deckInfo.getFormat();
            String deckId = deckInfo.getDeckId();
            String deckExtension = format == DeckFormat.LEGACY ? ".txt" : ".json";
            Path deckPath = decksDirectory.resolve(deckId + deckExtension);
            assert Files.exists(deckPath);
        });
        timestamps.getPlayerTimestamps().keySet().forEach(playerName -> {
            assert players.containsKey(playerName);
        });
        timestamps.getGameTimestamps().forEach((gameName, gameEntry) -> {
            assert games.containsKey(gameName);
            gameEntry.getPlayerTimestamps().keySet().forEach(playerName -> {
                assert players.containsKey(playerName);
            });
        });
    }

    private void deleteOldFolders() throws IOException {
        // Delete old folders
        Set<String> allDirectoryNames = new HashSet<>();
        allDirectoryNames.addAll(playerDirectoryNames);
        allDirectoryNames.addAll(gameDirectoryNames);
        Set<String> directoriesToDelete = new HashSet<>(allDirectoryNames);
        // keep the directories for active games
        games.keySet().forEach(directoriesToDelete::remove);
        for (String directoryId : directoriesToDelete) {
            Path directory = BASE_PATH.resolve(directoryId);
            logger.debug("Moving {} to deleted", directory);
            gameDirectoryNames.remove(directoryId);
            playerDirectoryNames.remove(directoryId);
            FileUtils.deleteDirectory(directory.toFile());
        }

        assert gameDirectoryNames.isEmpty();
        assert playerDirectoryNames.isEmpty();

        FileUtils.delete(BASE_PATH.resolve("system.properties").toFile());
    }

    private Properties loadProperties(Path path) throws IOException {
        Properties properties = new Properties();
        File propertiesFile = path.toFile();
        if (!propertiesFile.exists()) {
            System.out.println("Unable to find properties file at location " + path);
            System.exit(1);
        }
        try (FileReader systemReader = new FileReader(propertiesFile)) {
            properties.load(systemReader);
            return properties;
        }
    }

    private Properties loadSystemFile(Path basePath) throws IOException {
        return loadProperties(basePath.resolve("system.properties"));
    }

    private Properties loadPlayerFile(Path basePath, String playerId) throws IOException {
        return loadProperties(basePath.resolve(playerId).resolve("player.properties"));
    }

    private Properties loadGameFile(Path basePath, String gameId) throws IOException {
        return loadProperties(basePath.resolve(gameId).resolve("game.properties"));
    }

    private String getFileContents(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            logger.debug("Unable to read deck file", e);
            unreadableDeckCount++;
            return null;
        }
    }

    private void saveModernDeck(ExtendedDeck deck, DeckInfo info) throws IOException {
        Path deckPath = decksDirectory.resolve(info.getDeckId() + ".json");
        mapper.writeValue(deckPath.toFile(), deck);
    }

    private void saveModernGameDeck(ExtendedDeck deck, String deckId, String gameId) throws IOException {
        Path deckPath = gamesDirectory.resolve(gameId).resolve(deckId + ".json");
        mapper.writeValue(deckPath.toFile(), deck);
    }

    private void copyLegacyDeck(Path original, DeckInfo info) throws IOException {
        Path deckPath = decksDirectory.resolve(info.getDeckId() + ".txt");
        FileUtils.copyFile(original.toFile(), deckPath.toFile());
    }

    private void copyLegacyGameDeck(Path original, String deckId, String gameId) throws IOException {
        Path deckPath = gamesDirectory.resolve(gameId).resolve(deckId + ".txt");
        FileUtils.copyFile(original.toFile(), deckPath.toFile());
    }

    private Set<PlayerRole> parseRoles(Properties playerProperty) {
        Set<PlayerRole> roles = new HashSet<>();
        if (playerProperty.getProperty("admin", "no").equals("yes")) {
            roles.add(PlayerRole.ADMIN);
        }
        if (playerProperty.getProperty("admin", "no").equals("super")) {
            roles.add(PlayerRole.SUPER_USER);
        }
        if (playerProperty.getProperty("judge", "no").equals("yes")) {
            roles.add(PlayerRole.JUDGE);
        }
        return roles;
    }
}
