package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import net.deckserver.game.storage.state.RegionType;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.StringIdGenerator.class, property = "id")
@JsonIdentityReference
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RegionData {
    @JsonIdentityReference(alwaysAsId = true)
    private List<CardData> cards = new ArrayList<>();

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData player;
    private RegionType type;
    private String id;

    public RegionData(RegionType type, PlayerData playerData) {
        this.type = type;
        this.player = playerData;
    }

    public RegionData() {
    }
}
