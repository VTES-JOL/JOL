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
    private final String lastOnline;

    public UserSummaryBean(String name) {
        this.name = name;
        this.admin = JolAdmin.getInstance().isAdmin(name);
        this.superUser = JolAdmin.getInstance().isSuperUser(name);
        this.judge = JolAdmin.getInstance().isJudge(name);
        this.lastOnline = JolAdmin.getInstance().getPlayerAccess(name).truncatedTo(ChronoUnit.SECONDS).format(ISO_OFFSET_DATE_TIME);
    }

    public boolean isSpecialUser() {
        return admin || superUser || judge;
    }

}
