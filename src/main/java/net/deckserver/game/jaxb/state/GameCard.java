package net.deckserver.game.jaxb.state;

import javax.xml.bind.annotation.*;
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
public class GameCard {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String cardid;
    @XmlElement(required = true)
    protected String owner;
    protected List<Notation> notation;

    @XmlAttribute(name = "type")
    protected String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getCardid() {
        return cardid;
    }

    public void setCardid(String value) {
        this.cardid = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String value) {
        this.owner = value;
    }

    public List<Notation> getNotation() {
        if (notation == null) {
            notation = new ArrayList<Notation>();
        }
        return this.notation;
    }

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
