package deckserver.dwr;

import java.util.Map;

public interface DSRemote {
    String[] getTypes();

    Map<String, Object> doPoll();

    Map<String, Object> createGame(String name);

    Map<String, Object> endGame(String name);

    Map<String, Object> init();

    String inspect(String name);

    Map<String, Object> invitePlayer(String game, String name);

    Map<String, Object> startGame(String game);

    Map<String, Object> chat(String txt);

    Map<String, Object> navigate(String target);

    Map<String, Object> getState(String game, boolean forceLoad);

    String[] getHistory(String game, String turn);

    Map<String, Object> getCardText(String callback, String game,
                                    String id);

    Map<String, Object> doToggle(String game, String id);

    Map<String, Object> submitForm(String gamename, String phase,
                                   String command, String chat,
                                   String ping, String endTurn,
                                   String global, String text);

    Map<String, Object> submitDeck(String name, String deck);

    Map<String, Object> registerDeck(String game, String name);

    boolean removeDeck(String name);

    Map<String, Object> getDeck(String name);

    Map<String, Object> refreshDeck(String name, String deck,
                                    String shuffle);

    Map<String, Object> cardSearch(String type, String string);

    String doCommand(String cmd);
}
