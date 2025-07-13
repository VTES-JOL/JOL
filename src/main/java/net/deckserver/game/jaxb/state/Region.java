package net.deckserver.game.jaxb.state;

import lombok.Data;

import jakarta.xml.bind.annotation.*;
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
@Data
public class Region {

    @XmlElement(required = true)
    protected String name;

    @XmlElement
    protected List<Notation> notation = new ArrayList<>();

    @XmlElement(name = "game-card")
    protected List<GameCard> gameCard = new ArrayList<>();

    @Override
    public String toString() {
        return "Region{" +
                "name='" + name + '\'' +
                ", notation=" + notation +
                ", gameCard=" + gameCard +
                '}';
    }
}
