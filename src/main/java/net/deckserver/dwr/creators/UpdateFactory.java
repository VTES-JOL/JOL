package net.deckserver.dwr.creators;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.DeckserverRemote;
import net.deckserver.dwr.model.PlayerModel;
import org.directwebremoting.WebContextFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        viewMap.put("version", new VersionCreator());
        viewMap.put("tournament", new TournamentCreator());
    }

    private static ViewCreator getView(String type) {
        return viewMap.get(type);
    }

    public static Map<String, Object> getUpdate() {
        String playerName = DeckserverRemote.getPlayer(WebContextFactory.get().getHttpServletRequest());
        PlayerModel player = JolAdmin.getPlayerModel(playerName);
        JolAdmin.recordPlayerAccess(player.getPlayerName());
        Map<String, Object> object = Stream.of(player.getView(), "nav", "version")
                .map(UpdateFactory::getView)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ViewCreator::getFunction, v -> v.createData(player)));
        return object;
    }

}
