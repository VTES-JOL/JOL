package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LobbyPageBean {
    private final List<String> players;
    private final List<GameStatusBean> myGames;
    private final List<GameStatusBean> publicGames;
    private final List<PlayerRegistrationStatusBean> invitedGames;
    private final List<DeckInfoBean> decks;
    private final String message;

    public LobbyPageBean(String player) {
        JolAdmin admin = JolAdmin.getInstance();

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
                .collect(Collectors.toList());

        invitedGames = admin.getGames().stream()
                .filter(Objects::nonNull)
                .filter(admin::isStarting)
                .filter(gameName -> admin.isInGame(gameName, player))
                .map(gameName -> new PlayerRegistrationStatusBean(gameName, player))
                .collect(Collectors.toList());

        decks = admin.getDeckNames(player).stream()
                .filter(Objects::nonNull)
                .map(deckName -> new DeckInfoBean(player, deckName))
                .filter(deckInfoBean -> deckInfoBean.getDeckFormat().equals("MODERN"))
                .collect(Collectors.toList());

        message = JolAdmin.getInstance().getPlayerModel(player).getMessage();
    }

    public List<String> getPlayers() {
        return players;
    }

    public List<GameStatusBean> getMyGames() {
        return myGames;
    }

    public List<GameStatusBean> getPublicGames() {
        return publicGames;
    }

    public List<PlayerRegistrationStatusBean> getInvitedGames() {
        return invitedGames;
    }

    public List<DeckInfoBean> getDecks() {
        return decks;
    }

    public String getMessage() { return message; }
}
