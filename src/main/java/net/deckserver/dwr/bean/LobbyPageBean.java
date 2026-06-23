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
    private final List<GameStatusBean> games;
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

        // Unified game list: owner's private games (any status) + public starting games + invited private starting games
        games = JolAdmin.getGameNames().stream()
                .filter(Objects::nonNull)
                .filter(gameName -> JolAdmin.isViewable(gameName, player))
                .filter(gameName ->
                        (JolAdmin.isPrivate(gameName) && player.equals(JolAdmin.getOwner(gameName)))
                        || (JolAdmin.isStarting(gameName) && JolAdmin.isPublic(gameName))
                        || (JolAdmin.isStarting(gameName) && RegistrationService.isInGame(gameName, player)))
                .distinct()
                .map(gameName -> new GameStatusBean(gameName, player))
                .sorted(Comparator.comparing(GameStatusBean::getFormat).thenComparing(GameStatusBean::getCreated))
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
