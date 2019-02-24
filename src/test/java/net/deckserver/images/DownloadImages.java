package net.deckserver.images;

import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Ignore
public class DownloadImages {

    private static final String BASE_URL = "http://www.lackeyccg.com/vtes/high/cards/";
    private static final Path BASE_PATH = Paths.get("src/test/resources");
    private static final Path IMAGE_PATH = BASE_PATH.resolve("images");
    private static final Path CARDS_PATH = BASE_PATH.resolve("cards").resolve("cards.json");

    @Test
    public void download() throws Exception {

        CardSearch cardSearch = new CardSearch(CARDS_PATH);

        if (!Files.exists(IMAGE_PATH)) {
            Files.createDirectory(IMAGE_PATH);
        }
        for (CardEntry card : cardSearch.getAllCards()) {
            String id = card.getCardId();
            String name = StringUtils.stripAccents(card.getName()).replaceAll("\\W","").toLowerCase();
            String url = BASE_URL + name + ".jpg";
            String filename = id + ".jpg";
            if (!Files.exists(IMAGE_PATH.resolve(id + ".jpg"))) {
                try (InputStream in = new URL(url).openStream()) {
                    Files.copy(in, IMAGE_PATH.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Downloaded " + url);
                } catch (IOException e) {
                    System.err.println("Unable to download " + url);
                    e.printStackTrace();
                }
            }
        }
    }
}
