package net.deckserver.jobs;

import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.Visibility;
import net.deckserver.services.GameService;
import net.deckserver.services.GlobalChatService;
import net.deckserver.services.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PublicGameBuilder implements Runnable{

    private static final long MIN_GAMES = 3;
    private static final Logger logger = LoggerFactory.getLogger(PublicGameBuilder.class);

    @Override
    public void run() {
        for (GameFormat format : GameFormat.values()) {
            long currentGames = GameService.getPublicGameCount(format);
            long gamesNeeded = MIN_GAMES - currentGames;
            for (int x = 0; x < gamesNeeded; x++) {
                String gameName = NameService.generateName();
                GameService.create(gameName, UUID.randomUUID().toString(), "SYSTEM", Visibility.PUBLIC, format);
                if (format != GameFormat.PLAYTEST) {
                    GlobalChatService.chat("SYSTEM", String.format("New public game <b>%s</b> (%s) has been created.", gameName, format.getLabel()));
                }
            }
        }
        logger.debug("Finished building public games");
    }
}
