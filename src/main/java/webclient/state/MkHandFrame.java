/*
 * MkHandFrame.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package webclient.state;

import nbclient.model.state.SCard;
import nbclient.vtesmodel.JolGame;

import java.io.PrintWriter;

/**
 * @author Joe User
 */
public class MkHandFrame {

    JolGame game;
    String cardUrl = null;

    /**
     * Creates a new instance of MkState
     */
    public MkHandFrame(JolGame game) {
        this.game = game;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Throwable {
  /*      String name = args[0];
        JolGame game = nbclient.TestMain.mkGame();
        game.startGame();
        MkHandFrame generator = new MkHandFrame(game);
        PrintWriter out = new PrintWriter(new FileOutputStream(args[1]));
        generator.writeFrame(name,out); */
    }

    public void setCardUrl(String url) {
        cardUrl = url;
    }

    public void writeFrame(String player, PrintWriter out) {
        writeHeader(out);
        writePlayer(player, out);
        writeForm("/cgi-bin/live-submit.cgi", out);
        writeFooter(out);
    }

    public void writeHtml(String player, PrintWriter out) {
        writePlayer(player, out);
        writeForm("/cgi-bin/live-submit.cgi", out);
    }

    void writePlayer(String player, PrintWriter out) {
        out.println("<table>");
        out.println("<tr>");
        out.println("<td><ol>");
        SCard[] cards = game.getState().getPlayerLocation(player, JolGame.HAND).getCards();
        for (int i = 0; i < cards.length; i++) {
            out.print("<li>");
            writeCardName(cards[i], out);
            out.println("</li>");
        }
        out.println("</ol></td>");
        out.println("<td><ol>");
        cards = game.getState().getPlayerLocation(player, JolGame.INACTIVE_REGION).getCards();
        for (int i = 0; i < cards.length; i++) {
            out.print("<li>");
            writeCardName(cards[i], out);
            out.println("</li>");
        }
        out.println("</ol></td>");
        out.println("</tr>");
        out.println("</table>");

    }

    void writeCardName(SCard card, PrintWriter out) {
        if (cardUrl == null)
            out.print(card.getName());
        else out.print("<a href=" + cardUrl + card.getCardId() + ">" + card.getName() + "</a>");

    }

    void writeHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + game.getName() + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=#000000 text=#FF0000 link=#FF0000 alink=#FFFF00 vlink=#B03060>");
    }

    void writeForm(String url, PrintWriter out) {
        out.println("<hr>");
        out.println("<form action=" + url + " method=post>");
        out.println("<input name=command>");
        out.println("</form>");
    }

    void writeFooter(PrintWriter out) {
        out.println("</body></html>");
    }

}
