package net.deckserver.dwr.creators;

import net.deckserver.dwr.model.PlayerModel;

interface ViewCreator {

    String getFunction();

    Object createData(PlayerModel model);

}
