package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by shannon on 23/08/2016.
 */
class CommandParser {

    private final static Logger logger = getLogger(CommandParser.class);

    private String[] args;
    private int ind;
    private JolGame game;

    public CommandParser(String[] args, int ind, JolGame game) {
        this.args = args;
        this.ind = ind;
        this.game = game;
    }

    String getRegion(String defaultRegion) throws CommandException {
        if (!hasMoreArgs()) return defaultRegion;
        String arg = args[ind++].toLowerCase();
        logger.trace("Attempting to get region {}", arg);
        if (JolGame.ACTIVE_REGION.startsWith(arg))
            return JolGame.READY_REGION;
        if (JolGame.RFG.startsWith(arg))
            return JolGame.RFG;
        if (JolGame.READY_REGION.startsWith(arg))
            return JolGame.READY_REGION;
        if (JolGame.INACTIVE_REGION.startsWith(arg))
            return JolGame.INACTIVE_REGION;
        if (JolGame.ASHHEAP.startsWith(arg))
            return JolGame.ASHHEAP;
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

    public String getPlayer(String defaultPlayer) throws CommandException {
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

    public String getCard(boolean optional, String player, String region) throws CommandException {
        Location loc = game.getState().getPlayerLocation(player, region);
        return getCard(optional, loc.getCards());
    }

    private String getCard(boolean optional, Card[] cards) throws CommandException {
        if (cards == null) {
            return null;
        }
        try {
            if (!hasMoreArgs()) {
                if (optional) return null;
                throw new CommandException("Card not specified");
            }
            int size = cards.length;
            int num;
            if ("random".equals(args[ind])) {
                num = new Random().nextInt(size);
            } else {
                char first = args[ind].charAt(0);
                if (first == '+') num = -1;
                else num = Integer.parseInt(args[ind]) - 1;
                if (num < 0 && optional) return null;
                if (num < 0 || num >= size) throw new CommandException("Num out of range");
            }
            Card card = cards[num];
            ind++;
            String rec = getCard(true, card.getCards());
            if (rec == null) return card.getId();
            return rec;
        } catch (NumberFormatException nfe) {
            if (optional) return null;
            throw new CommandException("No card number specified");
        }
    }

    int getAmount(int amount) throws CommandException {
        try {
            char first = args[ind].charAt(0);
            if (!Arrays.asList('-', '+').contains(first)) {
                throw new CommandException("Must preface amount with '+' or '-'");
            }
            amount = Integer.parseInt(args[ind++].substring(1));
            if (first == '-') {
                amount = 0 - amount;
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
