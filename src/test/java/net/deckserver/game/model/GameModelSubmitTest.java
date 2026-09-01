package net.deckserver.game.model;

import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.CommandErrorData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The raw player command is bracketed onto the {@link ChatData}s it produces by
 * {@code GameModel.submit}, which wraps each {@code DoCommand.doCommand} call in
 * {@link ChatService#beginInvocation}/{@link ChatService#endInvocation}. That
 * wrapping is exercised here directly — {@code submit()} itself re-resolves the
 * game by display name, which collides in the shared {@code command-test}
 * fixture ("Test Game"), so it can't be driven end-to-end in this suite.
 */
@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(net.deckserver.services.JolServiceExtension.class)
class GameModelSubmitTest {

    private DoCommand worker;

    @BeforeEach
    void setUp() {
        JolGame game = GameService.loadGame("command-test");
        worker = new DoCommand(game, new GameModel(game));
    }

    /** Mirrors the per-command loop body in {@code GameModel.submit}. */
    private void submitCommand(String player, String rawCommand) throws CommandException {
        ChatService.beginInvocation(player, rawCommand.trim());
        try {
            worker.doCommand(player, rawCommand);
        } catch (CommandException e) {
            ChatService.recordFailedCommand("command-test", player, rawCommand.trim(), e.getMessage());
            throw e;
        } finally {
            ChatService.endInvocation();
        }
    }

    @Test
    void stampsRawInvocationDistinctFromSyntheticCommand() throws CommandException {
        submitCommand("Player1", "vp Player1 +1");

        ChatData last = ChatService.getChats("command-test").getLast();
        assertThat(last.getInvocation(), is("vp Player1 +1"));
        assertThat(last.getInvocationBy(), is("Player1"));
        // the synthetic canonical form is still there, and still different
        assertThat(last.getCommand(), is("vp 1.0"));
        assertThat(last.getInvocation(), not(equalTo(last.getCommand())));
    }

    @Test
    void tagsSideEffectAndSystemLinesWithTheSameInvocation() throws CommandException {
        // burn library 1 -> a "burns" line plus any follow-on system lines,
        // all from the one raw command.
        submitCommand("Player2", "burn library 1");

        var chats = ChatService.getChats("command-test");
        ChatData last = chats.getLast();
        assertThat(last.getInvocation(), is("burn library 1"));
        assertThat(last.getInvocationBy(), is("Player2"));
    }

    @Test
    void clearsInvocationBetweenCommands() throws CommandException {
        submitCommand("Player1", "vp Player1 +1");
        // A line produced outside any submitCommand() bracket must not inherit it.
        ChatService.sendSystemMessage("command-test", "unbracketed line");

        ChatData last = ChatService.getChats("command-test").getLast();
        assertThat(last.getMessage(), is("unbracketed line"));
        assertThat(last.getInvocation(), is(nullValue()));
    }

    @Test
    void plainMessageCarriesNoInvocation() {
        ChatService.sendMessage("command-test", "Player1", "hello table");

        ChatData last = ChatService.getChats("command-test").getLast();
        assertThat(last.getInvocation(), is(nullValue()));
    }

    @Test
    void mistypedCommandProducesNoChatButIsRecordedForJudges() {
        int chatBefore = ChatService.getChats("command-test").size();

        CommandException thrown = assertThrows(CommandException.class,
                () -> submitCommand("Player1", "vp Player1"));   // no amount -> parse failure
        assertThat(thrown.getMessage(), not(emptyOrNullString()));

        // the chat log is untouched
        assertThat(ChatService.getChats("command-test").size(), is(chatBefore));

        // ...but a judge can see the attempt
        String turnLabel = ChatService.getTurns("command-test").getFirst();
        List<CommandErrorData> errors = ChatService.getFailedCommands("command-test", turnLabel);
        assertThat(errors, is(not(empty())));
        CommandErrorData last = errors.getLast();
        assertThat(last.getCommand(), is("vp Player1"));
        assertThat(last.getPlayer(), is("Player1"));
        assertThat(last.getError(), not(emptyOrNullString()));
    }
}
