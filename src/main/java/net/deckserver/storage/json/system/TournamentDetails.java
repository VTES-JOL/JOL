package net.deckserver.storage.json.system;

import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class TournamentDetails {
    String name;
    String regStart;
    String regEnd;
    String playStart;
    String playEnd;
    int numRounds;
    String reqId;
    String tourFormat;
    String gameFormat;
    List<String> rules;
    String specRulesCon;
    List<String> specRules;

    public TournamentDetails(TournamentDefinition tournament) {
        this.name = tournament.getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.regStart = tournament.getRegistrationStart().format(formatter);
        this.regEnd = tournament.getRegistrationEnd().format(formatter);
        this.playStart = tournament.getPlayStarts().format(formatter);
        this.playEnd = tournament.getPlayEnds().format(formatter);
        this.numRounds = tournament.getNumberOfRounds();
        this.rules = tournament.getRules();
        this.specRulesCon = tournament.getSpecialRules().getCondition();
        this.specRules = tournament.getSpecialRules().getRules();
        this.tourFormat = tournament.getFormat().name();
        this.gameFormat = tournament.getDeckFormat().name();
        this.reqId = String.valueOf(tournament.isRequiresId());
    }
}
