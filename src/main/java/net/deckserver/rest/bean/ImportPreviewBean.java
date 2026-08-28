package net.deckserver.rest.bean;

import java.util.List;

/**
 * Response from {@code POST /jol/api/cards/preview}: what a pasted deck list
 * would import as, before the user confirms.
 *
 * @param format          "krcg" (JSON) or "jol" (plain text)
 * @param deckName         name lifted from KRCG meta, or null
 * @param deckDescription  description/comments from KRCG meta, or null
 * @param resolved         cards matched against the card database, with counts
 * @param errors           lines / ids that could not be resolved
 */
public record ImportPreviewBean(
        String format,
        String deckName,
        String deckDescription,
        List<ResolvedEntry> resolved,
        List<ParseError> errors
) {
    public record ResolvedEntry(int count, CardDetailBean card) {}

    public record ParseError(String line, String reason) {}
}
