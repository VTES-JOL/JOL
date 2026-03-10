package net.deckserver.dwr.model.bdd;

import net.deckserver.dwr.model.DoCommand;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {

    private static final CommandContext INSTANCE = new CommandContext();

    private JolGame game;
    private DoCommand worker;
    private Exception lastException;
    private String pendingCommand;
    private final Map<String, Integer> regionSizes = new HashMap<>();

    public static CommandContext getInstance() {
        return INSTANCE;
    }

    public void reset() {
        game = GameService.loadGame("command-test");
        worker = new DoCommand(game, new GameModel(game));
        lastException = null;
        pendingCommand = null;
        regionSizes.clear();
    }

    public void execute(String player, String command) {
        lastException = null;
        try {
            worker.doCommand(player, command);
        } catch (Exception e) {
            lastException = e;
        }
    }

    public JolGame game() {
        return game;
    }

    public Exception lastException() {
        return lastException;
    }

    public String lastMessage() {
        return ChatService.getChats("command-test").getLast().getMessage();
    }

    public void rememberRegionSize(String player, String region, int size) {
        regionSizes.put(player + "|" + region, size);
    }

    public int rememberedRegionSize(String player, String region) {
        return regionSizes.get(player + "|" + region);
    }

    public void setPendingCommand(String pendingCommand) {
        this.pendingCommand = pendingCommand;
    }

    public String getPendingCommand() {
        return pendingCommand;
    }
}