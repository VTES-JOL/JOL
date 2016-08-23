package net.deckserver.jol.game.jaxb.state;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the net.deckserver.jol.game.state package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.deckserver.jol.game.state
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GameState }
     */
    public GameState createGameState() {
        return new GameState();
    }

    /**
     * Create an instance of {@link Player }
     */
    public Player createPlayer() {
        return new Player();
    }

    /**
     * Create an instance of {@link Region }
     */
    public Region createRegion() {
        return new Region();
    }

    /**
     * Create an instance of {@link Notation }
     */
    public Notation createNotation() {
        return new Notation();
    }

    /**
     * Create an instance of {@link GameCard }
     */
    public GameCard createGameCard() {
        return new GameCard();
    }

}
