package net.deckserver.servlet;

import net.deckserver.dwr.model.JolAdmin;

public class ChatPersistenceJob implements Runnable {

    @Override
    public void run() {
        JolAdmin.getInstance().persistChats();
    }
}
