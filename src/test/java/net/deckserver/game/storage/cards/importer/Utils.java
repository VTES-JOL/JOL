package net.deckserver.game.storage.cards.importer;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    static Set<String> generatedNames = new HashSet<>();

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

    static Set<String> getSets(String source) {
        List<String> setDetails = split(source, ",").orElse(new ArrayList<>());
        return setDetails.stream()
                .map(String::trim)
                .map(s -> s.split(":")[0])
                .collect(Collectors.toSet());
    }

    static Optional<String> getClean(String source) {
        String cleaned = clean(source);
        return cleaned.isEmpty() ? Optional.empty() : Optional.of(cleaned);
    }

    static String generateUniqueName(String name, boolean advanced, Integer group) {
        String cleanName = getClean(name).orElse(name);
        List<String> suffix = generateSuffixes(advanced, group);
        if (!suffix.isEmpty()) {
            String suffixString = suffix.stream().collect(Collectors.joining(" ", "(", ")"));
            return cleanName + " " + suffixString;
        }
        return cleanName;
    }

    static List<String> generateSuffixes(boolean advanced, Integer group) {
        List<String> suffix = new ArrayList<>();
        if (group != null) suffix.add(String.format("G%d", group));
        if (advanced) suffix.add("ADV");
        return suffix;
    }

    static Names generateNames(String original, List<String> aliases, boolean advanced, Integer group) {
        Names result = new Names();
        Set<String> tempNames = new HashSet<>(aliases);
        Set<String> names = new HashSet<>();

        String simpleName = original.endsWith(", The") ?
                original.replaceAll(", The", "")
                        .replaceAll("^", "The ") : original;

        String uniqueName = original;
        if (generatedNames.contains(original)) {
            uniqueName = generateUniqueName(original, advanced, group);
        }
        names.add(generateUniqueName(original, advanced, null));
        names.add(uniqueName);
        tempNames.add(original);
        tempNames.add(simpleName);
        tempNames.add(StringUtils.stripAccents(original));
        tempNames.stream().map(name -> generateUniqueName(name, advanced, group)).forEach(names::add);
        names.addAll(tempNames);

        names.remove("");
        // If a name has been generated before, remove it from being eligible - this should mostly happen with the new V5 vamps
        names.removeAll(generatedNames);
        // Add generated names to the pool of generated names
        generatedNames.addAll(names);

        result.setDisplayName(original);
        result.setUniqueName(uniqueName);
        result.setNames(names);
        return result;
    }

}
