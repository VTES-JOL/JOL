package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.system.TournamentInviteStatus;
import net.deckserver.storage.json.system.TournamentMetadata;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class TournamentBean {

    private boolean veknLinked;
    private final List<TournamentMetadata> tournaments;
    private final List<TournamentInviteStatus> registeredGames;
    private final List<TournamentMetadata> finalsInvites;
    private final List<DeckInfoBean> decks;

    public TournamentBean(String playerName) {
        String veknId = PlayerService.get(playerName).getVeknId();
        veknLinked = !StringUtils.isEmpty(veknId) && veknId.matches("[0-9]+");
        tournaments = TournamentService.getOpenTournaments(playerName);
        registeredGames = TournamentService.getRegisteredTournaments(playerName);
        finalsInvites = TournamentService.getFinalsInvites(playerName);
        decks = DeckService.getPlayerDeckNames(playerName).stream()
                .filter(Objects::nonNull)
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .filter(deckInfoBean -> !deckInfoBean.getDeckFormat().equals(DeckFormat.LEGACY.toString()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
