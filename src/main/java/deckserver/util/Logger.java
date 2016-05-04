package deckserver.util;

import java.util.Collection;
import java.util.HashSet;

public class Logger {

    private static Collection<String> logs = new HashSet<String>();
    String name;

    public Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(Class<?> c) {
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        return new Logger(name);
    }

    public static void activateLog(String name) {
        logs.add(name);
    }

    public static void deactivateLog(String name) {
        logs.remove(name);
    }

    public void log(String msg) {
        //if(logs.contains(name))
        System.err.println("Log " + name + ":" + msg);
    }
}
