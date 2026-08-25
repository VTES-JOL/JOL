package net.deckserver.game.model;

import net.deckserver.game.enums.RegionType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

// Used to hold per-viewer region-collapse UI state (toggleCollapsed/
// isCollapsed) for a game, read/written by GameActionResource's doToggle
// endpoint. Previously also carried a hand/state JSP-fragment cache and
// dirty-flag tracking for GameView.create() (called only from the legacy
// DWR-era GameCreator/UpdateFactory) — removed along with UpdateFactory and
// its *Creator classes, since create() was their only caller and nothing
// else read those fragments/flags.
public class GameView {

    private final Collection<String> collapsed = new HashSet<>();

    public GameView(JolGame game, String gameName, String playerName) {
        List<String> players = game.getPlayers();
        for (int i = 0; i < players.size(); ) {
            boolean ousted = game.getPool(players.get(i)) < 1;
            i++;
            collapsed.add(i + "-" + RegionType.ASH_HEAP);
            collapsed.add(i + "-" + RegionType.REMOVED_FROM_GAME);
            collapsed.add(i + "-" + RegionType.LIBRARY);
            collapsed.add(i + "-" + RegionType.HAND);
            collapsed.add(i + "-" + RegionType.CRYPT);
            if (ousted) {
                collapsed.add(i + "-" + RegionType.TORPOR);
                collapsed.add(i + "-" + RegionType.RESEARCH);
                collapsed.add(i + "-" + RegionType.READY);
                collapsed.add(i + "-" + RegionType.UNCONTROLLED);
            }
        }
    }

    public boolean isCollapsed(String region) {
        return collapsed.contains(region);
    }

    public void toggleCollapsed(String id) {
        if (collapsed.contains(id))
            collapsed.remove(id);
        else
            collapsed.add(id);
    }
}
