package net.deckserver.dwr.creators;

import net.deckserver.dwr.DeckserverRemote;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import org.directwebremoting.WebContextFactory;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateFactory {

    private static final Map<String, ViewCreator> viewMap = new HashMap<>();

    static {
        viewMap.put("nav", new NavCreator());
        viewMap.put("game", new GameCreator());
        viewMap.put("main", new MainCreator());
        viewMap.put("deck", new DeckCreator());
        viewMap.put("lobby", new LobbyCreator());
        viewMap.put("admin", new AdminCreator());
        viewMap.put("profile", new ProfileCreator());
        viewMap.put("active", new AllGamesCreator());
        viewMap.put("super", new SuperCreator());
        viewMap.put("tournament", new TournamentCreator());
    }

    private static ViewCreator getView(String type) {
        return viewMap.get(type);
    }

    public static Map<String, Object> getUpdate() {
        JolAdmin admin = JolAdmin.getInstance();
        String playerName = DeckserverRemote.getPlayer(WebContextFactory.get().getHttpServletRequest());
        PlayerModel player = admin.getPlayerModel(playerName);
        admin.recordPlayerAccess(player.getPlayerName());
        List<String> views = Arrays.asList(player.getView(), "nav");
        return views.stream()
                .map(UpdateFactory::getView)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ViewCreator::getFunction, v -> v.createData(player)));
    }

}
