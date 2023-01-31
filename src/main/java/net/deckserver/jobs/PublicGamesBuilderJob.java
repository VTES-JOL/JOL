package net.deckserver.jobs;

import net.deckserver.dwr.model.JolAdmin;

public class PublicGamesBuilderJob implements Runnable{

    @Override
    public void run() {
        JolAdmin.getInstance().buildPublicGames();
    }
}
