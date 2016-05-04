/*
 * MailUtil.java
 *
 * Created on January 13, 2005, 9:20 PM
 */

package deckserver.util;

import nbclient.model.GameAction;
import nbclient.vtesmodel.JolAdminFactory;
import nbclient.vtesmodel.JolGame;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 *
 * @author  gfinklan
 */
public class MailUtil {

    /** Creates a new instance of MailUtil */
    public MailUtil() {
    }

    private static Session mySession;

    public static Session getSession() {
        if (mySession != null) {
            Properties props = new Properties();
            props.put("mail.smtp.host", "mark.bch.net");
            mySession = Session.getInstance(props);
        }
        return mySession;
    }

    private static int rcount = 0;
    private static String[] reminders =
        new String[] { "It's now your turn", "It's your turn to act",
                       "You now have priority in this game",
                       "Your presence is requested at the table."
            /*
        "Hurry up, we're all waiting for you here",
        "Get a move on!",
        "Our blood is boiling"*/
            } ;

    private static String getReminder() {
        try {
            return reminders[rcount++];
        } finally {
            if (rcount == reminders.length)
                rcount = 0;
        }
    }

    public static String ping(WebParams wp, String email) {
        return ping(wp.getGame(), email);
    }

    public static String ping(String name, String email) {
        if (sendMsg(new String[] { email }, "Ping from " + name,
                    getReminder()))
            return "Ping sent to " + email;
        return "Ping failed";
    }

    private static boolean sendMsg(String[] email, String subj, String body) {
        try {
            Session session = getSession();
            Message msg = new MimeMessage(session);
            Address from = new InternetAddress("jol@deckserver.net");
            Address[] to = new Address[email.length];
            for (int i = 0; i < to.length; i++)
                to[i] = new InternetAddress(email[i]);
            Address replys = new InternetAddress("dsadmin@deckserver.net");
            msg.addFrom(new Address[] { from });
            msg.setReplyTo(new Address[] { replys });
            msg.setSubject(subj);
            msg.setRecipients(Message.RecipientType.TO, to);
            msg.setText(body);
            doSend(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

    public static void doSend(final Message msg) {
        Thread t = new Thread() {
            public void run() {
                try {
                    Transport.send(msg);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        };
        t.start();
    }
    
    public static void sendStartMsg(JolGame game) {
        String header = "Game " + game.getName() + " is starting.";
        String msg = "Good luck!";
        String[] players = game.getPlayers();
        String[] emails = new String[players.length];
        for (int i = 0; i < emails.length; i++)
           emails[i] = JolAdminFactory.INSTANCE.getEmail(players[i]);
        sendMsg(emails, header, msg);
    }

    public static void sendTurn(JolGame game) {
        String turn = game.getCurrentTurn();
        GameAction[] actions = game.getActions(turn);
        StringBuffer buf = new StringBuffer();
        String header = game.getName() + " - " + turn;
        buf.append(header + "\n");
        buf.append("\n");
        for (int i = 0; i < actions.length; i++) {
            buf.append(actions[i].getText());
            buf.append("\n");
        }
        String[] players = game.getPlayers();
        String[] emails = new String[players.length];
        for (int i = 0; i < emails.length; i++)
            if(JolAdminFactory.INSTANCE.receivesTurnSummaries(players[i]))
                emails[i] = JolAdminFactory.INSTANCE.getEmail(players[i]);
        sendMsg(emails, header, buf.toString());
    }

    public static void sendError(WebParams params, Exception e) {
        sendError(params,null,e);
    }

    public static void sendError(WebParams params, String s, Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        if(params != null) {
            pw.println("Request for " + params.getRequest().getRequestURI() +
                       " from " + params.getPlayer() + " with");
            pw.println(params.getRequest().getParameterMap());
        } else {
            pw.println("beta interface Request");
        }
        if(s != null) pw.println(s);
        pw.println("Caused an exception : " + e.getMessage());
        e.printStackTrace(pw);
        if (e.getCause() != null) {
            pw.println("Caused by : ");
            e.getCause().printStackTrace(pw);
        }
        sendMsg(new String[] { "george.finklang@gmail.com" },
                "Deckserver Server Exception", writer.getBuffer().toString());
    }
    
    public static void sendError(String s, Throwable e) {
        sendError(null,s,e);
    }
}
