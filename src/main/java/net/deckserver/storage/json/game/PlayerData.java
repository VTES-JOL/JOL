package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.deckserver.game.storage.state.RegionType;

import java.util.HashMap;
import java.util.Map;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
@JsonIdentityReference
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class PlayerData {
    @Setter(AccessLevel.NONE)
    private String name;
    private int pool = 30;
    private float victoryPoints = 0.0f;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData prey;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData predator;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Map<RegionType, RegionData> regions = new HashMap<>();

    private boolean ousted = false;
    private String notes;

    public PlayerData(String name) {
        this.name = name;
        for (RegionType type : RegionType.values()) {
            this.regions.put(type, new RegionData(type, this));
        }
    }

    public PlayerData() {
    }

    public RegionData getRegion(RegionType type) {
        return this.regions.get(type);
    }
}
