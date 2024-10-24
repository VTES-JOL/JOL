package net.deckserver.jobs;

import net.deckserver.dwr.model.JolAdmin;

public class CleanupPlayersJob implements Runnable{
    @Override
    public void run() {
        JolAdmin.getInstance().cleanupInactivePlayers();
    }
}
