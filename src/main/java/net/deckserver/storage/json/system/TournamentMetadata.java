package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
public class TournamentMetadata {
    private String name;
    private String deckFormat;
    private String registrationEndTime;
    private String startTime;
    private String endTime;
    private List<String> rules;
    private String conditions;
    private List<String> specialRules;
    private boolean registered;
    private boolean decksChosen;
    private long playerCount;
    private int numberOfRounds;
    private int numberOfTables;

    public TournamentMetadata(TournamentDefinition definition) {
        this.name = definition.getName();
        this.deckFormat = definition.getDeckFormat().getLabel();
        this.registrationEndTime = definition.getRegistrationEnd().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.startTime = definition.getPlayStarts().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.endTime = definition.getPlayEnds().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.rules = definition.getRules();
        this.conditions = definition.getSpecialRules().getCondition();
        this.specialRules = definition.getSpecialRules().getRules();
        this.playerCount = definition.getPlayerCount();
        this.numberOfRounds = definition.getNumberOfRounds();
        this.numberOfTables = definition.getNumberOfTables();
    }

    public TournamentMetadata(TournamentDefinition definition, String player) {
        this(definition);
        this.registered = definition.hasPlayerRegistered(player);
        this.decksChosen = definition.hasPlayerChosenDecks(player);
    }
}
