/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package webclient.state;

import nbclient.vtesmodel.JolAdminFactory;
import nbclient.vtesmodel.JolGame;

import java.io.PrintWriter;

/**
 *
 * @author  Joe User
 */
public class ShowGame {
    
    JolGame game;
    String cardUrl;
    
    public ShowGame(String game) {
        this.game = JolAdminFactory.INSTANCE.getGame(game);
    }
    
    public void setCardUrl(String url) {
        cardUrl = url;
    }
    
    public void generate(String player,String url, PrintWriter out) {
        String[] players = game.getPlayers();
        for(int i = 0; i < players.length; i++)
            if(players[i].equals(player)) {
                generatePlayer(player,url,out);
                return;
            }
        MkHandFrame hand = new MkHandFrame(game);
        hand.writeHeader(out);
        out.println("<table border=2>");
        out.println("<td>");
        MkMessages msgs = new MkMessages(game.getName());
        msgs.writeMessages(out,10);
        out.println("</td>");
        out.println("<td>");
        MkState state = new MkState(game);
        state.setCardUrl(cardUrl);
        state.writeState(out);
        out.println("</td>");
        out.println("</table>");
        hand.writeFooter(out);
    }
    
    private void generatePlayer(String player, String url, PrintWriter out) {
        MkHandFrame hand = new MkHandFrame(game);
        hand.setCardUrl(cardUrl);
        hand.writeHeader(out);
        out.println("<table border=2>");
        out.println("<tr><td>");
        hand.writePlayer(player,out);
        hand.writeForm(url, out);
        out.println("</td>");
        out.println("<td>");
        MkMessages msgs = new MkMessages(game.getName());
        msgs.writeMessages(out,10);
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr><td colspan=2>");
        MkState state = new MkState(game);
        state.setCardUrl(cardUrl);
        state.writeState(out);
        out.println("</td>");
        hand.writeFooter(out);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Throwable {
        //  needs to be updated for GameFactory
     /*  String dir = args[0];
        String gamename = args[1];
        String player = args[2];
        String filename = dir + "/" + gamename + ".xml";
        InputStream in = new FileInputStream(filename);
        GameState state = GameState.createGraph(in);
        in.close();
        GameActions gameActions = new GameActions();
        gameActions.setCounter("1");
        ActionImpl actions = new ActionImpl(gameActions);
        JolGame game = new JolGameImpl(new GameImpl(state), actions);
        ShowGame generator = new ShowGame(game);
        generator.generate(player, "/cgi-bin/live-submit.cgi", new PrintWriter(System.out)); */
    }
    
}
