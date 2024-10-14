package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AdminPageBean {

    private final List<UserSummaryBean> userRoles;
    private final List<String> players;

    public AdminPageBean(PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        this.players = admin.getPlayers().stream().sorted().collect(Collectors.toList());
        this.userRoles = this.players.stream()
                .map(UserSummaryBean::new)
                .filter(UserSummaryBean::isSpecialUser)
                .collect(Collectors.toList());
    }
}
