package net.deckserver.game.jaxb.actions;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public GameActions createGameActions() {
        return new GameActions();
    }

    public Turn createTurn() {
        return new Turn();
    }

    public Action createAction() {
        return new Action();
    }

}
