package net.deckserver.dwr.model.bdd;

import net.deckserver.dwr.model.CommandException;
import net.deckserver.dwr.model.DoCommand;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;

public class CommandContext {

    private static final CommandContext INSTANCE = new CommandContext();

    private JolGame game;
    private DoCommand worker;
    private Exception lastException;

    public static CommandContext getInstance() {
        return INSTANCE;
    }

    public void reset() {
        game = GameService.loadGame("command-test");
        worker = new DoCommand(game, new GameModel(game));
        lastException = null;
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
}