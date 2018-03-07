package net.deckserver.tasks;

import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.GameActions;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class CleanActionsFiles {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Need to provide a file path");
            System.exit(1);
        }

        String basePath = args[0];

        Properties systemProperties = new Properties();
        File systemFile = new File(basePath, "system.properties");
        if (!systemFile.exists()) {
            System.out.println("Unable to find system.properties file at location " + basePath);
            System.exit(1);
        }
        try (FileReader systemReader = new FileReader(systemFile)) {
            systemProperties.load(systemReader);
        }

        List<String> gameNames = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith("game"))
                .collect(Collectors.toList());

        if (!systemFile.exists()) {
            System.out.println("Unable to find system.properties file at location " + basePath);
            System.exit(1);
        }

        System.out.println("Updating " + gameNames.size() + " games with latest actions definitions");
        gameNames.stream()
                .map(name -> Paths.get(basePath, name, "actions.xml").toFile())
                .filter(File::exists)
                .forEach(CleanActionsFiles::updateCardLinks);
    }

    private static void updateCardLinks(File actionsFile) {
        GameActions gameActions = FileUtils.loadGameActions(actionsFile);
        gameActions.getTurn().forEach(turn -> {
            turn.getAction()
                    .forEach(action -> {
                        String text = action.getText();
                        String newText = text.replaceAll("href='javascript:getCard\\((\".*?\")\\)';", "class='card-name' title=$1");
                        action.setText(newText);
                    });
        });
        FileUtils.saveGameActions(gameActions, actionsFile);
    }
}
