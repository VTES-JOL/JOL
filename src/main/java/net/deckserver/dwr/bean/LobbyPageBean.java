package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RegistrationService;

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
        OffsetDateTime currentMonth = OffsetDateTime.now().minusMonths(1);

        playtester = JolAdmin.isPlaytester(player);
        gameFormats = JolAdmin.getAvailableGameFormats(player).stream().map(GameFormat::getLabel).toList();

        players = PlayerService.getPlayers().stream()
                .sorted()
                .map(PlayerActivityStatus::new)
                .filter(playerActivityStatus -> playerActivityStatus.online().isAfter(currentMonth))
                .sorted(Comparator.comparing(PlayerActivityStatus::getLastOnline))
                .map(PlayerActivityStatus::getName)
                .collect(Collectors.toList());

        myGames = JolAdmin.getGameNames().stream()
                .filter(Objects::nonNull)
                .filter(JolAdmin::isPrivate)
                .filter(gameName -> JolAdmin.isViewable(gameName, player))
                .filter(gameName -> player.equals(JolAdmin.getOwner(gameName)))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());

        publicGames = JolAdmin.getGameNames().stream()
                .filter(Objects::nonNull)
                .filter(JolAdmin::isStarting)
                .filter(JolAdmin::isPublic)
                .filter(gameName -> JolAdmin.isViewable(gameName, player))
                .map(GameStatusBean::new)
                .sorted(Comparator.comparing(GameStatusBean::getCreated))
                .collect(Collectors.toList());

        invitedGames = JolAdmin.getGameNames().stream()
                .filter(Objects::nonNull)
                .filter(JolAdmin::isStarting)
                .filter(gameName -> RegistrationService.isInGame(gameName, player))
                .map(gameName -> new GameInviteStatus(gameName, player))
                .collect(Collectors.toList());

        decks = DeckService.getPlayerDeckNames(player).stream()
                .filter(Objects::nonNull)
                .map(deckName -> new DeckInfoBean(player, deckName))
                .filter(deckInfoBean -> !deckInfoBean.getDeckFormat().equals(DeckFormat.LEGACY.toString()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        message = JolAdmin.getPlayerModel(player).getMessage();

    }

}
