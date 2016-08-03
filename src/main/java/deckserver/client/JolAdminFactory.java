/*
 * JolGame.java
 *
 * Created on October 25, 2003, 8:38 PM
 */

package deckserver.client;

import deckserver.game.cards.OldCardSearch;

import java.util.Date;

/**
 * @author administrator
 */
public abstract class JolAdminFactory {

    public static JolAdminFactory INSTANCE = null;

    public abstract void recordAccess(String playerName);

    public abstract Date getLastAccess(String playerName);

    public abstract Date getGameTimeStamp(String gameName);

    public abstract void recordAccess(String gameName, String playerName);

    public abstract Date getAccess(String name, String player);

    public abstract boolean doInteractive(String playerName);

    public abstract boolean mkGame(String game);

    public abstract boolean registerPlayer(String name, String password, String email);

    public abstract boolean existsPlayer(String name);

    public abstract boolean existsGame(String name);

    public abstract boolean createDeck(String player, String name, String deck);

    public abstract void removeDeck(String player, String deckname);

    public abstract String getDeck(String player, String name);

    public abstract String[] getDeckNames(String player);

    public abstract String[] getGames(String player);

    public abstract String[] getGames();

    public abstract String[] getPlayers();

    public abstract String[] getPlayers(String game);

    public abstract boolean authenticate(String player, String password);

    public abstract boolean addPlayerToGame(String gameName, String player, String deckName);

    public abstract JolGame getGame(String name);

    public abstract String getId(String name);

    public abstract void saveGame(JolGame game);

    public abstract void startGame(String game);

    public abstract void invitePlayer(String gameName, String player);

    public abstract boolean isInvited(String gameName, String player);

    public abstract String getGameDeck(String gameName, String player);

    public abstract boolean isOpen(String gameName);

    public abstract boolean isActive(String gameName);

    public abstract boolean isFinished(String gameName);

    public abstract void endGame(String name);

    public abstract boolean isAdmin(String player);

    public abstract String getEmail(String player);

    public abstract void setOwner(String game, String player);

    public abstract String getOwner(String game);

    public abstract OldCardSearch getAllCards();

    public abstract void setGP(String gamename, String prop, String value);

    public abstract String getDeckName(String game, String player);

}
