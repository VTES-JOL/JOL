package dsclient.modelimpl;

import java.io.*;

import javaclient.gen.GameState;
import nbclient.model.*;
import nbclient.model.gen.v2.GameActions;
import nbclient.modelimpl.*;
//import nbclient.vtesmodel.JolGameImpl;

public final class ModelTest {

	public static void main(String[] argv) {
	    GameState state;
		GameActions actions;
		Game game = new DsGame();
		TurnRecorder rec = new ActionHistory();
		Game wgame;
		TurnRecorder wrec;
		try {
	    	String gameDir = "c:/testdir";
	        File file = new File(gameDir,"game.xml");
	        InputStream in = new FileInputStream(file);
	        state = GameState.createGraph(in);
	        in.close();
	        file = new File(gameDir,"actions.xml");
	        in = new FileInputStream(file);
	        actions = GameActions.createGraph(in);
	        in.close();
	        ModelLoader.createModel(game,new GameImpl(state));
	        ModelLoader.createRecorder(rec, new TurnImpl(actions));
	       // JolGameImpl jgame = new JolGameImpl(game, rec);
	        state = GameState.createGraph();
	        actions = GameActions.createGraph();
	        actions.setCounter("1");
            actions.setGameCounter("1");
	        wgame = new GameImpl(state);
	        wrec = new TurnImpl(actions);
	        ModelLoader.createModel(wgame,game);
	        ModelLoader.createRecorder(wrec,rec);
	        file = new File(gameDir,"newgame.xml");
	        OutputStream out = new FileOutputStream(file);
	        state.write(out);
	        out.close();
	        file = new File(gameDir,"newactions.xml");
	        out = new FileOutputStream(file);
	        actions.write(out);
	        out.close();
		} catch (IOException ie) {
	        ie.printStackTrace(System.err);
	    }
	}
}
