package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;

import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Getter
public class UserSummaryBean {
    private final String name;
    private final boolean admin;
    private final boolean superUser;
    private final boolean judge;
    private final boolean playtester;
    private final String lastOnline;

    public UserSummaryBean(String name) {
        this.name = name;
        this.admin = JolAdmin.isAdmin(name);
        this.superUser = JolAdmin.isSuperUser(name);
        this.judge = JolAdmin.isJudge(name);
        this.playtester = JolAdmin.isPlaytester(name);
        this.lastOnline = JolAdmin.getPlayerAccess(name).truncatedTo(ChronoUnit.SECONDS).format(ISO_OFFSET_DATE_TIME);
    }

    public boolean isSpecialUser() {
        return admin || superUser || judge || playtester;
    }

}
