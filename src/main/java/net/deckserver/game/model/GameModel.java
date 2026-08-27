package net.deckserver.game.model;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.Phase;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.ObjectArrayMessage;

import java.time.OffsetDateTime;
import java.util.Set;

public class GameModel implements Comparable<GameModel> {

    private static final Logger METRICS = LogManager.getLogger("net.deckserver.metrics");
    private static final Logger COMMANDS = LogManager.getLogger("net.deckserver.commands");

    @Getter
    private final String name;
    private final JolGame game;

    public GameModel(JolGame game) {
        this.name = game.getName();
        this.game = game;
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
                        try {
                            commander.doCommand(player, cmd);
                            COMMANDS.info("[{}] {}", player, cmd);
                        } catch (CommandException e) {
                            COMMANDS.error("[{}] {}", player, cmd);
                            status.append(e.getMessage());
                        }
                    }
                    stateChanged = true;
                }
                OffsetDateTime timestamp = OffsetDateTime.now();
                METRICS.info(new ObjectArrayMessage(timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(), timestamp.getHour(), player, game.getName(), didCommand, didChat));
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
