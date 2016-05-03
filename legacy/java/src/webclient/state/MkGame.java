/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package webclient.state;

/**
 *
 * @author  Joe User
 */
public class MkGame {
    
    /**
     * @param args the command line arguments
     
    public static void main(String[] args) throws Throwable {
        String name = args[1];
        String dir = args[0];
        new JolAdmin(dir);
        JolAdminFactory.INSTANCE.mkGame(name);
        JolGame game = JolAdminFactory.INSTANCE.getGame(name);
        int i = 2;
        while(i < args.length) {
            String player = args[i++];
            String deck = args[i++];
            game.addPlayer(player,dir + "/" + deck);
        }
        game.startGame();
        JolAdminFactory.INSTANCE.saveGame(game);
    /*    MkState generator = new MkState(game);
        PrintWriter out = new PrintWriter(new FileOutputStream(dir + "/" + name + ".html"));
        generator.writeState(out); 
    } */
    
}
