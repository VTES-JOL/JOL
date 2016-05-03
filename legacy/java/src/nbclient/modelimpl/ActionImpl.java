/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import nbclient.model.gen.*;
import nbclient.model.*;

/**
 *
 * @author  administrator
 */
public class ActionImpl implements Recorder {
    
    public GameActions actions;

    /** Creates a new instance of CardImpl */
    public ActionImpl(GameActions actions) {
        this.actions = actions;
    }
    
    public void addCommand(String text, String[] command) {
        addAction(text, command);
    }
    
    private synchronized void addAction(String text, String[] command) {
        Action act = new Action();
        act.setText(text);
        if(command != null && command.length > 0) act.setCommand(command);
        String cur = actions.getCounter();
        act.setCounter(cur);
        actions.setCounter((Integer.parseInt(cur) + 1) + "");
        actions.addAction(act);
    }
    
    public void addMessage(String text) {
        addAction(text,null);
    }
    
    public GameAction[] getAllActions() {
        Action[] acts = actions.getAction();
        GameAction[] ret = new GameAction[acts.length];
        for(int i = 0; i < acts.length; i++)
            ret[i] = new GameActionImpl(acts[i],i);
        return ret;
    }
    
    public GameAction[] getRecentActions(int count) {
        GameAction[] act = getAllActions();
        if(count >= act.length) return act;
        GameAction[] ret = new GameAction[count];
        for(int i = 0; i < ret.length; i++)
            ret[i] = act[act.length  - count + i];
        return ret;
    }
    
    public GameAction getAction(int actionNumber) {
        return new GameActionImpl(actions.getAction(actionNumber), actionNumber);
    }
    
    public int getNumActions() {
        return actions.getAction().length;
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
            return !act.getText().startsWith("[");//act.getCommand() != null && act.getCommand().length > 0;
        }
        
        public String toString() {
            return getText();
        }
        
    }
    
}
