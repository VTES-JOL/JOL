package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

@Getter
public class TournamentBean {
    private final boolean idValid;

    public TournamentBean(PlayerModel model) {
        String player = model.getPlayerName();
        JolAdmin jolAdmin = JolAdmin.getInstance();
        this.idValid = !Strings.isNullOrEmpty(jolAdmin.getVeknID(player));
    }
}
