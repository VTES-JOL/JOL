package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SuperAdminBean {

    private static Predicate<UserSummaryBean> isAdmin = UserSummaryBean::isAdmin;
    private static Predicate<UserSummaryBean> isJudge = UserSummaryBean::isJudge;

    private List<UserSummaryBean> players;
    private List<String> names;

    public SuperAdminBean(AdminBean abean, PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        names = admin.getPlayers();
        players = names.stream()
                .map(UserSummaryBean::new)
                .filter(isAdmin.or(isJudge))
                .collect(Collectors.toList());
    }

    public List<UserSummaryBean> getPlayers() {
        return players;
    }

    public List<String> getNames() {
        return names;
    }
}
