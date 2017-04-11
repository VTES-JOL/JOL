package net.deckserver.game.jaxb.state;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "notation",
        "gameCard"
})
@XmlRootElement(name = "region")
public class Region {

    @XmlElement(required = true)
    protected String name;
    protected List<Notation> notation;
    @XmlElement(name = "game-card")
    protected List<GameCard> gameCard;

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
     * Gets the value of the notation property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notation property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotation().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Notation }
     */
    public List<Notation> getNotation() {
        if (notation == null) {
            notation = new ArrayList<Notation>();
        }
        return this.notation;
    }

    /**
     * Gets the value of the gameCard property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gameCard property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGameCard().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GameCard }
     */
    public List<GameCard> getGameCard() {
        if (gameCard == null) {
            gameCard = new ArrayList<GameCard>();
        }
        return this.gameCard;
    }

    @Override
    public String toString() {
        return "Region{" +
                "name='" + name + '\'' +
                ", notation=" + notation +
                ", gameCard=" + gameCard +
                '}';
    }
}
