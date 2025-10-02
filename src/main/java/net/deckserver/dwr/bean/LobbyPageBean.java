package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.enums.GameFormat;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class LobbyPageBean {
    private final List<String> players;
    private final List<GameStatusBean> myGames;
    private final List<GameStatusBean> publicGames;
    private final List<GameInviteStatus> invitedGames;
    private final List<DeckInfoBean> decks;
    private final String message;
    private final boolean playtester;
    private final List<String> gameFormats;

    public LobbyPageBean(String player) {
        JolAdmin admin = JolAdmin.INSTANCE;
        OffsetDateTime currentMonth = OffsetDateTime.now().minusMonths(1);

        playtester = admin.isPlaytester(player);
        gameFormats = admin.getAvailableGameFormats(player).stream().map(GameFormat::getLabel).toList();

        players = admin.getPlayers().stream()
                .sorted()
                .map(PlayerActivityStatus::new)
                .filter(playerActivityStatus -> playerActivityStatus.online().isAfter(currentMonth))
                .sorted(Comparator.comparing(PlayerActivityStatus::getLastOnline))
                .map(PlayerActivityStatus::getName)
                .collect(Collectors.toList());

        myGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isPrivate)
                .filter(gameName -> admin.isViewable(gameName, player))
                .filter(gameName -> player.equals(admin.getOwner(gameName)))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());

        publicGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isStarting)
                .filter(admin::isPublic)
                .filter(gameName -> admin.isViewable(gameName, player))
                .map(GameStatusBean::new)
                .sorted(Comparator.comparing(GameStatusBean::getCreated))
                .collect(Collectors.toList());

        invitedGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isStarting)
                .filter(gameName -> admin.isInGame(gameName, player))
                .map(gameName -> new GameInviteStatus(gameName, player))
                .collect(Collectors.toList());

        decks = admin.getDeckNames(player).stream()
                .filter(Objects::nonNull)
                .map(deckName -> new DeckInfoBean(player, deckName))
                .filter(deckInfoBean -> !deckInfoBean.getDeckFormat().equals(DeckFormat.LEGACY.toString()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        message = JolAdmin.INSTANCE.getPlayerModel(player).getMessage();

    }

}
