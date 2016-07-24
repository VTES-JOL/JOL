package net.deckserver.jol.game.state;

import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Created by shannon on 25/07/2016.
 */
public class CardDbTest {

    @Test
    public void Connect() throws Exception {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:file:src/test/resources/db/jol;ifexists=true", "SA", "");
        assertNotNull(c);
        Statement s = c.createStatement();
        s.execute("SELECT count(*) FROM JOL.CARDS");
        ResultSet rs = s.getResultSet();
        rs.next();
        assertEquals(0, rs.getInt(1));
    }

    @Test
    public void ReadCardList() throws Exception {
        File file = new File("src/test/resources/cards/cardlist.txt");
        assertTrue(file.exists());
    }
}
