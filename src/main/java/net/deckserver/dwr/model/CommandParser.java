package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by shannon on 23/08/2016.
 */
class CommandParser {

    private final static Logger logger = getLogger(CommandParser.class);
    private final static Pattern VALID_POSITION_PATTERN = Pattern.compile("(?<!-\\+)\\d+(?:\\.\\d+)*");

    private String[] args;
    private int ind;
    private JolGame game;

    public CommandParser(String[] args, int ind, JolGame game) {
        this.args = args;
        this.ind = ind;
        this.game = game;
    }

    private String[] translateNextPosition() {
        if (!VALID_POSITION_PATTERN.matcher(args[ind]).matches()) {
            return new String[0];
        }
        String[] indexes = args[ind].split("\\.");
        for (int i = 0; i < indexes.length - 1; i++) {
            if ("top".equals(indexes[i])) {
                indexes[i] = "1";
            }
        }
        return indexes;
    }

    String getRegion(String defaultRegion) throws CommandException {
        if (!hasMoreArgs()) return defaultRegion;
        String arg = args[ind++].toLowerCase();
        logger.trace("Attempting to get region {}", arg);
        if (JolGame.RFG.startsWith(arg))
            return JolGame.RFG;
        if (JolGame.READY_REGION.startsWith(arg))
            return JolGame.READY_REGION;
        if (JolGame.INACTIVE_REGION.startsWith(arg))
            return JolGame.INACTIVE_REGION;
        if (JolGame.UNCONTROLLED_REGION.startsWith(arg))
            return JolGame.INACTIVE_REGION;
        if (JolGame.ASH_HEAP.startsWith(arg))
            return JolGame.ASH_HEAP;
        if (JolGame.HAND.startsWith(arg))
            return JolGame.HAND;
        if (JolGame.LIBRARY.startsWith(arg))
            return JolGame.LIBRARY;
        if (JolGame.CRYPT.startsWith(arg))
            return JolGame.CRYPT;
        if (JolGame.TORPOR.startsWith(arg))
            return JolGame.TORPOR;
        if (JolGame.RESEARCH.startsWith(arg))
            return JolGame.RESEARCH;
        ind--;
        return defaultRegion;
    }

    String getPlayer(String defaultPlayer) throws CommandException {
        if (!hasMoreArgs()) return defaultPlayer;
        String arg = args[ind++].toLowerCase();
        List<String> players = game.getPlayers();
        for (String player : players)
            if (player.toLowerCase().startsWith(arg)) {
                return player;
            }
        ind--;
        return defaultPlayer;
    }

    String findCard(boolean greedy, String player, String region) throws CommandException {
        Location location = game.getState().getPlayerLocation(player, region);
        Card[] cards = location.getCards();
        Card targetCard = null;
        boolean keepLooking = true;
        while (keepLooking && hasMoreArgs()) {
            // Get the position from the next arg
            String[] indexes = translateNextPosition();
            if (indexes.length == 0) {
                break;
            }
            try {
                for (String index : indexes) {
                    int indexInt;
                    if ("random".equals(index)) {
                        indexInt = new Random().nextInt(cards.length);
                    } else {
                        indexInt = Integer.parseInt(index);
                    }
                    targetCard = cards[indexInt - 1];
                    cards = targetCard.getCards();
                }
                ind++;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                keepLooking = false;
            }
        }

        if (targetCard == null && greedy) {
            throw new CommandException("Invalid card position.");
        }
        return Optional.ofNullable(targetCard).map(Card::getId).orElse(null);
    }

    String findCard(String player, String region) throws CommandException {
        return findCard(true, player, region);
    }

    int getAmount(int amount) throws CommandException {
        try {
            char first = args[ind].charAt(0);
            if (!Arrays.asList('-', '+').contains(first)) {
                throw new CommandException("Must preface amount with '+' or '-'");
            }
            amount = Integer.parseInt(args[ind++].substring(1));
            if (first == '-') {
                amount = -amount;
            }
            return amount;
        } catch (Exception e) {
            return amount;
        }
    }

    int getNumber(int def) throws CommandException {
        try {
            return Integer.parseInt(args[ind++]);
        } catch (Exception nfe) {
            return def;
        }
    }

    boolean hasMoreArgs() {
        return ind < args.length;
    }

    String nextArg() {
        return args[ind++];
    }

    boolean consumeString(String val) {
        if (!hasMoreArgs()) return false;
        if (val.equalsIgnoreCase(args[ind])) {
            ind++;
            return true;
        }
        return false;
    }

}
