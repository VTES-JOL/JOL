package net.deckserver.dwr.model.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VpCommandSteps {

    private final CommandContext context;

    public VpCommandSteps() {
        this.context = CommandContext.getInstance();
    }

    @Given("{string} has {double} victory points")
    public void playerHasVictoryPoints(String player, double expected) {
        assertEquals(expected, context.game().getVictoryPoints(player));
    }

}