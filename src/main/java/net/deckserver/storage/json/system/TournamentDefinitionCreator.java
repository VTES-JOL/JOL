package net.deckserver.storage.json.system;

import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public class TournamentDefinitionCreator {
    private TournamentDefinition tourDef;

    public TournamentDefinitionCreator() {
        this.tourDef = new TournamentDefinition();
    }

    public static TournamentDefinitionCreator newTourDef() {
        return new TournamentDefinitionCreator();
    }

    public TournamentDefinitionCreator withName(String newTourName) {
        getTourDef().setName(newTourName);
        return this;
    }
    public TournamentDefinitionCreator withRegStart(OffsetDateTime regStart) {
        getTourDef().setRegistrationStart(regStart);
        return this;
    }
    public TournamentDefinitionCreator withRegEnd(OffsetDateTime regEnd) {
        getTourDef().setRegistrationEnd(regEnd);
        return this;
    }
    public TournamentDefinitionCreator withPlayStart(OffsetDateTime playStart) {
        getTourDef().setPlayStarts(playStart);
        return this;
    }
    public TournamentDefinitionCreator withPlayEnd(OffsetDateTime playEnd) {
        getTourDef().setPlayEnds(playEnd);
        return this;
    }
    public TournamentDefinitionCreator withTourFormat(TournamentFormat tourFormat) {
        getTourDef().setFormat(tourFormat);
        return this;
    }
    public TournamentDefinitionCreator withDeckFormat(GameFormat gameFormat) {
        getTourDef().setDeckFormat(gameFormat);
        return this;
    }
    public TournamentDefinitionCreator withNumberOfRounds(int numberOfRounds) {
        getTourDef().setNumberOfRounds(numberOfRounds);
        return this;
    }
    public TournamentDefinitionCreator withRules(String[] rules) {
        getTourDef().setRules(List.of(rules));
        return this;
    }
    public TournamentDefinitionCreator withSpecRules(String condition, String[] rules) {
        TournamentSpecialRules tournamentSpecialRules = new TournamentSpecialRules();
        tournamentSpecialRules.setCondition(condition);
        tournamentSpecialRules.setRules(List.of(rules));
        getTourDef().setSpecialRules(tournamentSpecialRules);
        return this;
    }
    public TournamentDefinitionCreator withStatus(GameStatus status) {
        getTourDef().setStatus(status);
        return this;
    }
    public TournamentDefinition getTourDef() {
        return tourDef;
    }

    public TournamentDefinitionCreator withReqId(boolean reqId) {
        getTourDef().setRequiresId(reqId);
        return this;
    }

    public TournamentDefinitionCreator withRegistrations(Set<TournamentRegistration> registrations) {
        getTourDef().setRegistrations(registrations);
        return this;
    }
}
