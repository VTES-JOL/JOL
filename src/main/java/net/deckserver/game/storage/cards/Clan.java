package net.deckserver.game.storage.cards;

import lombok.Getter;

public enum Clan {
    ABOMINATION("Abomination"),
    AHRIMANE("Ahrimane"),
    AKUNANSE("Akunanse"),
    ASSAMITE("Assamite"),
    BAALI("Baali"),
    BLOOD_BROTHER("Blood Brother"),
    BRUJAH("Brujah"),
    BRUJAH_ANTITRIBU("Brujah Antitribu"),
    CAITIFF("Caitiff"),
    DAUGHTER_OF_CACOPHONY("Daughter of Cacophony"),
    FOLLOWER_OF_SET("Follower of Set"),
    GANGREL("Gangrel"),
    GANGREL_ANTITRIBU("Gangrel Antitribu"),
    GARGOYLE("Gargoyle"),
    GIOVANNI("Giovanni"),
    GURUHI("Guruhi"),
    HARBINGER_OF_SKULLS("Harbinger of Skulls"),
    ISHTARRI("Ishtarri"),
    KIASYD("Kiasyd"),
    LASOMBRA("Lasombra"),
    MALKAVIAN("Malkavian"),
    MALKAVIAN_ANTITRIBU("Malkavian Antitribu"),
    NAGARAJA("Nagaraja"),
    NOSFERATU("Nosferatu"),
    NOSFERATU_ANTITRIBU("Nosferatu Antitribu"),
    HECATA("Hecata"),
    OSEBO("Osebo"),
    PANDER("Pander"),
    RAVNOS("Ravnos"),
    SALUBRI("Salubri"),
    SALUBRI_ANTITRIBU("Salubri Antitribu"),
    SAMEDI("Samedi"),
    TOREADOR("Toreador"),
    TOREADOR_ANTITRIBU("Toreador Antitribu"),
    TREMERE("Tremere"),
    TREMERE_ANTITRIBU("Tremere Antitribu"),
    TRUE_BRUJAH("True brujah"),
    TZIMISCE("Tzimisce"),
    VENTRUE("Ventrue"),
    VENTRUE_ANTITRIBU("Ventrue Antitribu"),
    BANU_HAQIM("Banu Haqim"),
    MINISTRY("Ministry"),
    AVENGER("Avenger"),
    DEFENDER("Defender"),
    INNOCENT("Innocent"),
    JUDGE("Judge"),
    MARTYR("Marryr"),
    REDEEMER("Redeemer"),
    VISIONARY("Visionary"),
    NONE("");

    @Getter
    private final String description;

    Clan(String description) {
        this.description = description;
    }

    public static Clan from(String value) {
        return Clan.valueOf(value.toUpperCase());
    }

    public static Clan of(String description) {
        for (Clan clan : Clan.values()) {
            if (clan.description.equalsIgnoreCase(description)) {
                return clan;
            }
        }
        return NONE;
    }

    public static Clan startsWith(String prefix) {
        for (Clan clan : Clan.values()) {
            if (clan.description.toLowerCase().startsWith(prefix.toLowerCase())) {
                return clan;
            }
        }
        return NONE;
    }
}
