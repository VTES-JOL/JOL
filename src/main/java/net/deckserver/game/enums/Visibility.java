package net.deckserver.game.enums;

public enum Visibility {
    PRIVATE, PUBLIC;

    public static Visibility fromBoolean(boolean isPublic) {
        return isPublic ? PUBLIC : PRIVATE;
    }
}
