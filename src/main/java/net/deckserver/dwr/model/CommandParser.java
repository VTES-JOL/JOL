package net.deckserver.dwr.model;

import net.deckserver.game.enums.RegionType;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.RegionData;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by shannon on 23/08/2016.
 */
class CommandParser {

    private final static Pattern VALID_POSITION_PATTERN = Pattern.compile("(?<!-\\+)\\d+(?:\\.\\d+)*");
    private final static Pattern SPECIAL_POSITION_PATTERN = Pattern.compile("random");

    final String[] args;
    private final JolGame game;
    private int ind;

    public CommandParser(String[] args, int ind, JolGame game) {
        this.args = args;
        this.ind = ind;
        this.game = game;
    }

    private String[] translateNextPosition(boolean allowRandom) throws CommandException {
        if (!SPECIAL_POSITION_PATTERN.matcher(args[ind]).matches() && !VALID_POSITION_PATTERN.matcher(args[ind]).matches()) {
            return new String[0];
        }
        if (!allowRandom && "random".equals(args[ind])) {
            throw new CommandException("Unable to use random.");
        }
        return args[ind].split("\\.");
    }

    RegionType getRegion(RegionType defaultRegion) {
        if (!hasMoreArgs()) return defaultRegion;
        String arg = args[ind++].toLowerCase();
        RegionType results = RegionType.startsWith(arg);
        if (results == null) {
            ind--;
            return defaultRegion;
        }
        return results;
    }

    /*
    This function will attempt to find a player in the game based on the next argument, or return defaultPlayer
    It will throw a CommandException if player not found, and defaultPlayer is null, or if more than one player found.
     */
    String getPlayer(String defaultPlayer) throws CommandException {
        if (!hasMoreArgs()) return defaultPlayer;
        String playerArgument = args[ind++];
        assert playerArgument != null;
        playerArgument = playerArgument.toLowerCase();

        int matchLength = Integer.MAX_VALUE;
        String match = null;
        boolean unique = true;

        List<String> players = game.getPlayers();
        for (String player : players) {
            // Try a full match first
            if (playerArgument.equals(player.toLowerCase())) {
                return player;
            }
        }

        // Try partial match with accents on
        for (String player : players) {
            if (player.toLowerCase().startsWith(playerArgument)) {
                int length = player.length();
                if (length < matchLength) {
                    matchLength = length;
                    unique = true;
                    match = player;
                } else if (length == matchLength) {
                    unique = false;
                }
            }
        }

        // If no matches, then try stripping accents
        if (match == null) {
            String strippedArgument = StringUtils.stripAccents(playerArgument);
            for (String player : players) {
                String strippedPlayer = StringUtils.stripAccents(player.toLowerCase());
                if (strippedPlayer.startsWith(strippedArgument)) {
                    int length = player.length();
                    if (length < matchLength) {
                        matchLength = length;
                        unique = true;
                        match = player;
                    } else if (length == matchLength) {
                        unique = false;
                    }
                }
            }
        }

        if (match == null) {
            if (defaultPlayer == null) {
                throw new CommandException("Unable to find player.");
            }
            ind--;
            return defaultPlayer;
        } else if (!unique) {
            throw new CommandException("Unable to find unique player.  Please be more specific.");
        }
        return match;

    }

    CardData findCardData(boolean greedy, boolean allowRandom, String player, RegionType region) throws CommandException {
        RegionData regionData = game.data().getPlayerRegion(player, region);
        var cards = regionData.getCards();
        CardData targetCard = null;
        boolean keepLooking = true;
        while (keepLooking && hasMoreArgs()) {
            String[] indexes = translateNextPosition(allowRandom);
            if (indexes.length == 0) {
                break;
            }
            try {
                for (String index : indexes) {
                    int indexInt;
                    if ("random".equals(index)) {
                        indexInt = new Random().nextInt(cards.size()) + 1;
                    } else {
                        indexInt = Integer.parseInt(index);
                    }
                    targetCard = cards.get(indexInt - 1);
                    cards = targetCard.getCards();
                }
                ind++;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                keepLooking = false;
            }
        }

        if (targetCard == null && greedy) {
            throw new CommandException("Invalid card position");
        }
        return targetCard;
    }

    CardData findCardData(boolean allowRandom, String player, RegionType region) throws CommandException {
        return findCardData(true, allowRandom, player, region);
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
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return amount;
        }
    }

    int getNumber(int def) throws CommandException {
        try {
            return Integer.parseInt(args[ind++]);
        } catch (Exception nfe) {
            ind--;
            return def;
        }
    }

    boolean hasMoreArgs() {
        return ind < args.length;
    }

    String nextArg() throws CommandException {
        try {
            return args[ind++];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandException("Command requires at least one argument.");
        }
    }

    String remaining() throws CommandException {
        StringBuilder builder = new StringBuilder();
        while (hasMoreArgs()) {
            builder.append(nextArg());
            builder.append(" ");
        }
        return builder.toString().trim();
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
