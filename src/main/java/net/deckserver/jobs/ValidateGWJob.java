package net.deckserver.jobs;

import net.deckserver.JolAdmin;

public class ValidateGWJob implements Runnable {
    @Override
    public void run() {
        JolAdmin.INSTANCE.validateGW();
    }
}
