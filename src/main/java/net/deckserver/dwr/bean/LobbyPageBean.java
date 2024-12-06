package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class LobbyPageBean {
    private final List<String> players;
    private final List<GameStatusBean> myGames;
    private final List<GameStatusBean> publicGames;
    private final List<RegistrationStatus> invitedGames;
    private final List<DeckInfoBean> decks;
    private final String message;

    public LobbyPageBean(String player) {
        JolAdmin admin = JolAdmin.INSTANCE;

        players = admin.getPlayers().stream().sorted().collect(Collectors.toList());

        myGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isPrivate)
                .filter(gameName -> player.equals(admin.getOwner(gameName)))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());

        publicGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isStarting)
                .filter(admin::isPublic)
                .map(GameStatusBean::new)
                .sorted(Comparator.comparing(GameStatusBean::getCreated))
                .collect(Collectors.toList());

        invitedGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isStarting)
                .filter(gameName -> admin.isInGame(gameName, player))
                .map(gameName -> new RegistrationStatus(gameName, player))
                .collect(Collectors.toList());

        decks = admin.getDeckNames(player).stream()
                .filter(Objects::nonNull)
                .map(deckName -> new DeckInfoBean(player, deckName))
                .filter(deckInfoBean -> deckInfoBean.getDeckFormat().equals("MODERN"))
                .filter(deckInfoBean -> admin.isValid(player, deckInfoBean.getName()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        message = JolAdmin.INSTANCE.getPlayerModel(player).getMessage();
    }

}
