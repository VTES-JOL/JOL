package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class LibraryCard extends BaseCard{

    private String id;
    private String key;
    private String name;
    private String text;
    private List<String> type;
    private String displayName;
    private Set<String> names;
    private Set<String> partials;
    private Set<String> sets;

    private boolean unique;

    // Costs
    private String blood;
    private String pool;
    private String conviction;

    // Requirements
    private List<String> disciplines;
    private List<String> clans;
    private List<String> requirements;

    // Other
    private Boolean burnOption;
    private boolean banned;

    /**
     * The first line (or rarely, two lines: Make the Misere) of a card's text
     * in vteslib.csv. Contains restrictions and other information that apply
     * to the card regardless of which mode is used.
     */
    private String preamble;

    /**
     * The different ways a card may be played. For example, Earth Control has
     * two modes:
     * [pro] +1 stealth.
     * [PRO] +2 stealth.
     */
    private List<LibraryCardMode> modes;

    /**
     * Replacement card is not drawn until later.
     */
    private boolean doNotReplace;

    /**
     * Those Anarch cards that can be played with more than one discipline,
     * e.g. Guardian Vigil.
     */
    private boolean multiMode;
}
