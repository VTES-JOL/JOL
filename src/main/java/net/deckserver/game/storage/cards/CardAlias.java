package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CardAlias {

    private String key;
    private List<String> names = new ArrayList<>();
    private List<String> text = new ArrayList<>();
}
