package net.deckserver.jobs;

import net.deckserver.JolAdmin;

public class PublicGamesBuilderJob implements Runnable {

    @Override
    public void run() {
        JolAdmin.INSTANCE.buildPublicGames();
    }
}
