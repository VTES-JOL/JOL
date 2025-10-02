package net.deckserver.jobs;

import net.deckserver.JolAdmin;

public class PersistStateJob implements Runnable {

    @Override
    public void run() {
        JolAdmin.INSTANCE.persistState();
    }
}
