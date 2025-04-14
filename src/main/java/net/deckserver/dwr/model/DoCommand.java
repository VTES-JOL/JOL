/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.slf4j.LoggerFactory.getLogger;

public class DoCommand {

    private static final Logger logger = getLogger(DoCommand.class);
    private final JolGame game;

    public DoCommand(JolGame game) {
        this.game = game;
    }

    public String doMessage(String player, String message, boolean isJudge) {
        if (message.isEmpty())
            return "No message received";
        game.sendMsg(player, message, isJudge);
        return "Sent message";
    }

    public void doCommand(String player, String[] cmdStr) throws CommandException {
        String cmd = cmdStr[0];
        CommandParser cmdObj = new CommandParser(cmdStr, 1, game);
        boolean random = Arrays.asList(cmdStr).contains("random");
        if (cmd.equalsIgnoreCase("timeout")) {
            boolean cancel = cmdObj.consumeString("cancel");
            game.requestTimeout(player);
        }
        if (cmd.equalsIgnoreCase("vp")) {
            String targetPlayer = cmdObj.getPlayer(player);
            if (cmdObj.consumeString("withdraw")) {
                game.withdraw(targetPlayer);
            } else {
                int amount = cmdObj.getAmount(0);
                if (amount == 0) {
                    throw new CommandException("No amount given use +/-");
                }
                game.updateVP(targetPlayer, amount);
            }
        }
        if (cmd.equalsIgnoreCase("choose")) {
            String choice = cmdObj.nextArg();
            game.setChoice(player, choice);
        }
        if (cmd.equalsIgnoreCase("reveal")) {
            game.getChoices();
        }
        if (cmd.equalsIgnoreCase("label")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String card = cmdObj.findCard(targetPlayer, targetRegion);
            StringBuilder note = new StringBuilder();
            while (cmdObj.hasMoreArgs()) {
                note.append(" ");
                note.append(cmdObj.nextArg());
            }
            game.setText(card, note.toString(), false);
        }
        if (cmd.equalsIgnoreCase("votes")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String card = cmdObj.findCard(targetPlayer, targetRegion);

            game.setVotes(card, cmdObj.nextArg(), false);
        }
        if (cmd.equalsIgnoreCase("random")) {
            int limit = cmdObj.getNumber(-1);
            if (limit < 1) limit = 2;
            int result = ThreadLocalRandom.current().nextInt(1, limit + 1);
            if (result == 0) result = limit;
            game.random(player, limit, result);
        }
        if (cmd.equalsIgnoreCase("flip")) {
            String result = ThreadLocalRandom.current().nextInt(2) == 0 ? "Heads" : "Tails";
            game.flip(player, result);
        }
        if (cmd.equalsIgnoreCase("discard")) {
            String card = cmdObj.findCard(player, JolGame.HAND);
            game.discard(player, card, random);
            if (cmdObj.consumeString("draw")) {
                game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
            }
        }
        if (cmd.equalsIgnoreCase("draw")) {
            boolean crypt = cmdObj.consumeString("crypt") || cmdObj.consumeString("vamp");
            int count = cmdObj.getNumber(1);
            if (count <= 0) throw new CommandException("Must draw at least 1 card.");
            for (int j = 0; j < count; j++) {
                if (crypt)
                    game.drawCard(player, JolGame.CRYPT, JolGame.INACTIVE_REGION);
                else
                    game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
            }
        }
        if (cmd.equalsIgnoreCase("edge")) {
            // edge [<player> | burn]
            if (cmdObj.consumeString("burn")) {
                game.burnEdge();
            } else {
                String edge = cmdObj.getPlayer(player);
                game.setEdge(edge);
            }
        }
        if (cmd.equalsIgnoreCase("play")) {
            boolean crypt = cmdObj.consumeString("vamp");
            if (crypt) {
                throw new CommandException("Invalid command. Use influence instead");
            }
            String srcRegion = cmdObj.getRegion(JolGame.HAND);
            String srcCard = cmdObj.findCard(player, srcRegion);

            String[] modes = null;
            boolean modeSpecified = cmdObj.consumeString("@");
            if (modeSpecified)
                modes = cmdObj.nextArg().split(",");

            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.ASH_HEAP);
            String targetCard = cmdObj.findCard(false, targetPlayer, targetRegion);
            boolean draw = cmdObj.consumeString("draw");
            game.playCard(player, srcCard, targetPlayer, targetRegion, targetCard, modes);
            if (draw) game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
        }
        if (cmd.equalsIgnoreCase("influence")) {
            String srcCard = cmdObj.findCard(player, JolGame.INACTIVE_REGION);
            game.influenceCard(player, srcCard, player, JolGame.READY_REGION);
        }
        if (cmd.equalsIgnoreCase("move")) {
            String srcPlayer = cmdObj.getPlayer(player);
            String srcRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String srcCard = cmdObj.findCard(srcPlayer, srcRegion);
            String destPlayer = cmdObj.getPlayer(player);
            String destRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String destCard = cmdObj.findCard(false, destPlayer, destRegion);
            boolean bottom = !Arrays.asList(cmdStr).contains("top");

            logger.trace("Destination region for command {} is {}", cmd, destRegion);

            if ((destRegion.equals(JolGame.READY_REGION) || destRegion.equals(JolGame.INACTIVE_REGION) || destRegion.equals(JolGame.TORPOR)) && destCard != null) {
                game.moveToCard(player, srcCard, destCard);
            } else {
                game.moveToRegion(player, srcCard, destPlayer, destRegion, bottom);
            }
        }
        if (cmd.equalsIgnoreCase("burn")) {
            String srcPlayer = cmdObj.getPlayer(player);
            String srcRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String cardId = cmdObj.findCard(srcPlayer, srcRegion);
            boolean top = Arrays.asList(cmdStr).contains("top");
            game.burn(player, cardId, srcPlayer, srcRegion, top);
        }
        if (cmd.equalsIgnoreCase("pool")) {
            String targetPlayer = cmdObj.getPlayer(player);
            int amount = cmdObj.getAmount(0);
            if (amount != 0) {
                game.updatePool(targetPlayer, amount);
            } else {
                throw new CommandException("Must specify an amount of pool.");
            }
        }
        if (cmd.equalsIgnoreCase("blood")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String targetCard = cmdObj.findCard(false, targetPlayer, targetRegion);
            if (targetCard == null) throw new CommandException("Must specify a card in the region");
            int amount = cmdObj.getAmount(0);
            if (amount == 0) throw new CommandException("Must specify an amount of blood");
            game.changeCounters(player, targetCard, amount, false);
        }
        if (cmd.equalsIgnoreCase("contest")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String targetCard = cmdObj.findCard(targetPlayer, targetRegion);
            if (targetCard == null) throw new CommandException("Must specify a card in the region");
            boolean clear = cmdObj.consumeString("clear");
            game.contestCard(targetCard, clear);
        }
        if (cmd.equalsIgnoreCase("disc")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String targetCard = cmdObj.findCard(targetPlayer, targetRegion);
            if (targetCard == null) throw new CommandException("Must specify a card in the region");
            if (cmdObj.consumeString("reset")) {
                CardSummary card = CardSearch.INSTANCE.get(game.getCard(targetCard).getCardId());
                List<String> disciplines = card.getDisciplines();
                game.setDisciplines(player, targetCard, disciplines, false);
            } else {
                Set<String> additions = new HashSet<>();
                Set<String> removals = new HashSet<>();
                while (cmdObj.hasMoreArgs()) {
                    String next = cmdObj.nextArg();
                    String type = next.substring(0, 1);
                    String disc = next.substring(1);
                    if (!ChatParser.isDiscipline(disc.toLowerCase())) {
                        throw new CommandException("Not a valid discipline");
                    }
                    if (type.equals("+")) {
                        additions.add(disc);
                    } else if (type.equals("-")) {
                        removals.add(disc);
                    } else {
                        throw new CommandException("Need to specify + or - to change disciplines");
                    }
                }
                game.setDisciplines(player, targetCard, additions, removals);
            }
        }
        if (cmd.equalsIgnoreCase("capacity")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String targetCard = cmdObj.findCard(targetPlayer, targetRegion);
            if (targetCard == null) throw new CommandException("Must specify a card in the region");
            int amount = cmdObj.getAmount(0);
            if (amount == 0) throw new CommandException("Must specify an amount of blood");
            game.changeCapacity(targetCard, amount, false);
        }
        if (cmd.equalsIgnoreCase("unlock")) {
            String targetPlayer = cmdObj.getPlayer(player);
            if (!cmdObj.hasMoreArgs()) {
                game.untapAll(targetPlayer);
            }
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String card = cmdObj.findCard(targetPlayer, targetRegion);
            game.setTapped(player, card, false);
        }
        if (cmd.equalsIgnoreCase("lock")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
            String card = cmdObj.findCard(targetPlayer, targetRegion);
            if (game.isTapped(card))
                throw new CommandException("Card is already locked");
            game.setTapped(player, card, true);
        }
        if (cmd.equalsIgnoreCase("order")) {
            List<String> players = game.getPlayers();
            List<String> newOrder = new ArrayList<>();
            for (int j = 0; j < players.size(); j++) {
                int index = cmdObj.getNumber(-1);
                if (index == -1) throw new CommandException("Must specify a number for each player");
                if (index < 1 || index > players.size()) throw new CommandException("Bad number : " + index);
                newOrder.add(players.get(index - 1));
            }
            game.setOrder(newOrder);
        }
        if (cmd.equalsIgnoreCase("show")) {
            String targetRegion = cmdObj.getRegion(JolGame.LIBRARY);
            int amt = cmdObj.getNumber(100);
            boolean all = cmdObj.consumeString("all");
            List<String> recipients = all ? game.getPlayers() : Collections.singletonList(cmdObj.getPlayer(player));
            Location loc = game.getState().getPlayerLocation(player, targetRegion);
            Card[] cards = loc.getCards();
            StringBuilder buf = new StringBuilder();
            int len = Math.min(cards.length, amt);
            buf.append(len);
            buf.append(" cards of ");
            buf.append(player);
            buf.append("'s ");
            buf.append(targetRegion);
            buf.append(":\n");
            for (int j = 0; j < len; j++) {
                buf.append("  ");
                buf.append(j + 1);
                buf.append(" ");
                buf.append(cards[j].getName());
                buf.append("\n");
            }
            String text = buf.toString();
            for (String recipient : recipients) {
                String old = game.getPrivateNotes(recipient);
                game.setPrivateNotes(recipient, old.isEmpty() ? text : old + "\n" + text);
                JolAdmin.INSTANCE.getGameModel(game.getName()).getView(recipient).privateNotesChanged();
            }
            String msg;
            if (player.equals(recipients.getFirst())) {
                msg = player + " looks at " + len + " cards of their " + targetRegion + ".";
            } else {
                msg = player + " shows " + (recipients.size() > 1 ? "everyone" : recipients.getFirst()) + " " + len + " cards of their " + targetRegion + ".";
            }
            game.addMessage(msg);
        }
        if (cmd.equalsIgnoreCase("shuffle")) {
            String targetPlayer = cmdObj.getPlayer(player);
            String targetRegion = cmdObj.getRegion(JolGame.LIBRARY);
            int num = cmdObj.getNumber(0);
            game.shuffle(targetPlayer, targetRegion, num);
        }
        if (cmd.equalsIgnoreCase("transfer")) {
            String targetRegion = cmdObj.getRegion(JolGame.INACTIVE_REGION);
            String card = cmdObj.findCard(player, targetRegion);
            int amount = cmdObj.getAmount(1);
            if (amount == 0) throw new CommandException("Must transfer an amount");
            int pool = game.getPool(player);
            if (pool - amount < 0) throw new CommandException("Invalid amount to transfer.  Not enough pool.");
            game.transfer(player, card, amount);
        }
    }
}
