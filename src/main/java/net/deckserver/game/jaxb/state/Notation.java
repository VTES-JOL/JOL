package net.deckserver.game.jaxb.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.*;


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
