package net.deckserver.game.jaxb.actions;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

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
    @XmlElement
    protected List<String> command;

    public String getCounter() {
        return counter;
    }

    public void setCounter(String value) {
        this.counter = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        this.text = value;
    }

    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }
        return this.command;
    }

}
