package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.deckserver.game.enums.RegionType;

import java.util.HashMap;
import java.util.Map;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
@JsonIdentityReference
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@EqualsAndHashCode(exclude = {"prey", "predator"})
@ToString(of = {"name"})
public class PlayerData {
    private String name;
    private int pool = 30;
    private float victoryPoints = 0.0f;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData prey;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData predator;

    private Map<RegionType, RegionData> regions = new HashMap<>();

    private boolean ousted = false;
    private String notes;
    private String choice;

    public PlayerData(String name) {
        this.name = name;
        for (RegionType type : RegionType.values()) {
            this.regions.put(type, new RegionData(type, this));
        }
    }

    public PlayerData() {
    }

    @JsonIgnore
    public RegionData getRegion(RegionType type) {
        return this.regions.get(type);
    }

    public void addVictoryPoints(float points) {
        this.victoryPoints += points;
    }
}
