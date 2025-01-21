package net.deckserver.game.jaxb.state;

import lombok.Data;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "value"
})
@XmlRootElement(name = "notation")
@Data
public class Notation {

    @XmlElement(required = true)
    protected String name;

    @XmlElement(required = true)
    protected String value;

    @Override
    public String toString() {
        return "Notation{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
