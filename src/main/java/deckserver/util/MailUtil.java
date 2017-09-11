/*
 * MailUtil.java
 *
 * Created on January 13, 2005, 9:20 PM
 */

package deckserver.util;

import deckserver.client.JolAdmin;
import deckserver.client.JolGame;
import deckserver.game.turn.GameAction;
import org.slf4j.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class MailUtil {

    private static final Logger logger = getLogger(MailUtil.class);

    private static final String PING_TEXT = "You now have priority in this game";
    private static final String EMAIL_FROM = System.getProperty("jol.email");
    private static Session mySession;

    private static Session getSession() {
        if (mySession != null) {
            Properties properties = System.getProperties();
            String host = properties.getProperty("mail.smtp.host");
            String port = properties.getProperty("mail.smtp.port");
            logger.debug("Connecting to mail server {}:{}", host, port);
            mySession = Session.getDefaultInstance(properties);
        }
        return mySession;
    }

    public static String ping(String name, String email) {
        if (sendMsg("Ping from " + name, PING_TEXT, Collections.singletonList(email)))
            return "Ping sent to " + email;
        return "Ping failed";
    }

    private static boolean sendMsg(String subj, String body, List<String> emails) {
        try {
            Session session = getSession();
            Message msg = new MimeMessage(session);
            Address from = new InternetAddress(EMAIL_FROM, "Deckserver - V:TES Online");
            List<Address> to = new ArrayList<>(emails.size());
            for (String address : emails) {
                to.add(new InternetAddress(address));
            }
            msg.addFrom(new Address[]{from});
            msg.setSubject(subj);
            msg.setRecipients(Message.RecipientType.TO, to.toArray(new Address[to.size()]));
            msg.setText(body);
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            logger.error("Error sending emails", e);
            return false;
        }
    }

    public static void sendStartMsg(JolGame game) {
        String header = "Game " + game.getName() + " is starting.";
        String msg = "Good luck!";
        String[] players = game.getPlayers();
        List<String> emails = Stream.of(players).map(JolAdmin.getInstance()::getEmail).collect(Collectors.toList());
        sendMsg(header, msg, emails);
    }

    /*public static void sendTurn(JolGame game) {
        String turn = game.getCurrentTurn();
        GameAction[] actions = game.getActions(turn);
        StringBuilder buf = new StringBuilder();
        String header = game.getName() + " - " + turn;
        buf.append(header).append("\n");
        for (GameAction action : actions) {
            buf.append(action.getText());
            buf.append("\n");
        }
        String[] players = game.getPlayers();
        List<String> emails = Stream.of(players).map(JolAdmin.getInstance()::getEmail).collect(Collectors.toList());
        sendMsg(header, buf.toString(), emails);
    }*/
}
