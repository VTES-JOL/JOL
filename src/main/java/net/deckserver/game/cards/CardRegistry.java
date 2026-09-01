package net.deckserver.game.cards;

import net.deckserver.game.cards.importer.CryptMetadata;
import net.deckserver.game.cards.importer.PlayModeParser;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The single source of truth for all card reference data. Parses the VEKN CSVs
 * ({@code vtescrypt.csv} / {@code vteslib.csv}, plus optional
 * {@code *_playtest.csv}, under {@code jol.card.dir} — default {@code csv/core})
 * once at boot into an immutable {@link Index}, and serves every card concern
 * from it:
 *
 * <ul>
 *   <li>name resolution for deck text, chat card-links and KRCG/JOL import,</li>
 *   <li>structured data for the deck editor (autocomplete, analytics, validation),</li>
 *   <li>play-time data for the game board (modes, replace rules, css classes).</li>
 * </ul>
 *
 * <p>Replaces the old split between this class, {@code CardService} /
 * {@code CardSummary} and the CloudFront {@code cards.json}. Hot-reloadable:
 * {@link #reload()} rebuilds the {@link Index} and swaps it in one volatile
 * write, so a concurrent reader always sees a complete index (never a
 * half-cleared map).
 *
 * <p>Plain static holder — no CDI, no DB — matching the reference-data classes
 * it replaces. The index is built lazily on first access; {@link CardRegistryStartup}
 * forces it at boot so the first request doesn't pay for it.
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

    /** Immutable snapshot of one CSV load. Swapped wholesale by {@link #install}. */
    private record Index(
            List<Card> all,
            Map<String, Card> byId,
            Map<String, Card> byNameVariant,
            Map<String, Card> byNormalizedName,
            Instant loadedAt,
            String sourceDir
    ) {
        RegistryStatus status() {
            int crypt = (int) all.stream().filter(Card::isCrypt).count();
            return new RegistryStatus(all.size(), crypt, all.size() - crypt,
                    byNameVariant.size(), loadedAt, sourceDir);
        }
    }

    private static volatile Index index;

    private CardRegistry() {
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    private static Index index() {
        Index local = index;
        if (local == null) {
            synchronized (CardRegistry.class) {
                if (index == null) {
                    index = build(configuredCardDir());
                }
                local = index;
            }
        }
        return local;
    }

    /** Force the initial load (called at boot by {@link CardRegistryStartup}). */
    public static void bootstrap() {
        index();
    }

    /** Re-parse the configured card directory and swap the index atomically. */
    public static synchronized RegistryStatus reload() {
        return install(build(configuredCardDir()));
    }

    /** Re-parse a specific directory (tests / staged set imports). */
    public static synchronized RegistryStatus load(Path cardDir) {
        return install(build(cardDir));
    }

    public static RegistryStatus status() {
        return index().status();
    }

    private static RegistryStatus install(Index next) {
        index = next;
        RegistryStatus status = next.status();
        logger.info("card registry loaded: {} cards ({} crypt / {} library), {} lookup keys, from {}",
                status.cardCount(), status.cryptCount(), status.libraryCount(),
                status.lookupKeyCount(), status.sourceDir());
        return status;
    }

    private static Path configuredCardDir() {
        return Paths.get(ConfigProvider.getConfig()
                .getOptionalValue("jol.card.dir", String.class)
                .orElse("csv/core"));
    }

    // ── Canonical model access ───────────────────────────────────────────────

    public static List<Card> allCards() {
        return index().all();
    }

    /** The card with this printed id, or null. */
    public static Card findById(String id) {
        return index().byId().get(id);
    }

    public static boolean exists(String id) {
        return index().byId().containsKey(id);
    }

    /** Name-variant -> card, for the autocomplete scorer to iterate. */
    public static Map<String, Card> lookupEntries() {
        return index().byNameVariant();
    }

    // ── Name resolution ─────────────────────────────────────────────────────

    /** Every searchable string that resolves to this card (see {@link #buildIndexMaps}). */
    public static Set<String> nameVariants(String id) {
        Card card = findById(id);
        if (card == null) return Set.of();
        return index().byNameVariant().entrySet().stream()
                .filter(e -> e.getValue().id().equals(id))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Canonical display name: bare for library, name + " (G# ADV)" for crypt. */
    public static String displayName(Card card) {
        return card.displayName();
    }

    /** Exact resolve against a printed / aka / qualified variant, accent-sensitive. */
    public static Optional<Card> resolveExact(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(index().byNameVariant().get(name.trim()));
    }

    /** Exact resolve, accent- and case-insensitive; optionally excludes playtest cards. */
    public static Optional<Card> resolveNormalized(String name, boolean includePlaytest) {
        if (name == null) return Optional.empty();
        Card card = index().byNormalizedName().get(StringUtils.stripAccents(name).toLowerCase().trim());
        if (card == null) return Optional.empty();
        return (includePlaytest || !card.playtest()) ? Optional.of(card) : Optional.empty();
    }

    /** {@link #findByNormalizedName} kept for existing callers. */
    public static Card findByNormalizedName(String normalizedName) {
        return index().byNormalizedName().get(normalizedName);
    }

    /**
     * Loose resolve for free-text deck lines: exact-normalized first, then the
     * last name-variant that starts with the query. No playtest filtering —
     * callers inspect {@link Card#playtest()} themselves.
     */
    public static Optional<Card> resolveFuzzy(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String key = StringUtils.stripAccents(text).toLowerCase().trim();
        Card card = index().byNormalizedName().get(key);
        if (card == null) {
            for (Map.Entry<String, Card> e : index().byNormalizedName().entrySet()) {
                if (e.getKey().startsWith(key)) {
                    card = e.getValue();
                }
            }
        }
        return Optional.ofNullable(card);
    }

    // ── Projections ─────────────────────────────────────────────────────────

    public static Optional<CardRef> cardRef(String id) {
        return Optional.ofNullable(findById(id)).map(CardRef::of);
    }

    // ── Loading ─────────────────────────────────────────────────────────────

    private static Index build(Path cardDir) {
        List<Card> all = new ArrayList<>();

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

        loadCrypt(all, cardDir.resolve("vtescrypt.csv"), cryptFormat, false);
        loadLibrary(all, cardDir.resolve("vteslib.csv"), libraryFormat, false);

        Path cryptPlaytest = cardDir.resolve("vtescrypt_playtest.csv");
        Path libraryPlaytest = cardDir.resolve("vteslib_playtest.csv");
        if (Files.exists(cryptPlaytest)) loadCrypt(all, cryptPlaytest, cryptFormat, true);
        if (Files.exists(libraryPlaytest)) loadLibrary(all, libraryPlaytest, libraryFormat, true);

        return buildIndexMaps(all, cardDir.toString());
    }

    private static void loadCrypt(List<Card> sink, Path path, CSVFormat format, boolean playtest) {
        if (!Files.exists(path)) {
            logger.error("crypt card file not found at {}", path);
            return;
        }
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            for (CSVRecord r : format.parse(in)) {
                boolean advanced = "Advanced".equals(r.get("Adv"));
                String clan = nullIfBlank(r.get("Clan"));
                String text = r.get("Card Text");
                CryptCard card = new CryptCard(
                        r.get("Id"),
                        r.get("Name"),
                        splitSemicolon(r.get("Aka")),
                        parseSetColumn(r.get("Set")),
                        text,
                        r.get("Artist"),
                        StringUtils.isNotBlank(r.get("Banned")),
                        playtest,
                        CryptMetadata.isUnique(text),
                        "Imbued".equals(r.get("Type")) ? CryptType.IMBUED : CryptType.VAMPIRE,
                        clan,
                        CryptMetadata.determineSect(clan, text),
                        nullIfBlank(r.get("Path")),
                        r.get("Group"),
                        advanced,
                        CryptMetadata.isInfernal(text),
                        Integer.parseInt(r.get("Capacity")),
                        parseCryptDisciplines(r.get("Disciplines")),
                        nullIfBlank(r.get("Title")),
                        CryptMetadata.determineVotes(nullIfBlank(r.get("Title")))
                );
                sink.add(card);
            }
        } catch (IOException e) {
            logger.error("unable to read {}", path, e);
        }
    }

    private static void loadLibrary(List<Card> sink, Path path, CSVFormat format, boolean playtest) {
        if (!Files.exists(path)) {
            logger.error("library card file not found at {}", path);
            return;
        }
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

                List<String> types = splitOn(r.get("Type"), "/");
                PlayModeParser.Result modes = PlayModeParser.parse(types, r.get("Card Text"));

                LibraryCard card = new LibraryCard(
                        r.get("Id"),
                        r.get("Name"),
                        splitSemicolon(r.get("Aka")),
                        parseSetColumn(r.get("Set")),
                        r.get("Card Text"),
                        r.get("Artist"),
                        StringUtils.isNotBlank(r.get("Banned")),
                        playtest,
                        modes.unique(),
                        nullIfBlank(r.get("Flavor Text")),
                        types,
                        splitOn(r.get("Clan"), "/").stream().filter(StringUtils::isNotBlank).toList(),
                        nullIfBlank(r.get("Path")),
                        andDisciplines,
                        orDisciplines,
                        parseCost(r.get("Pool Cost")),
                        parseCost(r.get("Blood Cost")),
                        parseCost(r.get("Conviction Cost")),
                        parseBurnOption(r.get("Burn Option")),
                        modes.preamble(),
                        modes.modes(),
                        modes.multiMode(),
                        modes.doNotReplace()
                );
                sink.add(card);
            }
        } catch (IOException e) {
            logger.error("unable to read {}", path, e);
        }
    }

    // ── Indexing ─────────────────────────────────────────────────────────────

    private static Index buildIndexMaps(List<Card> all, String sourceDir) {
        Map<String, Card> byId = new HashMap<>();
        Map<String, Card> byNameVariant = new HashMap<>();
        for (Card card : all) {
            byId.put(card.id(), card);
            if (card instanceof CryptCard crypt) {
                indexCryptCard(byNameVariant, crypt);
            } else {
                indexLibraryCard(byNameVariant, (LibraryCard) card);
            }
        }

        Map<String, Card> byNormalized = new HashMap<>();
        byNameVariant.forEach((k, v) -> byNormalized.put(StringUtils.stripAccents(k).toLowerCase(), v));

        // Also index unambiguous crypt cards by their bare name so JOL imports
        // don't need a group qualifier; ambiguous ones resolve to the lowest
        // non-advanced numeric group (the original printing convention).
        Map<String, Set<Card>> bareNameToCards = new LinkedHashMap<>();
        for (Card card : all) {
            if (!(card instanceof CryptCard)) continue;
            for (String variant : bareNameVariants(card)) {
                bareNameToCards.computeIfAbsent(variant, k -> new LinkedHashSet<>()).add(card);
            }
        }
        for (Map.Entry<String, Set<Card>> entry : bareNameToCards.entrySet()) {
            if (entry.getValue().size() == 1) {
                byNormalized.putIfAbsent(entry.getKey(), entry.getValue().iterator().next());
            } else {
                entry.getValue().stream()
                        .filter(c -> c instanceof CryptCard cc && !cc.advanced())
                        .filter(c -> ((CryptCard) c).group().matches("\\d+"))
                        .min(Comparator.comparingInt(c -> Integer.parseInt(((CryptCard) c).group())))
                        .ifPresent(canonical -> byNormalized.putIfAbsent(entry.getKey(), canonical));
            }
        }

        return new Index(List.copyOf(all), Map.copyOf(byId), Map.copyOf(byNameVariant),
                Map.copyOf(byNormalized), Instant.now(), sourceDir);
    }

    private static void indexCryptCard(Map<String, Card> map, CryptCard card) {
        String suffix = card.cryptSuffix();
        // For an advanced card in a numbered group, also index the group-less
        // "(ADV)" form so [Card Name (ADV)] resolves without the group number
        // (matches the old Utils.generateNames behaviour). Lowest group wins.
        String advSuffix = (card.advanced() && !"ANY".equals(card.group())) ? " (ADV)" : null;
        Stream.concat(
                Stream.of(card.name(), StringUtils.stripAccents(card.name())),
                card.aka().stream().flatMap(a -> Stream.of(a, StringUtils.stripAccents(a)))
        ).map(String::trim).filter(StringUtils::isNotBlank).distinct().forEach(variant -> {
            map.put(variant + suffix, card);
            map.put(replaceArticle(variant) + suffix, card);
            if (advSuffix != null) {
                map.putIfAbsent(variant + advSuffix, card);
                map.putIfAbsent(replaceArticle(variant) + advSuffix, card);
            }
        });
    }

    private static void indexLibraryCard(Map<String, Card> map, LibraryCard card) {
        Stream.concat(
                Stream.of(card.name(), StringUtils.stripAccents(card.name())),
                card.aka().stream().flatMap(a -> Stream.of(a, StringUtils.stripAccents(a)))
        ).map(String::trim).filter(StringUtils::isNotBlank).distinct().forEach(variant -> {
            map.put(variant, card);
            map.put(replaceArticle(variant), card);
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

    private static String disciplineToCode(String name) {
        return DISC_NAME_TO_CODE.getOrDefault(name.trim().toLowerCase(), name.trim().toLowerCase());
    }

    private static String nullIfBlank(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static Set<String> bareNameVariants(Card card) {
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
