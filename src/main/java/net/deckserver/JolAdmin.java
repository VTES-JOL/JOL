/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver;

import io.azam.ulidj.ULID;
import net.deckserver.rest.bean.DeckEdit;
import net.deckserver.game.GameOutcome;
import net.deckserver.game.model.GameModel;
import net.deckserver.game.model.JolGame;
import net.deckserver.game.enums.*;
import net.deckserver.game.validators.DeckValidator;
import net.deckserver.game.validators.ValidationResult;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.services.*;
import net.deckserver.ws.WebSocketRegistry;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class JolAdmin {

    private static final Logger logger = LoggerFactory.getLogger(JolAdmin.class);

    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();

    public static int getRefreshInterval(String gameName) {
        OffsetDateTime lastChanged = PlayerGameActivityService.getGameTimestamp(gameName);
        OffsetDateTime now = OffsetDateTime.now();
        long interval = Duration.between(lastChanged, now).getSeconds();
        if (interval < 60) return 5000;
        if (interval < 180) return 10000;
        if (interval < 300) return 30000;
        return 60000;
    }

    public static GameModel getGameModel(String name) {
        // Pass `name` explicitly rather than deriving it from the JolGame: when a game
        // has been created but not yet started there's no game_state row, so
        // getGameByName returns an empty JolGame whose GameData.name is null. Deriving
        // the model's name from that once-cached nameless game left every later
        // getView() calling GameService.get(null) -> NPE.
        return gmap.computeIfAbsent(name, n -> new GameModel(n, GameService.getGameByName(n)));
    }

    public static void createGame(String gameName, Boolean isPublic, GameFormat format, String playerName) {
        createGame(gameName, isPublic, format, playerName, ULID.random());
    }

    public static void createGame(String gameName, Boolean isPublic, GameFormat format, String playerName, String gameId) {
        logger.trace("Creating game {} for player {}", gameName, playerName);
        if (gameName.length() > 2 && notExistsGame(gameName)) {
            try {
                GameService.create(gameName, gameId, playerName, Visibility.fromBoolean(isPublic), format);
                WebSocketRegistry.notifyInvalidate(List.of("nav"));
                WebSocketRegistry.notifyInvalidate(List.of("watch"));
                WebSocketRegistry.notifyInvalidate(List.of("main-games"));
            } catch (Exception e) {
                logger.error("Error creating game", e);
            }
        }
    }

    public static void chat(String player, String message) {
        GlobalChatService.chat(player, message);
    }

    public static void chat(String player, String message, String excludeClientId) {
        GlobalChatService.chat(player, message, excludeClientId);
    }

    public static void rollbackGame(String gameName, String adminName, String turn) {
        GameInfo gameInfo = GameService.get(gameName);
        // Guarded by the game's own lock — rollback replaces the cached JolGame outright,
        // which must not race a concurrent player submit mutating the game it's replacing.
        getGameModel(gameName).withLock(() -> GameService.rollbackGame(gameName, turn));
        ChatService.sendMessage(gameInfo.getId(), "SYSTEM", "Game state rolled back by administrator: " + adminName);
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

    public static String getDeckComment(String playerName, String deckName) {
        return DeckService.getDeckComments(playerName, deckName);
    }

    public static void setDeckComment(String playerName, String gameName, String comments) {
        getGameDeck(gameName, playerName).setComments(comments);
    }

    public static Deck getGameDeck(String gameName, String playerName) {
        return Optional.ofNullable(RegistrationService.getRegistration(gameName, playerName))
                .map(status -> DeckService.deserializeDeck(status.getDeckContent()).getDeck())
                .orElse(null);
    }

    public static DeckEdit selectDeck(String playerName, String deckName) {
        if (playerName == null || deckName == null) {
            return DeckEdit.EMPTY;
        }
        try {
            String deckId = getDeckId(playerName, deckName);
            DeckFormat deckFormat = getDeckFormat(playerName, deckName);
            ExtendedDeck deck;
            String contents;
            if (deckFormat.equals(DeckFormat.LEGACY)) {
                contents = DeckService.getLegacyContents(deckId).trim();
                deck = DeckParser.parseDeck(contents);
            } else {
                contents = DeckService.getDeckContents(deckId).trim();
                deck = DeckService.getDeck(deckId);
            }
            deck.getDeck().setName(deckName);
            return new DeckEdit(deck, contents, deckId);
        } catch (IOException e) {
            logger.error("Unable to load deck", e);
            return DeckEdit.EMPTY;
        }
    }

    public static DeckEdit newDeck(String playerName) {
        return DeckEdit.EMPTY;
    }

    public static synchronized DeckEdit saveDeck(String playerName, String deckName, String contents, String comment) {
        if (playerName == null || contents == null || deckName == null) {
            return DeckEdit.EMPTY;
        }
        deckName = deckName.trim();
        ExtendedDeck deck = DeckParser.parseDeck(contents);
        deck.getDeck().setName(deckName);
        deck.getDeck().setAuthor(playerName);
        deck.getDeck().setComments(comment);
        Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
        DeckInfo deckInfo = Optional.ofNullable(DeckService.get(playerName, deckName)).orElse(new DeckInfo(ULID.random(), deckName, DeckFormat.TAGGED, tags));
        deckInfo.setFormat(DeckFormat.MODERN);
        deckInfo.setGameFormats(tags);
        DeckService.addDeck(playerName, deckName, deckInfo);
        DeckService.saveDeck(deckInfo.getDeckId(), deck);
        DeckValidityService.computeAndPersist(deckInfo.getDeckId(), deck.getDeck());
        return new DeckEdit(deck, contents, deckInfo.getDeckId());
    }

    public static synchronized DeckEdit deleteDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            DeckService.remove(playerName, deckName);
        }
        return DeckEdit.EMPTY;
    }

    public static void saveGameState(JolGame game) {
        saveGameState(game, false);
    }

    public static void saveGameState(JolGame game, boolean silent) {
        saveGameState(game, silent, null);
    }

    public static void saveGameState(JolGame game, boolean silent, String excludeClientId) {
        if (!silent) {
            PlayerGameActivityService.setGameTimestamp(game.getName());
        }
        GameService.saveGame(game);
        WebSocketRegistry.notifyGame(game.id(), excludeClientId);
    }

    public static synchronized String registerDeck(String gameName, String playerName, String deckName) {
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
            RegistrationService.registerDeck(gameName, playerName, deckInfo.getDeckId(), deckName,
                    extendedDeck.getStats().getSummary(), DeckService.serializeDeck(extendedDeck));

            // Reset game time to the current time to extend idle timeout
            GameService.updateGameInfo(gameName, info -> info.setUpdated(OffsetDateTime.now()));

            attemptAutoStart(gameName);
        } catch (IllegalStateException exception) {
            logger.debug(exception.getMessage());
        }
        return result;
    }

    public static void recordPlayerAccess(String playerName) {
        if (playerName != null && PlayerService.existsPlayer(playerName)) {
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

    public static synchronized void setImageTooltipPreference(String player, boolean value) {
        PlayerService.setImageTooltipPreference(player, value);
    }
    public static synchronized void setEdgeColor(String player, String value) {
        PlayerService.setEdgeColor(player, value);
    }

    public static synchronized boolean getImageTooltipPreference(String player) {
        if (player == null) {
            return true;
        }
        return PlayerService.get(player).isShowImages();
    }

    public static synchronized String getEdgeColor(String player) {
        if (player == null) {
            return "#FFFFFF";
        }
        return PlayerService.get(player).getEdgeColor();
    }

    public static synchronized void setNotificationPreference(String player, boolean value) {
        PlayerService.setNotificationPreference(player, value);
    }

    public static synchronized boolean getNotificationPreference(String player) {
        if (player == null) {
            return false;
        }
        return PlayerService.get(player).isNotificationsEnabled();
    }

    public static synchronized boolean isAdmin(String player) {
        return PlayerService.get(player).getRoles().contains(PlayerRole.ADMIN);
    }

    public static synchronized boolean isTournamentAdmin(String player) {
        return PlayerService.get(player).getRoles().contains(PlayerRole.TOURNAMENT_ADMIN);
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
        return Optional.ofNullable(GameService.get(gameName)).map(GameInfo::getStatus).map(status -> status.equals(GameStatus.ACTIVE)).orElse(false);
    }

    public static boolean isTournament(String gameName) {
        return Optional.ofNullable(GameService.get(gameName)).map(GameInfo::getTournamentName).map(tournament -> tournament != null).orElse(false);
    }

    public static boolean isAlive(String gameName, String playerName) {
        try {
            return GameService.getGameByName(gameName).getPool(playerName) > 0;
        } catch (NullPointerException e) {
            logger.error("Unable to check if {} is alive in {}", playerName, gameName);
            return false;
        }
    }

    public static boolean isPrivate(String gameName) {
        return GameService.get(gameName).getVisibility().equals(Visibility.PRIVATE);
    }

    public static boolean isPublic(String gameName) {
        return GameService.get(gameName).getVisibility().equals(Visibility.PUBLIC);
    }

    /**
     * Builds and starts a game from its registrations. Nothing is persisted and the game status is not
     * flipped to {@link GameStatus#ACTIVE} unless every registered player's deck loads playably and the
     * game starts cleanly, so a failure here leaves the game recoverable in {@link GameStatus#STARTING}
     * (re-registering a deck, or the owner/admin "start" action, retries).
     *
     * @return {@code true} if the game is now ACTIVE.
     */
    public static boolean startGame(String gameName, List<String> players) {
        GameInfo gameInfo = GameService.get(gameName);
        if (gameInfo == null) {
            logger.warn("Cannot start game '{}' - no such game", gameName);
            return false;
        }
        // Guarded by the game's own lock, not just the caller's `synchronized` — this
        // constructs a brand-new JolGame and immediately caches/saves it, which can
        // otherwise race a concurrent GameStateResource submit that's mutating the
        // GameModel this same name is about to be associated with.
        return Boolean.TRUE.equals(getGameModel(gameName).withLock(() -> {
            GameData gameData = new GameData(gameInfo.getId(), gameName);
            JolGame game = new JolGame(gameInfo.getId(), gameData);
            List<String> broken = new ArrayList<>();
            for (Map.Entry<String, RegistrationStatus> entry : RegistrationService.getGameRegistrations(gameName).entrySet()) {
                if (entry.getValue().getDeckId() == null) {
                    continue;
                }
                String playerName = entry.getKey();
                Deck deck = getGameDeck(gameName, playerName);
                if (deck == null || deck.getCrypt() == null || deck.getLibrary() == null
                        || deck.getCrypt().getCards().isEmpty() || deck.getLibrary().getCards().isEmpty()) {
                    broken.add(playerName);
                } else {
                    game.addPlayer(playerName, deck);
                }
            }
            if (!broken.isEmpty()) {
                logger.error("Not starting game '{}' - unloadable/empty deck(s) for: {}", gameName, broken);
                return false;
            }
            if (game.getPlayers().isEmpty() || game.getPlayers().size() > 5) {
                logger.warn("Cannot start game '{}' - {} registered player(s)", gameName, game.getPlayers().size());
                return false;
            }
            try {
                game.startGame(players);
            } catch (RuntimeException e) {
                logger.error("Not starting game '{}' - startGame failed", gameName, e);
                return false;
            }
            saveGameState(game);
            GameService.updateGameInfo(gameName, info -> info.setStatus(GameStatus.ACTIVE));
            pingPlayer(game.getActivePlayer(), gameName);
            WebSocketRegistry.notifyInvalidate(List.of("nav"));
            WebSocketRegistry.notifyInvalidate(List.of("watch"));
            WebSocketRegistry.notifyInvalidate(List.of("main-games"));
            logger.info("Started game '{}' with players {}", gameName, players);
            return true;
        }));
    }

    public static synchronized boolean startGame(String gameName) {
        List<String> players = new ArrayList<>();
        List<String> invitedPlayers = new ArrayList<>();
        RegistrationService.getGameRegistrations(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                players.add(playerName);
            } else {
                invitedPlayers.add(playerName);
            }
        });
        invitedPlayers.forEach((playerName) -> RegistrationService.removePlayer(gameName, playerName));
        Collections.shuffle(players, new SecureRandom());
        return startGame(gameName, players);
    }

    /**
     * Starts {@code gameName} if it is still in the lobby with a full roster. Safe to call repeatedly —
     * a no-op unless the game is STARTING and has exactly the format's player count registered.
     */
    public static synchronized boolean attemptAutoStart(String gameName) {
        GameInfo gameInfo = GameService.get(gameName);
        if (gameInfo == null || !GameStatus.STARTING.equals(gameInfo.getStatus())) {
            return false;
        }
        long registeredPlayers = RegistrationService.getRegisteredPlayerCount(gameName);
        if (registeredPlayers != gameInfo.getGameFormat().getPlayerCount()) {
            return false;
        }
        return startGame(gameName);
    }

    public static void endGame(String gameName, boolean graceful) {
        // Guarded by the game's own lock (not `synchronized`, which is a different
        // monitor than GameModel's per-game ReentrantLock and doesn't exclude a
        // concurrent player submit) — this reads gameData.getPlayers()/getVictoryPoints()
        // while stats are computed, which must not tear against a mutation in flight.
        getGameModel(gameName).withLock(() -> {
            GameInfo gameInfo = GameService.get(gameName);
            // try and generate stats for game
            if (gameInfo.getStatus().equals(GameStatus.ACTIVE)) {
                JolGame gameData = GameService.getGameByName(gameName);
                if (gameData.getPlayers().size() >= 4 && graceful) {
                    GameHistory history = new GameHistory();
                    history.setName(gameName);
                    String startTime = gameInfo.getUpdated() != null ? gameInfo.getUpdated().format(ISO_OFFSET_DATE_TIME) : " --- ";
                    String endTime = OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
                    history.setStarted(startTime);
                    history.setEnded(endTime);
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
                        history.getResults().add(result);
                    }
                    GameOutcome.gameWinner(history.getResults(), PlayerResult::getVictoryPoints)
                            .ifPresent(winner -> winner.setGameWin(true));
                    double totalVp = history.getResults().stream().mapToDouble(PlayerResult::getVictoryPoints).sum();
                    int playerCount = gameData.getPlayers().size();
                    if (!GameOutcome.isPlausibleVictoryPointTotal(playerCount, totalVp)) {
                        logger.warn("Not recording game '{}' in history - recorded VP total {} exceeds the {} players in the game", gameName, totalVp, playerCount);
                    } else if (hasVp) {
                        HistoryService.addGame(OffsetDateTime.now(), history);
                    }
                }
            }
            // Clear out data
            RegistrationService.clearRegistrations(gameName);
            WebSocketRegistry.notifyGame(gameInfo.getId());
            GameService.remove(gameName, gameInfo.getId());
            PlayerGameActivityService.clearGame(gameName);
            gmap.remove(gameName);
            WebSocketRegistry.notifyInvalidate(List.of("nav"));
            WebSocketRegistry.notifyInvalidate(List.of("watch"));
            WebSocketRegistry.notifyInvalidate(List.of("main-games"));
        });
    }

    public static String getDeckId(String playerName, String deckName) {
        return DeckService.get(playerName, deckName).getDeckId();
    }

    public static String getGameId(String gameName) {
        return GameService.get(gameName).getId();
    }

    public static void replacePlayer(String gameName, String existingPlayer, String newPlayer) {
        RegistrationStatus existingRegistration = RegistrationService.getRegistration(gameName, existingPlayer);
        RegistrationStatus newRegistration = RegistrationService.getRegistration(gameName, newPlayer);
        // Only replace player if existing player is in the game, and the new player isn't
        if (existingRegistration != null && newRegistration == null) {
            // Guarded by the game's own lock, not `synchronized` (a different monitor than
            // GameModel's per-game ReentrantLock) — replacePlayer mutates the same shared
            // JolGame a concurrent player submit could be mutating.
            getGameModel(gameName).withLock(() -> {
                JolGame game = GameService.getGameByName(gameName);
                game.replacePlayer(existingPlayer, newPlayer);
                saveGameState(game);
            });
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

    public static OffsetDateTime getUpdatedTime(String gameName) {
        return Optional.ofNullable(GameService.get(gameName))
                .map(GameInfo::getUpdated)
                .orElse(null);
    }

    public static void endTurn(String gameName, String adminName) {
        // Guarded by the game's own lock, not `synchronized` (a different monitor than
        // GameModel's per-game ReentrantLock) — an admin-forced end turn mutates the same
        // shared JolGame a concurrent player submit/end-turn could be mutating.
        getGameModel(gameName).withLock(() -> {
            JolGame game = GameService.getGameByName(gameName);
            String id = GameService.get(gameName).getId();
            ChatService.sendMessage(id, "SYSTEM", "Turn ended by administrator: " + adminName);
            game.newTurn();
            saveGameState(game);
            pingPlayer(game.getActivePlayer(), gameName);
        });
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
        return GameService.getGameByName(gameName).getValidPlayers()
                .stream()
                .filter(player -> !JolAdmin.isPlayerPinged(player, gameName))
                .collect(Collectors.toList());
    }

    public static String getFormat(String gameName) {
        return GameService.get(gameName).getGameFormat().toString();
    }

    public static synchronized DeckEdit validateDeck(String deckName, String contents, GameFormat format) {
        ExtendedDeck deck = DeckParser.parseDeck(contents);
        ValidationResult result = validateDeck(deck.getDeck(), format);
        if (result.isValid()) {
            deck.setErrors(List.of("No errors found.  Deck is valid for " + format.getLabel() + "."));
        } else {
            deck.setErrors(result.getErrors());
        }
        if (deckName != null && !deckName.isBlank()) {
            deck.getDeck().setName(deckName);
        }
        return new DeckEdit(deck, contents, null);
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

    public static void setRole(String playerName, PlayerRole role, boolean enabled) {
        PlayerService.setRole(playerName, role, enabled);
    }

}
