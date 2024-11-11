package net.deckserver.storage.json.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TournamentData {

    private List<List<List<String>>> tables = new ArrayList<>();
    private Map<String, TournamentRegistration> registrations = new HashMap<>();
}
