package net.deckserver.game.cards.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derived crypt-card fields the VEKN CSV does not carry directly — intrinsic
 * sect, title vote count, and the unique / infernal text flags. Ported from
 * the old test-scope {@code CryptImporter}.
 */
public final class CryptMetadata {

    private static final Logger logger = LoggerFactory.getLogger(CryptMetadata.class);

    private CryptMetadata() {
    }

    /** Non-unique only when the card text says so explicitly. */
    public static boolean isUnique(String text) {
        String t = text == null ? "" : text.toLowerCase();
        return !(t.contains("are not unique") || t.contains("non-unique"));
    }

    public static boolean isInfernal(String text) {
        return text != null && text.contains("Infernal.");
    }

    /** Intrinsic starting sect from clan, overridden by an explicit sect prefix in the text. */
    public static String determineSect(String clan, String text) {
        String sect = switch (clan == null ? "" : clan) {
            case "Brujah", "Malkavian", "Nosferatu", "Toreador", "Tremere", "Ventrue", "Caitiff" -> "Camarilla";
            case "Brujah antitribu", "Malkavian antitribu", "Nosferatu antitribu", "Toreador antitribu",
                 "Tremere antitribu", "Ventrue antitribu", "Gangrel antitribu", "Salubri antitribu",
                 "Lasombra", "Tzimisce", "Pander", "Ahrimane", "Blood Brother", "Harbinger of Skulls",
                 "Kiasyd" -> "Sabbat";
            case "Akunanse", "Guruhi", "Ishtarri", "Osebo" -> "Laibon";
            default -> "Independent";
        };

        String body = (text == null ? "" : text).replaceAll("^Advanced, ", "").trim();
        String textSect = null;
        if (body.startsWith("Sabbat")) textSect = "Sabbat";
        else if (body.startsWith("Anarch")) textSect = "Anarch";
        else if (body.startsWith("Independent")) textSect = "Independent";
        else if (body.startsWith("Camarilla")) textSect = "Camarilla";
        else if (body.startsWith("Laibon")) textSect = "Laibon";

        if (textSect != null && !sect.equals(textSect)) {
            logger.debug("overriding intrinsic sect {} with {} from card text", sect, textSect);
            sect = textSect;
        }
        return sect;
    }

    public static String determineVotes(String title) {
        if (title == null) return "";
        return switch (title) {
            case "1 vote", "primogen", "bishop" -> "1";
            case "2 votes", "archbishop", "prince", "magaji", "baron" -> "2";
            case "justicar", "cardinal" -> "3";
            case "inner circle", "regent" -> "4";
            case "priscus" -> "P";
            default -> "";
        };
    }
}
