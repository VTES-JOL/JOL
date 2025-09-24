/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package net.deckserver.game.storage.turn;

import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.interfaces.turn.TurnRecorder;
import net.deckserver.game.jaxb.actions.Action;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.actions.Turn;

import java.util.Arrays;
import java.util.List;

/**
 * @author administrator
 */
public class StoreTurnRecorder implements TurnRecorder {

    public GameActions actions;

    /**
     * Creates a new instance of StoreCard
     */
    public StoreTurnRecorder(GameActions actions) {
        this.actions = actions;
    }

    public void addCommand(String turn, String text, String[] command) {
        addAction(turn, text, command);
    }

    public void addMessage(String turn, String text) {
        addAction(turn, text, null);
    }

    public void addTurn(String meth, String label) {
        Turn t = new Turn();
        String cur = actions.getCounter();
        t.setSequence(cur);
        actions.setCounter((Integer.parseInt(cur) + 1) + "");
        t.setCounter("1");
        t.setName(meth);
        t.setLabel(label);
        actions.getTurn().add(t);
    }

    public GameAction[] getActions(String turn) {
        Turn t = getTurn(turn);
        List<Action> acts = t.getAction();
        GameAction[] ret = new GameAction[acts.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new GameActionImpl(acts.get(i), i);
        }
        return ret;
    }

    public String getMethTurn(String label) {
        Turn t = getTurn(label);
        return t.getName();
    }

    public List<String> getTurns() {
        List<Turn> turns = actions.getTurn();
        return turns.stream()
                .map(Turn::getLabel)
                .toList();
    }

    public int getCounter() {
        return Integer.parseInt(actions.getGameCounter());
    }

    private Turn getTurn(String label) {
        List<Turn> turns = actions.getTurn();
        return turns.stream()
                .filter(turn -> turn.getLabel().equals(label))
                .findFirst()
                .orElse(null);
    }

    private synchronized void addAction(String turn, String text, String[] command) {
        Turn t = getTurn(turn);
        Action act = new Action();
        act.setText(text);
        if (command != null && command.length > 0) {
            act.getCommand().clear();
            act.getCommand().addAll(Arrays.asList(command));
        }
        String cur = t.getCounter();
        act.setCounter(cur);
        t.setCounter((Integer.parseInt(cur) + 1) + "");
        actions.setGameCounter((getCounter() + 1) + "");
        t.getAction().add(act);
    }

    static class GameActionImpl implements GameAction {

        Action act;
        int num;

        public GameActionImpl(Action act, int num) {
            this.num = num;
            this.act = act;
        }

        public String[] command() {
            return act.getCommand().toArray(new String[act.getCommand().size()]);
        }

        public String getText() {
            return act.getText();
        }

        public boolean isCommand() {
            return act.getCommand() != null && !act.getCommand().isEmpty();
        }

        public String toString() {
            return getText();
        }

    }

}
