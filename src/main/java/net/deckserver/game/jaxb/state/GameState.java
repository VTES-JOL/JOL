package net.deckserver.game.jaxb.state;

import javax.xml.bind.annotation.*;
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
public class GameState {

    @XmlElement(required = true)
    protected String name;
    @XmlElement
    protected String counter;
    @XmlElement
    protected List<String> player;
    protected List<Region> region;
    protected List<Notation> notation;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public List<String> getPlayer() {
        if (player == null) {
            player = new ArrayList<>();
        }
        return this.player;
    }

    public List<Region> getRegion() {
        if (region == null) {
            region = new ArrayList<Region>();
        }
        return this.region;
    }

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
                ", counter=" + counter +
                ", player=" + player +
                ", region=" + region +
                ", notation=" + notation +
                '}';
    }
}
