package net.deckserver.dwr.bean;

public class UserSummaryBean {
    private final String name;
    private final boolean admin;
    private final boolean superUser;
    private final boolean judge;

    public UserSummaryBean(String name, boolean admin, boolean superUser, boolean judge) {
        this.name = name;
        this.admin = admin;
        this.superUser = superUser;
        this.judge = judge;
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

    public boolean isJudge() {
        return judge;
    }
}
