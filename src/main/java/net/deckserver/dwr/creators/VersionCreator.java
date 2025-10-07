package net.deckserver.dwr.creators;

import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.VersionService;

public class VersionCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "checkVersion";
    }

    @Override
    public Object createData(PlayerModel model) {
        return VersionService.getVersion();
    }
}
