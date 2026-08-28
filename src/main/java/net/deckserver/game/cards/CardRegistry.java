package net.deckserver.game.cards;

import net.deckserver.game.enums.Discipline;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Structured card database loaded from the VEKN CSV files
 * ({@code vtescrypt.csv} / {@code vteslib.csv} under {@code jol.card.dir},
 * default {@code csv/core}).
 *
 * <p>Ported from the jol-quarkus rewrite and kept as a plain static holder to
 * match {@link net.deckserver.services.CardService} (which is likewise a
 * static, load-once reference-data class, not a CDI bean). The two run side by
 * side during the deck-editor migration: {@code CardService}/{@code CardSummary}
 * still backs the game board, chat card-links and {@code DeckParser}; this
 * registry adds the parsed structure they lack (split and/or disciplines,
 * structured costs, requirement clans/path, crypt group/capacity) for the
 * editor analytics, autocomplete DTOs and KRCG import work landing later.
 *
 * <p>Data loads on first access (class-init), exactly like {@code CardService}.
 * {@link #load(Path)} can be called again to point at a different directory —
 * only needed by tests.
 */
public final class CardRegistry {

    private static final Logger logger = getLogger(CardRegistry.class);

    /** Lowercased full discipline name (e.g. "blood sorcery") -> code (e.g. "tha"). */
    private static final Map<String, String> DISC_NAME_TO_CODE = new HashMap<>();

    static {
        for (Discipline d : Discipline.values()) {
            DISC_NAME_TO_CODE.put(d.name().toLowerCase().replace('_', ' '), d.code);
        }
    }

    private static final List<Card> ALL_CARDS = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Card> ID_MAP = new ConcurrentHashMap<>();

    /**
     * Lookup index keyed by every searchable name variant (printed name,
     * aliases, accent-stripped forms). For crypt cards the key carries the
     * "(G# ADV)" qualifier. One card appears under several keys.
     */
    private static final Map<String, Card> LOOKUP_MAP = new ConcurrentHashMap<>();

    /**
     * Case-insensitive, accent-stripped index for JOL import name resolution.
     * Key = lowercase accent-stripped lookup key; value = same card as {@link #LOOKUP_MAP}.
     */
    private static final Map<String, Card> LOWER_LOOKUP_MAP = new ConcurrentHashMap<>();

    static {
        String cardDir = ConfigProvider.getConfig()
                .getOptionalValue("jol.card.dir", String.class)
                .orElse("csv/core");
        load(Paths.get(cardDir));
    }

    private CardRegistry() {
    }

    // ── Public accessors ─────────────────────────────────────────────────────

    public static List<Card> allCards() {
        return List.copyOf(ALL_CARDS);
    }

    /** All lookup-map entries — used by autocomplete to iterate name variants. */
    public static Map<String, Card> lookupEntries() {
        return Collections.unmodifiableMap(LOOKUP_MAP);
    }

    public static Card findById(String id) {
        return ID_MAP.get(id);
    }

    /** Looks up a card by accent-stripped, lowercased name as used in JOL import. */
    public static Card findByNormalizedName(String normalizedName) {
        return LOWER_LOOKUP_MAP.get(normalizedName);
    }

    // ── Loading ──────────────────────────────────────────────────────────────

    public static synchronized void load(Path cardDir) {
        Path cryptPath = cardDir.resolve("vtescrypt.csv");
        Path libraryPath = cardDir.resolve("vteslib.csv");

        if (!Files.exists(cryptPath)) {
            logger.error("Unable to find crypt card file at {}", cryptPath);
        }
        if (!Files.exists(libraryPath)) {
            logger.error("Unable to find library card file at {}", libraryPath);
        }

        CSVFormat cryptFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Id", "Name", "Aka", "Type", "Clan", "Path", "Adv", "Group",
                        "Capacity", "Disciplines", "Card Text", "Set", "Title", "Banned", "Artist")
                .setSkipHeaderRecord(true)
                .get();
        CSVFormat libraryFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Id", "Name", "Aka", "Type", "Clan", "Path", "Discipline",
                        "Pool Cost", "Blood Cost", "Conviction Cost", "Burn Option",
                        "Card Text", "Flavor Text", "Set", "Banned", "Artist", "Capacity")
                .setSkipHeaderRecord(true)
                .get();

        ALL_CARDS.clear();
        ID_MAP.clear();
        LOOKUP_MAP.clear();
        LOWER_LOOKUP_MAP.clear();

        loadCrypt(cryptPath, cryptFormat);
        loadLibrary(libraryPath, libraryFormat);

        LOOKUP_MAP.forEach((k, v) -> LOWER_LOOKUP_MAP.put(StringUtils.stripAccents(k).toLowerCase(), v));

        // For crypt cards whose bare name is unique, also index by that bare
        // name so JOL imports don't need a group qualifier for unambiguous
        // vampires.
        Map<String, Set<Card>> bareNameToCards = new LinkedHashMap<>();
        for (Card card : ALL_CARDS) {
            if (!(card instanceof CryptCard)) continue;
            for (String variant : nameVariants(card)) {
                bareNameToCards.computeIfAbsent(variant, k -> new LinkedHashSet<>()).add(card);
            }
        }
        int bareAdded = 0;
        int bareDisambiguated = 0;
        for (Map.Entry<String, Set<Card>> entry : bareNameToCards.entrySet()) {
            if (entry.getValue().size() == 1) {
                LOWER_LOOKUP_MAP.putIfAbsent(entry.getKey(), entry.getValue().iterator().next());
                bareAdded++;
            } else {
                // Multiple versions share this name (different groups or ADV).
                // Resolve to the non-advanced card with the lowest numeric
                // group, matching the old JOL convention that the bare name
                // refers to the original printing.
                Optional<Card> canonical = entry.getValue().stream()
                        .filter(c -> c instanceof CryptCard cc && !cc.advanced())
                        .filter(c -> ((CryptCard) c).group().matches("\\d+"))
                        .min(Comparator.comparingInt(c -> Integer.parseInt(((CryptCard) c).group())));
                if (canonical.isPresent()) {
                    LOWER_LOOKUP_MAP.putIfAbsent(entry.getKey(), canonical.get());
                    bareDisambiguated++;
                }
            }
        }

        logger.info("Loaded {} cards ({} lookup keys, {} unambiguous crypt bare names, {} disambiguated to lowest non-adv group)",
                ALL_CARDS.size(), LOOKUP_MAP.size(), bareAdded, bareDisambiguated);
    }

    private static void loadCrypt(Path path, CSVFormat format) {
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            for (CSVRecord r : format.parse(in)) {
                boolean advanced = "Advanced".equals(r.get("Adv"));
                String group = r.get("Group");

                CryptCard card = new CryptCard(
                        r.get("Id"),
                        r.get("Name"),
                        splitSemicolon(r.get("Aka")),
                        parseSetColumn(r.get("Set")),
                        r.get("Card Text"),
                        r.get("Artist"),
                        StringUtils.isNotBlank(r.get("Banned")),
                        "Imbued".equals(r.get("Type")) ? CryptType.IMBUED : CryptType.VAMPIRE,
                        nullIfBlank(r.get("Clan")),
                        nullIfBlank(r.get("Path")),
                        group,
                        advanced,
                        Integer.parseInt(r.get("Capacity")),
                        parseCryptDisciplines(r.get("Disciplines")),
                        nullIfBlank(r.get("Title"))
                );

                ALL_CARDS.add(card);
                ID_MAP.put(card.id(), card);
                indexCryptCard(card, group, advanced);
            }
        } catch (IOException e) {
            logger.error("Unable to read file {}", path, e);
        }
    }

    private static void loadLibrary(Path path, CSVFormat format) {
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            for (CSVRecord r : format.parse(in)) {
                String rawDisc = r.get("Discipline").trim();
                List<String> andDisciplines;
                List<String> orDisciplines;

                if (rawDisc.contains(" & ")) {
                    andDisciplines = splitOn(rawDisc, " & ").stream().map(CardRegistry::disciplineToCode).toList();
                    orDisciplines = List.of();
                } else if (!rawDisc.isEmpty()) {
                    andDisciplines = List.of();
                    orDisciplines = splitOn(rawDisc, "/").stream().map(CardRegistry::disciplineToCode).toList();
                } else {
                    andDisciplines = List.of();
                    orDisciplines = List.of();
                }

                LibraryCard card = new LibraryCard(
                        r.get("Id"),
                        r.get("Name"),
                        splitSemicolon(r.get("Aka")),
                        parseSetColumn(r.get("Set")),
                        r.get("Card Text"),
                        r.get("Artist"),
                        StringUtils.isNotBlank(r.get("Banned")),
                        nullIfBlank(r.get("Flavor Text")),
                        splitOn(r.get("Type"), "/"),
                        splitOn(r.get("Clan"), "/").stream().filter(StringUtils::isNotBlank).toList(),
                        nullIfBlank(r.get("Path")),
                        andDisciplines,
                        orDisciplines,
                        parseCost(r.get("Pool Cost")),
                        parseCost(r.get("Blood Cost")),
                        parseCost(r.get("Conviction Cost")),
                        parseBurnOption(r.get("Burn Option"))
                );

                ALL_CARDS.add(card);
                ID_MAP.put(card.id(), card);
                indexLibraryCard(card);
            }
        } catch (IOException e) {
            logger.error("Unable to read file {}", path, e);
        }
    }

    // ── Indexing ─────────────────────────────────────────────────────────────

    private static void indexCryptCard(CryptCard card, String group, boolean advanced) {
        final String suffix = "ANY".equals(group)
                ? (advanced ? " (ADV)" : "")
                : " (G" + group + (advanced ? " ADV" : "") + ")";

        Stream.concat(
                Stream.of(card.name(), StringUtils.stripAccents(card.name())),
                card.aka().stream().flatMap(a -> Stream.of(a, StringUtils.stripAccents(a)))
        ).map(String::trim).filter(StringUtils::isNotBlank).distinct().forEach(variant -> {
            LOOKUP_MAP.put(variant + suffix, card);
            LOOKUP_MAP.put(replaceArticle(variant) + suffix, card);
        });
    }

    private static void indexLibraryCard(LibraryCard card) {
        Stream.concat(
                Stream.of(card.name(), StringUtils.stripAccents(card.name())),
                card.aka().stream().flatMap(a -> Stream.of(a, StringUtils.stripAccents(a)))
        ).map(String::trim).filter(StringUtils::isNotBlank).distinct().forEach(variant -> {
            LOOKUP_MAP.put(variant, card);
            LOOKUP_MAP.put(replaceArticle(variant), card);
        });
    }

    // ── Parsing helpers ──────────────────────────────────────────────────────

    private static List<String> parseCryptDisciplines(String raw) {
        if (StringUtils.isBlank(raw) || "-none-".equals(raw)) return List.of();
        return Arrays.stream(raw.split(" "))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private static List<String> splitSemicolon(String raw) {
        if (StringUtils.isBlank(raw)) return List.of();
        return Arrays.stream(raw.split(";"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private static List<String> splitOn(String raw, String delimiter) {
        if (StringUtils.isBlank(raw)) return List.of();
        return Arrays.stream(raw.split(Pattern.quote(delimiter)))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    /** Null when blank, -1 when variable (X), otherwise the integer value. */
    private static Integer parseCost(String raw) {
        if (StringUtils.isBlank(raw)) return null;
        if ("X".equalsIgnoreCase(raw.trim())) return -1;
        return Integer.parseInt(raw.trim());
    }

    private static List<String> parseSetColumn(String raw) {
        if (StringUtils.isBlank(raw)) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(s -> s.startsWith("Promo-") ? "Promo:" + s.substring(6) : s)
                .map(s -> s.split(":")[0])
                .map(String::toUpperCase)
                .distinct()
                .toList();
    }

    private static boolean parseBurnOption(String raw) {
        return "Y".equalsIgnoreCase(raw) || "Yes".equalsIgnoreCase(raw);
    }

    /** Converts a full discipline name from the CSV to its short code. */
    private static String disciplineToCode(String name) {
        return DISC_NAME_TO_CODE.getOrDefault(name.trim().toLowerCase(), name.trim().toLowerCase());
    }

    private static String nullIfBlank(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    /**
     * Stripped-lowercase bare name variants (name + AKAs) used to build the
     * unambiguous crypt bare-name index.
     */
    private static Set<String> nameVariants(Card card) {
        return Stream.concat(Stream.of(card.name()), card.aka().stream())
                .flatMap(n -> Stream.of(n, replaceArticle(n)))
                .map(n -> StringUtils.stripAccents(n).toLowerCase().strip())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** "The Foo" &lt;-&gt; "Foo, The" article swap for alternate lookup keys. */
    private static String replaceArticle(String text) {
        if (text.endsWith(", The")) return "The " + text.substring(0, text.length() - 5);
        if (text.startsWith("The ")) return text.substring(4) + ", The";
        return text;
    }
}
