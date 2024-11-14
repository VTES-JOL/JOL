/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.ui.state.CardDetail;
import net.deckserver.storage.json.cards.CardSummary;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.slf4j.LoggerFactory.getLogger;

public class DoCommand {

    private static final Logger logger = getLogger(DoCommand.class);
    private final JolGame game;

    public DoCommand(JolGame game) {
        this.game = game;
    }

    private static String format(String[] str) {
        if (str == null || str.length == 0) return "";
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length; ) {
            buf.append(str[i++]);
            if (i < str.length) buf.append(" ");
        }
        return buf.toString();
    }

    public String doMessage(String player, String message, boolean isJudge) {
        if (message.equals(""))
            return "No message received";
        game.sendMsg(player, message, isJudge);
        return "Sent message";
    }

    public String doCommand(String player, String[] cmdStr) throws CommandException {
        String cmd = cmdStr[0];
        CommandParser cmdObj = new CommandParser(cmdStr, 1, game);
        boolean random = Arrays.asList(cmdStr).contains("random");
        try {
            if (cmd.equalsIgnoreCase("timeout")) {
                boolean cancel = cmdObj.consumeString("cancel");
                return game.requestTimeout(player, cancel);
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
                return "Updated victory points";
            }
            if (cmd.equalsIgnoreCase("choose")) {
                String choice = cmdObj.nextArg();
                game.setChoice(player, choice);
                return "Made choice";
            }
            if (cmd.equalsIgnoreCase("reveal")) {
                game.getChoices();
                return "Choices revealed";
            }
            if (cmd.equalsIgnoreCase("label")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                StringBuilder note = new StringBuilder();
                while (cmdObj.hasMoreArgs()) {
                    note.append(" ");
                    note.append(cmdObj.nextArg());
                }
                game.setText(card, note.toString(), false);
                return "Adjusted label";
            }
            if (cmd.equalsIgnoreCase("votes")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);

                game.setVotes(card, cmdObj.nextArg());
                return "Adjusted votes";
            }
            if (cmd.equalsIgnoreCase("random")) {
                int limit = cmdObj.getNumber(-1);
                if (limit < 1) limit = 2;
                int result = ThreadLocalRandom.current().nextInt(1, limit + 1);
                if (result == 0) result = limit;
                game.random(player, limit, result);
                return "Rolled the die";
            }
            if (cmd.equalsIgnoreCase("flip")) {
                String result = ThreadLocalRandom.current().nextInt(2) == 0 ? "Heads" : "Tails";
                game.flip(player, result);
                return "Flipped a coin";
            }
            if (cmd.equalsIgnoreCase("discard")) {
                String card = cmdObj.getCard(false, player, JolGame.HAND);
                game.discard(player, card, random);
                if (cmdObj.consumeString("draw")) {
                    game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                }
                return "Discarded";
            }
            if (cmd.equalsIgnoreCase("draw")) {
                // draw [vamp] [<numcards>]
                boolean crypt = cmdObj.consumeString("crypt") || cmdObj.consumeString("vamp");
                int count = cmdObj.getNumber(1);
                if (count <= 0) return "Must draw > 0 cards";
                for (int j = 0; j < count; j++) {
                    if (crypt)
                        game.drawCard(player, JolGame.CRYPT, JolGame.INACTIVE_REGION);
                    else
                        game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                }
                return "Drew " + count + " cards.";
            }
            if (cmd.equalsIgnoreCase("edge")) {
                // edge [<player> | burn]
                if (cmdObj.consumeString("burn")) {
                    game.setEdge(null);
                    return "Burned the edge.";
                }
                String edge = cmdObj.getPlayer(player);
                game.setEdge(edge);
                return edge + " grabs the edge";
            }
            if (cmd.equalsIgnoreCase("play")) {
                // play [vamp] <cardnumber> [@ <modes>] [<targetplayer>] [<targetregion>] [<targetcard>] [draw]
                boolean crypt = cmdObj.consumeString("vamp");
                String srcRegion = crypt ? JolGame.INACTIVE_REGION : JolGame.HAND;
                boolean docap = srcRegion.equals(JolGame.INACTIVE_REGION);
                String srcCard = cmdObj.getCard(false, player, srcRegion);

                String[] modes = null;
                boolean modeSpecified = cmdObj.consumeString("@");
                if (modeSpecified)
                    modes = cmdObj.nextArg().split(",");

                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(crypt ? JolGame.READY_REGION : JolGame.ASHHEAP);
                String targetCard = cmdObj.getCard(true, targetPlayer, targetRegion);
                boolean draw = cmdObj.consumeString("draw");
                if (targetCard != null) {
                    game.moveToCard(true, player, srcCard, targetPlayer, targetRegion, targetCard, modes);
                    if (draw) game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                    return "Played a card on another card";
                } else {
                    game.playCard(player, srcCard, targetPlayer, targetRegion, modes);
                    if (docap) {
                        int curcap = game.getCapacity(srcCard);
                        if (curcap <= 0) {
                            CardDetail detail = game.getCard(srcCard);
                            CardSummary card = CardSearch.INSTANCE.get(detail.getCardId());
                            Integer capacity = card.getCapacity();
                            if (capacity != null) {
                                game.changeCapacity(srcCard, capacity, false);
                            }
                            // Do disciplines
                            List<String> disciplines = card.getDisciplines();
                            game.setDisciplines(srcCard, disciplines, true);
                            // Do votes
                            String votes = card.getVotes();
                            if (votes != null && !votes.isEmpty()) {
                                game.setVotes(srcCard, votes);
                            }
                        }
                    }
                    if (draw) game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                    return "Played a card";
                }
            }
            if (cmd.equalsIgnoreCase("move")) {
                String srcPlayer = cmdObj.getPlayer(player);
                String srcRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String srcCard = cmdObj.getCard(false, srcPlayer, srcRegion);
                String destPlayer = cmdObj.getPlayer(player);
                String destRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String destCard = cmdObj.getCard(true, destPlayer, destRegion);
                boolean bottom = !Arrays.asList(cmdStr).contains("top");

                logger.trace("Destination region for command {} is {}", cmd, destRegion);

                if ((destRegion.equals(JolGame.READY_REGION) || destRegion.equals(JolGame.INACTIVE_REGION) || destRegion.equals(JolGame.TORPOR)) && destCard != null) {
                    game.moveToCard(player, srcCard, destCard);
                    return "Moved it onto the card";
                } else {
                    game.moveToRegion(player, srcCard, destPlayer, destRegion, bottom);
                    return "Moved the card";
                }
            }
            if (cmd.equalsIgnoreCase("burn")) {
                String srcPlayer = cmdObj.getPlayer(player);
                String srcRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String cardId = cmdObj.getCard(false, srcPlayer, srcRegion, true);
                boolean top = Arrays.asList(cmdStr).contains("top");
                game.burn(player, cardId, srcPlayer, srcRegion, top);
                return "Burned the card";
            }
            if (cmd.equalsIgnoreCase("pool")) {
                String targetPlayer = cmdObj.getPlayer(player);
                int amount = cmdObj.getAmount(0);
                game.changePool(targetPlayer, amount);
                return "Adjusted pool";
            }
            if (cmd.equalsIgnoreCase("blood")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (targetCard == null) throw new CommandException("Must specify a card in the region");
                int amount = cmdObj.getAmount(0);
                if (amount == 0) return "Must specify an amount of blood";
                game.changeCounters(player, targetCard, amount, false);
                return "Adjusted blood";
            }
            if (cmd.equalsIgnoreCase("contest")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (targetCard == null) throw new CommandException("Must specify a card in the region");
                boolean clear = cmdObj.consumeString("clear");
                game.contestCard(player, targetCard, clear);
                return clear ? "Cleared Contest." : "Contested card.";
            }
            if (cmd.equalsIgnoreCase("disc")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (targetCard == null) throw new CommandException("Must specify a card in the region");
                if (cmdObj.consumeString("reset")) {
                    CardSummary card = CardSearch.INSTANCE.get(game.getCard(targetCard).getCardId());
                    List<String> disciplines = card.getDisciplines();
                    game.setDisciplines(targetCard, disciplines, false);
                }
                while (cmdObj.hasMoreArgs()) {
                    String next = cmdObj.nextArg();
                    String type = next.substring(0, 1);
                    String disc = next.substring(1);
                    if (!ChatParser.isDiscipline(disc.toLowerCase())) {
                        throw new CommandException("Not a valid discipline");
                    }
                    if (type.equals("+")) {
                        game.addDiscipline(targetCard, disc);
                    } else if (type.equals("-")) {
                        game.removeDiscipline(targetCard, disc);
                    } else {
                        throw new CommandException("Need to specify + or - to change disciplines");
                    }
                }
                return "Updated disciplines";
            }
            if (cmd.equalsIgnoreCase("capacity")) {
                // blood [<targetplayer>] [<targetregion>] <targetcard> [+|-]<amount>
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (targetCard == null) throw new CommandException("Must specify a card in the region");
                int amount = cmdObj.getAmount(0);
                if (amount == 0) return "Must specify an amount of blood";
                game.changeCapacity(targetCard, amount, false);
                return "Adjusted capacity";
            }
            if (cmd.equalsIgnoreCase("unlock")) {
                // unlock [<targetplayer>] [<targetregion>] [<targetcard>]
                String targetPlayer = cmdObj.getPlayer(player);
                if (!cmdObj.hasMoreArgs()) {
                    game.untapAll(targetPlayer);
                    return "Unlock all";
                }
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                game.setTapped(player, card, false);
                return "Unlock card";
            }
            if (cmd.equalsIgnoreCase("lock")) {
                // lock [<targetplayer>] [<targetregion>] <targetcard>
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (game.isTapped(card))
                    throw new CommandException("Card is already locked");
                game.setTapped(player, card, true);
                return "Locked card";
            }
            // TODO Fix this
            if (cmd.equalsIgnoreCase("show")) {
                // show [<targetregion>] <amount> [<recipientplayer>]
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
                    game.setPrivateNotes(recipient, old + "\n" + text);
                    JolAdmin.INSTANCE.getGameModel(game.getName()).getView(recipient).privateNotesChanged();
                }
                String msg = null;
                if (recipients.size() == 1) {
                    msg = "Looks at " + len + " cards of " +
                            (!player.equals(recipients.get(0)) ? player + "'s " : "") + targetRegion + ".";
                    game.sendMsg(recipients.get(0), msg, false);
                } else {
                    msg = "Everyone looks at " + len + " cards of " + player + "'s " + targetRegion + ".";
                    game.sendMsg(player, msg, false);
                }
                return "Showed cards.";
            }
            if (cmd.equalsIgnoreCase("shuffle")) {
                // shuffle [<targetplayer>] [<targetregion>]
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.LIBRARY);
                int num = cmdObj.getNumber(0);
                game.shuffle(targetPlayer, targetRegion, num);
                return "Shuffled.";
            }
            if (cmd.equalsIgnoreCase("transfer")) {
                // transfer [<targetPlayer>] <vampno> [+|-]<amount>
                String targetRegion = cmdObj.getRegion(JolGame.INACTIVE_REGION);
                String card = cmdObj.getCard(false, player, targetRegion);
                int amount = cmdObj.getAmount(1);
                if (amount == 0) return "Must transfer an amount";
                game.changePool(player, -amount);
                game.changeCounters(player, card, amount, false);
                return "Did the transfer";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return format(cmdStr) + " had a badly formatted number";
        } catch (CommandException ce) {
            throw ce;
        } catch (Exception e) {
            e.printStackTrace();
            return cmd + " created error: " + e.getMessage();
        }
        return cmd + " not a valid command";
    }
}
