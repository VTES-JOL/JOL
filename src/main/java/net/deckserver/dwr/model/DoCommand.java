/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import net.deckserver.services.CardService;
import net.deckserver.game.enums.Clan;
import net.deckserver.game.enums.Path;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.enums.Sect;
import net.deckserver.services.ParserService;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.game.CardData;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public record DoCommand(JolGame game, GameModel model) {

    public String doMessage(String player, String message, boolean isJudge) {
        if (message.isEmpty())
            return "No message received";
        game.sendMsg(player, message, isJudge);
        return null;
    }

    public void doCommand(String player, String command) throws CommandException {
        command = command.replaceAll("\\s{2,}", " ");
        String[] cmdStr = command.trim().split("[\\s\n\r\f\t]");
        String cmd = cmdStr[0];
        CommandParser cmdObj = new CommandParser(cmdStr, 1, game);
        switch (cmd.toLowerCase()) {
            case "timeout":
                timeOut(player);
                break;
            case "vp":
                vp(cmdObj, player);
                break;
            case "choose":
                choose(cmdObj, player);
                break;
            case "reveal":
                reveal();
                break;
            case "label":
                label(cmdObj, player);
                break;
            case "votes":
                votes(cmdObj, player);
                break;
            case "random":
                random(cmdObj, player);
                break;
            case "flip":
                flip(player);
                break;
            case "discard":
                discard(cmdObj, player);
                break;
            case "draw":
                draw(cmdObj, player);
                break;
            case "edge":
                edge(cmdObj, player);
                break;
            case "play":
                play(cmdObj, player);
                break;
            case "influence":
                influence(cmdObj, player);
                break;
            case "move":
                move(cmdObj, player);
                break;
            case "burn":
                burn(cmdObj, player);
                break;
            case "pool":
                pool(cmdObj, player);
                break;
            case "blood":
                blood(cmdObj, player);
                break;
            case "contest":
                contest(cmdObj, player);
                break;
            case "disc":
                disciplines(cmdObj, player);
                break;
            case "capacity":
                capacity(cmdObj, player);
                break;
            case "unlock":
                unlock(cmdObj, player);
                break;
            case "lock":
                lock(cmdObj, player);
                break;
            case "order":
                order(cmdObj, player);
                break;
            case "show":
                show(cmdObj, player);
                break;
            case "shuffle":
                shuffle(cmdObj, player);
                break;
            case "transfer":
                transfer(cmdObj, player);
                break;
            case "rfg":
                rfg(cmdObj, player);
                break;
            case "path":
                path(cmdObj, player);
                break;
            case "sect":
                sect(cmdObj, player);
                break;
            case "clan":
                clan(cmdObj, player);
                break;
        }
    }

    private void sect(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        if (!cmdObj.hasMoreArgs()) {
            game.setSect(player, targetCard.getId(), Sect.NONE, false);
        } else {
            String sectString = cmdObj.nextArg();
            Sect sect = Sect.startsWith(sectString);
            if (sect == null) {
                throw new CommandException("Invalid sect");
            }
            game.setSect(player, targetCard.getId(), sect, false);
        }
    }

    private void
    path(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        if (!cmdObj.hasMoreArgs()) {
            game.setPath(player, targetCard.getId(), Path.NONE, false);
        } else {
            String pathString = cmdObj.nextArg();
            Path path = Path.startsWith(pathString);
            if (Path.NONE.equals(path)) {
                throw new CommandException("Invalid path");
            }
            game.setPath(player, targetCard.getId(), path, false);
        }
    }

    private void clan(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        if (!cmdObj.hasMoreArgs()) {
            game.setClan(player, targetCard.getId(), Clan.NONE, false);
        } else {
            String clanString = cmdObj.remaining();
            Clan clan = Clan.startsWith(clanString);
            game.setClan(player, targetCard.getId(), clan, false);
        }
    }

    void contest(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        boolean clear = cmdObj.consumeString("clear");
        game.contestCard(player, targetCard.getId(), clear);
    }

    void transfer(CommandParser cmdObj, String player) throws CommandException {
        RegionType targetRegion = cmdObj.getRegion(RegionType.UNCONTROLLED);
        CardData card = cmdObj.findCardData(false, player, targetRegion);
        int amount = cmdObj.getAmount(1);
        if (amount == 0) throw new CommandException("Must transfer an amount");
        int pool = game.getPool(player);
        if (pool - amount < 0) throw new CommandException("Invalid amount to transfer.  Not enough pool.");
        if (amount < 0 && card.getCounters() < Math.abs(amount)) throw new CommandException("Not enough counters to transfer.");
        game.transfer(player, card.getId(), amount);
    }

    void shuffle(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.LIBRARY);
        int num = cmdObj.getNumber(0);
        game.shuffle(targetPlayer, targetRegion, num);
    }

    void show(CommandParser cmdObj, String player) throws CommandException {
        RegionType targetRegion = cmdObj.getRegion(RegionType.LIBRARY);
        int amount = cmdObj.getNumber(100);
        boolean all = cmdObj.consumeString("all");
        List<String> recipients = all ? game.getValidPlayers() : Collections.singletonList(cmdObj.getPlayer(player));
        game.show(player, targetRegion, amount, recipients);
        for (String recipient: recipients) {
            model.getView(recipient).privateNotesChanged();
        }
    }

    void order(CommandParser cmdObj, String player) throws CommandException {
        List<String> players = game.getPlayers();
        List<String> newOrder = new ArrayList<>();
        for (int j = 0; j < players.size(); j++) {
            int index = cmdObj.getNumber(0);
            if (index < 1 || index > players.size()) throw new CommandException("Bad number : " + index);
            newOrder.add(players.get(index - 1));
        }
        game.setOrder(player, newOrder);
    }

    void lock(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData card = cmdObj.findCardData(true, targetPlayer, targetRegion);
        if (card.isLocked())
            throw new CommandException("Card is already locked");
        game.setLocked(player, card.getId(), true);
    }

    void unlock(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        if (!cmdObj.hasMoreArgs()) {
            game.unlockAll(targetPlayer);
        } else {
            RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
            CardData card = cmdObj.findCardData(true, targetPlayer, targetRegion);
            game.setLocked(player, card.getId(), false);
        }
    }

    void capacity(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        int amount = cmdObj.getAmount(0);
        if (amount == 0) throw new CommandException("Must specify an amount of blood");
        game.changeCapacity(player, targetCard.getId(), amount, false);
    }

    void disciplines(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, targetPlayer, targetRegion);
        if (cmdObj.consumeString("reset")) {
            CardSummary card = CardService.get(targetCard.getCardId());
            List<String> disciplines = card.getDisciplines();
            game.setDisciplines(player, targetCard.getId(), disciplines, false);
        } else {
            Set<String> additions = new HashSet<>();
            Set<String> removals = new HashSet<>();
            while (cmdObj.hasMoreArgs()) {
                String next = cmdObj.nextArg();
                String type = next.substring(0, 1);
                String disc = next.substring(1);
                if (!ParserService.isDiscipline(disc.toLowerCase())) {
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
            game.setDisciplines(player, targetCard.getId(), additions, removals);
        }
    }

    void blood(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData targetCard = cmdObj.findCardData(false, false, targetPlayer, targetRegion);
        int amount = cmdObj.getAmount(0);
        if (amount == 0) throw new CommandException("Must specify an amount of blood");
        game.changeCounters(player, targetCard.getId(), amount, false);
    }

    void pool(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        int amount = cmdObj.getAmount(0);
        if (amount != 0) {
            game.changePool(player, targetPlayer, amount);
        } else {
            throw new CommandException("Must specify an amount of pool.");
        }
    }

    void burn(CommandParser cmdObj, String player) throws CommandException {
        String srcPlayer = cmdObj.getPlayer(player);
        RegionType srcRegion = cmdObj.getRegion(RegionType.READY);
        CardData card = cmdObj.findCardData(true, srcPlayer, srcRegion);
        boolean random = Arrays.asList(cmdObj.args).contains("random");
        game.burn(player, card.getId(), srcPlayer, srcRegion, random);
    }

    void rfg(CommandParser cmdObj, String player) throws CommandException {
        String srcPlayer = cmdObj.getPlayer(player);
        RegionType srcRegion = cmdObj.getRegion(RegionType.ASH_HEAP);
        CardData card = cmdObj.findCardData(true, srcPlayer, srcRegion);
        boolean random = Arrays.asList(cmdObj.args).contains("random");
        game.rfg(player, card.getId(), srcPlayer, srcRegion, random);
    }

    void move(CommandParser cmdObj, String player) throws CommandException {
        String srcPlayer = cmdObj.getPlayer(player);
        RegionType srcRegion = cmdObj.getRegion(RegionType.READY);
        CardData srcCard = cmdObj.findCardData(false, srcPlayer, srcRegion);
        String destPlayer;
        boolean predatorFlag = cmdObj.consumeString("predator");
        boolean preyFlag = cmdObj.consumeString("prey");
        if (predatorFlag) {
            destPlayer = game.getPredatorOf(srcPlayer);
        } else if (preyFlag) {
            destPlayer = game.getPreyOf(srcPlayer);
        } else {
            destPlayer = cmdObj.getPlayer(player);
        }
        RegionType destRegion = cmdObj.getRegion(RegionType.READY);
        CardData destCard = cmdObj.findCardData(false, false, destPlayer, destRegion);
        boolean top = Arrays.asList(cmdObj.args).contains("top");

        if (List.of(RegionType.READY, RegionType.UNCONTROLLED, RegionType.TORPOR).contains(destRegion) && destCard != null) {
            game.moveToCard(player, srcCard.getId(), destCard.getId());
        } else {
            game.moveToRegion(player, srcCard.getId(), destPlayer, destRegion, top);
        }
    }

    void influence(CommandParser cmdObj, String player) throws CommandException {
        CardData srcCard = cmdObj.findCardData(false, player, RegionType.UNCONTROLLED);
        game.influenceCard(player, srcCard.getId());
    }

    void play(CommandParser cmdObj, String player) throws CommandException {
        boolean crypt = cmdObj.consumeString("vamp");
        if (crypt) {
            throw new CommandException("Invalid command. Use influence instead");
        }
        RegionType srcRegion = cmdObj.getRegion(RegionType.HAND);
        CardData srcCard = cmdObj.findCardData(false, player, srcRegion);

        String[] modes = null;
        boolean modeSpecified = cmdObj.consumeString("@");
        if (modeSpecified)
            modes = cmdObj.nextArg().split(",");

        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.ASH_HEAP);
        String targetCardId = Optional.ofNullable(cmdObj.findCardData(false, false, targetPlayer, targetRegion)).map(CardData::getId).orElse(null);
        boolean draw = cmdObj.consumeString("draw");
        game.playCard(player, srcCard.getId(), targetPlayer, targetRegion, targetCardId, modes);
        if (draw) game.drawCard(player, RegionType.LIBRARY, RegionType.HAND);
    }

    void edge(CommandParser cmdObj, String player) throws CommandException {
        // edge [<player> | burn]
        if (cmdObj.consumeString("burn")) {
            game.burnEdge(player);
        } else {
            String edge = cmdObj.getPlayer(player);
            game.setEdge(player, edge);
        }
    }

    void draw(CommandParser cmdObj, String player) throws CommandException {
        boolean crypt = cmdObj.consumeString("crypt") || cmdObj.consumeString("vamp");
        int count = cmdObj.getNumber(1);
        int size = crypt ? game.getSize(player, RegionType.CRYPT) : game.getSize(player, RegionType.LIBRARY);
        if (count <= 0) throw new CommandException("Must draw at least 1 card.");
        if (count > size) throw new CommandException("Unable to draw, only " + size + " cards left.");
        for (int j = 0; j < count; j++) {
            if (crypt)
                game.drawCard(player, RegionType.CRYPT, RegionType.UNCONTROLLED);
            else
                game.drawCard(player, RegionType.LIBRARY, RegionType.HAND);
        }
    }

    void label(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData card = cmdObj.findCardData(false, targetPlayer, targetRegion);
        StringBuilder note = new StringBuilder();
        while (cmdObj.hasMoreArgs()) {
            note.append(" ");
            note.append(cmdObj.nextArg());
        }
        game.setLabel(player, card.getId(), note.toString(), false);
    }

    void random(CommandParser cmdObj, String player) throws CommandException {
        int limit = cmdObj.getNumber(-1);
        if (limit < 1) limit = 2;
        int result = ThreadLocalRandom.current().nextInt(1, limit + 1);
        game.random(player, limit, result);
    }

    void discard(CommandParser cmdObj, String player) throws CommandException {
        boolean random = Arrays.asList(cmdObj.args).contains("random");
        CardData card = cmdObj.findCardData(true, player, RegionType.HAND);
        game.discard(player, card.getId(), random);
        if (cmdObj.consumeString("draw")) {
            game.drawCard(player, RegionType.LIBRARY, RegionType.HAND);
        }
    }

    void flip(String player) {
        String result = ThreadLocalRandom.current().nextInt(2) == 0 ? "Heads" : "Tails";
        game.flip(player, result);
    }

    void votes(CommandParser cmdObj, String player) throws CommandException {
        String targetPlayer = cmdObj.getPlayer(player);
        RegionType targetRegion = cmdObj.getRegion(RegionType.READY);
        CardData card = cmdObj.findCardData(false, targetPlayer, targetRegion);
        game.setVotes(player, card.getId(), cmdObj.nextArg(), false);
    }

    void reveal() {
        game.getChoices();
    }

    void timeOut(String player) {
        game.requestTimeout(player);
    }

    void vp(CommandParser cmdObj, String player) throws CommandException {
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

    void choose(CommandParser cmdObj, String player) throws CommandException {
        String choice = cmdObj.nextArg();
        game.setChoice(player, choice);
    }
}
