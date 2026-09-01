package net.deckserver.game.model;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.Phase;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.MetricsService;
import net.deckserver.services.RegistrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class GameModel implements Comparable<GameModel> {

    private static final Logger COMMANDS = LogManager.getLogger("net.deckserver.commands");

    @Getter
    private final String name;
    private final JolGame game;
    // GameModel is cached one-per-game (JolAdmin.gmap), so this lock serializes every
    // mutate-then-save-then-snapshot sequence for a single game across concurrent requests
    // (e.g. two near-simultaneous submits, or a submit racing a plain view fetch) — without
    // it, one request's DoCommand can still be mutating GameData's region lists while
    // another request's GameSnapshotFactory.build() iterates them (ConcurrentModificationException),
    // and two concurrent saveGame() calls can both read the same stale @Version before either
    // writes, so the second commit fails with an OptimisticLockException.
    private final ReentrantLock lock = new ReentrantLock();

    public GameModel(JolGame game) {
        this.name = game.getName();
        this.game = game;
    }

    public <T> T withLock(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void withLock(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public void endTurn(String player, String excludeClientId) {
        JolGame game = GameService.getGameByName(name);
        if (game.getActivePlayer().equals(player)) {
            game.newTurn();
            JolAdmin.saveGameState(game, false, excludeClientId);
            JolAdmin.pingPlayer(game.getActivePlayer(), name);
        }
    }

    public String submit(String player, String phase, String command, String chat, String ping, String excludeClientId) {
        // Only players and judges can issue commands.  A judge can't be a player
        boolean isJudge = JolAdmin.isJudge(player) && !getPlayers().contains(player);
        if (!getPlayers().contains(player) && !isJudge) {
            return "Not authorized";
        }
        JolGame game = GameService.getGameByName(name);
        StringBuilder status = new StringBuilder();
        if (player != null) {
            boolean stateChanged = false;
            boolean phaseChanged = false;
            boolean chatChanged = false;
            if (ping != null) {
                boolean pingSuccessful = JolAdmin.pingPlayer(ping, name);
                if (!pingSuccessful) {
                    status.append("Player is already pinged");
                }
            }
            if (phase != null &&
                    game.getActivePlayer().equals(player)
                    && !game.getPhase().equals(Phase.of(phase))) {
                game.setPhase(Phase.of(phase));
                phaseChanged = true;
            }
            if (command != null || chat != null) {
                DoCommand commander = new DoCommand(game, this);
                boolean didCommand = false;
                boolean didChat = false;
                if (chat != null) {
                    didChat = true;
                    commander.doMessage(player, chat, isJudge);
                    chatChanged = true;
                }
                if (command != null) {
                    didCommand = true;
                    String[] commands = command.split(";");
                    ThreadContext.put("DYNAMIC_LOG", name);
                    for (String cmd : commands) {
                        // Bracket the command so every ChatData it produces — including
                        // side-effect and system lines — carries the raw text the player
                        // typed and who typed it (surfaced to judges via ChatData.invocation).
                        ChatService.beginInvocation(player, cmd.trim());
                        try {
                            commander.doCommand(player, cmd);
                            COMMANDS.info("[{}] {}", player, cmd);
                        } catch (CommandException e) {
                            COMMANDS.error("[{}] {}", player, cmd);
                            status.append(e.getMessage());
                        } finally {
                            ChatService.endInvocation();
                        }
                    }
                    stateChanged = true;
                }
                MetricsService.record(player, game.getName(), didCommand, didChat);
                JolAdmin.clearPing(player, name);
            }
            if (stateChanged || phaseChanged || chatChanged) {
                JolAdmin.saveGameState(game, false, excludeClientId);
            }
        }
        return status.toString();
    }

    public Set<String> getPlayers() {
        return RegistrationService.getPlayers(name);
    }

    public int compareTo(GameModel arg0) {
        return -name.compareToIgnoreCase(arg0.getName());
    }

    public void updateGlobalNotes(String notes, String excludeClientId) {
        JolGame game = GameService.getGameByName(name);
        if (!notes.equals(game.getGlobalText())) {
            game.setGlobalText(notes);
            JolAdmin.saveGameState(game, false, excludeClientId);
        }
    }

    public void updatePrivateNotes(String player, String notes, String excludeClientId) {
        JolGame game = GameService.getGameByName(name);
        if (!notes.equals(game.getPrivateNotes(player))) {
            game.setPrivateNotes(player, notes);
            JolAdmin.saveGameState(game, true, excludeClientId);
        }
    }

}
