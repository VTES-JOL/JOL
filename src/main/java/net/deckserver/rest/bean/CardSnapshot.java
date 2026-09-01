package net.deckserver.rest.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Viewer-aware, structural replacement for card.jsp/card-simple.jsp/card-hidden.jsp
 * for the React game page — see GameSnapshotFactory for the visibility rules this
 * must exactly replicate (region.jsp's `${visible}` cascade). When `visible` is
 * false, every field below `counters` is omitted/default — this is the actual
 * security boundary (an opponent's hidden card must never reach the browser with
 * identifying data), not just a client-side display choice.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardSnapshot {
    private final String id;
    private final boolean visible;
    private final int counters;

    // Visible-only fields:
    private final String cardId;
    private final String name;
    private final boolean advanced;
    private final List<String> disciplines;
    private final int capacity;
    private final String votes;
    private final boolean contested;
    private final boolean locked;
    private final boolean infernal;
    private final boolean playtest;
    private final String clan;
    private final String sect;
    private final String path;
    private final String label;
    private final boolean minion;
    private final String typeClass;
    private final List<String> clanClasses;
    private final boolean hasBlood;
    private final boolean hasLife;
    private final List<CardSnapshot> cards;

    // Play-card-modal fields — populated only for cards in the viewer's own
    // HAND / RESEARCH region (GameSnapshotFactory), null everywhere else.
    private final List<PlayModeBean> modes;
    private final Boolean multiMode;
    private final Boolean doNotReplace;
    private final String preamble;
    private final String cost;
}
