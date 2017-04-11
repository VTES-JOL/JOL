package net.deckserver.game.jaxb.actions;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "sequence",
        "name",
        "counter",
        "label",
        "action"
})
@XmlRootElement(name = "actions")
public class Turn {

    @XmlElement(required = true)
    protected String sequence;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String counter;
    @XmlElement(required = true)
    protected String label;
    protected List<Action> action;

    /**
     * Gets the value of the sequence property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSequence(String value) {
        this.sequence = value;
    }

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
     * Gets the value of the label property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the action property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the action property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAction().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Action }
     */
    public List<Action> getAction() {
        if (action == null) {
            action = new ArrayList<Action>();
        }
        return this.action;
    }

}
