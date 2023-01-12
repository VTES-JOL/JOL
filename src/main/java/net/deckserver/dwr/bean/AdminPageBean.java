package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.*;
import java.util.stream.Collectors;

public class AdminPageBean {
    private final List<String> players;
    private final List<GameStatusBean> myGames;
    private final List<GameStatusBean> publicGames;
    private final List<PlayerRegistrationStatusBean> invitedGames;
    private final List<DeckSummaryBean> decks;

    public AdminPageBean(String player) {
        JolAdmin admin = JolAdmin.getInstance();
        PlayerModel model = admin.getPlayerModel(player);

        players = admin.getPlayers();
        Collections.sort(players);

        myGames = Arrays.stream(admin.getGames())
                .filter(Objects::nonNull)
                .filter(gameName -> player.equals(admin.getOwner(gameName)))
                .filter(gameName -> !admin.isFinished(gameName))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());

        publicGames = Arrays.stream(admin.getGames())
                .filter(Objects::nonNull)
                .filter(admin::isOpen)
                .filter(gameName -> !admin.isPrivate(gameName))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());

        invitedGames = Arrays.stream(admin.getGames())
                .filter(Objects::nonNull)
                .filter(admin::isOpen)
                .filter(gameName -> admin.isInvited(gameName, player) || admin.isRegistered(gameName, player))
                .map(gameName -> new PlayerRegistrationStatusBean(gameName, player))
                .collect(Collectors.toList());

        decks = Arrays.stream(admin.getDeckNames(player))
                .filter(Objects::nonNull)
                .map(deckName -> new DeckSummaryBean(model, deckName, true))
                .sorted(Comparator.comparing(DeckSummaryBean::getName))
                .collect(Collectors.toList());
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

    public List<DeckSummaryBean> getDecks() {
        return decks;
    }
}
