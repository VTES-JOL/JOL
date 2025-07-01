package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.storage.json.system.DeckFormat;
import net.deckserver.storage.json.system.TournamentRegistration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TournamentBean {
    private final boolean idValid;
    private final List<String> decks;
    private final List<String> registrations;
    private final String message;
    private final List<String> registeredDecks;

    public TournamentBean(PlayerModel model) {
        String player = model.getPlayerName();
        JolAdmin jolAdmin = JolAdmin.INSTANCE;
        this.idValid = !Strings.isNullOrEmpty(jolAdmin.getVeknID(player));
        this.decks = jolAdmin.getDeckNames(model.getPlayerName()).stream()
                .filter(deckName -> jolAdmin.getDeckFormat(player, deckName) == DeckFormat.MODERN)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        Collection<TournamentRegistration> currentRegistrations = jolAdmin.getTournamentRegistrations();
        this.registrations = currentRegistrations
                .stream()
                .map(TournamentRegistration::getPlayerName)
                .collect(Collectors.toList());
        this.registeredDecks = currentRegistrations
                .stream()
                .filter(currentRegistration -> currentRegistration.getPlayerName().equals(player))
                .findFirst()
                .map(TournamentRegistration::getDecks)
                .orElse(new ArrayList<>());
        message = JolAdmin.INSTANCE.getPlayerModel(player).getMessage();
    }
}
