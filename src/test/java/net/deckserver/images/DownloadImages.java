package net.deckserver.images;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadImages {

    private static final String BASE_URL = "http://www.lackeyccg.com/vtes/high/cards/";
    private static final Path IMAGE_PATH = Paths.get("src/test/resources/images");

    @Test
    public void download() throws Exception {

        Document doc = Jsoup.connect(BASE_URL).get();
        Elements links = doc.select("li a");
        links.stream().map(e -> e.attr("href"))
                .filter(href -> href.endsWith(".jpg"))
                .map(href -> BASE_URL + href)
                .forEach(url -> {
                    String filename = Paths.get(url).getFileName().toString();
                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, IMAGE_PATH.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Downloaded " + url);
                    } catch (IOException e) {
                        System.err.println("Unable to download " + url);
                        e.printStackTrace();
                    }
                });
    }
}
