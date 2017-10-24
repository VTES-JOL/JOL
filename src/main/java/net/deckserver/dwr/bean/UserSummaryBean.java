package net.deckserver.dwr.bean;

public class UserSummaryBean {
    private final String name;
    private final boolean admin;
    private final boolean superUser;

    public UserSummaryBean(String name, boolean admin, boolean superUser) {
        this.name = name;
        this.admin = admin;
        this.superUser = superUser;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isSuperUser() {
        return superUser;
    }
}
