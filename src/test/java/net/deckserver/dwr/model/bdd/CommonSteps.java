package net.deckserver.dwr.model.bdd;

import io.cucumber.java.Before;

public class CommonSteps {

    private final CommandContext context;

    public CommonSteps() {
        this.context = CommandContext.getInstance();
    }

    @Before
    public void setUp() {
        context.reset();
    }
}