package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.services.PlayerService;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Getter
public class UserSummaryBean {
    private final String name;
    private final List<String> roles;
    private final String lastOnline;

    public UserSummaryBean(String name) {
        this.name = name;
        this.roles = PlayerService.get(name).getRoles().stream().map(PlayerRole::toString).toList();
        this.lastOnline = JolAdmin.getPlayerAccess(name).truncatedTo(ChronoUnit.SECONDS).format(ISO_OFFSET_DATE_TIME);
    }

    public boolean isSpecialUser() {
        return !roles.isEmpty();
    }

}
