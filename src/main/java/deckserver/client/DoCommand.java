/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package deckserver.client;

import deckserver.game.cards.CardEntry;
import deckserver.game.state.Card;
import deckserver.game.state.Location;
import org.slf4j.Logger;

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

    public String doMessage(String player, String message) {
        if (message.equals(""))
            return "No message received";
        game.sendMsg(player, message);
        return "Sent message";
    }

    public String doCommand(String player, String[] cmdStr) throws CommandException {
        String cmd = cmdStr[0];
        CommandParser cmdObj = new CommandParser(cmdStr, 1, game);
        try {
            if (cmd.equalsIgnoreCase("label")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                StringBuilder note = new StringBuilder();
                while (cmdObj.hasMoreArgs()) {
                    note.append(" ");
                    note.append(cmdObj.nextArg());
                }
                game.setText(card, note.toString());
                return "Adjusted label";
            }
            if (cmd.equalsIgnoreCase("order")) {
                String[] players = game.getPlayers();
                String[] neworder = new String[players.length];
                for (int j = 0; j < players.length; j++) {
                    int index = cmdObj.getNumber(-1);
                    if (index == -1) return "Must specify a number for each player";
                    if (index < 1 || index > players.length) return "Bad number : " + index;
                    neworder[j] = players[index - 1];
                }
                game.setOrder(neworder);
                return "Changed seating order";
            }
            if (cmd.equalsIgnoreCase("random")) {
                int d = cmdObj.getNumber(-1);
                if (d < 1) d = 2;
                int num = ThreadLocalRandom.current().nextInt(1, d + 1);
                if (num == 0) num = d;
                game.sendMsg(player, player + " rolls from 1-" + d + " : " + num);
                return "Rolled the die";
            }
            if (cmd.equalsIgnoreCase("discard")) {
                String card = cmdObj.getCard(false, player, JolGame.HAND);
                game.moveToRegion(card, player, JolGame.ASHHEAP, false);
                if (cmdObj.consumeString("draw")) {
                    game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                }
                return "Discarded";
            }
            if (cmd.equalsIgnoreCase("draw")) {
                // draw [vamp] [<numcards>]
                boolean vamp = cmdObj.consumeString("vamp");
                int count = cmdObj.getNumber(1);
                if (count <= 0) return "Must draw > 0 cards";
                for (int j = 0; j < count; j++) {
                    if (vamp)
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
                // play [vamp] <cardnumber> [<targetplayer>] [<targetregion>] [<targetcard>] [draw]
                boolean crypt = cmdObj.consumeString("vamp");
                String srcRegion = crypt ? JolGame.INACTIVE_REGION : JolGame.HAND;
                boolean docap = srcRegion.equals(JolGame.INACTIVE_REGION);
                String srcCard = cmdObj.getCard(false, player, srcRegion);
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(crypt ? JolGame.READY_REGION : JolGame.ASHHEAP);
                String targetCard = cmdObj.getCard(true, targetPlayer, targetRegion);
                boolean draw = cmdObj.consumeString("draw");
                if (targetCard != null) {
                    game.moveToCard(srcCard, targetCard);
                    if (draw) game.drawCard(player, JolGame.LIBRARY, JolGame.HAND);
                    return "Put a card on another card";
                } else {
                    game.moveToRegion(srcCard, targetPlayer, targetRegion, true);
                    if (docap) {
                        int curcap = game.getCapacity(srcCard);
                        if (curcap <= 0) {
                            CardEntry card =
                                    JolAdmin.getInstance().getAllCards().getCardById(game.getCardDescripId(srcCard));
                            int capincr = 1;
                            String[] text = card.getFullText();
                            for (String aText : text) {
                                if (aText.startsWith("Capacity:")) {
                                    capincr = Integer.parseInt(aText.substring(10).trim());
                                }
                            }
                            game.changeCapacity(srcCard, capincr);
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

                logger.trace("Destination region for command {} is {}", cmd, destRegion);

                if ((destRegion.equals(JolGame.READY_REGION) || destRegion.equals(JolGame.INACTIVE_REGION) || destRegion.equals(JolGame.TORPOR)) && destCard != null) {
                    game.moveToCard(srcCard, destCard);
                    return "Moved it onto the card";
                } else {
                    game.moveToRegion(srcCard, destPlayer, destRegion, true);
                    return "Moved the card";
                }
            }
            if (cmd.equalsIgnoreCase("blood")) {
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(null);
                String targetCard = null;
                if (targetRegion != null) {
                    targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                    if (targetCard == null) throw new CommandException("Must specify a card in the region");
                }
                int amount = cmdObj.getAmount(0);
                if (amount == 0) return "Must specify an amount of blood";
                if (targetRegion == null) {
                    // int pool = game.getPool(targetPlayer);
                    game.changePool(targetPlayer, amount);
                    return "Adjusted pool";
                } else {
                    game.changeCounters(targetCard, amount);
                    return "Adjusted blood";
                }
            }
            if (cmd.equalsIgnoreCase("capacity")) {
                // blood [<targetplayer>] [<targetregion>] <targetcard> [+|-]<amount>
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String targetCard = cmdObj.getCard(false, targetPlayer, targetRegion);
                if (targetCard == null) throw new CommandException("Must specify a card in the region");
                int amount = cmdObj.getAmount(0);
                if (amount == 0) return "Must specify an amount of blood";
                game.changeCapacity(targetCard, amount);
                return "Adjusted capacity";
            }
            if (cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("message")) {
                String message = "";
                while (cmdObj.hasMoreArgs())
                    message = message + " " + cmdObj.nextArg();
                return doMessage(player, message);
            }
            if (cmd.equalsIgnoreCase("untap") || cmd.equalsIgnoreCase("unlock")) {
                // unlock [<targetplayer>] [<targetregion>] [<targetcard>]
                String targetPlayer = cmdObj.getPlayer(player);
                if (!cmdObj.hasMoreArgs()) {
                    game.untapAll(targetPlayer);
                    return "Unlock all";
                }
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                game.setTapped(card, false);
                return "Unlock card";
            }
            if (cmd.equalsIgnoreCase("tap") || cmd.equalsIgnoreCase("lock")) {
                // lock [<targetplayer>] [<targetregion>] <targetcard>
                String targetPlayer = cmdObj.getPlayer(player);
                String targetRegion = cmdObj.getRegion(JolGame.READY_REGION);
                String card = cmdObj.getCard(false, targetPlayer, targetRegion);
                game.setTapped(card, true);
                return "Locked card";
            }
            if (cmd.equalsIgnoreCase("show")) {
                // show [<targetregion>] <amount> [<recipientplayer>]
                String targetRegion = cmdObj.getRegion(JolGame.LIBRARY);
                int amt = cmdObj.getNumber(100);
                boolean all = cmdObj.consumeString("all");
                String[] recipients = all ? game.getPlayers() : new String[]{cmdObj.getPlayer(player)};
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
                    String old = game.getPlayerText(recipient);
                    game.setPlayerText(recipient, old + "\n" + text);
                }
                String msg = null;
                if (recipients.length == 1) {
                    msg = "Looks at " + len + " cards of " +
                            (!player.equals(recipients[0]) ? player + "'s " : "") + targetRegion + ".";
                    game.sendMsg(recipients[0], msg);
                } else {
                    msg = "Everyone looks at " + len + " cards of " + player + "'s " + targetRegion + ".";
                    game.sendMsg(player, msg);
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
                game.changeCounters(card, amount);
                return "Did the transfer";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return format(cmdStr) + " had a badly formatted number";
        } catch (CommandException ce) {
            throw ce;
        } catch (Exception e) {
            e.printStackTrace();
            return cmd + " produced exception.";
        }
        return cmd + " not a valid command";
    }
}
