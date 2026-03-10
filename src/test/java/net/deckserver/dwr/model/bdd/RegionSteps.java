package net.deckserver.dwr.model.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.deckserver.game.enums.RegionType;
import net.deckserver.storage.json.game.CardData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegionSteps {

    private final CommandContext context;

    public RegionSteps() {
        this.context = CommandContext.getInstance();
    }

    @Given("{string} has {int} cards in their {string}")
    public void playerHasCardsInTheirRegion(String player, int expected, String regionName) {
        RegionType region = toRegionType(regionName);
        int actual = context.game().data().getPlayerRegion(player, region).getCards().size();
        assertEquals(expected, actual);
        context.rememberRegionSize(player, regionName, actual);
    }

    @Given("the ash heap for {string} is empty")
    public void ashHeapForSourceIsEmpty(String source) {
        RegionRef ref = parseRegionRef(source);
        int ashHeapSize = context.game().data().getPlayerRegion(ref.player(), RegionType.ASH_HEAP).getCards().size();
        assertEquals(0, ashHeapSize);
        context.rememberRegionSize(ref.player(), "ash heap", ashHeapSize);
    }

    @Given("the card in {string} is {string}")
    public void theCardInSourceIs(String source, String expectedCardName) {
        RegionRef ref = parseRegionRef(source);

        String pendingCommand = context.getPendingCommand();
        int position = parsePosition(pendingCommand);

        List<? extends CardData> cards = context.game().data().getPlayerRegion(ref.player(), ref.region()).getCards();
        CardData card = cards.get(position - 1);

        assertEquals(expectedCardName, card.getName());
        context.rememberRegionSize(ref.player(), ref.regionKey(), cards.size());
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

    private int parsePosition(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalStateException(
                    "No pending command is available in CommandContext. " +
                            "The step \"the card in ... is ...\" depends on the command being stored before it runs."
            );
        }

        String[] args = command.trim().split("\\s+");
        for (String arg : args) {
            if ("top".equalsIgnoreCase(arg)) {
                return 1;
            }
            if (arg.matches("\\d+")) {
                return Integer.parseInt(arg);
            }
        }
        throw new IllegalStateException("Unable to determine position from command: " + command);
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