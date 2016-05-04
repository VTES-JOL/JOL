/*
 * LoginServlet.java
 *
 * Created on March 25, 2004, 2:32 PM
 */

package deckserver.login;

import deckserver.util.WebParams;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Joe User
 */
public class SessionStorage {

    private static final String sessionFileName = "sessions.properties";
    static Session oldest = null;
    static Session newest = null;
    Map<String, Session> sessions = null;
    private File sessionFile = null;

    public SessionStorage() {
    }

    Map<String, Session> getSessionMap() {
        return sessions;
    }

    private Map<String, Session> getSessionMap(WebParams p) {
        if (sessions == null) {
            synchronized (this) {
                if (sessions == null) {
                    if (sessionFile == null)
                        sessionFile = new File(p.getDataDir(), sessionFileName);
                    readSessionMap();
                }
            }
        }
        return sessions;
    }

    private void readSessionMap() {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(sessionFile);
            props.load(in);
            sessions = new HashMap<String, Session>();
            oldest = null;
            newest = null;
            for (Enumeration<?> i = props.keys(); i.hasMoreElements(); ) {
                String id = (String) i.nextElement();
                Session s = new Session(id, props.getProperty(id));
                sessions.put(id, s);
            }
        } catch (IOException ie) {
            ie.printStackTrace(System.err);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ie2) {
                ie2.printStackTrace(System.err);
            }
        }
    }

    /*
    private void writeSessionMap() {
        Properties props = new Properties();
        for(Session s = oldest; s != null ; s = s.getNext())
            props.setProperty(s.getId(),s.getPlayer());
        OutputStream out = null;
        try {
            out = new FileOutputStream(sessionFile);
            props.store(out,"JOL3 Session storage");
        } catch (IOException ie) {
            ie.printStackTrace(System.err);
        } finally {
            try {
                if(out != null) out.close();
            } catch (IOException ie2) {
                ie2.printStackTrace(System.err);
            }
        }
    }
    */
    // returns the player to whom this session belongs
    public String lookupSession(WebParams p) {
        HttpSession sess = p.getRequest().getSession();
        if (sess == null) return null;
        String id = sess.getId();
        Session s = getSessionMap(p).get(id);
        if (s == null) return null;
        s.resetTime();
        return s.getPlayer();
    }

    public void createSession(WebParams p) {
        HttpSession sess = p.getRequest().getSession(true);
        String id = sess.getId();
        sessions.put(id, new Session(id, p.getPlayer()));
    }

    class Session {
        final String session;
        final String player;
        long timestamp;
        Session prev = null, next = null;

        Session(String id, String player) {
            session = id;
            this.player = player;
            resetTime();
        }

        public Session getPrevious() {
            return prev;
        }

        public void setPrevious(Session prev) {
            this.prev = prev;
        }

        public Session getNext() {
            return next;
        }

        public void setNext(Session next) {
            this.next = next;
        }

        public String getId() {
            return session;
        }

        public void resetTime() {
            timestamp = (new Date()).getTime();
            if (this == newest) return;
            if (oldest == this) oldest = next;
            if (prev != null) prev.setNext(next);
            if (next != null) next.setPrevious(prev);
            if (oldest == null) oldest = this;
            if (newest != null) newest.setNext(this);
            prev = newest;
            next = null;
            newest = this;
            doPurge(timestamp);
        }

        private void doPurge(long ctime) {
            long otime = oldest.getTime();
            if (otime + 1000000 < ctime) {
                Session n = oldest.getNext();
                n.setPrevious(null);
                getSessionMap().remove(oldest.getId());
                oldest = n;
            }
        }

        public long getTime() {
            return timestamp;
        }

        public String getPlayer() {
            return player;
        }
    }
}
