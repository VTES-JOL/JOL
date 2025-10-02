package net.deckserver.dwr.creators;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

public class VersionCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "checkVersion";
    }

    @Override
    public Object createData(PlayerModel model) {
        return JolAdmin.INSTANCE.getVersion();
    }
}
