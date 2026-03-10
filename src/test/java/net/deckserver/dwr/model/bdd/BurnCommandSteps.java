package net.deckserver.dwr.model.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.deckserver.game.enums.RegionType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BurnCommandSteps {

    private final CommandContext context;

    public BurnCommandSteps() {
        this.context = CommandContext.getInstance();
    }

    @Given("{string} has {int} cards in their ash heap")
    public void playerHasCardsInAshHeap(String player, int expected) {
        assertEquals(expected, context.game().data().getPlayerRegion(player, RegionType.ASH_HEAP).getCards().size());
    }

    @Given("the top card of {string}'s {word} is {string}")
    public void topCardOfRegionIs(String player, String regionName, String cardId) {
        RegionType region = RegionType.valueOf(regionName.toUpperCase());
        assertEquals(cardId, context.game().data().getPlayerRegion(player, region).getCard(0).getId());
    }

    @When("{string} enters the command {string}")
    public void playerEntersTheCommand(String player, String command) {
        context.execute(player, command);
    }

    @Then("{string} has {int} cards in their {word}")
    public void playerHasCardsInRegion(String player, int expected, String regionName) {
        RegionType region = RegionType.valueOf(regionName.toUpperCase());
        assertEquals(expected, context.game().data().getPlayerRegion(player, region).getCards().size());
    }

    @Then("the top card of {string}'s {word} is now {string}")
    public void topCardOfRegionIsNow(String player, String regionName, String cardId) {
        RegionType region = RegionType.valueOf(regionName.toUpperCase());
        assertEquals(cardId, context.game().data().getPlayerRegion(player, region).getCard(0).getId());
    }

    @Then("the last chat message contains {string}")
    public void lastChatMessageContains(String expectedText) {
        assertThat(context.lastMessage(), containsString(expectedText));
    }
}