package net.deckserver.game.jaxb.state;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "id",
        "cardid",
        "notation"
})
@XmlRootElement(name = "game-card")
public class GameCard {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String cardid;
    protected List<Notation> notation;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the cardid property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCardid() {
        return cardid;
    }

    /**
     * Sets the value of the cardid property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCardid(String value) {
        this.cardid = value;
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

    @Override
    public String toString() {
        return "GameCard{" +
                "id='" + id + '\'' +
                ", cardid='" + cardid + '\'' +
                ", notation=" + notation +
                '}';
    }
}
