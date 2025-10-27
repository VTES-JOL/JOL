/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.azam.ulidj.ULID;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.enums.*;
import net.deckserver.game.validators.DeckValidator;
import net.deckserver.game.validators.ValidationResult;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.services.*;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class JolAdmin {

    private static final Logger logger = LoggerFactory.getLogger(JolAdmin.class);

    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private static final Map<String, PlayerModel> pmap = new ConcurrentHashMap<>();
    // Cache of users / status
    private static final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(JolAdmin::loadGameState);

    public static String getDate() {
        return OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

    public static int getRefreshInterval(String gameName) {
        OffsetDateTime lastChanged = PlayerGameActivityService.getGameTimestamp(gameName);
        OffsetDateTime now = OffsetDateTime.now();
        long interval = Duration.between(lastChanged, now).getSeconds();
        if (interval < 60) return 5000;
        if (interval < 180) return 10000;
        if (interval < 300) return 30000;
        return 60000;
    }

    public static synchronized PlayerModel getPlayerModel(String name) {
        if (name == null) {
            return new PlayerModel(null, false);
        } else {
            PlayerModel playerModel = pmap.computeIfAbsent(name, k -> new PlayerModel(k, true));
            GlobalChatService.subscribe(playerModel);
            return playerModel;
        }
    }

    public static synchronized GameModel getGameModel(String name) {
        return gmap.computeIfAbsent(name, n -> new GameModel(getGame(name)));
    }

    public static List<String> getWho() {
        return PlayerService.activeUsers().stream().map(UserSummary::getName).toList();
    }

    public static void remove(String player) {
        logger.debug("removing player");
        if (player != null) {
            pmap.remove(player);
            for (GameModel gameModel : gmap.values()) {
                JolAdmin.resetView(player, gameModel.getName());
            }
        }
    }

    public static synchronized void createGame(String gameName, Boolean isPublic, GameFormat format, String playerName) {
        createGame(gameName, isPublic, format, playerName, ULID.random());
    }

    public static synchronized void createGame(String gameName, Boolean isPublic, GameFormat format, String playerName, String gameId) {
        logger.trace("Creating game {} for player {}", gameName, playerName);
        if (gameName.length() > 2 || notExistsGame(gameName)) {
            try {
                GameService.create(gameName, gameId, playerName, Visibility.fromBoolean(isPublic), format);
            } catch (Exception e) {
                logger.error("Error creating game", e);
            }
        }
    }

    public static synchronized void chat(String player, String message) {
        GlobalChatService.chat(player, message);
    }

    public static synchronized void rollbackGame(String gameName, String adminName, String turn) {
        String id = GameService.get(gameName).getId();
        logger.info("Rolling back game {} for turn {}", gameName, turn);
        JolGame game = GameService.loadSnapshot(id, turn);
        ChatService.sendMessage(id, "SYSTEM", "Game state rolled back by administrator: " + adminName);
        saveGameState(game, true);
        gameCache.refresh(gameName);
    }

    public static boolean notExistsGame(String name) {
        return name == null || !GameService.existsGame(name);
    }

    public static DeckFormat getDeckFormat(String playerName, String deckName) {
        return DeckService.get(playerName, deckName).getFormat();
    }

    public static Set<String> getTags(String playerName, String deckName) {
        return DeckService.get(playerName, deckName).getGameFormats();
    }

    public static synchronized Deck getGameDeck(String gameName, String playerName) {
        return Optional.ofNullable(RegistrationService.getRegistration(gameName, playerName))
                .map(status -> {
                    String deckId = status.getDeckId();
                    String gameId = getGameId(gameName);
                    ExtendedDeck extendedDeck = DeckService.getGameDeck(gameId, deckId);
                    return extendedDeck.getDeck();
                }).orElse(null);
    }

    public static synchronized void selectDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).loadDeck(deckName);
        }
    }

    public static synchronized void newDeck(String playerName) {
        if (playerName != null) {
            getPlayerModel(playerName).clearDeck();
        }
    }

    public static synchronized void saveDeck(String playerName, String deckName, String contents) {
        if (playerName != null && contents != null && deckName != null) {
            deckName = deckName.trim();
            ExtendedDeck deck = DeckParser.parseDeck(contents);
            deck.getDeck().setName(deckName);
            PlayerModel playerModel = getPlayerModel(playerName);
            playerModel.setDeck(deck);
            playerModel.setContents(contents);
            Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
            DeckInfo deckInfo = Optional.ofNullable(DeckService.get(playerName, deckName)).orElse(new DeckInfo(ULID.random(), deckName, DeckFormat.TAGGED, tags));
            deckInfo.setFormat(DeckFormat.MODERN);
            deckInfo.setGameFormats(tags);
            DeckService.addDeck(playerName, deckName, deckInfo);
            DeckService.saveDeck(deckInfo.getDeckId(), deck);
        }
    }

    public static synchronized void deleteDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).clearDeck();
            DeckService.remove(playerName, deckName);
        }
    }

    public static synchronized JolGame getGame(String gameName) {
        return gameCache.get(gameName);
    }

    public static void saveGameState(JolGame game) {
        saveGameState(game, false);
    }

    public static void saveGameState(JolGame game, boolean silent) {
        if (!silent) {
            PlayerGameActivityService.setGameTimestamp(game.getName());
        }
        GameService.saveGame(game);
    }

    public static synchronized void registerDeck(String gameName, String playerName, String deckName) {
        deckName = deckName.trim();
        DeckInfo deckInfo = DeckService.get(playerName, deckName);
        GameInfo gameInfo = GameService.get(gameName);
        String result = "Successfully registered " + deckName + " in game " + gameName;
        try {
            if (!gameInfo.getStatus().equals(GameStatus.STARTING)) {
                result = "Game is not starting.  Unable to register deck.";
                throw new IllegalStateException(result);
            }
            if (deckInfo == null) {
                result = "Unable to find deck '" + deckName + "'.";
                throw new IllegalStateException(result);
            }
            if (deckInfo.getFormat().equals(DeckFormat.LEGACY)) {
                result = "Unable to register legacy formats in new games.  Please edit, and save deck to convert to new format.";
                throw new IllegalStateException(result);
            }
            if (RegistrationService.getRegisteredPlayerCount(gameName) >= 5) {
                result = "Unable to register deck.  Already has 5 players registered.";
                throw new IllegalStateException(result);
            }
            ExtendedDeck extendedDeck = DeckService.getDeck(deckInfo.getDeckId());
            if (!validateDeck(extendedDeck.getDeck(), gameInfo.getGameFormat()).isValid()) {
                result = "Unable to register deck.  Not valid for defined format.";
                throw new IllegalStateException(result);
            }
            if (!RegistrationService.isInvited(gameName, playerName)) {
                result = "Unable to register deck in game that has no invite";
                throw new IllegalStateException(result);
            }
            boolean copySuccess = DeckService.copyDeck(deckInfo.getDeckId(), gameInfo.getId());
            if (!copySuccess) {
                result = "Unable to copy deck file to game";
                throw new IllegalStateException(result);
            }
            RegistrationService.registerDeck(gameName, playerName, deckInfo.getDeckId(), deckName, extendedDeck.getStats().getSummary());

            // Reset game time to the current time to extend idle timeout
            gameInfo.setCreated(OffsetDateTime.now());

            long registeredPlayers = RegistrationService.getRegisteredPlayerCount(gameName);
            if (registeredPlayers == gameInfo.getGameFormat().getPlayerCount()) {
                startGame(gameName);
            }
        } catch (IllegalStateException exception) {
            logger.debug(exception.getMessage());
        } finally {
            getPlayerModel(playerName).setMessage(result);
        }
    }

    public static void recordPlayerAccess(String playerName) {
        if (playerName != null) {
            PlayerActivityService.recordPlayerAccess(playerName);
            PlayerService.refreshActive(playerName);
        }
    }

    public static synchronized void recordPlayerAccess(String playerName, String gameName) {
        PlayerGameActivityService.recordPlayerAccess(playerName, gameName);
    }

    public static OffsetDateTime getGameTimeStamp(String gameName) {
        return PlayerGameActivityService.getGameTimestamp(gameName);
    }

    public static OffsetDateTime getPlayerAccess(String playerName) {
        return PlayerActivityService.getPlayerAccess(playerName);
    }

    public static OffsetDateTime getPlayerAccess(String playerName, String gameName) {
        return PlayerGameActivityService.getPlayerAccess(playerName, gameName);
    }

    public static boolean isPlayerPinged(String playerName, String gameName) {
        return PlayerGameActivityService.isPlayerPinged(playerName, gameName);
    }

    public static boolean pingPlayer(String playerName, String gameName) {
        if (isPlayerPinged(playerName, gameName)) {
            logger.debug("{} already pinged for {}; not pinging again", playerName, gameName);
            return false;
        }

        PlayerInfo player = PlayerService.get(playerName);
        PlayerGameActivityService.pingPlayer(playerName, gameName);
        NotificationService.pingPlayer(playerName, player.getDiscordId(), gameName);
        return true;
    }

    public static void clearPing(String playerName, String gameName) {
        PlayerGameActivityService.clearPing(playerName, gameName);
    }

    public static Set<String> getGameNames() {
        return GameService.getGameNames();
    }

    public static String getEmail(String player) {
        return PlayerService.get(player).getEmail();
    }

    public static String getDiscordID(String player) {
        return PlayerService.get(player).getDiscordId();
    }

    public static String getVeknID(String player) {
        return PlayerService.get(player).getVeknId();
    }

    public static synchronized void setImageTooltipPreference(String player, boolean value) {
        PlayerService.get(player).setShowImages(value);
    }

    public static synchronized boolean getImageTooltipPreference(String player) {
        if (player == null) {
            return true;
        }
        return PlayerService.get(player).isShowImages();
    }

    public static synchronized boolean isAdmin(String player) {
        return PlayerService.get(player).getRoles().contains(PlayerRole.ADMIN);
    }

    public static synchronized boolean isPlaytester(String player) {
        return PlayerService.get(player).getRoles().contains(PlayerRole.PLAYTESTER);
    }

    public static synchronized boolean isSuperUser(String playerName) {
        return PlayerService.get(playerName).getRoles().contains(PlayerRole.SUPER_USER);
    }

    public static synchronized boolean isJudge(String playerName) {
        return PlayerService.get(playerName).getRoles().contains(PlayerRole.JUDGE);
    }

    public static synchronized String getOwner(String gameName) {
        String playerName = GameService.get(gameName).getOwner();
        if (!PlayerService.existsPlayer(playerName)) {
            playerName = "SYSTEM";
        }
        return playerName;
    }

    public static synchronized void unInvitePlayer(String gameName, String playerName) {
        if (isStarting(gameName)) {
            RegistrationService.removePlayer(gameName, playerName);
        }
    }

    public static synchronized boolean isStarting(String gameName) {
        return GameService.get(gameName).getStatus().equals(GameStatus.STARTING);
    }

    public static boolean isActive(String gameName) {
        return GameService.get(gameName).getStatus().equals(GameStatus.ACTIVE);
    }

    public static boolean isAlive(String gameName, String playerName) {
        return loadGameState(gameName).getPool(playerName) > 0;
    }

    public static boolean isPrivate(String gameName) {
        return GameService.get(gameName).getVisibility().equals(Visibility.PRIVATE);
    }

    public static boolean isPublic(String gameName) {
        return GameService.get(gameName).getVisibility().equals(Visibility.PUBLIC);
    }

    public static void startGame(String gameName, List<String> players) {
        GameInfo gameInfo = GameService.get(gameName);
        GameData gameData = new GameData(gameInfo.getId(), gameName);
        JolGame game = new JolGame(gameInfo.getId(), gameData);
        RegistrationService.getGameRegistrations(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                Deck deck = getGameDeck(gameName, playerName);
                game.addPlayer(playerName, deck);
            }
        });
        if (!game.getPlayers().isEmpty() && game.getPlayers().size() <= 5) {
            game.startGame(players);
            saveGameState(game);
            gameInfo.setStatus(GameStatus.ACTIVE);
            pingPlayer(game.getActivePlayer(), gameName);
        }
    }

    public static synchronized void startGame(String gameName) {
        List<String> players = new ArrayList<>();
        RegistrationService.getGameRegistrations(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                players.add(playerName);
            }
        });
        Collections.shuffle(players, new SecureRandom());
        startGame(gameName, players);
    }

    public static synchronized void endGame(String gameName, boolean graceful) {
        GameInfo gameInfo = GameService.get(gameName);
        // try and generate stats for game
        if (gameInfo.getStatus().equals(GameStatus.ACTIVE)) {
            JolGame gameData = getGame(gameName);
            if (gameData.getPlayers().size() >= 4 && graceful) {
                GameHistory history = new GameHistory();
                history.setName(gameName);
                String startTime = gameInfo.getCreated() != null ? gameInfo.getCreated().format(ISO_OFFSET_DATE_TIME) : " --- ";
                String endTime = OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
                history.setStarted(startTime);
                history.setEnded(endTime);
                PlayerResult winner = null;
                double topVP = 0.0;
                boolean hasVp = false;
                for (String player : gameData.getPlayers()) {
                    PlayerResult result = new PlayerResult();
                    String deckName = Optional.ofNullable(RegistrationService.getRegistration(gameName, player)).map(RegistrationStatus::getDeckName).orElse("-- no deck name --");
                    double victoryPoints = gameData.getVictoryPoints(player);
                    if (victoryPoints > 0) {
                        hasVp = true;
                    }
                    result.setPlayerName(player);
                    result.setDeckName(deckName);
                    result.setVictoryPoints(victoryPoints);
                    if (victoryPoints >= 2.0) {
                        if (winner == null) {
                            winner = result;
                            topVP = victoryPoints;
                        } else if (victoryPoints > topVP) {
                            winner = result;
                            topVP = victoryPoints;
                        } else {
                            winner = null;
                        }
                    }
                    history.getResults().add(result);
                }
                if (winner != null) {
                    winner.setGameWin(true);
                }
                if (hasVp) {
                    HistoryService.addGame(OffsetDateTime.now(), history);
                }
            }
        }
        // Clear out data
        RegistrationService.clearRegistrations(gameName);
        GameService.remove(gameName, gameInfo.getId());
        PlayerGameActivityService.clearGame(gameName);
        gmap.remove(gameName);

    }

    public static String getDeckId(String playerName, String deckName) {
        return DeckService.get(playerName, deckName).getDeckId();
    }

    public static String getGameId(String gameName) {
        return GameService.get(gameName).getId();
    }

    public static synchronized void replacePlayer(String gameName, String existingPlayer, String newPlayer) {
        RegistrationStatus existingRegistration = RegistrationService.getRegistration(gameName, existingPlayer);
        RegistrationStatus newRegistration = RegistrationService.getRegistration(gameName, newPlayer);
        // Only replace player if existing player is in the game, and the new player isn't
        if (existingRegistration != null && newRegistration == null) {
            JolGame game = getGame(gameName);
            game.replacePlayer(existingPlayer, newPlayer);
            saveGameState(game);
            resetView(existingPlayer, gameName);
            // Set up the registrations
            RegistrationService.put(gameName, newPlayer, existingRegistration);
            RegistrationService.removePlayer(gameName, existingPlayer);
        }
    }

    public static synchronized void deletePLayer(String playerName) {
        Map<String, RegistrationStatus> playerRegistrations = RegistrationService.getPlayerRegistrations(playerName);
        if (playerRegistrations.isEmpty()) {
            logger.info("Deleting unused player {}", playerName);
            PlayerService.remove(playerName);
            DeckService.getPlayerDecks(playerName).forEach((deckName, deckInfo) -> DeckService.remove(playerName, deckName));
        } else {
            logger.info("Unable to delete an active player - {}", playerName);
        }
    }

    public static synchronized void setJudge(String playerName, boolean value) {
        PlayerInfo info = PlayerService.get(playerName);
        setRole(info, PlayerRole.JUDGE, value);
    }

    public static synchronized void setAdmin(String playerName, boolean value) {
        PlayerInfo info = PlayerService.get(playerName);
        setRole(info, PlayerRole.ADMIN, value);
    }

    public static synchronized void setPlaytester(String playerName, boolean value) {
        PlayerInfo info = PlayerService.get(playerName);
        setRole(info, PlayerRole.PLAYTESTER, value);
    }

    public static synchronized void setSuperUser(String playerName, boolean value) {
        PlayerInfo info = PlayerService.get(playerName);
        setRole(info, PlayerRole.SUPER_USER, value);
    }

    public static OffsetDateTime getCreatedTime(String gameName) {
        return Optional.ofNullable(GameService.get(gameName))
                .map(GameInfo::getCreated)
                .orElse(null);
    }

    public static synchronized void resetView(String playerName, String gameName) {
        getGameModel(gameName).resetView(playerName);
    }

    public static synchronized void endTurn(String gameName, String adminName) {
        JolGame game = getGame(gameName);
        String id = GameService.get(gameName).getId();
        ChatService.sendMessage(id, "SYSTEM", "Turn ended by administrator: " + adminName);
        game.newTurn();
        pingPlayer(game.getActivePlayer(), gameName);
    }

    public static boolean isInRole(String username, String role) {
        return PlayerService.get(username).getRoles().contains(PlayerRole.valueOf(role));
    }

    public static List<String> getPings(String gameName) {
        var entry = PlayerGameActivityService.getGameTimestamps().get(gameName);
        if (entry == null) {
            return List.of();
        }

        return entry.getPlayerPings().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static List<String> getPingList(String gameName) {
        return getGame(gameName).getValidPlayers()
                .stream()
                .filter(player -> !JolAdmin.isPlayerPinged(player, gameName))
                .collect(Collectors.toList());
    }

    public static String getFormat(String gameName) {
        return GameService.get(gameName).getGameFormat().toString();
    }

    public static synchronized void validateDeck(String playerName, String contents, GameFormat format) {
        PlayerModel model = getPlayerModel(playerName);
        ExtendedDeck deck = DeckParser.parseDeck(contents);
        ValidationResult result = validateDeck(deck.getDeck(), format);
        if (result.isValid()) {
            deck.setErrors(List.of("No errors found.  Deck is valid for " + format.getLabel() + "."));
        } else {
            deck.setErrors(result.getErrors());
        }
        ExtendedDeck existingDeck = model.getDeck();
        if (existingDeck != null) {
            String deckName = model.getDeck().getDeck().getName();
            deck.getDeck().setName(deckName);
        }
        model.setDeck(deck);
        model.setContents(contents);
    }

    public static List<GameFormat> getAvailableGameFormats(String playerName) {
        Set<PlayerRole> roles = PlayerService.get(playerName).getRoles();
        List<GameFormat> formats = new ArrayList<>(EnumSet.of(GameFormat.STANDARD, GameFormat.V5, GameFormat.DUEL));
        if (roles.contains(PlayerRole.PLAYTESTER)) {
            formats.add(GameFormat.PLAYTEST);
        }
        return formats;
    }

    public static boolean isViewable(String gameName, String player) {
        GameFormat format = GameService.get(gameName).getGameFormat();
        return format != GameFormat.PLAYTEST || isPlaytester(player);
    }

    private static ValidationResult validateDeck(Deck deck, GameFormat gameFormat) {
        try {
            Constructor<? extends DeckValidator> validatorConstructor = gameFormat.getDeckValidator().getConstructor();
            var validator = validatorConstructor.newInstance();
            return validator.validate(deck);
        } catch (Exception e) {
            logger.error("Could not find constructor for DeckValidator", e);
            throw new RuntimeException(e);
        }
    }

    private static void setRole(PlayerInfo info, PlayerRole role, boolean enabled) {
        if (enabled) {
            info.getRoles().add(role);
        } else {
            info.getRoles().remove(role);
        }
    }

    private DeckInfo loadDeckInfo(String playerName, String deckName) {
        return DeckService.get(playerName, deckName);
    }

    private static JolGame loadGameState(String gameName) {
        logger.debug("Loading {}", gameName);
        GameInfo gameInfo = GameService.get(gameName);
        String gameId = gameInfo.getId();
        return GameService.loadGame(gameId);
    }

}
