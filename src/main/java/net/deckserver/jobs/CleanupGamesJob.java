package net.deckserver.jobs;

import net.deckserver.JolAdmin;

public class CleanupGamesJob implements Runnable {

    @Override
    public void run() {
        JolAdmin.INSTANCE.cleanupGames();
    }
}
