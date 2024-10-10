package net.deckserver.jobs;

import net.deckserver.dwr.model.JolAdmin;

public class ValidateGWJob implements Runnable {
    @Override
    public void run() {
        JolAdmin.getInstance().validateGW();
    }
}
