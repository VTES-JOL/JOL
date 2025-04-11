package net.deckserver.game.ui.turn;

import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.interfaces.turn.TurnRecorder;

import java.util.*;

public class DsTurnRecorder implements TurnRecorder {

    Map<String, Turn> turns = new HashMap<>();
    Collection<String> names = new Vector<>();
    private int counter = 1;

    public void addTurn(String meth, String label) {
        turns.put(label, new Turn(meth, label));
        names.add(label);
        counter++;
    }

    public String[] getTurns() {
        return names.toArray(new String[0]);
    }

    public String getMethTurn(String label) {
        return getTurn(label).m;
    }

    public void addCommand(String turn, String text, String[] command) {
        new Act(text, command, getTurn(turn));
    }

    public void addMessage(String turn, String text) {
        new Act(text, getTurn(turn));
    }

    Turn getTurn(String turn) {
        return turns.get(turn);
    }

    public GameAction[] getActions(String turn) {
        Turn t = getTurn(turn);
        return t.c.toArray(new GameAction[0]);
    }

    public int getCounter() {
        return counter;
    }

    static class Turn {
        //private final String l;
        final LinkedList<Act> c = new LinkedList<>();
        private final String m;

        Turn(String m, String l) {
            this.m = m;
            //	this.l = l;
        }
    }

    static class Act implements GameAction {

        private final String text;
        private final String[] command;

        public Act(String text, String[] command, Turn turn) {
            this.text = text;
            this.command = command;
            turn.c.addLast(this);
        }

        public Act(String text, Turn turn) {
            this(text, null, turn);
        }

        public boolean isCommand() {
            return command == null;
        }

        public String getText() {
            return text;
        }

        public String[] command() {
            return command;
        }

    }
}
