package net.deckserver.rest.bean;

import net.deckserver.game.cards.PlayMode;

import java.util.List;

/**
 * One play option for a hand/research card, mirrored to the client as
 * {@code CardMode} in {@code api/types.ts}. Carried on {@link CardSnapshot}
 * only for cards in the viewer's own hand or research region (see
 * {@code GameSnapshotFactory}).
 */
public record PlayModeBean(List<String> disciplines, String text, String target) {

    public static PlayModeBean of(PlayMode mode) {
        return new PlayModeBean(
                mode.disciplines(),
                mode.text(),
                mode.target() == null ? null : mode.target().name());
    }
}
