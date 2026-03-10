package net.deckserver.dwr.model.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommonSteps {

    private final CommandContext context;

    public CommonSteps() {
        this.context = CommandContext.getInstance();
    }

    @Before
    public void setUp() {
        context.reset();
    }

    @Given("{string} will enter the command {string}")
    public void playerWillEnterCommand(String player, String command) {
        context.setPendingCommand(command);
    }

    @When("{string} enters the command {string}")
    public void playerEntersTheCommand(String player, String command) {
        context.execute(player, command);
    }

    @Then("the last chat message contains {string}")
    public void lastChatMessageContains(String expectedText) {
        assertThat(context.lastMessage(), containsString(expectedText));
    }

    @Then("the command fails")
    public void theCommandFails() {
        assertNotNull(context.lastException());
    }
}