/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import nbclient.model.GameAction;
import nbclient.model.TurnRecorder;
import nbclient.model.gen.v2.Action;
import nbclient.model.gen.v2.GameActions;
import nbclient.model.gen.v2.Turn;

/**
 *
 * @author  administrator
 */
public class TurnImpl implements TurnRecorder {
    
    public GameActions actions;

    /** Creates a new instance of CardImpl */
    public TurnImpl(GameActions actions) {
        this.actions = actions;
    }
    
    private Turn getTurn(String label) {
        Turn[] turns = actions.getTurn();
        int i = turns.length;
        while(i-- > 0) 
            if(turns[i].getLabel().equals(label)) return turns[i];
        return null;
    }
    
    private synchronized void addAction(String turn, String text, String[] command) {
        Turn t = getTurn(turn);
        Action act = new Action();
        act.setText(text);
        if(command != null && command.length > 0) act.setCommand(command);
        String cur = t.getCounter();
        act.setCounter(cur);
        t.setCounter((Integer.parseInt(cur) + 1) + "");
        actions.setGameCounter((getCounter() + 1) + "");
        t.addAction(act);
    }
    
    public void addCommand(String turn, String text, String[] command) {
        addAction(turn,text,command);
    }
    
    public void addMessage(String turn, String text) {
        addAction(turn,text,null);
    }
    
    public void addTurn(String meth, String label) {
        Turn t = new Turn();
        String cur = actions.getCounter();
        t.setSequence(cur);
        actions.setCounter((Integer.parseInt(cur) + 1) + "");
        t.setCounter("1");
        t.setName(meth);
        t.setLabel(label);
        actions.addTurn(t);
    }
    
    public GameAction[] getActions(String turn) {
        Turn t = getTurn(turn);
        Action[] acts = t.getAction();
        GameAction[] ret = new GameAction[acts.length];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = new GameActionImpl(acts[i],i);
        }
        return ret;
    }
    
    public String getMethTurn(String label) {
        Turn t = getTurn(label);
        return t.getName();
    }
    
    public String[] getTurns() {
        Turn[] turns = actions.getTurn();
        String[] ret = new String[turns.length];
        for(int i = 0; i < ret.length; i++)
            ret[i] = turns[i].getLabel();
        return ret;
    }
    
    public int getCounter() {
        return Integer.parseInt(actions.getGameCounter());
    }
    
    static class GameActionImpl implements GameAction {
        
        Action act;
        int num;
        
        public GameActionImpl(Action act, int num) {
            this.num = num;
            this.act = act;
        }
        
        public String[] command() {
            return act.getCommand();
        }
        
        public int getSequence() {
            return num;
        }
        
        public String getText() {
            return act.getText();
        }
        
        public boolean isCommand() {
            return act.getCommand() != null && act.getCommand().length > 0;
        }
        
        public String toString() {
            return getText();
        }
        
    }
    
}
