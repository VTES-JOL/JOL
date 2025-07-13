package net.deckserver.game.jaxb.state;

import lombok.Data;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "counter",
        "player",
        "region",
        "notation"
})
@XmlRootElement(name = "game-state")
@Data
public class GameState {

    @XmlElement
    protected String name;

    @XmlElement
    protected String counter;

    @XmlElement
    protected List<String> player = new ArrayList<>();

    @XmlElement
    protected List<Region> region = new ArrayList<>();

    @XmlElement
    protected List<Notation> notation = new ArrayList<>();
}
