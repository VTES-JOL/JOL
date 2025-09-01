package net.deckserver.game.jaxb.state;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "id",
        "cardid",
        "owner",
        "notation"
})
@XmlRootElement(name = "game-card")
@Data
public class GameCard {

    @XmlElement(required = true)
    protected String id;

    @XmlElement(required = true)
    protected String cardid;

    @XmlElement(required = true)
    protected String owner;

    @XmlElement(required = true)
    protected List<Notation> notation = new ArrayList<Notation>();

    @XmlAttribute(name = "type")
    protected String type;

    @Override
    public String toString() {
        return "GameCard{" +
                "id='" + id + '\'' +
                ", cardid='" + cardid + '\'' +
                ", owner='" + owner + '\'' +
                ", notation=" + notation +
                '}';
    }
}
