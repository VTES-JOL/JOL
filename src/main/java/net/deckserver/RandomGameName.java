package net.deckserver;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomGameName {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RandomGameName.class);
    private static final Random rand = new Random();
    private static final RandomGameName INSTANCE = new RandomGameName();

    final List<String> adjectives;
    final List<String> verbs;
    final List<String> nouns;

    private RandomGameName() {
        adjectives = IOUtils.readLines(Objects.requireNonNull(RandomGameName.class.getResourceAsStream("names_adjectives.txt")), StandardCharsets.UTF_8);
        logger.info("Loaded {} adjectives", adjectives.size());
        verbs = IOUtils.readLines(Objects.requireNonNull(RandomGameName.class.getResourceAsStream("names_verbs.txt")), StandardCharsets.UTF_8);
        logger.info("Loaded {} verbs", verbs.size());
        nouns = IOUtils.readLines(Objects.requireNonNull(RandomGameName.class.getResourceAsStream("names_nouns.txt")), StandardCharsets.UTF_8);
        logger.info("Loaded {} nouns", nouns.size());
    }

    public static String generateName() {
        return INSTANCE.getAdjective() + " " + INSTANCE.getVerb() + " " + INSTANCE.getNoun();
    }

    public String getAdjective() {
        return adjectives.get(rand.nextInt(adjectives.size()));
    }

    public String getVerb() {
        return verbs.get(rand.nextInt(verbs.size()));
    }

    public String getNoun() {
        return nouns.get(rand.nextInt(nouns.size()));
    }
}
