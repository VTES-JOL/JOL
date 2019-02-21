package net.deckserver.game.storage.cards.importer;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private static String clean(String source) {
        if (source == null) {
            return "";
        }
        return source.replaceAll("[{}]", "").replaceAll("-none-", "").trim();
    }

    static Optional<List<String>> split(String source, String separator) {
        Map<Boolean, List<String>> results = Stream.of(source.split(separator)).map(Utils::clean).collect(Collectors.groupingBy(String::isEmpty));
        return Optional.ofNullable(results.get(false));
    }

    static Optional<String> getClean(String source) {
        String cleaned = clean(source);
        return cleaned.isEmpty() ? Optional.empty() : Optional.of(cleaned);
    }

    static String generateDisplayName(String name, boolean advanced) {
        return getClean(name + (advanced ? " (ADV)" : "")).orElse(name);
    }

    static Set<String> otherNames(String original, boolean advanced, List<String> aliases) {
        Set<String> tempNames = new HashSet<>(aliases);

        String simpleName = original.endsWith(", The") ?
                original.replaceAll(", The", "")
                        .replaceAll("^", "The ") : original;

        // Add base, or original name
        if (advanced) {
            tempNames.add(generateDisplayName(original, true));
            tempNames.add(generateDisplayName(simpleName, true));
        } else {
            tempNames.add(original);
            tempNames.add(simpleName);
        }

        Set<String> names = new HashSet<>();
        tempNames.forEach(name -> {
            names.add(name);
            names.add(StringUtils.stripAccents(name));
        });

        names.remove("");

        return names;
    }

}
