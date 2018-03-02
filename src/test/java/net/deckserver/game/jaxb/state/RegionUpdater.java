package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.Action;
import net.deckserver.game.jaxb.actions.GameActions;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegionUpdater {

    private static final String BASE_PATH = "/Users/shannon/data/";

    @Test
    public void updateCardLinks() throws Exception {
        Properties systemProperties = new Properties();
        File systemFile = new File(BASE_PATH, "system.properties");
        assertTrue(systemFile.exists());
        try (FileReader systemReader = new FileReader(systemFile)) {
            systemProperties.load(systemReader);
        }

        List<String> gameNames = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith("game"))
                .collect(Collectors.toList());
        assertFalse(gameNames.isEmpty());

        gameNames.stream()
                .map(name -> Paths.get(BASE_PATH, name, "actions.xml").toFile())
                .filter(File::exists)
                .forEach(this::updateCardLinks);
    }

    private void updateCardLinks(File actionsFile) {
        GameActions gameActions = FileUtils.loadGameActions(actionsFile);
        gameActions.getTurn().forEach(turn -> {
            turn.getAction()
                    .forEach(action -> {
                        String text = action.getText();
                        String newText = text.replaceAll("href='javascript:getCard\\((\".*\")\\)';", "class='card-name' title=$1");
                        action.setText(newText);
                    });
        });
        FileUtils.saveGameActions(gameActions, actionsFile);
    }


}
