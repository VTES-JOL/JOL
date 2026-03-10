package net.deckserver.dwr.model.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.deckserver.game.enums.RegionType;
import net.deckserver.storage.json.game.CardData;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BurnCommandSteps {

    private final CommandContext context;

    public BurnCommandSteps() {
        this.context = CommandContext.getInstance();
    }

    @Given("{string} will enter the command {string}")
    public void playerWillEnterTheCommand(String player, String command) {
        context.setPendingCommand(command);
    }

    @Given("{string} has {int} cards in their {string}")
    public void playerHasCardsInTheirRegion(String player, int expected, String regionName) {
        RegionType region = toRegionType(regionName);
        int actual = context.game().data().getPlayerRegion(player, region).getCards().size();
        assertEquals(expected, actual);
        context.rememberRegionSize(player, regionName, actual);
    }

    @Given("the card in {string} is {string}")
    public void theCardInSourceIs(String source, String expectedCardName) {
        RegionRef ref = parseRegionRef(source);
        int position = parseBurnPosition(context.getPendingCommand());

        List<? extends CardData> cards = context.game().data().getPlayerRegion(ref.player(), ref.region()).getCards();
        CardData card = cards.get(position - 1);

        assertEquals(expectedCardName, card.getName());
        context.rememberRegionSize(ref.player(), ref.regionKey(), cards.size());
    }

    @Given("the ash heap for {string} is empty")
    public void ashHeapForSourceIsEmpty(String source) {
        RegionRef ref = parseRegionRef(source);
        int ashHeapSize = context.game().data().getPlayerRegion(ref.player(), RegionType.ASH_HEAP).getCards().size();
        assertEquals(0, ashHeapSize);
        context.rememberRegionSize(ref.player(), "ash heap", ashHeapSize);
    }

    @When("{string} enters the command {string}")
    public void playerEntersTheCommand(String player, String command) {
        context.execute(player, command);
    }

    @Then("1 card has been burned from {string}")
    public void oneCardHasBeenBurnedFrom(String source) {
        RegionRef ref = parseRegionRef(source);

        int sourceBefore = context.rememberedRegionSize(ref.player(), ref.regionKey());
        int ashHeapBefore = context.rememberedRegionSize(ref.player(), "ash heap");

        int sourceAfter = context.game().data().getPlayerRegion(ref.player(), ref.region()).getCards().size();
        int ashHeapAfter = context.game().data().getPlayerRegion(ref.player(), RegionType.ASH_HEAP).getCards().size();

        assertEquals(sourceBefore - 1, sourceAfter);
        assertEquals(ashHeapBefore + 1, ashHeapAfter);
    }

    @Then("the last chat message contains {string}")
    public void lastChatMessageContains(String expectedText) {
        assertThat(context.lastMessage(), containsString(expectedText));
    }

    private int parseBurnPosition(String command) {
        String[] args = command.trim().split("\\s+");
        for (String arg : args) {
            if ("top".equalsIgnoreCase(arg)) {
                return 1;
            }
            if (arg.matches("\\d+")) {
                return Integer.parseInt(arg);
            }
        }
        throw new IllegalStateException("Unable to determine burn position from command: " + command);
    }

    private RegionRef parseRegionRef(String source) {
        String[] parts = source.split("'s ", 2);
        String player = parts[0].trim();
        String regionName = parts[1].trim();
        return new RegionRef(player, toRegionType(regionName), regionName);
    }

    private RegionType toRegionType(String regionName) {
        return switch (regionName.trim().toLowerCase()) {
            case "ash", "ash heap" -> RegionType.ASH_HEAP;
            case "ready" -> RegionType.READY;
            case "library" -> RegionType.LIBRARY;
            case "hand" -> RegionType.HAND;
            case "crypt" -> RegionType.CRYPT;
            case "uncontrolled" -> RegionType.UNCONTROLLED;
            case "removed from game", "rfg" -> RegionType.REMOVED_FROM_GAME;
            default -> RegionType.valueOf(regionName.trim().toUpperCase().replace(' ', '_'));
        };
    }

    private record RegionRef(String player, RegionType region, String regionKey) {
    }
}