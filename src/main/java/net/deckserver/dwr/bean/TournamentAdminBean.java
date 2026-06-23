package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.system.TournamentMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
public class TournamentAdminBean {

    private static final Map<String, Integer> STATUS_ORDER = Map.of(
            "ACTIVE", 0, "STARTING", 1, "EDIT", 2);

    private final List<TournamentMetadata> tournaments;

    public TournamentAdminBean(PlayerModel playerModel) {
        tournaments = TournamentService.getTournamentsWithStatus(
                        List.of(GameStatus.EDIT, GameStatus.STARTING, GameStatus.ACTIVE))
                .stream()
                .sorted(Comparator
                        .comparingInt((TournamentMetadata t) -> STATUS_ORDER.getOrDefault(t.getStatus(), 3))
                        .thenComparing(TournamentMetadata::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
