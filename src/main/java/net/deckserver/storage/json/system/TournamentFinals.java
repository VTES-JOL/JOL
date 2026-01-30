package net.deckserver.storage.json.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TournamentFinals {
    private List<String> seeding = new ArrayList<>();
    private List<String> seating = new ArrayList<>();
}
