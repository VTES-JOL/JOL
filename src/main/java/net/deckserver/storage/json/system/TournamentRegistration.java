package net.deckserver.storage.json.system;

import lombok.Data;

import java.util.List;

@Data
public class TournamentRegistration{
    private String playerName;
    private String veknId;
    private List<String> decks;
}
