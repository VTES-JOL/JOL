package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.TournamentFormat;

import java.time.OffsetDateTime;
import java.util.*;

@Data
public class TournamentDefinition {
    private String id = UUID.randomUUID().toString();
    private OffsetDateTime registrationStart;
    private OffsetDateTime registrationEnd;
    private OffsetDateTime playStarts;
    private OffsetDateTime playEnds;
    private TournamentFormat format;
    private GameFormat deckFormat;
    private int numberOfRounds;
    private boolean finalEnabled;
    private boolean requiresId;
    private String name;
    private List<String> rules = new ArrayList<>();
    private TournamentSpecialRules specialRules = new TournamentSpecialRules();
    private Set<TournamentRegistration> registrations = new HashSet<>();
    // Round, Table, Players
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Table<Integer, Integer, List<TournamentPlayer>> rounds = HashBasedTable.create();

    @JsonIgnore
    public List<TournamentPlayer> getPlayers(int round, int table) {
        return rounds.get(round, table);
    }

    @JsonIgnore
    public boolean hasPlayerRegistered(String player) {
        return registrations.stream().anyMatch(r -> r.getPlayer().equals(player));
    }

    @JsonIgnore
    public boolean hasPlayerChosenDecks(String player) {
        return registrations.stream()
                .filter(r -> r.getPlayer().equals(player))
                .anyMatch(r -> r.getDeck() != null);
    }

    @JsonIgnore
    public boolean isOpenForRegistration() {
        OffsetDateTime now = OffsetDateTime.now();
        return registrationStart.isBefore(now) && registrationEnd.isAfter(now);
    }

    @JsonIgnore
    public boolean isCurrent() {
        OffsetDateTime now = OffsetDateTime.now();
        return playStarts.isBefore(now) && playEnds.isAfter(now);
    }

    @JsonIgnore
    public Optional<TournamentRegistration> getRegistration(String player) {
        return registrations.stream().filter(r -> r.getPlayer().equals(player)).findFirst();
    }

    @JsonSetter("rounds")
    public void setRounds(Map<Integer, Map<Integer, List<TournamentPlayer>>> data) {
        data.forEach((round, tableMap) -> {
            tableMap.forEach((table, players) -> {
                rounds.put(round, table, players);
            });
        });
    }

    @JsonIgnore
    public long getPlayerCount() {
        return registrations.stream().filter(r -> r.getDeck() != null).count();
    }
}