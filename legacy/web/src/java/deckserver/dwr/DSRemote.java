package deckserver.dwr;

import deckserver.dwr.bean.BugDetailBean;

import java.util.Map;

public interface DSRemote {
    public String[] getTypes();

    public Map<String, Object> doPoll();

    public Map<String, Object> createGame(String name);

    public Map<String, Object> endGame(String name);

    public Map<String, Object> init();

    public String inspect(String name);

    public Map<String, Object> invitePlayer(String game, String name);

    public Map<String, Object> startGame(String game);

    public Map<String, Object> chat(String txt);

    public Map<String, Object> navigate(String target);

    public Map<String, Object> getState(String game, boolean forceLoad);

    public String[] getHistory(String game, String turn);

    public Map<String, Object> getCardText(String callback, String game, 
                                           String id);

    public Map<String, Object> doToggle(String game, String id);

    public Map<String, Object> submitForm(String gamename, String phase, 
                                          String command, String chat, 
                                          String ping, String endTurn, 
                                          String global, String text);

    public Map<String, Object> submitDeck(String name, String deck);

    public Map<String, Object> registerDeck(String game, String name);

    public boolean removeDeck(String name);

    public Map<String, Object> getDeck(String name);

    public Map<String, Object> refreshDeck(String name, String deck, 
                                           String shuffle);

    public Map<String, Object> cardSearch(String type, String string);

    public String doCommand(String cmd);

    public BugDetailBean getBugDetail(String index);
}
