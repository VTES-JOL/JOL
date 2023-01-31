package net.deckserver.jobs;

import net.deckserver.dwr.model.JolAdmin;

public class CleanupGamesJob implements Runnable {

    @Override
    public void run() {
        JolAdmin.getInstance().closeGames();
    }
}
