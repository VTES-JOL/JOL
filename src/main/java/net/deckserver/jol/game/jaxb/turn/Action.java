package net.deckserver.jol.game.jaxb.turn;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "counter",
        "text",
        "command"
})
@XmlRootElement(name = "action")
public class Action {

    @XmlElement(required = true)
    protected String counter;
    @XmlElement(required = true)
    protected String text;
    protected List<Command> command;

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
     * Gets the value of the text property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the command property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the command property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommand().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Command }
     */
    public List<Command> getCommand() {
        if (command == null) {
            command = new ArrayList<Command>();
        }
        return this.command;
    }

}
