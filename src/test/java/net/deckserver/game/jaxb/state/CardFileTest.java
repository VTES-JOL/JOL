package net.deckserver.game.jaxb.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CardFileTest {

    private Path cardPath;

    public CardFileTest(String cardLocation) {
        cardPath = Paths.get(cardLocation);
    }

    List<String> read() throws IOException {
        return Files.readAllLines(cardPath);
    }

    public static void main(String[] args) throws IOException {
        CardFileTest cardFileTest = new CardFileTest("src/test/resources/cards/cardlist.txt");
        cardFileTest.read();

    }
}
