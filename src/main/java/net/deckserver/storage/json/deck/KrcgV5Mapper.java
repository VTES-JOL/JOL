package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.game.cards.CryptType;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Converts between the canonical in-memory {@link Deck} model and the KRCG v5
 * deck document ({@link KrcgV5Deck}) that is now the single stored form in
 * {@code deck_content} / {@code registration.deck_content} /
 * {@code tournament_registration.deck_content}.
 *
 * <ul>
 *   <li><b>write</b> ({@link #toJson}) — flattens the crypt/library tree into
 *       v5's {@code cards[]}, deriving {@code printed_name} / {@code types} /
 *       {@code suffix} / {@code unicity_suffix} from {@link CardRegistry};</li>
 *   <li><b>read</b> ({@link #fromJson}) — partitions {@code cards[]} back by
 *       {@code kind}, re-buckets the library by {@link Card#typeLine()} (the
 *       same grouping {@link DeckParser} uses), and resolves every card by its
 *       numeric id. Counts and {@code stats}/{@code errors} are recomputed
 *       downstream by {@link DeckParser#analyze(Deck)}.</li>
 * </ul>
 *
 * <p>Unknown card ids are preserved verbatim (name + id kept, empty derived
 * fields) so {@code analyze()} still reports them.
 */
public final class KrcgV5Mapper {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private KrcgV5Mapper() {
    }

    /** True when {@code trimmed} (already {@code strip()}ed) is a KRCG v5 document. */
    public static boolean looksLikeV5(String trimmed) {
        return trimmed.startsWith("{")
                && trimmed.contains("\"cards\"")
                && !trimmed.contains("\"crypt\"")
                && !trimmed.contains("\"deck\"");
    }

    // ── write ────────────────────────────────────────────────────────────────

    public static String toJson(Deck deck) {
        try {
            return MAPPER.writeValueAsString(toV5(deck));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static KrcgV5Deck toV5(Deck deck) {
        KrcgV5Deck out = new KrcgV5Deck();
        out.setId(nullIfBlank(deck.getId()));
        out.setName(deck.getName());
        out.setComment(nullIfBlank(deck.getComments()));
        out.setAuthor(nullIfBlank(deck.getAuthor()));
        out.setPlayer(nullIfBlank(deck.getPlayer()));

        List<KrcgV5Card> cards = new ArrayList<>();
        for (CardCount cc : deck.getCrypt().getCards()) {
            cards.add(toV5Card(cc, "Crypt"));
        }
        for (LibraryCard group : deck.getLibrary().getCards()) {
            for (CardCount cc : group.getCards()) {
                cards.add(toV5Card(cc, "Library"));
            }
        }
        out.setCards(cards);
        return out;
    }

    private static KrcgV5Card toV5Card(CardCount cc, String fallbackKind) {
        KrcgV5Card c = new KrcgV5Card();
        c.setCount(cc.getCount());
        c.setComment(nullIfBlank(cc.getComments()));

        Card card = cc.getId() == null ? null : CardRegistry.findById(String.valueOf(cc.getId()));
        if (card == null) {
            c.setId(cc.getId());
            c.setPrintedName(cc.getName());
            c.setKind(fallbackKind);
            c.setTypes(List.of());
            return c;
        }

        c.setId(Integer.valueOf(card.id()));
        c.setPrintedName(card.name());
        c.setKind(card.isCrypt() ? "Crypt" : "Library");
        if (card instanceof CryptCard crypt) {
            c.setTypes(List.of(crypt.type() == CryptType.IMBUED ? "Imbued" : "Vampire"));
            c.setUnicitySuffix(crypt.advanced() ? "ADV" : "");
            c.setSuffix(cryptSuffix(crypt));
        } else if (card instanceof net.deckserver.game.cards.LibraryCard lib) {
            c.setTypes(List.copyOf(lib.types()));
        }
        return c;
    }

    /** {@code "G3"} / {@code "G3 ADV"} / {@code ""} / {@code "ADV"} — KRCG's crypt suffix form. */
    private static String cryptSuffix(CryptCard crypt) {
        if ("ANY".equalsIgnoreCase(crypt.group())) {
            return crypt.advanced() ? "ADV" : "";
        }
        return "G" + crypt.group() + (crypt.advanced() ? " ADV" : "");
    }

    // ── read ─────────────────────────────────────────────────────────────────

    public static Deck fromJson(String json) {
        try {
            return fromV5(MAPPER.readValue(json, KrcgV5Deck.class));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Deck fromV5(KrcgV5Deck src) {
        Deck deck = new Deck();
        deck.setId(src.getId());
        deck.setName(src.getName());
        deck.setComments(src.getComment());
        deck.setAuthor(src.getAuthor());
        deck.setPlayer(src.getPlayer());

        List<CardCount> cryptCards = new ArrayList<>();
        Map<String, List<CardCount>> libraryByType = new LinkedHashMap<>();

        for (KrcgV5Card c : src.getCards()) {
            CardCount cc = new CardCount();
            cc.setId(c.getId());
            cc.setCount(c.getCount());
            cc.setComments(nullIfBlank(c.getComment()));

            Card card = c.getId() == null ? null : CardRegistry.findById(String.valueOf(c.getId()));
            boolean crypt;
            if (card != null) {
                cc.setName(card.name());
                crypt = card.isCrypt();
            } else {
                cc.setName(c.getPrintedName());
                crypt = "Crypt".equalsIgnoreCase(c.getKind());
            }

            if (crypt) {
                cryptCards.add(cc);
            } else {
                String bucket = card != null ? card.typeLine() : libraryBucket(c);
                libraryByType.computeIfAbsent(bucket, k -> new ArrayList<>()).add(cc);
            }
        }

        Crypt crypt = new Crypt();
        crypt.setCards(mergeById(cryptCards));
        deck.setCrypt(crypt);

        Library library = new Library();
        List<LibraryCard> groups = new ArrayList<>();
        libraryByType.forEach((type, list) -> {
            LibraryCard group = new LibraryCard();
            group.setType(type);
            group.setCards(mergeById(list));
            groups.add(group);
        });
        library.setCards(groups);
        deck.setLibrary(library);

        return deck;
    }

    private static String libraryBucket(KrcgV5Card c) {
        return c.getTypes() == null || c.getTypes().isEmpty() ? "Unknown" : String.join("/", c.getTypes());
    }

    /** Collapses same-id entries, summing counts and keeping the first non-blank comment. Null ids are left un-merged. */
    private static List<CardCount> mergeById(List<CardCount> in) {
        Map<Integer, CardCount> byId = new LinkedHashMap<>();
        List<CardCount> out = new ArrayList<>();
        for (CardCount cc : in) {
            if (cc.getId() == null) {
                out.add(cc);
                continue;
            }
            CardCount existing = byId.get(cc.getId());
            if (existing == null) {
                byId.put(cc.getId(), cc);
                out.add(cc);
            } else {
                existing.setCount(existing.getCount() + cc.getCount());
                if (isBlank(existing.getComments()) && !isBlank(cc.getComments())) {
                    existing.setComments(cc.getComments());
                }
            }
        }
        return out;
    }

    private static String nullIfBlank(String s) {
        return isBlank(s) ? null : s;
    }
}
