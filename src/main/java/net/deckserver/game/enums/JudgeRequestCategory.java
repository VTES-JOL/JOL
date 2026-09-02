package net.deckserver.game.enums;

/**
 * General context a player attaches to a "call a judge" request, so judges can
 * filter the ruling history (e.g. "show me prior card rulings").
 */
public enum JudgeRequestCategory {
    INCORRECT_PLAY,
    CARD_RULING,
    OTHER
}
