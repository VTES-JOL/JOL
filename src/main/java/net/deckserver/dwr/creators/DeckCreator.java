package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.DeckBean;
import net.deckserver.dwr.bean.DeckInfoBean;
import net.deckserver.dwr.bean.DeckSummaryBean;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class DeckCreator implements ViewCreator {
    String player;

    public String getFunction() {
        return "callbackShowDecks";
    }

    public Object createData(PlayerModel model) {
        this.player = model.getPlayer();
        List<DeckInfoBean> decks = model.getDecks().stream().map(this::getDeck).sorted(Comparator.comparing(DeckInfoBean::getName)).collect(Collectors.toList());
        return new DeckBean(decks);
    }

    private DeckInfoBean getDeck(String deckName) {
        JolAdmin admin = JolAdmin.getInstance();
        String deckId = admin.getDeckId(this.player, deckName);
        String playerId = admin.getPlayerId(this.player);
        String authHeader = playerId + ":" + deckId;
        String url = "rest/deck/" + Base64.getEncoder().encodeToString(authHeader.getBytes());
        return new DeckInfoBean(url, deckName);
    }

}
