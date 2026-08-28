package net.deckserver.game.enums;

/**
 * VTES disciplines (and Imbued virtues), each paired with the 3-4 letter code
 * used throughout the card data and the icon font. Ported from the jol-quarkus
 * rewrite; the {@code name() -> code} mapping is what
 * {@link net.deckserver.game.cards.CardRegistry} uses to normalise the
 * full-word discipline names in {@code vteslib.csv}'s "Discipline" column.
 *
 * <p>Two names share the {@code tha} code (Blood Sorcery is the V5 rename of
 * Thaumaturgy) — both are kept so either spelling in the source data resolves.
 */
public enum Discipline {
    ABOMBWE("abo"),
    ANIMALISM("ani"),
    AUSPEX("aus"),
    BLOOD_SORCERY("tha"),
    THAUMATURGY("tha"),
    CELERITY("cel"),
    CHIMERSTRY("chi"),
    DAIMOINON("dai"),
    DEMENTATION("dem"),
    DOMINATE("dom"),
    FORTITUDE("for"),
    MELPOMINEE("mel"),
    MYTHERCERIA("myt"),
    NECROMANCY("nec"),
    OBEAH("obe"),
    OBFUSCATE("obf"),
    OBTENEBRATION("obt"),
    OBLIVION("obl"),
    POTENCE("pot"),
    PRESENCE("pre"),
    PROTEAN("pro"),
    QUIETUS("qui"),
    SANGUINUS("san"),
    SERPENTIS("ser"),
    SPIRITUS("spi"),
    TEMPORIS("tem"),
    THANATOSIS("thn"),
    VALEREN("val"),
    VICISSITUDE("vic"),
    VISCERATIKA("vis"),

    // Imbued virtues — VEKN's data spells this virtue "Judgment"; JUSTICE is
    // kept as an alias so either form resolves to the same code.
    VENGEANCE("ven"),
    DEFENSE("def"),
    INNOCENCE("inn"),
    JUSTICE("jud"),
    JUDGMENT("jud"),
    MARTYRDOM("mar"),
    REDEMPTION("red"),
    VISION("viz"),

    // Other
    FLIGHT("flight"),
    STRIGA("str"),
    MALEFICIA("mal");

    public final String code;

    Discipline(String code) {
        this.code = code;
    }
}
