package net.deckserver.servlet;

import net.deckserver.dwr.bean.AdminBean;

public class ChatPersistenceJob implements Runnable {

    private final AdminBean INSTANCE;

    ChatPersistenceJob(AdminBean instance) {
        this.INSTANCE = instance;
    }

    @Override
    public void run() {
        INSTANCE.persistChats();
    }
}
