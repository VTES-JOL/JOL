package deckserver.client;

import java.util.Collections;
import java.util.LinkedList;

class WriterThread extends Thread {
    private LinkedList<String> writes = new LinkedList<>();

    public WriterThread() {
        setDaemon(true);
        this.start();
    }

    public synchronized void addWrite(String game) {
        writes.addLast(game);
    }

    private synchronized String pop() {
        if (writes.isEmpty()) return null;
        String ret = writes.getFirst();
        writes.removeAll(Collections.singleton(ret));
        return ret;
    }

    public void run() {
        while (true) {
            String game = pop();
            if (game == null) {
                try {
                    sleep(60000);
                } catch (InterruptedException e) {
                    continue;
                }
            } else {
                ((JolAdmin) JolAdmin.INSTANCE).getGameInfo(game).dowrite();
            }
        }
    }
}
