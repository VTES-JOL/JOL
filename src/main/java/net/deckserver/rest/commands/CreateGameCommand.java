package net.deckserver.rest.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateGameCommand {
    private String gameName;
    @JsonProperty("public")
    private boolean isPublic;
}
