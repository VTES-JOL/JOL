package net.deckserver.servlet;

import net.deckserver.dwr.bean.AdminBean;

public class CardPersistenceJob implements Runnable {

    private final AdminBean INSTANCE;

    CardPersistenceJob(AdminBean instance) {
        this.INSTANCE = instance;
    }

    @Override
    public void run() {
        INSTANCE.persistChats();
    }
}
