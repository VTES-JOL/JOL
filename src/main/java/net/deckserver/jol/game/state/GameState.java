
package net.deckserver.jol.game.state;

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
    "player",
    "region",
    "notation"
})
@XmlRootElement(name = "game-state")
public class GameState {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String counter;
    protected List<Player> player;
    protected List<Region> region;
    protected List<Notation> notation;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the counter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCounter() {
        return counter;
    }

    /**
     * Sets the value of the counter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCounter(String value) {
        this.counter = value;
    }

    /**
     * Gets the value of the player property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the player property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Player }
     * 
     * 
     */
    public List<Player> getPlayer() {
        if (player == null) {
            player = new ArrayList<Player>();
        }
        return this.player;
    }

    /**
     * Gets the value of the region property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the region property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Region }
     * 
     * 
     */
    public List<Region> getRegion() {
        if (region == null) {
            region = new ArrayList<Region>();
        }
        return this.region;
    }

    /**
     * Gets the value of the notation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Notation }
     * 
     * 
     */
    public List<Notation> getNotation() {
        if (notation == null) {
            notation = new ArrayList<Notation>();
        }
        return this.notation;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "name='" + name + '\'' +
                ", counter='" + counter + '\'' +
                ", player=" + player +
                ", region=" + region +
                ", notation=" + notation +
                '}';
    }
}
