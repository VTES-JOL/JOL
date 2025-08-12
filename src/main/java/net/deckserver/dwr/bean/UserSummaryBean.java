package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;

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
        this.admin = JolAdmin.INSTANCE.isAdmin(name);
        this.superUser = JolAdmin.INSTANCE.isSuperUser(name);
        this.judge = JolAdmin.INSTANCE.isJudge(name);
        this.playtester = JolAdmin.INSTANCE.isPlaytester(name);
        this.lastOnline = JolAdmin.INSTANCE.getPlayerAccess(name).truncatedTo(ChronoUnit.SECONDS).format(ISO_OFFSET_DATE_TIME);
    }

    public boolean isSpecialUser() {
        return admin || superUser || judge || playtester;
    }

}
