package net.deckserver.game.jaxb.state;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "value"
})
@XmlRootElement(name = "notation")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "value"})
public class Notation {

    @XmlElement(required = true)
    protected String name;

    @XmlElement(required = true)
    protected String value;

    public Notation(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Notation{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
