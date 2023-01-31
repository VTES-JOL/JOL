package net.deckserver.storage.json.system;

public enum Visibility {
    PRIVATE, PUBLIC;

    public static Visibility fromBoolean(boolean isPublic) {
        return isPublic ? PUBLIC : PRIVATE;
    }
}
