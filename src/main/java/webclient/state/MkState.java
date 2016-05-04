/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package webclient.state;

import nbclient.model.state.SCard;
import nbclient.vtesmodel.JolGame;

import java.io.PrintWriter;

/**
 *
 * @author  Joe User
 */
public class MkState {
    
    JolGame game;
    String cardUrl = null;
    
    /** Creates a new instance of MkState */
    public MkState(JolGame game) {
        this.game = game;
    }
    
    public void setCardUrl(String url) {
        cardUrl = url;
    }
    
    public void writeStatePage(PrintWriter out) {
        writeHeader(out);
        writeState(out);
        writeFooter(out);
    }
    
    public void writeState(PrintWriter out) {
        writeEdge(out);
        out.println("<table border=2>");
        String[] players = game.getPlayers();
        for(int i = 0; i < players.length; i++) {
            out.println("<td>");
            writePlayer(players[i],out);
            out.println("</td>");
        }
        out.println("</table>");
    }
    
    private void writePlayer(String player, PrintWriter out) {
        out.println("Methuselah: " + player + ".  Pool: " + game.getPool(player));
        out.println("<br>");
        out.print("Library:"+getSize(player,JolGame.LIBRARY));
        out.print("  Crypt:"+getSize(player,JolGame.CRYPT));
        out.println("   Hand:"+getSize(player,JolGame.HAND));
        out.println("<br>");
        out.println("READY:");
        out.println("<ol>");
        SCard[] cards = game.getState().getPlayerLocation(player,JolGame.READY_REGION).getCards();
        for(int i = 0; i < cards.length; i++)
            writeCard(cards[i],out,true);
        out.println("</ol>");
        out.println("TORPOR:");
        out.println("<ol>");
        cards = game.getState().getPlayerLocation(player,JolGame.TORPOR).getCards();
        for(int i = 0; i < cards.length; i++)
            writeCard(cards[i],out,true);
        out.println("</ol>");
        out.println("INACTIVE:");
        out.println("<ol>");
        cards = game.getState().getPlayerLocation(player,JolGame.INACTIVE_REGION).getCards();
        for(int i = 0; i < cards.length; i++)
            writeCard(cards[i],out,false);
        out.println("</ol>");
        out.println("ASHHEAP:");
        cards = game.getState().getPlayerLocation(player,JolGame.ASHHEAP).getCards();
        for(int i = 0; i < cards.length; i++) {
            if(i > 0) out.println(", ");
            writeCardName(cards[i],out);
        }
        out.println("<br>");
        
    }
    
    private void writeCard(SCard card, PrintWriter out, boolean visible) {
        writeCard(card,out,visible,false);
    }
    
    private void writeCard(SCard card, PrintWriter out, boolean visible, boolean contained) {
        out.println("<li>");
        if(visible) writeCardName(card,out);
        else out.print("XXXXX");
        int counters = game.getCounters(card.getId());
        if(counters > 0)
            out.print(", Blood: " + game.getCounters(card.getId()));
        if(!contained && game.isTapped(card.getId())) {
            out.print(", TAPPED");
        }
        out.println();
        SCard[] cards = card.getCards();
        if(cards != null && cards.length > 0) {
            out.println("<ol>");
            for(int i = 0; i < cards.length; i++)
                writeCard(cards[i],out,true,true);
            out.println("</ol>");
        }
        out.println("</li>");
    }
    
    void writeCardName(SCard card, PrintWriter out) {
        if(cardUrl == null)
            out.print(card.getName());
        else out.print("<a href=" + cardUrl + card.getCardId() + ">" + card.getName() + "</a>");
    }
    
    private int getSize(String player, String region) {
        return game.getState().getPlayerLocation(player,region).getCards().length;
    }
    
    private void writeHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + game.getName() + " Game State</title>");
        out.println("</head>");
        out.println("<body bgcolor=#000000 text=#FF0000 link=#FF0000 alink=#FFFF00 vlink=#B03060>");
    }
    
    private void writeEdge(PrintWriter out) {
        out.println("Edge : " + game.getEdge());
    }
    
    private void writeFooter(PrintWriter out) {
        out.println("<hr><br><br>");
        out.println("Last Modified : ");// + System.get)
        out.println("</body></html>");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Throwable {
      /*  String name = args[0];
        JolGame game = nbclient.TestMain.mkGame();
        game.startGame();
        MkState generator = new MkState(game);
        PrintWriter out = new PrintWriter(new FileOutputStream(args[1]));
        generator.writeState(out); */
    }
    
}
