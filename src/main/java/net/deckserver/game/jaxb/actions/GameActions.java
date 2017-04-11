package net.deckserver.game.jaxb.actions;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "counter",
        "gameCounter",
        "turn"
})
@XmlRootElement(name = "game-actions")
public class GameActions {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String counter;
    @XmlElement(name = "game-counter", required = true)
    protected String gameCounter;
    protected List<Turn> turn;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the counter property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCounter() {
        return counter;
    }

    /**
     * Sets the value of the counter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCounter(String value) {
        this.counter = value;
    }

    /**
     * Gets the value of the gameCounter property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getGameCounter() {
        return gameCounter;
    }

    /**
     * Sets the value of the gameCounter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGameCounter(String value) {
        this.gameCounter = value;
    }

    /**
     * Gets the value of the actions property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actions property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTurn().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Turn }
     */
    public List<Turn> getTurn() {
        if (turn == null) {
            turn = new ArrayList<Turn>();
        }
        return this.turn;
    }

}
